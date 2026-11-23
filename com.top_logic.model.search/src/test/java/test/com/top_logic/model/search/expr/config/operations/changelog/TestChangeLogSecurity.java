/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.config.operations.changelog;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import junit.framework.Test;

import org.junit.Assume;

import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.changelog.ChangeLogBuilder;
import com.top_logic.element.changelog.model.Change;
import com.top_logic.element.changelog.model.ChangeSet;
import com.top_logic.element.changelog.model.Modification;
import com.top_logic.element.changelog.model.Update;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.DiscoverRight;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.model.ModelService;

/**
 * Test for the security of the change log: the function {@code changeSetRestrict()}.
 *
 * <p>
 * The test uses the {@code Named} types of the model {@code TestTLScriptSecurity} (see
 * <code>TestTLScriptSecurity-test.config.xml</code>): the person in {@code accountA} of an object
 * holds the role {@code NameReader}, which reads the object, but not the attribute {@code name} of
 * a {@code Foo}. A user may discover the own account only.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestChangeLogSecurity extends AbstractSearchExpressionTest {

	private static final String MODULE = "TestTLScriptSecurity";

	private static final String NAME = "name";

	private static final String ACCOUNT_A = "accountA";

	private static final String ACCOUNT_B = "accountB";

	private static final String RELATED = "related";

	private static final String RESTRICT = "cs -> changeSetRestrict($cs)";

	private KnowledgeBase _kb;

	private final Map<ObjectKey, KnowledgeItem> _toDelete = new HashMap<>();

	private final UpdateListener _creationListener = new UpdateListener() {
		@Override
		public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
			_toDelete.putAll(event.getCreatedObjects());
			_toDelete.keySet().removeAll(event.getDeletedObjectKeys());
		}
	};

	/** Reads {@link #_barA}, {@link #_fooA} (except its name) and {@link #_related}. */
	private Person _reader;

	/** Holds the role on {@link #_barB}, which {@link #_reader} can neither read nor discover. */
	private Person _other;

	private TLObject _barA;

	private TLObject _barB;

	private TLObject _fooA;

	private TLObject _related;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_kb = PersistencyLayer.getKnowledgeBase();
		_kb.addUpdateListener(_creationListener);

		try (Transaction tx = beginTx()) {
			_reader = TestPerson.createPerson("changeLogSecReader");
			_other = TestPerson.createPerson("changeLogSecOther");
			_barA = newNamed("Bar", "barA", _reader);
			_barB = newNamed("Bar", "barB", _other);
			_fooA = newNamed("Foo", "fooA", _reader);
			_related = newNamed("DiscoverNamed", "related", _reader);
			tx.commit();
		}
	}

	private TLObject newNamed(String className, String name, Person accountA) {
		TLObject result = DynamicModelService.getFactoryFor(MODULE).createObject(type(className));
		result.tUpdateByName(NAME, name);
		result.tUpdateByName(ACCOUNT_A, accountA);
		return result;
	}

	@Override
	protected void tearDown() throws Exception {
		_kb.removeUpdateListener(_creationListener);
		try (Transaction tx = beginTx()) {
			KBUtils.deleteAllKI(_toDelete.values().iterator());
			tx.commit();
		}
		_toDelete.clear();
		_kb = null;
		super.tearDown();
	}

	@Override
	protected boolean checksSecurity() {
		return true;
	}

	/**
	 * The restriction of a commit changing readable and unreadable objects keeps the readable
	 * attributes of the readable objects and the discoverable objects in their values only.
	 */
	public void testRestrictMixedCommit() throws Exception {
		ChangeSet commit = commit(ResKey.text("mixed"), () -> {
			_barA.tUpdateByName(NAME, "barA2");
			_barA.tUpdateByName(ACCOUNT_B, _other);
			_fooA.tUpdateByName(NAME, "fooA2");
			_barB.tUpdateByName(NAME, "barB2");
			_related.tUpdateByName(RELATED, List.of(_barA, _barB));
			_related.tUpdateByName(ACCOUNT_B, _reader);
		});
		assertEquals(Set.of(_barA, _barB, _fooA, _related), changedObjects(commit));

		becomeUser(_reader);
		assertTrue("Precondition: the reader discovers the own account.", DiscoverRight.isDiscoverable(_reader));
		assertFalse("Precondition: the reader cannot discover another account.", DiscoverRight.isDiscoverable(_other));

		ChangeSet restricted = restrict(commit);
		assertNotSame(commit, restricted);
		assertTrue(restricted.getIncomplete());
		assertEquals(commit.getRevision(), restricted.getRevision());
		assertEquals(commit.getMessage(), restricted.getMessage());

		// fooA: Its only change is the name, which the reader cannot read. barB: not readable.
		assertEquals(Set.of(_barA, _related), changedObjects(restricted));

		Update barA = update(restricted, _barA);
		assertEquals(Set.of(NAME, ACCOUNT_B), modifiedParts(barA));
		assertEquals(List.of("barA"), List.copyOf(modification(barA, NAME).getOldValue()));
		assertEquals(List.of("barA2"), List.copyOf(modification(barA, NAME).getNewValue()));
		// The other account is not discoverable.
		assertTrue(modification(barA, ACCOUNT_B).getNewValue().isEmpty());

		Update related = update(restricted, _related);
		assertEquals(Set.of(RELATED, ACCOUNT_B), modifiedParts(related));
		// barB is not discoverable.
		assertEquals(Set.of(_barA), current(modification(related, RELATED).getNewValue()));
		assertEquals(Set.of(_reader), current(modification(related, ACCOUNT_B).getNewValue()));

		// The original is not touched.
		assertFalse(commit.getIncomplete());
		assertEquals(Set.of(_barA, _barB), current(modification(update(commit, _related), RELATED).getNewValue()));
	}

	/**
	 * A commit without a visible change is restricted to an empty commit with the revision, date
	 * and message of the original.
	 */
	public void testRestrictInvisibleCommit() throws Exception {
		ChangeSet commit = commit(ResKey.text("hidden"), () -> {
			_barB.tUpdateByName(NAME, "barB2");
		});
		assertEquals(Set.of(_barB), changedObjects(commit));

		becomeUser(_reader);
		ChangeSet restricted = restrict(commit);
		assertTrue(restricted.getChanges().isEmpty());
		assertTrue(restricted.getIncomplete());
		assertEquals(commit.getRevision(), restricted.getRevision());
		assertEquals(commit.getDate(), restricted.getDate());
		assertEquals(commit.getMessage(), restricted.getMessage());
	}

	/**
	 * A commit the user sees completely is restricted to a complete copy.
	 */
	public void testRestrictVisibleCommit() throws Exception {
		becomeUser(_reader);
		ChangeSet commit = commit(ResKey.text("visible"), () -> {
			_barA.tUpdateByName(NAME, "barA2");
			_related.tUpdateByName(RELATED, List.of(_barA));
		});

		ChangeSet restricted = restrict(commit);
		assertFalse(restricted.getIncomplete());
		assertEquals(Set.of(_barA, _related), changedObjects(restricted));
		assertEquals(Set.of(NAME), modifiedParts(update(restricted, _barA)));
		assertEquals(Set.of(_barA), current(modification(update(restricted, _related), RELATED).getNewValue()));
		assertEquals(_reader, WrapperHistoryUtils.getCurrent(restricted.getAuthor()));
	}

	/**
	 * The author of a commit is hidden, if the user cannot discover the author's account. This does
	 * not make the commit incomplete.
	 */
	public void testHiddenAuthor() throws Exception {
		becomeUser(_other);
		ChangeSet commit = commit(ResKey.text("by other"), () -> {
			_barA.tUpdateByName(NAME, "barA2");
		});
		assertEquals(_other, WrapperHistoryUtils.getCurrent(commit.getAuthor()));

		becomeUser(_reader);
		ChangeSet restricted = restrict(commit);
		assertNull(restricted.getAuthor());
		assertFalse(restricted.getIncomplete());
		assertEquals(Set.of(_barA), changedObjects(restricted));
	}

	/**
	 * For an administrator, the restriction keeps everything.
	 */
	public void testRestrictForAdministrator() throws Exception {
		ChangeSet commit = commit(ResKey.text("mixed"), () -> {
			_barA.tUpdateByName(ACCOUNT_B, _other);
			_fooA.tUpdateByName(NAME, "fooA2");
			_barB.tUpdateByName(NAME, "barB2");
		});

		becomeUser(PersonManager.getManager().getRoot());
		ChangeSet restricted = restrict(commit);
		assertFalse(restricted.getIncomplete());
		assertEquals(Set.of(_barA, _barB, _fooA), changedObjects(restricted));
		assertEquals(Set.of(_other), current(modification(update(restricted, _barA), ACCOUNT_B).getNewValue()));
	}

	/**
	 * The user whose rights decide can be given explicitly.
	 */
	public void testRestrictForGivenUser() throws Exception {
		ChangeSet commit = commit(ResKey.text("mixed"), () -> {
			_barA.tUpdateByName(NAME, "barA2");
			_barB.tUpdateByName(NAME, "barB2");
		});

		becomeUser(PersonManager.getManager().getRoot());
		ChangeSet restricted =
			(ChangeSet) execute(search("cs -> user -> changeSetRestrict($cs, $user)"), commit, _reader);
		assertTrue(restricted.getIncomplete());
		assertEquals(Set.of(_barA), changedObjects(restricted));
	}

	/**
	 * Restricts the given commit with {@link #RESTRICT} for the current user.
	 */
	private static ChangeSet restrict(ChangeSet commit) throws Exception {
		// Executed once: two evaluations deliver two different copies.
		return (ChangeSet) execute(search(RESTRICT), commit);
	}

	/**
	 * Commits the changes of the given action and reads the commit from the change log.
	 */
	private ChangeSet commit(ResKey message, Runnable action) {
		Assume.assumeTrue("The change log needs a versioned knowledge base.", hasHistory(_barA));
		try (Transaction tx = _kb.beginTransaction(message)) {
			action.run();
			tx.commit();
		}
		long revision = _kb.getHistoryManager().getLastRevision();

		Collection<ChangeSet> log =
			new ChangeLogBuilder(_kb, ModelService.getApplicationModel()).setNumberEntries(1).build();
		assertEquals(1, log.size());
		ChangeSet result = log.iterator().next();
		assertEquals(revision, result.getRevision().getCommitNumber());
		return result;
	}

	private static Set<TLObject> changedObjects(ChangeSet changeSet) {
		return changeSet.getChanges().stream()
			.map(Change::getObject)
			.map(WrapperHistoryUtils::getCurrent)
			.collect(Collectors.toSet());
	}

	private static Update update(ChangeSet changeSet, TLObject object) {
		for (Change change : changeSet.getChanges()) {
			if (WrapperHistoryUtils.getCurrent(change.getObject()) == object) {
				return (Update) change;
			}
		}
		fail("No change of " + object);
		return null;
	}

	private static Set<String> modifiedParts(Update update) {
		return update.getModifications().stream()
			.map(m -> m.getPart().getName())
			.collect(Collectors.toSet());
	}

	private static Modification modification(Update update, String partName) {
		for (Modification modification : update.getModifications()) {
			if (modification.getPart().getName().equals(partName)) {
				return modification;
			}
		}
		fail("No modification of " + partName);
		return null;
	}

	private static Set<Object> current(Collection<Object> values) {
		return values.stream()
			.map(v -> v instanceof TLObject obj ? WrapperHistoryUtils.getCurrent(obj) : v)
			.collect(Collectors.toSet());
	}

	private static TLClass type(String className) {
		return (TLClass) TLModelUtil.findType(MODULE + ":" + className);
	}

	private Transaction beginTx() {
		return _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);
	}

	public static Test suite() {
		return suite(TestChangeLogSecurity.class,
			AccessManager.Module.INSTANCE,
			TLSecurityDeviceManager.Module.INSTANCE,
			InitialRolesManager.Module.INSTANCE,
			SecurityConfigurationService.Module.INSTANCE);
	}

}
