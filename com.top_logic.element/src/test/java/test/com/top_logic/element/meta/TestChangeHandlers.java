/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.ExpectedFailure;
import test.com.top_logic.basic.ExpectedFailureProtocol;

import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.ModelFactory;
import com.top_logic.knowledge.service.CommitActions;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLChangeHandler;
import com.top_logic.model.annotate.TLChangeHandlers;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.I18NConstants;
import com.top_logic.util.model.ModelService;

/**
 * Test of {@link TLChangeHandlers} annotated to attributes with different storages.
 */
@SuppressWarnings("javadoc")
public class TestChangeHandlers extends TestWithModelExtension {

	private static final String MODULE = TestChangeHandlers.class.getName();

	private static final String HOLDER = "Holder";

	private static final String SUB = "Sub";

	private static final String ITEM = "Item";

	private static final String NAME = "name";

	private static final String COUNT = "count";

	private static final String SINGLE = "single";

	private static final String OTHERS = "others";

	private static final String ITEMS = "items";

	private static final String OWNED = "owned";

	private static final String SINGLE_HOLDERS = "singleHolders";

	private static final String OTHER_HOLDERS = "otherHolders";

	private static final String PLAIN = "plain";

	private static final String LOG = "log";

	private static final String REQUIRED = "requiredTestChangeHandlers";

	/**
	 * The calls of {@link Recorder} since the last {@link #setUp()}.
	 */
	static final List<Call> CALLS = new ArrayList<>();

	private ModelFactory _factory;

