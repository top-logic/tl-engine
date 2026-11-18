/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import junit.framework.Test;

import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.ModelFactory;
import com.top_logic.knowledge.service.CommitActions;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.event.Modification;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLChangeHandler;
import com.top_logic.model.annotate.TLLifecycleHandler;
import com.top_logic.model.annotate.TLLifecycleHandlers;
import com.top_logic.model.initializer.TLObjectInitializer;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test of {@link TLLifecycleHandlers} annotated to types.
 */
@SuppressWarnings("javadoc")
public class TestLifecycleHandlers extends TestWithModelExtension {

	private static final String MODULE = TestLifecycleHandlers.class.getName();

	private static final String BASE = "Base";

	private static final String SUB = "Sub";

	private static final String PLAIN = "Plain";

	private static final String LOG = "log";

	private static final String WATCHED = "watched";

	private static final String INIT_TAG = "init";

	private static final String BASE_TAG = "base";

	private static final String SUB_TAG = "sub";

	private static final String CHANGED_TAG = "changed";

	private static final String PARTS = "parts";

	private static final String CREATED = "created";

	private static final String DELETING = "deleting";

	private static final String DELETED = "deleted";

	/**
	 * The {@link #PLAIN} objects created by the {@link Modification}s of the {@link Recorder}s
	 * after a deletion.
	 */
	static final List<TLObject> RECORDS = new ArrayList<>();

	private static TestLifecycleHandlers _current;

	/**
	 * The calls of the handlers since the last {@link #setUp()}.
	 */
	static final List<Call> CALLS = new ArrayList<>();

	/**
	 * Additional action of the {@link Recorder}s, <code>null</code> for none.
	 */
	static Consumer<TLObject> ACTION;

	private ModelFactory _factory;

