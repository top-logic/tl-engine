/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased.storage;

import static com.top_logic.knowledge.service.KBUtils.*;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.col.factory.CollectionFactory;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.element.meta.kbbased.storage.ListStorage;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.util.OrderedLinkUtil;
import com.top_logic.model.StorageDetail;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.model.ModelService;

/**
 * {@link TestCase} for {@link ListStorage}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestListStorage extends BasicTestCase {

	/**
	 * Upper limit for the CPU time of the test thread when setting a list of
	 * {@link #CHILDREN_COUNT} elements.
	 * 
	 * <p>
	 * The limit applies to CPU time, not wall-clock time: on a loaded build node, the time the test
	 * thread waits for a free CPU does not count. All work of the operation (including the embedded
	 * database and the commit) runs in the test thread. When the JVM cannot measure thread CPU
	 * time, the limit applies to wall-clock time.
	 * </p>
	 * 
	 * @see StopWatch#createThreadCpuWatch()
	 */
	private static final long MAX_ALLOWED_SECONDS = 10;

	private static final Class<TestListStorage> THIS_CLASS = TestListStorage.class;

	private static final String MODULE_NAME = THIS_CLASS.getSimpleName();

	private static final String PARENT_TYPE_NAME = "Parent";

	private static final String CHILD_TYPE_NAME = "Child";

	private static final String PARENT_TO_CHILDREN_ATTRIBUTE_NAME = "children";

	/**
	 * Number of elements in the list set by {@link #testLargeListPerformance()}.
	 * 
	 * <p>
	 * Setting an ordered list through {@link ListStorage} must take time linear in the number of
	 * elements. For this number of elements, a linear implementation needs a small fraction of
	 * {@link #MAX_ALLOWED_SECONDS}, whereas a quadratic one needs far more.
	 * </p>
	 *
	 * <p>
	 * {@link ListStorage} appends the elements one by one, each with a sort order of
	 * {@link OrderedLinkUtil#APPEND_INC} above its predecessor. This number of elements stays far
	 * below the point where the range of sort-order values is exhausted and the list is renumbered
	 * ({@link OrderedLinkUtil#MAX_ORDER} / {@link OrderedLinkUtil#APPEND_INC}). The test therefore
	 * checks the cost of appending a single element, not the renumbering.
	 * </p>
	 */
	private static final int CHILDREN_COUNT = 10_000;

	private TLObject _parent;

	private List<TLObject> _children = CollectionFactory.list();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		inTransaction(this::setUpParentAndChildren);
	}

	private void setUpParentAndChildren() {
		_parent = instantiate(getParentType());
		for (int i = 0; i < CHILDREN_COUNT; i++) {
			_children.add(instantiate(getChildType()));
		}
	}

	/**
	 * Checks that setting a list of {@link #CHILDREN_COUNT} elements stays within
	 * {@link #MAX_ALLOWED_SECONDS} of CPU time.
	 */
	public void testLargeListPerformance() {
		assertEquals(ListStorage.class, getTestedStorage().getClass());
		measurePerformance();
	}

	private void measurePerformance() {
		StopWatch cpuWatch = StopWatch.createStartedThreadCpuWatch();
		StopWatch wallWatch = StopWatch.createStartedWatch();
		inTransaction(() -> setChildren(_children));
		wallWatch.stop();
		cpuWatch.stop();
		inTransaction(() -> setChildren(emptyList()));

		assertTrue(createErrorMessage(cpuWatch, wallWatch),
			cpuWatch.getElapsedNanos() < TimeUnit.SECONDS.toNanos(MAX_ALLOWED_SECONDS));
	}

	private String createErrorMessage(StopWatch cpuWatch, StopWatch wallWatch) {
		String limit = cpuWatch.isThreadCpuTime() ? "CPU time" : "wall-clock time (CPU time not available)";
		String cpuTime = cpuWatch.isThreadCpuTime() ? cpuWatch.toString() : "not available";
		return "TLObject.setList(" + CHILDREN_COUNT + " elements) should take less than " + MAX_ALLOWED_SECONDS
			+ " seconds of " + limit + ", but took: CPU time " + cpuTime + ", wall-clock time " + wallWatch;
	}

	private void setChildren(List<TLObject> children) {
		_parent.tUpdateByName(PARENT_TO_CHILDREN_ATTRIBUTE_NAME, children);
	}

	private StorageDetail getTestedStorage() {
		return getTestedAttribute().getStorageImplementation();
	}

	private TLStructuredTypePart getTestedAttribute() {
		return _parent.tType().getPart(PARENT_TO_CHILDREN_ATTRIBUTE_NAME);
	}

	@Override
	protected void tearDown() throws Exception {
		inTransaction(this::tearDownParentAndChildren);
		super.tearDown();
	}

	private void tearDownParentAndChildren() {
		List<TLObject> all = new ArrayList<>(_children.size() + 1);
		all.addAll(_children);
		all.add(_parent);
		deleteAll(all);
	}

	private TLObject instantiate(TLClass type) {
		return DynamicModelService.getInstance().createObject(type);
	}

	private TLClass getParentType() {
		return (TLClass) getModule().getType(PARENT_TYPE_NAME);
	}

	private TLClass getChildType() {
		return (TLClass) getModule().getType(CHILD_TYPE_NAME);
	}

	private TLModule getModule() {
		return ModelService.getInstance().getModel().getModule(MODULE_NAME);
	}

	public static Test suite() {
		Test kbSetup = KBSetup.getSingleKBTest(THIS_CLASS);
		Test customConfigSetup = createCustomConfigSetup(THIS_CLASS, kbSetup);
		return TLTestSetup.createTLTestSetup(customConfigSetup);
	}

	private static Test createCustomConfigSetup(Class<?> testClass, Test innerSetup) {
		String configFileName = testClass.getSimpleName() + FileUtilities.XML_FILE_ENDING;
		String createFilePath = CustomPropertiesDecorator.createFileName(testClass, configFileName);
		return TestUtils.doNotMerge(new CustomPropertiesSetup(innerSetup, createFilePath, true));
	}

}