	private TLModule _module;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_module = TLModelUtil.findModule(MODULE);
		_factory = DynamicModelService.getFactoryFor(MODULE);
		CALLS.clear();
	}

	public void testColumnAttribute() {
		TLObject holder = create(HOLDER);
		try {
			update(holder, COUNT, 5);
			assertCalls(new Call(holder, COUNT, null, 5));

			update(holder, COUNT, 7);
			assertCalls(new Call(holder, COUNT, 5, 7));
		} finally {
			delete(holder);
		}
	}

	public void testFlexAttribute() {
		TLObject holder = create(HOLDER);
		try {
			update(holder, NAME, "a");
			assertCalls(new Call(holder, NAME, null, "a"));

			update(holder, NAME, "b");
			assertCalls(new Call(holder, NAME, "a", "b"));
		} finally {
			delete(holder);
		}
	}

	public void testSingleReferenceInColumn() {
		TLObject holder = create(HOLDER);
		TLObject item1 = create(ITEM);
		TLObject item2 = create(ITEM);
		try {
			update(holder, SINGLE, item1);
			assertCalls(
				new Call(holder, SINGLE, null, item1),
				new Call(item1, SINGLE_HOLDERS, Set.of(), Set.of(holder)));

			update(holder, SINGLE, item2);
			assertCalls(
				new Call(holder, SINGLE, item1, item2),
				new Call(item1, SINGLE_HOLDERS, Set.of(holder), Set.of()),
				new Call(item2, SINGLE_HOLDERS, Set.of(), Set.of(holder)));

			update(holder, SINGLE, null);
			assertCalls(
				new Call(holder, SINGLE, item2, null),
				new Call(item2, SINGLE_HOLDERS, Set.of(holder), Set.of()));
		} finally {
			delete(holder, item1, item2);
		}
	}

	public void testReferenceInLinkTable() {
		TLObject holder = create(HOLDER);
		TLObject item1 = create(ITEM);
		TLObject item2 = create(ITEM);
		try {
			update(holder, OTHERS, Set.of(item1));
			assertCalls(
				new Call(holder, OTHERS, Set.of(), Set.of(item1)),
				new Call(item1, OTHER_HOLDERS, Set.of(), Set.of(holder)));

			update(holder, OTHERS, Set.of(item1, item2));
			assertCalls(
				new Call(holder, OTHERS, Set.of(item1), Set.of(item1, item2)),
				new Call(item2, OTHER_HOLDERS, Set.of(), Set.of(holder)));

			update(holder, OTHERS, Set.of());
			assertCalls(
				new Call(holder, OTHERS, Set.of(item1, item2), Set.of()),
				new Call(item1, OTHER_HOLDERS, Set.of(holder), Set.of()),
				new Call(item2, OTHER_HOLDERS, Set.of(holder), Set.of()));
		} finally {
			delete(holder, item1, item2);
		}
	}

	public void testReferenceInTargetTable() {
		TLObject holder = create(HOLDER);
		TLObject item = null;
		try {
			try (Transaction tx = beginTX()) {
				item = newObject(ITEM);
				holder.tUpdateByName(ITEMS, Set.of(item));
				tx.commit();
			}
			assertCalls(new Call(holder, ITEMS, Set.of(), Set.of(item)));

			update(holder, ITEMS, Set.of());
			assertCalls(new Call(holder, ITEMS, Set.of(item), Set.of()));
		} finally {
			delete(holder);
			if (item != null && item.tValid()) {
				delete(item);
			}
		}
	}

	public void testReferenceStoredInTargetRow() {
		TLObject holder = create(HOLDER);
		TLObject item1 = create(ITEM);
		TLObject item2 = create(ITEM);
		try {
			update(holder, OWNED, item1);
			assertCalls(new Call(holder, OWNED, null, item1));

			update(holder, OWNED, item2);
			assertCalls(new Call(holder, OWNED, item1, item2));

			update(holder, OWNED, null);
			assertCalls(new Call(holder, OWNED, item2, null));
		} finally {
			delete(holder, item1, item2);
		}
	}

	public void testReverseReferenceOfCreatedAndDeletedReferrer() {
		TLObject item = create(ITEM);
		try {
			TLObject holder;
			try (Transaction tx = beginTX()) {
				holder = newObject(HOLDER);
				holder.tUpdateByName(SINGLE, item);
				tx.commit();
			}
			assertCalls(new Call(item, SINGLE_HOLDERS, Set.of(), Set.of(holder)));

			delete(holder);
			assertCalls(new Call(item, SINGLE_HOLDERS, Set.of(holder), Set.of()));
		} finally {
			delete(item);
		}
	}

	public void testRejectedForUnobservableAttribute() {
		try (Transaction tx = beginTX()) {
			DynamicModelService.extendModel(new ExpectedFailureProtocol(), ModelService.getApplicationModel(),
				ModelService.getInstance().getFactory(),
				ClassRelativeBinaryContent.withSuffix(TestChangeHandlers.class, "rejected.model.xml"));
			fail("Change handlers on a derived attribute must be rejected.");
		} catch (ExpectedFailure ex) {
			for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
				if (cause.getMessage() != null
					&& cause.getMessage().contains("Rejected#derived is not allowed: Changes of the values of")) {
					return;
				}
			}
			throw ex;
		}
	}

	public void testChangeOfHandlerInSameCommit() {
		TLObject holder = create(HOLDER);
		try {
			update(holder, NAME, "a");
			assertEquals(NAME, holder.tValueByName(LOG));
		} finally {
			delete(holder);
		}
	}

	public void testUnannotatedAttribute() {
		TLObject holder = create(HOLDER);
		try {
			update(holder, PLAIN, "a");
			assertCalls();
		} finally {
			delete(holder);
		}
	}

	public void testValueSetBack() {
		TLObject holder = create(HOLDER);
		TLObject item = create(ITEM);
		try {
			update(holder, NAME, "a");
			update(holder, OTHERS, Set.of(item));
			CALLS.clear();

			try (Transaction tx = beginTX()) {
				holder.tUpdateByName(NAME, "b");
				holder.tUpdateByName(NAME, "a");
				holder.tUpdateByName(OTHERS, Set.of());
				holder.tUpdateByName(OTHERS, Set.of(item));
				tx.commit();
			}
			assertCalls();
		} finally {
			delete(holder, item);
		}
	}

	public void testCreatedObject() {
		TLObject holder;
		try (Transaction tx = beginTX()) {
			holder = newObject(HOLDER);
			holder.tUpdateByName(NAME, "a");
			holder.tUpdateByName(COUNT, 1);
			tx.commit();
		}
		try {
			assertCalls();
		} finally {
			delete(holder);
		}
	}

	public void testDeletedObject() {
		TLObject holder = create(HOLDER);
		TLObject item = create(ITEM);
		update(holder, OTHERS, Set.of(item));
		CALLS.clear();
		try (Transaction tx = beginTX()) {
			holder.tUpdateByName(NAME, "a");
			holder.tDelete();
			tx.commit();
		}
		// No call for the deleted object, but for the reverse reference of the object it referenced.
		assertCalls(new Call(item, OTHER_HOLDERS, Set.of(holder), Set.of()));
		delete(item);
	}

	public void testChangeOfOtherTypeInSameTable() {
		TLObject item = create(ITEM);
		try {
			// Item#name is stored in the same table and attribute as Holder#name.
			update(item, NAME, "a");
			assertCalls();
		} finally {
			delete(item);
		}
	}

	public void testSubtype() {
		TLObject sub = create(SUB);
		try {
			// Sub#name ignores the inherited handler of Holder#name, it has one handler of its own.
			update(sub, NAME, "a");
			assertCalls(new Call(sub, NAME, null, "a"));
		} finally {
			delete(sub);
		}
	}

	public void testInheritedAndLocalHandlers() {
		TLObject sub = create(SUB);
		try {
			// Sub#count has a handler of its own besides the inherited one of Holder#count.
			update(sub, COUNT, 1);
			assertCalls(new Call(sub, COUNT, null, 1), new Call(sub, COUNT, null, 1));
		} finally {
			delete(sub);
		}
	}

	public void testHandlerOfOverride() {
		TLObject holder = create(HOLDER);
		TLObject sub = create(SUB);
		try {
			update(holder, PLAIN, "a");
			assertCalls();

			update(sub, PLAIN, "a");
			assertCalls(new Call(sub, PLAIN, null, "a"));
		} finally {
			delete(holder, sub);
		}
	}

	public void testVetoedCommitRollsBackHandlerChanges() {
		TLObject holder = create(HOLDER);
		try {
			withMandatoryAttribute(holder.tType(), () -> {
				try (Transaction tx = beginTX()) {
					holder.tUpdateByName(NAME, "a");
					tx.commit();
					fail("Commit of a change to an invalid object did not fail.");
				} catch (RuntimeException ex) {
					assertConstraintViolation(ex);
				}
				assertCalls(new Call(holder, NAME, null, "a"));
				assertNull(holder.tValueByName(NAME));
				assertNull("Change of the handler must be rolled back.", holder.tValueByName(LOG));
			});
		} finally {
			delete(holder);
		}
	}

	private void assertCalls(Call... expected) {
		// The order of calls for different objects is not specified.
		assertEquals(count(List.of(expected)), count(CALLS));
		CALLS.clear();
	}

	private static Map<Call, Integer> count(List<Call> calls) {
		Map<Call, Integer> result = new HashMap<>();
		for (Call call : calls) {
			result.merge(call, 1, Integer::sum);
		}
		return result;
	}

	private TLObject create(String typeName) {
		try (Transaction tx = beginTX()) {
			TLObject result = newObject(typeName);
			tx.commit();
			return result;
		}
	}

	private TLObject newObject(String typeName) {
		return _factory.createObject((TLClass) TLModelUtil.findType(_module, typeName));
	}

	private void update(TLObject object, String attribute, Object value) {
		try (Transaction tx = beginTX()) {
			object.tUpdateByName(attribute, value);
			tx.commit();
		}
	}

	/**
	 * Runs the given test while the given type has an additional mandatory attribute without
	 * value in all existing instances.
	 */
	private void withMandatoryAttribute(TLType type, Runnable test) {
		TLStructuredTypePart required;
		try (Transaction tx = beginTX()) {
			TLType stringType = ModelService.getApplicationModel().getModule("tl.core").getType("String");
			required = TLModelUtil.addProperty((TLClass) type, REQUIRED, stringType);
			required.setMandatory(true);
			tx.commit();
		}
		try {
			test.run();
		} finally {
			delete(required);
		}
	}

	private static void assertConstraintViolation(RuntimeException ex) {
		for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
			if (cause instanceof TopLogicException tlEx
				&& I18NConstants.CONSTRAINTS_VIOLATED.equals(tlEx.getErrorKey())) {
				return;
			}
		}
		throw new AssertionError("Commit failed without a constraint violation.", ex);
	}

	/**
	 * A call of {@link Recorder}.
	 */
	record Call(TLObject object, TLStructuredTypePart attribute, Object oldValue, Object newValue) {

		Call(TLObject object, String attribute, Object oldValue, Object newValue) {
			this(object, object.tType().getPart(attribute), oldValue, newValue);
		}

	}

	/**
	 * {@link TLChangeHandler} recording its calls in {@link TestChangeHandlers#CALLS} and writing
	 * the name of the changed attribute to the attribute {@link TestChangeHandlers#LOG}.
	 */
	public static class Recorder implements TLChangeHandler {

		@Override
		public void changed(TLObject object, TLStructuredTypePart attribute, Object oldValue, Object newValue,
				CommitActions commitActions) {
			CALLS.add(new Call(object, attribute, oldValue, newValue));
			object.tUpdateByName(LOG, attribute.getName());
		}

	}

	public static Test suite() {
		return suite(new ModelExtensionTestSetup(TestChangeHandlers.class));
	}

}