	private TLModule _module;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_module = TLModelUtil.findModule(MODULE);
		_factory = DynamicModelService.getFactoryFor(MODULE);
		CALLS.clear();
		RECORDS.clear();
		ACTION = null;
		_current = this;
	}

	@Override
	protected void tearDown() throws Exception {
		ACTION = null;
		_current = null;
		if (!RECORDS.isEmpty()) {
			delete(RECORDS.stream().filter(TLObject::tValid).toArray(TLObject[]::new));
			RECORDS.clear();
		}
		super.tearDown();
	}

	public void testCreated() {
		TLObject base = create(BASE);
		try {
			assertCalls(new Call(CREATED, BASE_TAG, base));
		} finally {
			delete(base);
		}
	}

	public void testCreatedSubtype() {
		TLObject sub = create(SUB);
		try {
			// Handlers of the generalization are called first.
			assertCalls(new Call(CREATED, BASE_TAG, sub), new Call(CREATED, SUB_TAG, sub));
		} finally {
			delete(sub);
		}
	}

	public void testCreatedAfterInitializersInSameCommit() {
		TLObject sub = create(SUB);
		try {
			// The initializer ran first, the changes of the handlers are committed with the object.
			assertEquals(INIT_TAG + "," + BASE_TAG + "," + SUB_TAG, sub.tValueByName(LOG));
		} finally {
			delete(sub);
		}
	}

	public void testCreatedSeveral() {
		List<TLObject> objects = new ArrayList<>();
		try (Transaction tx = beginTX()) {
			objects.add(newObject(BASE));
			objects.add(newObject(PLAIN));
			objects.add(newObject(SUB));
			tx.commit();
		}
		try {
			TLObject base = objects.get(0);
			TLObject sub = objects.get(2);
			assertCallsUnordered(
				new Call(CREATED, BASE_TAG, base),
				new Call(CREATED, BASE_TAG, sub),
				new Call(CREATED, SUB_TAG, sub));
		} finally {
			delete(objects.toArray(TLObject[]::new));
		}
	}

	public void testUnannotatedType() {
		TLObject plain = create(PLAIN);
		try {
			assertCalls();
			assertNull(plain.tValueByName(LOG));
		} finally {
			delete(plain);
		}
	}

	public void testNotCalledForUpdate() {
		TLObject base = create(BASE);
		try {
			CALLS.clear();
			try (Transaction tx = beginTX()) {
				base.tUpdateByName(LOG, "x");
				tx.commit();
			}
			assertCalls();
		} finally {
			delete(base);
		}
	}

	public void testCreatedAndDeletedInSameTransaction() {
		try (Transaction tx = beginTX()) {
			TLObject base = newObject(BASE);
			base.tDelete();
			tx.commit();
		}
		assertCalls();
	}

	public void testObjectsCreatedByHandlerNotReported() {
		List<TLObject> created = new ArrayList<>();
		ACTION = object -> {
			if (created.isEmpty()) {
				created.add(newObject(BASE));
			}
		};
		TLObject base = create(BASE);
		ACTION = null;
		try {
			assertEquals(1, created.size());
			assertCalls(new Call(CREATED, BASE_TAG, base));
			assertTrue(created.get(0).tValid());
			assertNull("Initializers are not applied to objects created by handlers.",
				created.get(0).tValueByName(LOG));
		} finally {
			delete(base, created.get(0));
		}
	}

	public void testChangeByHandlerDoesNotTriggerChangeHandlers() {
		TLObject other = create(BASE);
		CALLS.clear();
		ACTION = object -> {
			if (object != other) {
				other.tUpdateByName(WATCHED, "x");
			}
		};
		TLObject base = create(BASE);
		ACTION = null;
		try {
			assertCalls(new Call(CREATED, BASE_TAG, base));
			assertEquals("x", other.tValueByName(WATCHED));

			// The change handler itself is active.
			update(other, WATCHED, "y");
			assertCalls(new Call(CHANGED_TAG, WATCHED, other));
		} finally {
			delete(base, other);
		}
	}

	public void testDeleting() {
		TLObject base = create(BASE);
		CALLS.clear();

		delete(base);

		// The handler read the value of the object before the deletion.
		assertCalls(new Call(DELETING, BASE_TAG, base));
		assertEquals(1, RECORDS.size());
		TLObject record = RECORDS.get(0);
		assertTrue("Change of the modification is committed.", record.tValid());
		assertEquals(DELETED + ":" + BASE_TAG + ":" + INIT_TAG + "," + BASE_TAG, record.tValueByName(LOG));
		assertFalse(base.tValid());
	}

	public void testDeletingSubtype() {
		TLObject sub = create(SUB);
		CALLS.clear();

		delete(sub);

		// Handlers of the generalization are called first.
		assertCalls(new Call(DELETING, BASE_TAG, sub), new Call(DELETING, SUB_TAG, sub));
		assertEquals(2, RECORDS.size());
		assertEquals(DELETED + ":" + BASE_TAG + ":" + INIT_TAG + "," + BASE_TAG + "," + SUB_TAG,
			RECORDS.get(0).tValueByName(LOG));
		assertEquals(DELETED + ":" + SUB_TAG + ":" + INIT_TAG + "," + BASE_TAG + "," + SUB_TAG,
			RECORDS.get(1).tValueByName(LOG));
	}

	public void testDeletingWithinTransaction() {
		TLObject base = create(BASE);
		CALLS.clear();
		try (Transaction tx = beginTX()) {
			base.tDelete();

			// Reported with the delete, the changes of the modification are part of the transaction.
			assertCalls(new Call(DELETING, BASE_TAG, base));
			assertEquals(1, RECORDS.size());
			assertTrue(RECORDS.get(0).tValid());
			tx.commit();
		}
		assertTrue(RECORDS.get(0).tValid());
	}

	public void testDeletingRolledBack() {
		TLObject base = create(BASE);
		CALLS.clear();
		try {
			try (Transaction tx = beginTX()) {
				base.tDelete();
				assertEquals(1, RECORDS.size());
				tx.rollback();
			}
			assertCalls(new Call(DELETING, BASE_TAG, base));
			assertTrue(base.tValid());
			assertFalse("Change of the modification must be rolled back.", RECORDS.get(0).tValid());
			RECORDS.clear();
		} finally {
			delete(base);
		}
	}

	public void testDeletingUnannotatedType() {
		TLObject plain = create(PLAIN);
		delete(plain);
		assertCalls();
		assertTrue(RECORDS.isEmpty());
	}

	public void testDeletingNotCalledForNewObject() {
		try (Transaction tx = beginTX()) {
			TLObject base = newObject(BASE);
			base.tDelete();
			tx.commit();
		}
		assertCalls();
		assertTrue(RECORDS.isEmpty());
	}

	public void testDeletingComposition() {
		TLObject owner;
		TLObject part;
		try (Transaction tx = beginTX()) {
			owner = newObject(BASE);
			part = newObject(SUB);
			owner.tUpdateByName(PARTS, List.of(part));
			tx.commit();
		}
		CALLS.clear();

		delete(owner);

		assertFalse(part.tValid());
		assertCallsUnordered(
			new Call(DELETING, BASE_TAG, owner),
			new Call(DELETING, BASE_TAG, part),
			new Call(DELETING, SUB_TAG, part));
		assertEquals(3, RECORDS.size());
	}

	public void testDeleteInModificationReportedAgain() {
		TLObject first = create(BASE);
		TLObject second = create(BASE);
		CALLS.clear();
		ACTION = object -> {
			if (object == first) {
				second.tDelete();
			}
		};

		delete(first);
		ACTION = null;

		assertCalls(new Call(DELETING, BASE_TAG, first), new Call(DELETING, BASE_TAG, second));
		assertFalse(second.tValid());
	}

	private void assertCalls(Call... expected) {
		assertEquals(List.of(expected), CALLS);
		CALLS.clear();
	}

	private void assertCallsUnordered(Call... expected) {
		assertEquals(set(expected), set(CALLS.toArray(Call[]::new)));
		assertEquals(expected.length, CALLS.size());

		// Per object, the handlers of the generalizations come first.
		List<Call> expectedOrder = new ArrayList<>(List.of(expected));
		for (Call call : new ArrayList<>(CALLS)) {
			Call firstOfObject = expectedOrder.stream().filter(c -> c.object() == call.object()).findFirst().get();
			assertEquals(firstOfObject, call);
			expectedOrder.remove(firstOfObject);
		}
		CALLS.clear();
	}

	private TLObject create(String typeName) {
		try (Transaction tx = beginTX()) {
			TLObject result = newObject(typeName);
			tx.commit();
			return result;
		}
	}

	TLObject newObject(String typeName) {
		return _factory.createObject((TLClass) TLModelUtil.findType(_module, typeName));
	}

	private void update(TLObject object, String attribute, Object value) {
		try (Transaction tx = beginTX()) {
			object.tUpdateByName(attribute, value);
			tx.commit();
		}
	}

	static void appendLog(TLObject object, String tag) {
		Object log = object.tValueByName(LOG);
		object.tUpdateByName(LOG, log == null ? tag : log + "," + tag);
	}

	/**
	 * A call of a handler.
	 *
	 * @param event
	 *        The kind of the notification.
	 * @param tag
	 *        Identification of the handler, or the changed attribute for a change handler.
	 * @param object
	 *        The object the handler was called for.
	 */
	record Call(String event, String tag, TLObject object) {
		// Pure data.
	}

	/**
	 * {@link TLObjectInitializer} writing {@link TestLifecycleHandlers#INIT_TAG} to
	 * {@link TestLifecycleHandlers#LOG}.
	 */
	public static class Init implements TLObjectInitializer {

		@Override
		public void initializeObject(TLObject object) {
			appendLog(object, INIT_TAG);
		}

	}

	/**
	 * {@link TLLifecycleHandler} recording its calls in {@link TestLifecycleHandlers#CALLS} and
	 * appending its tag to the attribute {@link TestLifecycleHandlers#LOG}.
	 */
	public static abstract class Recorder implements TLLifecycleHandler {

		private final String _tag;

		Recorder(String tag) {
			_tag = tag;
		}

		@Override
		public void created(TLObject object, CommitActions commitActions) {
			CALLS.add(new Call(CREATED, _tag, object));
			appendLog(object, _tag);
			if (ACTION != null) {
				ACTION.accept(object);
			}
		}

		/**
		 * Reads the {@link TestLifecycleHandlers#LOG} of the deleted object and records it in a
		 * new {@link TestLifecycleHandlers#PLAIN} object after the deletion.
		 */
		@Override
		public Modification deleting(TLObject object, CommitActions commitActions) {
			CALLS.add(new Call(DELETING, _tag, object));
			String record = DELETED + ":" + _tag + ":" + object.tValueByName(LOG);
			Consumer<TLObject> action = ACTION;
			return () -> {
				TLObject plain = _current.newObject(PLAIN);
				plain.tUpdateByName(LOG, record);
				RECORDS.add(plain);
				if (action != null) {
					action.accept(object);
				}
			};
		}

	}

	/**
	 * {@link Recorder} annotated to the type {@link TestLifecycleHandlers#BASE}.
	 */
	public static class BaseRecorder extends Recorder {

		public BaseRecorder() {
			super(BASE_TAG);
		}

	}

	/**
	 * {@link Recorder} annotated to the type {@link TestLifecycleHandlers#SUB}.
	 */
	public static class SubRecorder extends Recorder {

		public SubRecorder() {
			super(SUB_TAG);
		}

	}

	/**
	 * {@link TLChangeHandler} recording its calls in {@link TestLifecycleHandlers#CALLS}.
	 */
	public static class ChangeRecorder implements TLChangeHandler {

		@Override
		public void changed(TLObject object, TLStructuredTypePart attribute, Object oldValue, Object newValue,
				CommitActions commitActions) {
			CALLS.add(new Call(CHANGED_TAG, attribute.getName(), object));
		}

	}

	public static Test suite() {
		return suite(new ModelExtensionTestSetup(TestLifecycleHandlers.class));
	}

}
