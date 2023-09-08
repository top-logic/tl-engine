/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased.storage;

import static com.top_logic.knowledge.service.KBUtils.*;
import static java.util.Collections.*;

import java.util.List;

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

	private static final long MILLIS_TO_SECONDS = 1000;

	private static final long MAX_ALLOWED_SECONDS = 30;

	private static final Class<TestListStorage> THIS_CLASS = TestListStorage.class;

	private static final String MODULE_NAME = THIS_CLASS.getSimpleName();

	private static final String PARENT_TYPE_NAME = "Parent";

	private static final String CHILD_TYPE_NAME = "Child";

	private static final String PARENT_TO_CHILDREN_ATTRIBUTE_NAME = "children";

	/**
	 * Before #23922 the execution time would increase from 6 seconds for 32'000 elements to 15
	 * seconds for 33'000 elements, 5 minutes for 5'000 elements and 15 minutes for 50'000 elements.
	 */
	private static final int CHILDREN_COUNT = 50_000;

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

	public void testLargeListPerformance() {
		assertEquals(ListStorage.class, getTestedStorage().getClass());
		measurePerformance();
	}

	private void measurePerformance() {
		StopWatch stopWatch = StopWatch.createStartedWatch();
		inTransaction(() -> setChildren(_children));
		stopWatch.stop();
		inTransaction(() -> setChildren(emptyList()));
		// System.err.println(stopWatch); // For debugging.
		assertTrue(createErrorMessage(stopWatch), isFastEnough(stopWatch));
	}

	private String createErrorMessage(StopWatch stopWatch) {
		return "TLObject.setList(" + CHILDREN_COUNT + " elements) should take less than " + MAX_ALLOWED_SECONDS
			+ " seconds, but took: " + stopWatch;
	}

	private boolean isFastEnough(StopWatch stopWatch) {
		return stopWatch.getElapsedMillis() < MILLIS_TO_SECONDS * MAX_ALLOWED_SECONDS;
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
		_children.forEach(TLObject::tDelete);
		_parent.tDelete();
	}

	private TLObject instantiate(TLClass type) {
		return DynamicModelService.getInstance().createObject(type, null, null);
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
