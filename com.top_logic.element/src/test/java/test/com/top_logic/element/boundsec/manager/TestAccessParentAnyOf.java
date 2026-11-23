/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
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
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * Test of the access decision for objects whose access parent has several candidates, against the
 * model module {@code TestAccessParentAnyOf} of the element test application.
 *
 * <p>
 * A {@code Contact} delegates Read to every document and task referring to it. A {@code Part}
 * delegates every operation to its folder and Read to every document relating it. A
 * {@code Mixed} delegates only Write to its folder and Read to every document relating it.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestAccessParentAnyOf extends BasicTestCase {

	private static final String MODULE = "TestAccessParentAnyOf";

	private static final String FOLDER = MODULE + ":Folder";

	private static final String DOCUMENT = MODULE + ":Document";

	private static final String TASK = MODULE + ":Task";

	private static final String CONTACT = MODULE + ":Contact";

	private static final String PART = MODULE + ":Part";

	private static final String MIXED = MODULE + ":Mixed";

	private static final String SINGLE = MODULE + ":Single";

	private static final String DIRECT = MODULE + ":Direct";

	private static final String NODE = MODULE + ":Node";

	private static final String HOLDER = MODULE + ":Holder";

	private static final String BOTH = MODULE + ":Both";

	private Person _folderOwner;

	private Person _folderReader;

	private Person _otherOwner;

	private Person _docReader;

	private Person _docEditor;

	private Person _taskOwner;

	private Person _other;

	/** Member of {@link #_rootOwners}. */
	private Person _rootOwner;

	/** Members hold the folder owner role on the security root. */
	private Group _rootOwners;

	/** Owned by {@link #_folderOwner}, read by {@link #_folderReader}. */
	private TLObject _folder;

	/** Owned by {@link #_folderOwner}. */
	private TLObject _folder2;

	/** Owned by {@link #_otherOwner}. */
	private TLObject _otherFolder;

	/** Read by {@link #_docReader}, edited by {@link #_docEditor}. */
	private TLObject _document;

	/** Read by nobody. */
	private TLObject _hiddenDocument;

	/** Owned by {@link #_taskOwner}. */
	private TLObject _task;

	/** Author of {@link #_document}, assignee of {@link #_task}. */
	private TLObject _contact;

	/** Author of {@link #_hiddenDocument} only. */
	private TLObject _hiddenContact;

	/** Part of {@link #_folder}, related by {@link #_document}. */
	private TLObject _part;

	/** Part of {@link #_folder}, related by {@link #_document}. */
	private TLObject _mixed;

	private TLObject _single;

	private TLObject _direct;

	private final List<TLObject> _created = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		inTransaction(() -> {
			_folderOwner = createPerson("anyOfFolderOwner");
			_folderReader = createPerson("anyOfFolderReader");
			_otherOwner = createPerson("anyOfOtherOwner");
			_docReader = createPerson("anyOfDocReader");
			_docEditor = createPerson("anyOfDocEditor");
			_taskOwner = createPerson("anyOfTaskOwner");
			_other = createPerson("anyOfOther");
			_rootOwner = createPerson("anyOfRootOwner");

			_rootOwners = Group.createGroup("TestAccessParentAnyOfRootOwners");
			_rootOwners.addMember(_rootOwner);

			_folder = createFolder(_folderOwner);
			_folder.tUpdateByName("reader", _folderReader);
			_folder2 = createFolder(_folderOwner);
			_otherFolder = createFolder(_otherOwner);
		});
		inTransaction(() -> {
			_contact = create(CONTACT);
			_hiddenContact = create(CONTACT);

			_part = create(PART);
			_folder.tUpdateByName("parts", List.of(_part));
			_mixed = create(MIXED);
			_folder.tUpdateByName("mixed", List.of(_mixed));
			_single = create(SINGLE);
			_folder.tUpdateByName("singles", List.of(_single));
			_direct = create(DIRECT);
			_folder.tUpdateByName("directs", List.of(_direct));

			_document = create(DOCUMENT);
			_document.tUpdateByName("reader", _docReader);
			_document.tUpdateByName("editor", _docEditor);
			_document.tUpdateByName("author", _contact);
			_document.tUpdateByName("relatedParts", List.of(_part));
			_document.tUpdateByName("relatedMixed", List.of(_mixed));

			_hiddenDocument = create(DOCUMENT);
			_hiddenDocument.tUpdateByName("author", _hiddenContact);

			_task = create(TASK);
			_task.tUpdateByName("owner", _taskOwner);
			_task.tUpdateByName("assignee", _contact);
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
			_rootOwners.tDelete();
			for (Person person : persons()) {
				person.tDelete();
			}
			_otherOwner.tDelete();
			_rootOwner.tDelete();
		});
		_created.clear();

		super.tearDown();
	}

	/**
	 * Runs the test body with checked security, see {@link TestAccessParentOperations#runTest()}.
	 */
	@Override
	protected void runTest() throws Throwable {
		TLContextManager.<Void, Throwable, RuntimeException> inPersonContext(null, () -> {
			super.runTest();
			return null;
		});
	}

	public void testContactReadableViaAnyReferer() {
		assertTrue("The reader of the document the contact is the author of reads it.",
			allowed(_docReader, _contact, SimpleBoundCommandGroup.READ));
		assertTrue("The owner of the task the contact is assigned to reads it.",
			allowed(_taskOwner, _contact, SimpleBoundCommandGroup.READ));
		assertTrue("Discover is delegated together with Read.",
			allowed(_taskOwner, _contact, SimpleBoundCommandGroup.DISCOVER));
		assertFalse(allowed(_other, _contact, SimpleBoundCommandGroup.READ));
		assertFalse(allowed(_folderOwner, _contact, SimpleBoundCommandGroup.READ));
		for (Person person : persons()) {
			assertFalse("Write is not delegated, and the contact grants it to nobody: " + person.getName(),
				allowed(person, _contact, SimpleBoundCommandGroup.WRITE));
		}
	}

	public void testPartKeepsFolderRightsAndGainsReaders() {
		assertTrue(allowed(_folderOwner, _part, SimpleBoundCommandGroup.READ));
		assertTrue(allowed(_folderOwner, _part, SimpleBoundCommandGroup.WRITE));
		assertTrue("Deleting a part is writing its folder.", allowed(_folderOwner, _part, SimpleBoundCommandGroup.DELETE));
		assertTrue("The folder reader reads the part through the folder.",
			allowed(_folderReader, _part, SimpleBoundCommandGroup.READ));
		assertFalse(allowed(_folderReader, _part, SimpleBoundCommandGroup.WRITE));

		assertTrue("The reader of the relating document reads the part.",
			allowed(_docReader, _part, SimpleBoundCommandGroup.READ));
		assertFalse(allowed(_docReader, _part, SimpleBoundCommandGroup.WRITE));
		assertFalse(allowed(_other, _part, SimpleBoundCommandGroup.READ));
	}

	public void testRefererReadDoesNotOpenWrite() {
		assertTrue("The editor may write the document.", allowed(_docEditor, _document, SimpleBoundCommandGroup.WRITE));
		assertTrue(allowed(_docEditor, _part, SimpleBoundCommandGroup.READ));
		assertFalse("Only Read is delegated to the relating document.",
			allowed(_docEditor, _part, SimpleBoundCommandGroup.WRITE));
		assertFalse("Only Read is delegated to the relating document.",
			allowed(_docEditor, _part, SimpleBoundCommandGroup.DELETE));
	}

	public void testUnreadableRefererGrantsNothing() {
		for (Person person : persons()) {
			assertFalse("Nobody may read the only document referring to the contact: " + person.getName(),
				allowed(person, _hiddenContact, SimpleBoundCommandGroup.READ));
		}
	}

	public void testDifferentlyRestrictedCandidates() {
		assertFalse("Read is not delegated to the folder, and no document relating the part is readable.",
			allowed(_folderReader, _mixed, SimpleBoundCommandGroup.READ));
		assertTrue("Write is delegated to the folder.", allowed(_folderOwner, _mixed, SimpleBoundCommandGroup.WRITE));
		assertFalse("Write is delegated to the folder only.",
			allowed(_docEditor, _mixed, SimpleBoundCommandGroup.WRITE));
		assertTrue("Read is delegated to the relating document.",
			allowed(_docReader, _mixed, SimpleBoundCommandGroup.READ));

		assertEquals(List.of(_document), holders(_mixed, SimpleBoundCommandGroup.READ));
		assertEquals(List.of(_folder), holders(_mixed, SimpleBoundCommandGroup.WRITE));
	}

	public void testSingleCandidateAnyOfEqualsDirect() {
		for (BoundCommandGroup operation : rights().getObjectOperations(_direct).getOperations()) {
			for (Person person : persons()) {
				assertEquals(operation.getID() + " for " + person.getName(),
					allowed(person, _direct, operation), allowed(person, _single, operation));
			}
			assertEquals(operation.getID(), holders(_direct, operation), holders(_single, operation));
			assertEquals(operation.getID(), rights().getOperationOnRoleHolder(_direct, operation),
				rights().getOperationOnRoleHolder(_single, operation));
		}
		for (Person person : persons()) {
			assertEquals(person.getName(),
				rights().isAllowedCreate(person, _folder, type(FOLDER).getPart("directs"), type(DIRECT)),
				rights().isAllowedCreate(person, _folder, type(FOLDER).getPart("singles"), type(SINGLE)));
			assertEquals(person.getName(),
				rights().isAllowedCreate(person, type(DIRECT), _folder),
				rights().isAllowedCreate(person, type(SINGLE), _folder));
		}
		assertTrue(rights().isAllowedCreate(_folderOwner, type(SINGLE), _folder));
	}

	public void testMultiValuedTarget() {
		TLObject holder = inTransaction(() -> {
			TLObject result = create(HOLDER);
			result.tUpdateByName("owners", List.of(_otherFolder, _folder));
			return result;
		});
		assertTrue("The owner of the second folder reads the holder.",
			allowed(_folderOwner, holder, SimpleBoundCommandGroup.READ));
		assertTrue("The owner of the first folder reads the holder.",
			allowed(_otherOwner, holder, SimpleBoundCommandGroup.READ));
		assertFalse(allowed(_other, holder, SimpleBoundCommandGroup.READ));
		assertEquals("The reference is unordered.", Set.of(_otherFolder, _folder),
			rights().getRoleHolders(holder, SimpleBoundCommandGroup.READ));
	}

	public void testCycleWithAlternativeIsNotCachedAsDenied() {
		assertNotNull("The decisions are cached per interaction.", ThreadContextManager.getInteraction());
		List<TLObject> nodes = inTransaction(() -> {
			TLObject n1 = create(NODE);
			TLObject n2 = create(NODE);
			n1.tUpdateByName("next", n2);
			n2.tUpdateByName("next", n1);
			n1.tUpdateByName("alt", _folder);
			return List.of(n1, n2);
		});
		TLObject n1 = nodes.get(0);
		TLObject n2 = nodes.get(1);
		assertTrue("The first node delegates to the folder as well.",
			allowed(_folderOwner, n1, SimpleBoundCommandGroup.READ));
		assertTrue("The second node delegates to the first one, which the folder owner may read.",
			allowed(_folderOwner, n2, SimpleBoundCommandGroup.READ));
		assertFalse(allowed(_other, n1, SimpleBoundCommandGroup.READ));
		assertFalse(allowed(_other, n2, SimpleBoundCommandGroup.READ));
		assertEquals(List.of(_folder), holders(n2, SimpleBoundCommandGroup.READ));
	}

	public void testCreatePartChecksContainerOnly() {
		TLStructuredTypePart parts = type(FOLDER).getPart("parts");
		assertTrue(rights().isAllowedCreate(_folderOwner, _folder, parts, type(PART)));
		assertFalse("A document relating parts of the folder grants no creation.",
			rights().isAllowedCreate(_docReader, _folder, parts, type(PART)));
		assertFalse(rights().isAllowedCreate(_docEditor, _folder, parts, type(PART)));
		assertFalse("Creation is not delegated to the folder.",
			rights().isAllowedCreate(_folderOwner, type(MIXED), _folder));
	}

	public void testRoleHoldersOfPart() {
		assertEquals(List.of(_folder, _document), holders(_part, SimpleBoundCommandGroup.READ));
		assertEquals(List.of(_folder), holders(_part, SimpleBoundCommandGroup.WRITE));
		assertEquals("The deprecated role holder is the first one deciding Read.", _folder,
			deprecatedRoleHolder(_part));
		assertEquals(List.of(_document, _task), holders(_contact, SimpleBoundCommandGroup.READ));
		assertEquals("Write is decided by the contact itself.", List.of(_contact),
			holders(_contact, SimpleBoundCommandGroup.WRITE));
		assertEquals("Nothing refers to a new contact.", List.of(),
			holders(inTransaction(() -> create(CONTACT)), SimpleBoundCommandGroup.READ));
	}

	public void testAttributeGrantOnAnyHolder() {
		TLStructuredTypePart name = type(PART).getPart("name");
		assertTrue("The reader holds the role on the document relating the part.",
			rights().isAllowed(_docReader, _part, name, SimpleBoundCommandGroup.READ));
		assertFalse("The folder owner holds no role granted reading the name.",
			rights().isAllowed(_folderOwner, _part, name, SimpleBoundCommandGroup.READ));
		assertFalse(rights().isAllowed(_other, _part, name, SimpleBoundCommandGroup.READ));

		assertTrue("Write is decided by the roles held on the folder.",
			rights().isAllowed(_folderOwner, _part, name, SimpleBoundCommandGroup.WRITE));
		assertFalse("Write is not delegated to the relating document.",
			rights().isAllowed(_docEditor, _part, name, SimpleBoundCommandGroup.WRITE));
	}

	public void testInitialValuesOfDraftPart() {
		TLStructuredTypePart name = type(PART).getPart("name");
		TLObject draft = TransientObjectFactory.INSTANCE.createObject(type(PART), _folder);

		assertFalse("Nothing relates a draft, only the folder decides.",
			rights().isAllowedInitial(_docReader, draft, name, SimpleBoundCommandGroup.READ));
		assertFalse(rights().isAllowedInitial(_folderOwner, draft, name, SimpleBoundCommandGroup.READ));
		assertTrue("Write is decided by the roles held on the folder.",
			rights().isAllowedInitial(_folderOwner, draft, name, SimpleBoundCommandGroup.WRITE));
		assertFalse(rights().isAllowedInitial(_docEditor, draft, name, SimpleBoundCommandGroup.WRITE));
	}

	public void testRefererResolvedInRevisionOfObject() {
		TLObject contact = inTransaction(() -> {
			TLObject result = create(CONTACT);
			_document.tUpdateByName("reviewers", List.of(result));
			return result;
		});
		Revision before = kb().getHistoryManager().getRevision(kb().getHistoryManager().getLastRevision());
		inTransaction(() -> _document.tUpdateByName("reviewers", List.of()));

		assertFalse("The contact is no longer reviewed by the document.",
			allowed(_docReader, contact, SimpleBoundCommandGroup.READ));

		TLObject historic = WrapperHistoryUtils.getWrapper(before, contact);
		assertNotSame(contact, historic);
		assertTrue("In its former revision, the contact was reviewed by the document.",
			allowed(_docReader, historic, SimpleBoundCommandGroup.READ));
		assertFalse(allowed(_other, historic, SimpleBoundCommandGroup.READ));
	}

	public void testCustomCombinatorAllOf() {
		TLObject both = inTransaction(() -> {
			TLObject result = create(BOTH);
			result.tUpdateByName("first", _folder);
			result.tUpdateByName("second", _folder2);
			return result;
		});
		TLObject mixedOwners = inTransaction(() -> {
			TLObject result = create(BOTH);
			result.tUpdateByName("first", _folder);
			result.tUpdateByName("second", _otherFolder);
			return result;
		});
		assertTrue("The folder owner owns both folders.", allowed(_folderOwner, both, SimpleBoundCommandGroup.READ));
		assertFalse("The folder owner owns only the first folder.",
			allowed(_folderOwner, mixedOwners, SimpleBoundCommandGroup.READ));
		assertFalse("The other owner owns only the second folder.",
			allowed(_otherOwner, mixedOwners, SimpleBoundCommandGroup.READ));
		assertFalse(allowed(_folderReader, both, SimpleBoundCommandGroup.WRITE));

		Set<TLClass> accessible = rights().getAccessibleTypes(_rootOwner, SimpleBoundCommandGroup.READ);
		assertTrue("The type level is a superset of the access.", accessible.contains(type(BOTH)));
		assertFalse(rights().getAccessibleTypes(_other, SimpleBoundCommandGroup.READ).contains(type(BOTH)));
	}

	private List<Person> persons() {
		return List.of(_folderOwner, _folderReader, _docReader, _docEditor, _taskOwner, _other);
	}

	private static List<TLObject> holders(TLObject object, BoundCommandGroup operation) {
		return new ArrayList<>(rights().getRoleHolders(object, operation));
	}

	@SuppressWarnings("deprecation")
	private static TLObject deprecatedRoleHolder(TLObject object) {
		return rights().getRoleHolder(object);
	}

	private TLObject createFolder(Person owner) {
		TLObject result = create(FOLDER);
		result.tUpdateByName("owner", owner);
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
		inTransaction(() -> {
			modification.run();
			return null;
		});
	}

	private static <T> T inTransaction(Supplier<T> modification) {
		return ThreadContext.withoutSecurityChecks(() -> {
			try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
				T result = modification.get();
				tx.commit();
				return result;
			}
		});
	}

	/** Return the suite of tests to perform. */
	public static Test suite() {
		Test suite = ServiceTestSetup.createSetup(new TestSuite(TestAccessParentAnyOf.class),
			TLSecurityDeviceManager.Module.INSTANCE, PersonManager.Module.INSTANCE);
		return ElementWebTestSetup.createElementWebTestSetup(suite);
	}

}
