/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLType;
import com.top_logic.model.security.ContainerRelation;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * Test of the model based access rights of objects whose type is declared locally in another
 * object, against the model module {@code TestLocalTypeSecurity} of the element test application.
 * <p>
 * A {@code Holder} declares local types specializing the global types {@code Item} (a composition
 * part delegating to its container), {@code Free} (without security) and {@code Granted} (with
 * grants of its own). The owner of a holder is responsible for it by a role rule. A local type is
 * treated as a global specialization of its generalizations without a configuration of its own,
 * which {@code ItemSub} and {@code GrantedSub} are.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestLocalTypeSecurity extends BasicTestCase {

	private static final String MODULE = "TestLocalTypeSecurity";

	private static final String HOLDER_NAME = "Holder";

	private static final String HOLDER = MODULE + ":" + HOLDER_NAME;

	private static final String ITEM_SUB = MODULE + ":ItemSub";

	private static final String GRANTED = MODULE + ":Granted";

	private static final String GRANTED_SUB = MODULE + ":GrantedSub";

	private static final String LOCAL_ITEM = "LocalItem";

	private static final String LOCAL_FREE = "LocalFree";

	private static final String LOCAL_GRANTED = "LocalGranted";

	private static final String RESPONSIBLE = "TestLocalTypeSecurity.Responsible";

	private static final String OWNER = "owner";

	private static final String ITEMS = "items";

	private static final String FREES = "frees";

	private static final List<BoundCommandGroup> OPERATIONS = List.of(
		SimpleBoundCommandGroup.READ,
		SimpleBoundCommandGroup.WRITE,
		SimpleBoundCommandGroup.CREATE,
		SimpleBoundCommandGroup.DELETE,
		SimpleBoundCommandGroup.DISCOVER,
		SimpleBoundCommandGroup.EXPORT);

	private Person _owner;

	private Person _other;

	private TLObject _holder;

	private TLObject _otherHolder;

	private TLObject _localItem;

	private TLObject _otherLocalItem;

	private TLObject _localFree;

	private final List<TLObject> _created = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		inTransaction(() -> {
			_owner = createPerson("localTypeOwner");
			_other = createPerson("localTypeOther");

			_holder = createHolder(_owner);
			_otherHolder = createHolder(_other);
		});
		inTransaction(() -> {
			_localItem = create(localType(_holder, LOCAL_ITEM));
			_holder.tUpdateByName(ITEMS, List.of(_localItem));

			_otherLocalItem = create(localType(_otherHolder, LOCAL_ITEM));
			_otherHolder.tUpdateByName(ITEMS, List.of(_otherLocalItem));

			_localFree = create(localType(_holder, LOCAL_FREE));
			_holder.tUpdateByName(FREES, List.of(_localFree));
		});
	}

	@Override
	protected void tearDown() throws Exception {
		inTransaction(() -> {
			List<TLObject> holders = List.of(_holder, _otherHolder);
			Collections.reverse(_created);
			for (TLObject object : _created) {
				if (!holders.contains(object)) {
					object.tDelete();
				}
			}
			for (TLObject holder : holders) {
				for (TLType localType : new ArrayList<>(((TLScope) holder).getTypes())) {
					// Deleting a type deletes its specializations as well.
					if (localType.tValid()) {
						deleteType(localType);
					}
				}
				holder.tDelete();
			}
			_owner.tDelete();
			_other.tDelete();
		});
		_created.clear();

		super.tearDown();
	}

	/**
	 * Runs the test body with checked security.
	 *
	 * <p>
	 * The fixture is created in {@link #setUp()} in the system context of the test. The body
	 * decides access for explicitly given persons, which is only checked outside a system context.
	 * </p>
	 */
	@Override
	protected void runTest() throws Throwable {
		TLContextManager.<Void, Throwable, RuntimeException> inPersonContext(null, () -> {
			super.runTest();
			return null;
		});
	}

	public void testFixtureHasLocalTypes() {
		TLClass localType = (TLClass) _localItem.tType();
		assertFalse("The type of the item is declared locally in its holder.", TLModelUtil.isGlobal(localType));
		assertSame(_holder, localType.getScope());
	}

	public void testResponsibleDiscoversAndReadsPart() {
		assertTrue(allowed(_owner, _holder, SimpleBoundCommandGroup.READ));
		assertTrue("The part of a local type delegates to its container, which the owner may discover.",
			allowed(_owner, _localItem, SimpleBoundCommandGroup.DISCOVER));
		assertTrue("The part of a local type delegates to its container, which the owner may read.",
			allowed(_owner, _localItem, SimpleBoundCommandGroup.READ));
		assertTrue(allowed(_owner, _localItem, SimpleBoundCommandGroup.WRITE));
	}

	public void testOtherIsDenied() {
		assertFalse(allowed(_other, _localItem, SimpleBoundCommandGroup.DISCOVER));
		assertFalse(allowed(_other, _localItem, SimpleBoundCommandGroup.READ));
		assertFalse(allowed(_other, _localItem, SimpleBoundCommandGroup.WRITE));
	}

	public void testRoleOnUnrelatedObjectDoesNotAdmit() {
		assertTrue("The other person is responsible for the other holder and its part.",
			allowed(_other, _otherLocalItem, SimpleBoundCommandGroup.READ));
		assertFalse("A role on another holder admits nothing on the part of this holder.",
			allowed(_other, _localItem, SimpleBoundCommandGroup.READ));
		assertFalse(allowed(_owner, _otherLocalItem, SimpleBoundCommandGroup.READ));
	}

	public void testLocalTypeWithoutSecurity() {
		TLClass localFree = (TLClass) _localFree.tType();
		assertTrue(rights().isWithoutSecurity(localFree));
		assertEquals(type(MODULE + ":Free"), rights().getWithoutSecurityOrigin(localFree));
		assertTrue("A local type without security is accessible without any role.",
			allowed(_other, _localFree, SimpleBoundCommandGroup.READ));
		assertTrue(allowed(_other, _localFree, SimpleBoundCommandGroup.DISCOVER));
	}

	public void testSameAsGlobalSpecialization() {
		assertSameRights(type(ITEM_SUB), (TLClass) _localItem.tType());
		assertSameRights(type(GRANTED_SUB), localType(_holder, LOCAL_GRANTED));

		TLClass localItem = (TLClass) _localItem.tType();
		assertEquals(ContainerRelation.DEFAULT, rights().getAccessParent(type(ITEM_SUB)));
		assertEquals(rights().getAccessParent(type(ITEM_SUB)), rights().getAccessParent(localItem));
		assertEquals(rights().getAccessParentTypes(type(ITEM_SUB)), rights().getAccessParentTypes(localItem));
		assertEquals(Set.of(type(HOLDER)), rights().getAccessParentTypes(localItem));
	}

	public void testInheritsExactlyPassedOnGrants() {
		TLClass localGranted = localType(_holder, LOCAL_GRANTED);
		BoundedRole responsible = BoundedRole.getRoleByName(kb(), RESPONSIBLE);
		assertEquals("The grant passed on applies to the local type.", Set.of(responsible),
			rights().getAllowedRoles(localGranted, SimpleBoundCommandGroup.READ));
		assertEquals(Set.of(responsible), rights().getAllowedRoles(type(GRANTED), SimpleBoundCommandGroup.WRITE));
		assertEquals("A grant not passed on does not apply to the local type.", Set.of(),
			rights().getAllowedRoles(localGranted, SimpleBoundCommandGroup.WRITE));
		assertNull("Without a container, the local type decides for itself.",
			rights().getAccessParent(localGranted));
	}

	public void testAccessibleTypesNameGlobalClasses() {
		Set<TLClass> accessible = rights().getAccessibleTypes(_owner, SimpleBoundCommandGroup.READ);
		assertTrue("The type level query is answered for the global classes of the model.",
			accessible.stream().allMatch(TLModelUtil::isGlobal));
		assertTrue(accessible.contains(type(MODULE + ":Free")));
	}

	private static void assertSameRights(TLClass expected, TLClass actual) {
		ModelAccessRights rights = rights();
		for (BoundCommandGroup operation : OPERATIONS) {
			assertEquals(operation.getID(), rights.getAllowedRoles(expected, operation),
				rights.getAllowedRoles(actual, operation));
			assertEquals(operation.getID(), rights.getInheritedRoles(expected, operation),
				rights.getInheritedRoles(actual, operation));
		}
		assertEquals(rights.getOperations(expected).getOperations(), rights.getOperations(actual).getOperations());
		assertEquals(rights.isWithoutSecurity(expected), rights.isWithoutSecurity(actual));
		assertEquals(rights.isInternal(expected), rights.isInternal(actual));
		assertEquals(rights.getAccessParent(expected), rights.getAccessParent(actual));
	}

	private static boolean allowed(Person person, TLObject object, BoundCommandGroup operation) {
		return rights().isAllowed(person, object, operation);
	}

	private static ModelAccessRights rights() {
		return ModelAccessRights.getInstance();
	}

	private TLObject createHolder(Person owner) {
		TLClass holderType = type(HOLDER);
		TLObject result = create(holderType);
		result.tUpdateByName(OWNER, owner);
		DynamicModelService.getInstance().setupLocalScope(holderType.getModule(), (TLScope) result, HOLDER_NAME);
		return result;
	}

	private static void deleteType(TLType type) {
		try {
			TLModelUtil.deleteRecursive(type);
		} catch (KnowledgeBaseException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static TLClass localType(TLObject holder, String name) {
		TLType result = ((TLScope) holder).getType(name);
		assertNotNull("No local type " + name + " in " + holder, result);
		return (TLClass) result;
	}

	private TLObject create(TLClass type) {
		TLObject result = ModelService.getInstance().getFactory().createObject(type);
		_created.add(result);
		return result;
	}

	private static Person createPerson(String name) {
		return Person.create(kb(), name, TLSecurityDeviceManager.getInstance().getAuthenticationDevice("dbSecurity"));
	}

	private static TLClass type(String qualifiedTypeName) {
		return (TLClass) TLModelUtil.findType(qualifiedTypeName);
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	private static void inTransaction(Runnable modification) {
		ThreadContext.withoutSecurityChecks(() -> {
			try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
				modification.run();
				tx.commit();
			}
		});
	}

	/** Return the suite of tests to perform. */
	public static Test suite() {
		Test suite = ServiceTestSetup.createSetup(new TestSuite(TestLocalTypeSecurity.class),
			TLSecurityDeviceManager.Module.INSTANCE, PersonManager.Module.INSTANCE);
		return ElementWebTestSetup.createElementWebTestSetup(suite);
	}

}
