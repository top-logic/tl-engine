/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased.storage;

import static com.top_logic.knowledge.service.KBUtils.*;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import com.top_logic.element.meta.kbbased.storage.SetStorage;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.model.StorageDetail;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * {@link TestCase} for {@link SetStorage}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestSetStorage extends BasicTestCase {

	/**
	 * Upper limit for the CPU time of the test thread for the operation measured in each
	 * performance test.
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
	private static final long MAX_ALLOWED_SECONDS = 3;

	private static final Class<TestSetStorage> THIS_CLASS = TestSetStorage.class;

	private static final String MODULE_NAME = THIS_CLASS.getSimpleName();

	private static final String PARENT_TYPE_NAME = "Parent";

	private static final String MANDATORY_PARENT_TYPE_NAME = "MandatoryParent";

	private static final String CHILD_TYPE_NAME = "Child";

	private static final String PARENT_TO_CHILDREN_ATTRIBUTE_NAME = "children";

	private static final String PARENT_TO_REQUIRED_CHILDREN_ATTRIBUTE_NAME = "requiredChildren";

	/**
	 * Number of elements the performance tests add to or remove from a set one by one.
	 *
	 * <p>
	 * Adding or removing a single element through {@link SetStorage} must take constant time,
	 * independent of the number of elements already in the set, so that building or clearing a set
	 * element by element takes time linear in the number of elements. For this number of elements,
	 * a linear implementation needs a small fraction of {@link #MAX_ALLOWED_SECONDS}, whereas an
	 * implementation that reads the whole set for each added or removed element (and is therefore
	 * quadratic) needs far more.
	 * </p>
	 */
	private static final int CHILDREN_COUNT = 10_000;

	private TLObject _parent;

	/**
	 * Object with a mandatory set, created only by {@link #testRemoveLastMandatoryRejected()}.
	 */
	private TLObject _mandatoryParent;

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
	 * Checks adding and removing single elements, within the transaction and after its commit.
	 */
	public void testAddAndRemove() {
		assertEquals(SetStorage.class, getStorage(_parent, PARENT_TO_CHILDREN_ATTRIBUTE_NAME).getClass());

		TLObject a = _children.get(0);
		TLObject b = _children.get(1);
		TLObject c = _children.get(2);
		TLObject d = _children.get(3);

		inTransaction(() -> {
			addChild(a);
			assertEquals(Set.of(a), getChildren());
			addChild(b);
			addChild(c);
			addChild(d);
			assertEquals(Set.of(a, b, c, d), getChildren());
		});
		assertEquals(Set.of(a, b, c, d), getChildren());

		inTransaction(() -> {
			removeChild(b);
			assertEquals(Set.of(a, c, d), getChildren());
			removeChild(d);
			assertEquals(Set.of(a, c), getChildren());
		});
		assertEquals(Set.of(a, c), getChildren());

		inTransaction(() -> {
			removeChild(a);
			removeChild(c);
			assertEquals(emptySet(), getChildren());
		});
		assertEquals(emptySet(), getChildren());
	}

	/**
	 * Checks that adding an element that is already contained in the set is rejected with an
	 * {@link IllegalArgumentException} and leaves the set unchanged.
	 * 
	 * @see #assertRejected(String, Class, Runnable)
	 */
	public void testAddDuplicateRejected() {
		TLObject a = _children.get(0);
		TLObject b = _children.get(1);

		inTransaction(() -> {
			addChild(a);
			addChild(b);
		});

		inTransaction(() -> {
			assertRejected("Adding an element that is already contained must be rejected.",
				IllegalArgumentException.class, () -> addChild(a));
			assertEquals(Set.of(a, b), getChildren());
		});
		assertEquals(Set.of(a, b), getChildren());

		inTransaction(() -> setChildren(emptyList()));
	}

	/**
	 * Checks that removing an element that is not contained in the set is rejected with an
	 * {@link IllegalArgumentException} and leaves the set unchanged.
	 * 
	 * @see #assertRejected(String, Class, Runnable)
	 */
	public void testRemoveAbsentRejected() {
		TLObject a = _children.get(0);
		TLObject b = _children.get(1);
		TLObject absent = _children.get(2);

		inTransaction(() -> {
			addChild(a);
			addChild(b);
		});

		inTransaction(() -> {
			assertRejected("Removing an element that is not contained must be rejected.",
				IllegalArgumentException.class, () -> removeChild(absent));
			assertEquals(Set.of(a, b), getChildren());
		});
		assertEquals(Set.of(a, b), getChildren());

		inTransaction(() -> setChildren(emptyList()));
	}

	/**
	 * Checks that removing the last element of a mandatory set is rejected with a
	 * {@link TopLogicException}, whereas removing an element from a mandatory set with more than
	 * one element succeeds.
	 * 
	 * @see #assertRejected(String, Class, Runnable)
	 */
	public void testRemoveLastMandatoryRejected() {
		TLObject a = _children.get(0);
		TLObject b = _children.get(1);

		inTransaction(() -> {
			_mandatoryParent = instantiate(getType(MANDATORY_PARENT_TYPE_NAME));
			_mandatoryParent.tAddByName(PARENT_TO_REQUIRED_CHILDREN_ATTRIBUTE_NAME, a);
			_mandatoryParent.tAddByName(PARENT_TO_REQUIRED_CHILDREN_ATTRIBUTE_NAME, b);
		});
		assertEquals(SetStorage.class,
			getStorage(_mandatoryParent, PARENT_TO_REQUIRED_CHILDREN_ATTRIBUTE_NAME).getClass());

		inTransaction(() -> {
			_mandatoryParent.tRemoveByName(PARENT_TO_REQUIRED_CHILDREN_ATTRIBUTE_NAME, b);
			assertEquals(Set.of(a), getRequiredChildren());

			assertRejected("Removing the last element of a mandatory set must be rejected.",
				TopLogicException.class,
				() -> _mandatoryParent.tRemoveByName(PARENT_TO_REQUIRED_CHILDREN_ATTRIBUTE_NAME, a));
			assertEquals(Set.of(a), getRequiredChildren());
		});
		assertEquals(Set.of(a), getRequiredChildren());
	}

	/**
	 * Checks {@link TLObject#tContains(com.top_logic.model.TLStructuredTypePart, Object)} within
	 * the transaction after adds and after a remove, and after the commit.
	 */
	public void testContains() {
		TLObject a = _children.get(0);
		TLObject b = _children.get(1);
		TLObject absent = _children.get(2);

		assertFalse(containsChild(a));
		assertFalse(containsChild(null));

		inTransaction(() -> {
			addChild(a);
			assertTrue(containsChild(a));
			assertFalse(containsChild(b));

			addChild(b);
			assertTrue(containsChild(a));
			assertTrue(containsChild(b));
			assertFalse(containsChild(absent));
		});
		assertTrue(containsChild(a));
		assertTrue(containsChild(b));
		assertFalse(containsChild(absent));
		assertFalse("A value that is no element type instance is not contained.", containsChild("a"));
		assertFalse(containsChild(null));

		inTransaction(() -> {
			removeChild(a);
			assertFalse(containsChild(a));
			assertTrue(containsChild(b));
		});
		assertFalse(containsChild(a));
		assertTrue(containsChild(b));

		inTransaction(() -> setChildren(emptyList()));
		assertFalse(containsChild(b));
	}

	/**
	 * Checks that adding {@link #CHILDREN_COUNT} elements one by one to an empty set, each after
	 * checking that it is not yet contained, stays within {@link #MAX_ALLOWED_SECONDS} of CPU time.
	 */
	public void testContainsThenAddOneByOnePerformance() {
		assertFastEnough("check and add " + CHILDREN_COUNT + " elements one by one", () -> {
			for (TLObject child : _children) {
				if (!containsChild(child)) {
					addChild(child);
				}
			}
		});
		assertEquals(new HashSet<>(_children), getChildren());
		inTransaction(() -> setChildren(emptyList()));
	}

	/**
	 * Checks that adding {@link #CHILDREN_COUNT} elements one by one to an empty set stays within
	 * {@link #MAX_ALLOWED_SECONDS} of CPU time.
	 */
	public void testAddOneByOnePerformance() {
		assertFastEnough("add " + CHILDREN_COUNT + " elements one by one", () -> {
			for (TLObject child : _children) {
				addChild(child);
			}
		});
		assertEquals(new HashSet<>(_children), getChildren());
		inTransaction(() -> setChildren(emptyList()));
	}

	/**
	 * Checks that removing {@link #CHILDREN_COUNT} elements one by one from a set stays within
	 * {@link #MAX_ALLOWED_SECONDS} of CPU time.
	 */
	public void testRemoveOneByOnePerformance() {
		inTransaction(() -> setChildren(_children));
		assertEquals(new HashSet<>(_children), getChildren());

		assertFastEnough("remove " + CHILDREN_COUNT + " elements one by one", () -> {
			for (TLObject child : _children) {
				removeChild(child);
			}
		});
		assertEquals(emptySet(), getChildren());
	}

	/**
	 * Checks that the given modification is rejected by the storage with the given exception type.
	 * 
	 * <p>
	 * {@link TLObject#tAddByName(String, Object)} and {@link TLObject#tRemoveByName(String, Object)}
	 * wrap the exception of the storage into an {@link IllegalStateException}, so the expected
	 * exception is the cause of the exception thrown by the public API.
	 * </p>
	 */
	private static void assertRejected(String message, Class<? extends Throwable> expectedCause,
			Runnable modification) {
		try {
			modification.run();
		} catch (IllegalStateException ex) {
			assertInstanceof(ex.getCause(), expectedCause);
			return;
		}
		fail(message);
	}

	private void assertFastEnough(String operation, Runnable change) {
		StopWatch cpuWatch = StopWatch.createStartedThreadCpuWatch();
		StopWatch wallWatch = StopWatch.createStartedWatch();
		inTransaction(change);
		wallWatch.stop();
		cpuWatch.stop();

		assertTrue(createErrorMessage(operation, cpuWatch, wallWatch),
			cpuWatch.getElapsedNanos() < TimeUnit.SECONDS.toNanos(MAX_ALLOWED_SECONDS));
	}

	private String createErrorMessage(String operation, StopWatch cpuWatch, StopWatch wallWatch) {
		String limit = cpuWatch.isThreadCpuTime() ? "CPU time" : "wall-clock time (CPU time not available)";
		String cpuTime = cpuWatch.isThreadCpuTime() ? cpuWatch.toString() : "not available";
		return "TLObject.tAdd/tRemove/tContains(" + operation + ") should take less than " + MAX_ALLOWED_SECONDS
			+ " seconds of " + limit + ", but took: CPU time " + cpuTime + ", wall-clock time " + wallWatch;
	}

	private Set<?> getChildren() {
		return valueAsSet(_parent, PARENT_TO_CHILDREN_ATTRIBUTE_NAME);
	}

	private Set<?> getRequiredChildren() {
		return valueAsSet(_mandatoryParent, PARENT_TO_REQUIRED_CHILDREN_ATTRIBUTE_NAME);
	}

	private static Set<?> valueAsSet(TLObject object, String attributeName) {
		return new HashSet<>((Collection<?>) object.tValueByName(attributeName));
	}

	private void addChild(TLObject child) {
		_parent.tAddByName(PARENT_TO_CHILDREN_ATTRIBUTE_NAME, child);
	}

	private boolean containsChild(Object child) {
		return _parent.tContainsByName(PARENT_TO_CHILDREN_ATTRIBUTE_NAME, child);
	}

	private void removeChild(TLObject child) {
		_parent.tRemoveByName(PARENT_TO_CHILDREN_ATTRIBUTE_NAME, child);
	}

	private void setChildren(List<TLObject> children) {
		_parent.tUpdateByName(PARENT_TO_CHILDREN_ATTRIBUTE_NAME, children);
	}

	private static StorageDetail getStorage(TLObject object, String attributeName) {
		return object.tType().getPart(attributeName).getStorageImplementation();
	}

	@Override
	protected void tearDown() throws Exception {
		inTransaction(this::tearDownParentAndChildren);
		super.tearDown();
	}

	private void tearDownParentAndChildren() {
		List<TLObject> all = new ArrayList<>(_children.size() + 2);
		all.addAll(_children);
		all.add(_parent);
		if (_mandatoryParent != null) {
			all.add(_mandatoryParent);
		}
		deleteAll(all);
	}

	private TLObject instantiate(TLClass type) {
		return DynamicModelService.getInstance().createObject(type);
	}

	private TLClass getParentType() {
		return getType(PARENT_TYPE_NAME);
	}

	private TLClass getChildType() {
		return getType(CHILD_TYPE_NAME);
	}

	private TLClass getType(String name) {
		return (TLClass) getModule().getType(name);
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
