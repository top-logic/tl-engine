/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.changelog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyTemplate;
import com.top_logic.element.changelog.ChangeLogBuilder;
import com.top_logic.element.changelog.ChangeSetReverter;
import com.top_logic.element.changelog.I18NConstants;
import com.top_logic.element.changelog.SubtreeFilter;
import com.top_logic.element.changelog.model.Change;
import com.top_logic.element.changelog.model.ChangeSet;
import com.top_logic.element.changelog.model.Deletion;
import com.top_logic.element.changelog.model.trans.TransientChangeSet;
import com.top_logic.element.changelog.model.trans.TransientDeletion;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Test of {@link ChangeSetReverter#checkWritable(ChangeSet, Person)} against the model module
 * {@code TestRevertWriteCheck} of the element test application.
 *
 * <p>
 * A {@code Folder} admits its readers to read it and its creators to create items in it. An
 * {@code Item} admits its readers to read it, its editors to write it, its deleters to delete it and
 * its creators to create parts in it. The restricted user may read everything but holds no other
 * role, the privileged user holds every role.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestChangeSetReverterRights extends BasicTestCase {

	private static final String MODULE = "TestRevertWriteCheck";

	private static final String FOLDER = MODULE + ":Folder";

	private static final String ITEM = MODULE + ":Item";

	private static final String NAME = "name";

	private static final String READERS = "readers";

	private static final String EDITORS = "editors";

	private static final String DELETERS = "deleters";

	private static final String CREATORS = "creators";

	private static final String ITEMS = "items";

	private static final String PARTS = "parts";

	private Person _restricted;

	private Person _privileged;

	private TLObject _folder;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		commit(() -> {
			_restricted = createPerson("revertWriteCheckRestricted");
			_privileged = createPerson("revertWriteCheckPrivileged");

			_folder = ModelService.getInstance().getFactory().createObject(type(FOLDER));
			_folder.tUpdateByName(NAME, "folder");
			_folder.tUpdateByName(READERS, List.of(_restricted, _privileged));
			_folder.tUpdateByName(CREATORS, List.of(_privileged));
		});
	}

	@Override
	protected void tearDown() throws Exception {
		commit(() -> {
			List<TLObject> instances = new ArrayList<>();
			instances.addAll(MetaElementUtil.getAllDirectInstancesOf(type(ITEM), TLObject.class));
			instances.addAll(MetaElementUtil.getAllDirectInstancesOf(type(FOLDER), TLObject.class));
			for (TLObject object : instances) {
				if (object.tValid()) {
					object.tDelete();
				}
			}
			_restricted.tDelete();
			_privileged.tDelete();
		});
		super.tearDown();
	}

	/**
	 * Runs the test body with checked security.
	 *
	 * <p>
	 * The fixture is created in the system context of the test. The body decides access for
	 * explicitly given persons, which is only checked outside a system context.
	 * </p>
	 */
	@Override
	protected void runTest() throws Throwable {
		TLContextManager.<Void, Throwable, RuntimeException> inPersonContext(null, () -> {
			super.runTest();
			return null;
		});
	}

	public void testRevertUpdate() {
		TLObject item = inTransaction(() -> createItem(_folder, ITEMS, "item"));
		Revision rename = commit(() -> item.tUpdateByName(NAME, "item-2"));
		ChangeSet change = changeAt(rename);

		assertRefused(I18NConstants.ERROR_REVERT_NO_WRITE_ACCESS__OBJECT_ATTRIBUTE, _restricted, change);
		assertEquals("item-2", item.tValueByName(NAME));

		revertAs(_privileged, change);
		assertEquals("item", item.tValueByName(NAME));
	}

	public void testRevertCreation() {
		Revision creation = commit(() -> createItem(_folder, ITEMS, "item"));
		TLObject item = single(ITEMS);
		ChangeSet change = changeAt(creation);

		assertRefused(I18NConstants.ERROR_REVERT_NO_DELETE_ACCESS__OBJECT, _restricted, change);
		assertTrue(item.tValid());

		revertAs(_privileged, change);
		assertFalse(item.tValid());
		assertTrue(items(_folder, ITEMS).isEmpty());
	}

	public void testRevertDeletion() {
		TLObject item = inTransaction(() -> createItem(_folder, ITEMS, "item"));
		Revision deletion = commit(() -> item.tDelete());
		ChangeSet change = changeAt(deletion);

		assertRefused(I18NConstants.ERROR_REVERT_NO_CREATE_ACCESS__OBJECT_TYPE, _restricted, change);
		assertTrue(items(_folder, ITEMS).isEmpty());

		revertAs(_privileged, change);
		assertEquals("item", single(ITEMS).tValueByName(NAME));
	}

	/**
	 * A change set with an allowed and a forbidden change is refused completely.
	 */
	public void testRevertMixedRefusedCompletely() {
		TLObject own = inTransaction(() -> {
			TLObject result = createItem(_folder, ITEMS, "own");
			result.tUpdateByName(EDITORS, List.of(_restricted, _privileged));
			return result;
		});
		TLObject other = inTransaction(() -> createItem(_folder, ITEMS, "other"));
		Revision rename = commit(() -> {
			own.tUpdateByName(NAME, "own-2");
			other.tUpdateByName(NAME, "other-2");
		});
		ChangeSet change = changeAt(rename);

		assertRefused(I18NConstants.ERROR_REVERT_NO_WRITE_ACCESS__OBJECT_ATTRIBUTE, _restricted, change);
		assertEquals("own-2", own.tValueByName(NAME));
		assertEquals("other-2", other.tValueByName(NAME));
	}

	/**
	 * The parts of a container re-created by the same revert follow their container: only the
	 * right to create the container is checked.
	 */
	public void testRevertDeletionOfContainerWithPart() {
		Person containerCreator = inTransaction(() -> createPerson("revertWriteCheckContainerCreator"));
		try {
			commit(() -> {
				_folder.tUpdateByName(READERS, List.of(_restricted, _privileged, containerCreator));
				_folder.tUpdateByName(CREATORS, List.of(_privileged, containerCreator));
			});
			TLObject item = inTransaction(() -> {
				TLObject result = createItem(_folder, ITEMS, "item");
				createItem(result, PARTS, "part");
				return result;
			});
			Revision deletion = commit(() -> item.tDelete());
			ChangeSet change = changeAt(deletion);
			assertEquals("Deletion of the item and its part expected.", 2, deletions(change).size());

			assertRefused(I18NConstants.ERROR_REVERT_NO_CREATE_ACCESS__OBJECT_TYPE, _restricted, change);
			assertTrue(items(_folder, ITEMS).isEmpty());

			// May create items in the folder, but holds no role on the item to create parts in it.
			revertAs(containerCreator, change);
			TLObject restored = single(ITEMS);
			assertEquals("item", restored.tValueByName(NAME));
			assertEquals(1, items(restored, PARTS).size());
		} finally {
			commit(() -> containerCreator.tDelete());
		}
	}

	/**
	 * A change set lacking the deletion of the container checks the deletion of the part.
	 */
	public void testRevertDeletionOfPartWithoutContainer() {
		TLObject item = inTransaction(() -> {
			TLObject result = createItem(_folder, ITEMS, "item");
			createItem(result, PARTS, "part");
			return result;
		});
		Revision deletion = commit(() -> item.tDelete());
		ChangeSet change = changeAt(deletion);

		TransientChangeSet partOnly = new TransientChangeSet();
		partOnly.setRevision(change.getRevision());
		partOnly.setParentRev(change.getParentRev());
		for (Deletion entry : deletions(change)) {
			TLObject deleted = entry.getObject();
			if (WrapperHistoryUtils.getUnversionedIdentity(deleted.tContainer())
				.equals(WrapperHistoryUtils.getUnversionedIdentity(item))) {
				TransientDeletion copy = new TransientDeletion();
				copy.setObject(deleted);
				copy.setImplicit(entry.getImplicit());
				partOnly.addChange(copy);
			}
		}
		assertEquals("Deletion of the part expected.", 1, partOnly.getChanges().size());

		assertRefused(I18NConstants.ERROR_REVERT_NO_CREATE_ACCESS__OBJECT_TYPE, _privileged, partOnly);
	}

	private void assertRefused(ResKeyTemplate expected, Person user, ChangeSet change) {
		Revision before = currentRevision();
		try {
			revertAs(user, change);
			fail("Revert must be refused for " + user.getName() + ".");
		} catch (TopLogicException ex) {
			assertEquals(expected.getKey(), ex.getErrorKey().getKey());
		}
		assertEquals("Nothing must be committed.", before.getCommitNumber(), currentRevision().getCommitNumber());
	}

	private void revertAs(Person user, ChangeSet change) {
		ChangeSetReverter.checkWritable(change, user);
		List<ResKey> problems =
			ThreadContext.withoutSecurityChecks(() -> ChangeSetReverter.revert(kb(), change, false));
		assertEquals(List.of(), problems);
	}

	private static List<Deletion> deletions(ChangeSet change) {
		List<Deletion> result = new ArrayList<>();
		for (Change entry : change.getChanges()) {
			if (entry instanceof Deletion deletion) {
				result.add(deletion);
			}
		}
		return result;
	}

	private TLObject single(String reference) {
		Collection<?> items = items(_folder, reference);
		assertEquals(1, items.size());
		return (TLObject) items.iterator().next();
	}

	private static Collection<?> items(TLObject container, String reference) {
		return (Collection<?>) container.tValueByName(reference);
	}

	private ChangeSet changeAt(Revision revision) {
		ChangeLogBuilder builder = new ChangeLogBuilder(kb(), ModelService.getApplicationModel());
		builder.setFilter(new SubtreeFilter(_folder, true));
		for (ChangeSet cs : builder.build()) {
			if (cs.getRevision().getCommitNumber() == revision.getCommitNumber()) {
				return cs;
			}
		}
		fail("No change set for revision " + revision + ".");
		return null;
	}

	private TLObject createItem(TLObject container, String reference, String name) {
		TLObject result = ModelService.getInstance().getFactory().createObject(type(ITEM), container);
		result.tUpdateByName(NAME, name);
		result.tUpdateByName(READERS, List.of(_restricted, _privileged));
		result.tUpdateByName(EDITORS, List.of(_privileged));
		result.tUpdateByName(DELETERS, List.of(_privileged));
		result.tUpdateByName(CREATORS, List.of(_privileged));
		List<Object> contents = new ArrayList<>(items(container, reference));
		contents.add(result);
		container.tUpdateByName(reference, contents);
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

	private static Revision currentRevision() {
		return kb().getHistoryManager().getRevision(kb().getHistoryManager().getLastRevision());
	}

	/**
	 * Runs the given modification in a transaction of its own and returns its commit revision.
	 */
	private static Revision commit(Runnable modification) {
		return ThreadContext.withoutSecurityChecks(() -> {
			try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
				modification.run();
				tx.commit();
				return tx.getCommitRevision();
			}
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
		Test suite = ServiceTestSetup.createSetup(new TestSuite(TestChangeSetReverterRights.class),
			TLSecurityDeviceManager.Module.INSTANCE, PersonManager.Module.INSTANCE);
		return ElementWebTestSetup.createElementWebTestSetup(suite);
	}

}
