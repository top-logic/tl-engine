/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * Test of the access decision for objects whose access parent is restricted to some operations,
 * against the model module {@code TestAccessParentOperations} of the element test application.
 *
 * <p>
 * An {@code Entry} delegates Read to its {@code Catalog} and decides every other operation by the
 * grants of its type and the roles held on the entry itself. A {@code Part} of a catalog delegates
 * Read and Write to the catalog, a {@code FullPart} delegates every operation.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestAccessParentOperations extends BasicTestCase {

	private static final String MODULE = "TestAccessParentOperations";

	private static final String CATALOG = MODULE + ":Catalog";

	private static final String ENTRY = MODULE + ":Entry";

	private static final String ENTRY_SUB = MODULE + ":EntrySub";

	private static final String PART = MODULE + ":Part";

	private static final String FULL_PART = MODULE + ":FullPart";

	private static final String ROLE_CATALOG_READER = MODULE + ".CatalogReader";

	private static final String ROLE_ENTRY_EDITOR = MODULE + ".EntryEditor";

	private Person _reader;

	private Person _editor;

	private Person _other;

	private TLObject _catalog;

	private TLObject _entry;

	private TLObject _entrySub;

	private TLObject _part;

	private TLObject _fullPart;

	private final List<TLObject> _created = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		inTransaction(() -> {
			_reader = createPerson("accessParentOperationsReader");
			_editor = createPerson("accessParentOperationsEditor");
			_other = createPerson("accessParentOperationsOther");

			_catalog = create(CATALOG);
			_catalog.tUpdateByName("reader", _reader);
		});
		inTransaction(() -> {
			_entry = createEntry(ENTRY);
			_entrySub = createEntry(ENTRY_SUB);

			_part = create(PART);
			_catalog.tUpdateByName("parts", List.of(_part));

			_fullPart = create(FULL_PART);
			_fullPart.tUpdateByName("editor", _editor);
			_catalog.tUpdateByName("fullParts", List.of(_fullPart));
		});
	}

	@Override
	protected void tearDown() throws Exception {
		inTransaction(() -> {
			Collections.reverse(_created);
			for (TLObject object : _created) {
				if (object.tValid()) {
					object.tDelete();
				}
			}
			_reader.tDelete();
			_editor.tDelete();
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

	public void testReadDelegatedWriteOwn() {
		assertReadDelegatedWriteOwn(_entry);
	}

	public void testOwnWriteImpliesDiscover() {
		assertOwnWriteImpliesDiscover(_entry);
	}

	public void testCreateDeleteOnlyWhenListed() {
		assertTrue("Read is delegated to the catalog.", allowed(_reader, _part, SimpleBoundCommandGroup.READ));
		assertTrue("Write is delegated to the catalog.", allowed(_reader, _part, SimpleBoundCommandGroup.WRITE));
		assertFalse("Delete is not delegated, and the part grants it to nobody.",
			allowed(_reader, _part, SimpleBoundCommandGroup.DELETE));

		TLStructuredTypePart parts = type(CATALOG).getPart("parts");
		assertFalse("Create is not delegated, and the part grants it to nobody.",
			rights().isAllowedCreate(_reader, _catalog, parts, type(PART)));
	}

	public void testInheritedGrantOfUndelegatedOperation() {
		assertTrue("The Export grant inherited from EntryBase is kept, since Export is not delegated.",
			allowed(_editor, _entry, SimpleBoundCommandGroup.EXPORT));
	}

	public void testFullDelegationIgnoresInheritedGrants() {
		assertFalse(allowed(_editor, _fullPart, SimpleBoundCommandGroup.READ));
		assertFalse(allowed(_editor, _fullPart, SimpleBoundCommandGroup.WRITE));
		assertFalse("The Export grant inherited from EntryBase is ignored, since every operation is delegated.",
			allowed(_editor, _fullPart, SimpleBoundCommandGroup.EXPORT));

		assertTrue(allowed(_reader, _fullPart, SimpleBoundCommandGroup.READ));
		assertTrue(allowed(_reader, _fullPart, SimpleBoundCommandGroup.WRITE));
		assertTrue("Deleting a part is writing its container.",
			allowed(_reader, _fullPart, SimpleBoundCommandGroup.DELETE));
	}

	public void testSpecializationInheritsRestriction() {
		assertReadDelegatedWriteOwn(_entrySub);
		assertOwnWriteImpliesDiscover(_entrySub);
	}

	public void testAttributeGrantsPerOperation() {
		TLStructuredTypePart name = type(ENTRY).getPart("name");

		assertTrue("Reading the name is checked by the roles held on the catalog.",
			rights().isAllowed(_reader, _entry, name, SimpleBoundCommandGroup.READ));
		assertTrue("Writing the name is checked by the roles held on the entry.",
			rights().isAllowed(_editor, _entry, name, SimpleBoundCommandGroup.WRITE));
		assertFalse("The reader holds no role on the entry.",
			rights().isAllowed(_reader, _entry, name, SimpleBoundCommandGroup.WRITE));
		assertFalse("The editor holds no role on the catalog.",
			rights().isAllowed(_editor, _entry, name, SimpleBoundCommandGroup.READ));
	}

	public void testRoleHolderPerOperation() {
		assertSame(_catalog, rights().getRoleHolder(_entry, SimpleBoundCommandGroup.READ));
		assertSame(_catalog, rights().getRoleHolder(_entry, SimpleBoundCommandGroup.DISCOVER));
		assertSame(_entry, rights().getRoleHolder(_entry, SimpleBoundCommandGroup.WRITE));
		assertSame(_entry, rights().getRoleHolder(_entry, SimpleBoundCommandGroup.EXPORT));
		assertSame(SimpleBoundCommandGroup.WRITE,
			rights().getOperationOnRoleHolder(_entry, SimpleBoundCommandGroup.WRITE));

		assertSame(_catalog, rights().getRoleHolder(_part, SimpleBoundCommandGroup.WRITE));
		assertSame("Delete is not delegated by the part.", _part,
			rights().getRoleHolder(_part, SimpleBoundCommandGroup.DELETE));
		assertSame("Delete is checked on the part itself.", SimpleBoundCommandGroup.DELETE,
			rights().getOperationOnRoleHolder(_part, SimpleBoundCommandGroup.DELETE));

		assertSame(_catalog, rights().getRoleHolder(_fullPart, SimpleBoundCommandGroup.DELETE));
		assertSame("Deleting a fully delegating part is writing its container.", SimpleBoundCommandGroup.WRITE,
			rights().getOperationOnRoleHolder(_fullPart, SimpleBoundCommandGroup.DELETE));
	}

	public void testAdmittingRolesPerOperation() {
		BoundedRole catalogReader = BoundedRole.getRoleByName(ROLE_CATALOG_READER);
		BoundedRole entryEditor = BoundedRole.getRoleByName(ROLE_ENTRY_EDITOR);

		assertEquals(Set.of(catalogReader), rights().getAdmittingRoles(_entry, SimpleBoundCommandGroup.READ));
		assertEquals(Set.of(entryEditor), rights().getAdmittingRoles(_entry, SimpleBoundCommandGroup.WRITE));
		assertEquals(Set.of(catalogReader), rights().getAdmittingRoles(_part, SimpleBoundCommandGroup.WRITE));
		assertEquals(Set.of(), rights().getAdmittingRoles(_part, SimpleBoundCommandGroup.DELETE));

		Map<TLObject, Set<BoundedRole>> discover =
			rights().getAdmittingRolesByHolder(_entry, SimpleBoundCommandGroup.DISCOVER);
		assertEquals("Discover is delegated to the catalog.", Set.of(catalogReader), discover.get(_catalog));
		assertTrue("Discover is implied by the own Write grant of the entry.",
			discover.get(_entry).contains(entryEditor));
		assertEquals(Map.of(_catalog, Set.of(catalogReader)),
			rights().getAdmittingRolesByHolder(_entry, SimpleBoundCommandGroup.READ));
		assertEquals(Map.of(_entry, Set.of(entryEditor)),
			rights().getAdmittingRolesByHolder(_entry, SimpleBoundCommandGroup.WRITE));
		assertEquals("Every operation is decided by the catalog.", Map.of(_catalog, Set.of(catalogReader)),
			rights().getAdmittingRolesByHolder(_fullPart, SimpleBoundCommandGroup.DELETE));
	}

	public void testInitialValuesWithOwnWrite() {
		TLStructuredTypePart name = type(ENTRY).getPart("name");
		TLObject draft = TransientObjectFactory.INSTANCE.createObject(type(ENTRY));
		draft.tUpdateByName("catalog", _catalog);

		assertTrue("Writing the name is decided by the roles the entry will hold.",
			rights().isAllowedInitial(_editor, draft, name, SimpleBoundCommandGroup.WRITE));
		assertTrue("Reading the name is decided by the roles held on the catalog.",
			rights().isAllowedInitial(_reader, draft, name, SimpleBoundCommandGroup.READ));
		assertFalse("The editor holds no role on the catalog.",
			rights().isAllowedInitial(_editor, draft, name, SimpleBoundCommandGroup.READ));
	}

	private void assertReadDelegatedWriteOwn(TLObject entry) {
		assertTrue("Read is delegated to the catalog.", allowed(_reader, entry, SimpleBoundCommandGroup.READ));
		assertTrue("Discover is delegated with Read.", allowed(_reader, entry, SimpleBoundCommandGroup.DISCOVER));
		assertFalse("Write is decided by the entry.", allowed(_reader, entry, SimpleBoundCommandGroup.WRITE));
		assertFalse("Delete is decided by the entry.", allowed(_reader, entry, SimpleBoundCommandGroup.DELETE));
		assertFalse("Export is decided by the entry.", allowed(_reader, entry, SimpleBoundCommandGroup.EXPORT));

		assertTrue("Write is granted to the editor of the entry.",
			allowed(_editor, entry, SimpleBoundCommandGroup.WRITE));

		for (BoundCommandGroup operation : List.of(SimpleBoundCommandGroup.READ, SimpleBoundCommandGroup.WRITE,
			SimpleBoundCommandGroup.CREATE, SimpleBoundCommandGroup.DELETE, SimpleBoundCommandGroup.DISCOVER,
			SimpleBoundCommandGroup.EXPORT)) {
			assertFalse(operation.getID(), allowed(_other, entry, operation));
		}
	}

	private void assertOwnWriteImpliesDiscover(TLObject entry) {
		assertTrue("The own Write grant implies Discover.", allowed(_editor, entry, SimpleBoundCommandGroup.DISCOVER));
		assertFalse("Read is delegated to the catalog, the inherited Read grant is dropped.",
			allowed(_editor, entry, SimpleBoundCommandGroup.READ));
	}

	private TLObject createEntry(String typeName) {
		TLObject result = create(typeName);
		result.tUpdateByName("catalog", _catalog);
		result.tUpdateByName("editor", _editor);
		return result;
	}

	private static boolean allowed(Person person, TLObject object, BoundCommandGroup operation) {
		return rights().isAllowed(person, object, operation);
	}

	private static ModelAccessRights rights() {
		return ModelAccessRights.getInstance();
	}

	private TLObject create(String typeName) {
		TLObject result = ModelService.getInstance().getFactory().createObject(type(typeName));
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
		Test suite = ServiceTestSetup.createSetup(new TestSuite(TestAccessParentOperations.class),
			TLSecurityDeviceManager.Module.INSTANCE, PersonManager.Module.INSTANCE);
		return ElementWebTestSetup.createElementWebTestSetup(suite);
	}

}
