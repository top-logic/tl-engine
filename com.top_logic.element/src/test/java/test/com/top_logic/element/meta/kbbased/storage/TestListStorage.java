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
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.element.meta.kbbased.storage.ListStorage;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.util.OrderedLinkUtil;
import com.top_logic.model.StorageDetail;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
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

	private static final Class<TestListStorage> THIS_CLASS = TestListStorage.class;

	private static final String MODULE_NAME = THIS_CLASS.getSimpleName();

	private static final String PARENT_TYPE_NAME = "Parent";

	private static final String CHILD_TYPE_NAME = "Child";

	private static final String PARENT_TO_CHILDREN_ATTRIBUTE_NAME = "children";

	private static final String PARENT_TO_BAG_CHILDREN_ATTRIBUTE_NAME = "bagChildren";

	private static final String PARENT_TO_PARTS_ATTRIBUTE_NAME = "parts";

	/**
	 * Number of elements in the lists set by the performance tests.
	 * 
	 * <p>
	 * Setting an ordered list through {@link ListStorage} must take time linear in the number of
	 * elements. For this number of elements, a linear implementation needs a small fraction of
	 * {@link #MAX_ALLOWED_SECONDS}, whereas a quadratic one needs far more.
	 * </p>
	 *
	 * <p>
	 * {@link #testLargeListPerformance()} appends all elements to an empty list. Each element gets a
	 * sort order of {@link OrderedLinkUtil#APPEND_INC} above its predecessor. This number of
	 * elements stays far below the point where the range of sort-order values is exhausted
	 * ({@link OrderedLinkUtil#MAX_ORDER} / {@link OrderedLinkUtil#APPEND_INC}).
	 * </p>
	 * 
	 * <p>
	 * {@link #testInsertBeforeExistingPerformance()} inserts half of the elements in front of the
	 * other half. The inserted elements share the sort-order gap before the first existing element.
	 * Inserting them one after another halves the remaining gap with each element and renumbers the
	 * whole list again and again. Splitting the elements in halves maximizes the cost of this, since
	 * each renumbering covers the whole list.
	 * </p>
	 * 
	 * <p>
	 * {@link #testInsertRunExceedingGapPerformance()} inserts a run of {@link #LARGE_RUN_COUNT}
	 * elements in front of {@link #LARGE_RUN_EXISTING_COUNT} existing elements.
	 * </p>
	 */
	private static final int CHILDREN_COUNT = 10_000;

	/**
	 * Number of elements in the list before {@link #testInsertRunExceedingGapPerformance()} inserts
	 * {@link #LARGE_RUN_COUNT} elements in front of them.
	 */
	private static final int LARGE_RUN_EXISTING_COUNT = 1_000;

	/**
	 * Number of elements that {@link #testInsertRunExceedingGapPerformance()} inserts in front of
	 * {@link #LARGE_RUN_EXISTING_COUNT} existing elements.
	 * 
	 * <p>
	 * The existing elements are appended to an empty list, so the free sort-order range before the
	 * first existing element is {@link OrderedLinkUtil#APPEND_INC}. The inserted run is at least as
	 * large as this range, so the elements of the run cannot be spaced within it, and the list is
	 * renumbered once for the whole run.
	 * </p>
	 */
	private static final int LARGE_RUN_COUNT = CHILDREN_COUNT - LARGE_RUN_EXISTING_COUNT;

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
		assertFastEnough("append " + CHILDREN_COUNT + " elements", () -> setChildren(_children));
		assertEquals(_children, getChildren());
		inTransaction(() -> setChildren(emptyList()));
	}

	/**
	 * Checks that inserting {@link #CHILDREN_COUNT} / 2 elements in front of the other
	 * {@link #CHILDREN_COUNT} / 2 elements stays within {@link #MAX_ALLOWED_SECONDS} of CPU time.
	 */
	public void testInsertBeforeExistingPerformance() {
		assertInsertFastEnough(CHILDREN_COUNT / 2);
	}

	/**
	 * Checks that inserting {@link #LARGE_RUN_COUNT} elements in front of
	 * {@link #LARGE_RUN_EXISTING_COUNT} existing elements stays within {@link #MAX_ALLOWED_SECONDS}
	 * of CPU time.
	 */
	public void testInsertRunExceedingGapPerformance() {
		assertTrue("Inserted run must not fit into the sort-order range before the first existing element.",
			LARGE_RUN_COUNT >= OrderedLinkUtil.APPEND_INC);
		assertInsertFastEnough(LARGE_RUN_COUNT);
	}

	/**
	 * Sets the list to all but the given number of leading elements of {@link #CHILDREN_COUNT}
	 * elements, and checks the time for inserting the leading elements in front of them.
	 */
	private void assertInsertFastEnough(int insertedCount) {
		inTransaction(() -> setChildren(_children.subList(insertedCount, CHILDREN_COUNT)));

		assertFastEnough(
			"insert " + insertedCount + " elements in front of " + (CHILDREN_COUNT - insertedCount) + " elements",
			() -> setChildren(_children));
		assertEquals(_children, getChildren());
		inTransaction(() -> setChildren(emptyList()));
	}

	/**
	 * Checks the order of the list after inserting elements before, between, and after existing
	 * elements.
	 */
	public void testInsertAtSeveralPositions() {
		TLObject a = _children.get(0);
		TLObject b = _children.get(1);
		TLObject c = _children.get(2);
		TLObject d = _children.get(3);
		TLObject e = _children.get(4);
		TLObject f = _children.get(5);
		TLObject g = _children.get(6);

		inTransaction(() -> setChildren(List.of(b, e)));
		assertEquals(List.of(b, e), getChildren());

		inTransaction(() -> setChildren(List.of(a, b, c, d, e, f, g)));
		assertEquals(List.of(a, b, c, d, e, f, g), getChildren());

		inTransaction(() -> setChildren(List.of(a, c, e)));
		assertEquals(List.of(a, c, e), getChildren());

		inTransaction(() -> setChildren(emptyList()));
		assertEquals(emptyList(), getChildren());
	}

	/**
	 * Checks {@link TLObject#tContains(TLStructuredTypePart, Object)} for an ordered reference that
	 * is no {@link TLStructuredTypePart#isBag() bag}: after adding, reordering, replacing, and
	 * removing elements, within the transaction, after the commit, and after a rollback.
	 */
	public void testContains() {
		assertFalse(getTestedAttribute().isBag());
		assertEquals(ListStorage.class, getTestedStorage().getClass());

		TLObject a = _children.get(0);
		TLObject b = _children.get(1);
		TLObject c = _children.get(2);
		TLObject absent = _children.get(3);

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
		assertEquals(List.of(a, b), getChildren());
		assertContainsExactly(List.of(a, b));
		assertFalse("A value that is no element type instance is not contained.", containsChild("a"));
		assertFalse(containsChild(null));

		// Reorder.
		inTransaction(() -> {
			setChildren(List.of(b, a));
			assertContainsExactly(List.of(a, b));
		});
		assertEquals(List.of(b, a), getChildren());
		assertContainsExactly(List.of(a, b));

		// Replace.
		inTransaction(() -> {
			setChildren(List.of(c, b));
			assertContainsExactly(List.of(b, c));
		});
		assertEquals(List.of(c, b), getChildren());
		assertContainsExactly(List.of(b, c));

		// Remove.
		inTransaction(() -> {
			removeChild(c);
			assertContainsExactly(List.of(b));
		});
		assertContainsExactly(List.of(b));

		// Rolled back add, reorder, and remove.
		inRolledBackTransaction(() -> {
			addChild(a);
			assertContainsExactly(List.of(a, b));
			setChildren(List.of(a, c));
			assertContainsExactly(List.of(a, c));
			removeChild(a);
			assertContainsExactly(List.of(c));
		});
		assertEquals(List.of(b), getChildren());
		assertContainsExactly(List.of(b));

		inTransaction(() -> setChildren(emptyList()));
		assertContainsExactly(emptyList());
	}

	/**
	 * Checks {@link TLObject#tContains(TLStructuredTypePart, Object)} for an ordered reference that
	 * is no {@link TLStructuredTypePart#isBag() bag} but holds more than one link to the same
	 * element, as data stored without duplicate check may do.
	 */
	public void testContainsDuplicateLinks() {
		TLObject a = _children.get(0);
		TLObject b = _children.get(1);

		inTransaction(() -> {
			addChild(a);
			addChild(b);
			addChild(a);
			assertContainsExactly(List.of(a, b));
		});
		assertEquals(List.of(a, b, a), getChildren());
		assertContainsExactly(List.of(a, b));

		inTransaction(() -> {
			removeChild(a);
			assertEquals(List.of(b, a), getChildren());
			assertContainsExactly(List.of(a, b));
		});
		assertEquals(List.of(b, a), getChildren());
		assertContainsExactly(List.of(a, b));

		inTransaction(() -> setChildren(emptyList()));
		assertContainsExactly(emptyList());
	}

	/**
	 * Checks {@link TLObject#tContains(TLStructuredTypePart, Object)} for an ordered
	 * {@link TLStructuredTypePart#isBag() bag} holding an element more than once.
	 */
	public void testBagContains() {
		TLStructuredTypePart bag = _parent.tType().getPart(PARENT_TO_BAG_CHILDREN_ATTRIBUTE_NAME);
		assertTrue(bag.isBag());
		assertEquals(ListStorage.class, bag.getStorageImplementation().getClass());

		TLObject a = _children.get(0);
		TLObject b = _children.get(1);

		inTransaction(() -> {
			_parent.tAdd(bag, a);
			_parent.tAdd(bag, b);
			_parent.tAdd(bag, a);
			assertTrue(_parent.tContains(bag, a));
			assertTrue(_parent.tContains(bag, b));
		});
		assertEquals(List.of(a, b, a), _parent.tValue(bag));

		inTransaction(() -> {
			_parent.tRemove(bag, a);
			assertTrue(_parent.tContains(bag, a));
			_parent.tRemove(bag, a);
			assertFalse(_parent.tContains(bag, a));
			assertTrue(_parent.tContains(bag, b));
		});
		assertFalse(_parent.tContains(bag, a));
		assertTrue(_parent.tContains(bag, b));

		inTransaction(() -> _parent.tUpdate(bag, emptyList()));
		assertFalse(_parent.tContains(bag, b));
	}

	/**
	 * Checks that reordering a composite ordered reference keeps the moved parts alive and in the
	 * list at their new positions.
	 */
	public void testReorderComposite() {
		TLStructuredTypePart parts = _parent.tType().getPart(PARENT_TO_PARTS_ATTRIBUTE_NAME);
		assertTrue(((TLReference) parts).isComposite());
		assertEquals(ListStorage.class, parts.getStorageImplementation().getClass());

		TLObject a = _children.get(0);
		TLObject b = _children.get(1);
		TLObject c = _children.get(2);

		inTransaction(() -> _parent.tUpdate(parts, List.of(a, b, c)));
		assertEquals(List.of(a, b, c), _parent.tValue(parts));

		inTransaction(() -> {
			_parent.tUpdate(parts, List.of(c, a, b));
			assertTrue(c.tValid());
			assertEquals(List.of(c, a, b), _parent.tValue(parts));
		});
		assertTrue(a.tValid());
		assertTrue(b.tValid());
		assertTrue(c.tValid());
		assertEquals(List.of(c, a, b), _parent.tValue(parts));

		inTransaction(() -> {
			_parent.tUpdate(parts, List.of(b, c));
			assertEquals(List.of(b, c), _parent.tValue(parts));
		});
		assertTrue(a.tValid());
		assertTrue(b.tValid());
		assertTrue(c.tValid());
		assertEquals(List.of(b, c), _parent.tValue(parts));

		inTransaction(() -> _parent.tUpdate(parts, emptyList()));
		assertTrue(b.tValid());
		assertTrue(c.tValid());
	}

	/**
	 * Checks that appending {@link #CHILDREN_COUNT} elements one by one to an empty list, each
	 * after checking that it is not yet contained, stays within {@link #MAX_ALLOWED_SECONDS} of CPU
	 * time.
	 */
	public void testContainsThenAddOneByOnePerformance() {
		assertFastEnough("check and add " + CHILDREN_COUNT + " elements one by one", () -> {
			for (TLObject child : _children) {
				if (!containsChild(child)) {
					addChild(child);
				}
			}
		});
		assertEquals(_children, getChildren());
		inTransaction(() -> setChildren(emptyList()));
	}

	private void assertContainsExactly(List<TLObject> expected) {
		for (TLObject child : _children.subList(0, 4)) {
			assertEquals("Contains " + _children.indexOf(child) + ":", expected.contains(child), containsChild(child));
		}
	}

	private static void inRolledBackTransaction(Runnable change) {
		Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("rolled back"));
		try {
			change.run();
		} finally {
			tx.rollback();
		}
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
		return "TLObject.setList/tContains/tAdd(" + operation + ") should take less than " + MAX_ALLOWED_SECONDS
			+ " seconds of " + limit + ", but took: CPU time " + cpuTime + ", wall-clock time " + wallWatch;
	}

	private List<?> getChildren() {
		return (List<?>) _parent.tValueByName(PARENT_TO_CHILDREN_ATTRIBUTE_NAME);
	}

	private boolean containsChild(Object child) {
		return _parent.tContainsByName(PARENT_TO_CHILDREN_ATTRIBUTE_NAME, child);
	}

	private void addChild(TLObject child) {
		_parent.tAddByName(PARENT_TO_CHILDREN_ATTRIBUTE_NAME, child);
	}

	private void removeChild(TLObject child) {
		_parent.tRemoveByName(PARENT_TO_CHILDREN_ATTRIBUTE_NAME, child);
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
