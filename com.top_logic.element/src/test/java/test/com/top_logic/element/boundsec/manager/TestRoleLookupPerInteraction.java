/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.LoggingDataSourceProxy;
import com.top_logic.basic.sql.LoggingDataSourceProxy.StatementAnalyzer;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.model.ModelService;

/**
 * Test for {@link BoundedRole#getLocalAndGlobalAndGroupRoles(TLObject, Person)} answering from the
 * role assignments of the person's groups, loaded once per interaction and revision.
 * <p>
 * The test uses the model module {@code TestSecurityCoverage} of the element test application,
 * where a {@code ChildOfCovered} object has the {@code Covered} object holding it as role parent.
 * Each result is compared with the roles found by one query per group, object and role parent
 * ({@link BoundedRole#getRoles(TLObject, Group)}), which does not use the memo.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestRoleLookupPerInteraction extends BasicTestCase {

	private static final String MODULE = "TestSecurityCoverage";

	private static final String COVERED = MODULE + ":Covered";

	private static final String CHILD_OF_COVERED = MODULE + ":ChildOfCovered";

	private static final String CHILDREN = "children";

	private static final String ROLE_READER = MODULE + ".Reader";

	private static final String ROLE_DIRECT = MODULE + ".Direct";

	/**
	 * More role assignments than the memo loads at once for a single group.
	 */
	private static final int MANY_ASSIGNMENTS = 1001;

	private BoundedRole _reader;

	private BoundedRole _direct;

	private Group _group;

	private Person _member;

	private Person _other;

	private TLObject _parent;

	private List<TLObject> _children;

	private final List<TLObject> _created = new ArrayList<>();

	private final List<Group> _createdGroups = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		inTransaction(() -> {
			_reader = BoundedRole.getRoleByName(ROLE_READER);
			_direct = BoundedRole.getRoleByName(ROLE_DIRECT);
			assertNotNull(_reader);
			assertNotNull(_direct);

			_group = createGroup("roleLookupGroup");
			_member = createPerson("roleLookupMember");
			_other = createPerson("roleLookupOther");
			_group.addMember(_member);

			_parent = create(COVERED);
			_children = createChildren(_parent, 60);

			BoundedRole.assignRole(_parent, _group, _reader);
		});
		assertNotNull(_member.getRepresentativeGroup());
		assertNotNull(_other.getRepresentativeGroup());
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
			for (Group group : _createdGroups) {
				group.tDelete();
			}
			_member.getRepresentativeGroup().tDelete();
			_other.getRepresentativeGroup().tDelete();
			_member.tDelete();
			_other.tDelete();
		});
		_created.clear();
		_createdGroups.clear();
		_children = null;

		super.tearDown();
	}

	public void testRolesOfGroupOnRoleParent() {
		inInteraction(() -> {
			for (TLObject child : _children) {
				assertRoles(Set.of(_reader), child, _member);
				assertRoles(Set.of(), child, _other);
			}
			assertRoles(Set.of(_reader), _parent, _member);
			assertRoles(Set.of(), _parent, _other);
		});
	}

	public void testDirectAssignmentOnObject() {
		TLObject assigned = _children.get(3);
		inTransaction(() -> BoundedRole.assignRole(assigned, _other, _direct));

		inInteraction(() -> {
			for (TLObject child : _children) {
				assertRoles(child == assigned ? Set.of(_direct) : Set.of(), child, _other);
				assertRoles(Set.of(_reader), child, _member);
			}
		});
	}

	public void testAssignmentCommittedInSameInteraction() {
		inInteraction(() -> {
			TLObject child = _children.get(0);
			assertRoles(Set.of(), child, _other);

			inTransaction(() -> BoundedRole.assignRole(_parent, _other, _reader));
			assertRoles(Set.of(_reader), child, _other);

			inTransaction(() -> BoundedRole.removeRoleAssignments(_parent, _other.getRepresentativeGroup(), _reader));
			assertRoles(Set.of(), child, _other);
		});
	}

	public void testUncommittedAssignment() {
		inInteraction(() -> {
			TLObject child = _children.get(0);
			assertRoles(Set.of(), child, _other);

			ThreadContext.pushSuperUser();
			try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
				BoundedRole.assignRole(_parent, _other, _reader);
				assertRoles("The assignment is visible in the transaction that makes it.", Set.of(_reader), child,
					_other);
				// Not committed: closing the transaction rolls it back.
			} finally {
				ThreadContext.popSuperUser();
			}
			assertRoles("A rolled back assignment is gone.", Set.of(), child, _other);
		});
	}

	public void testMembershipChangedInSameInteraction() {
		inInteraction(() -> {
			TLObject child = _children.get(0);
			assertRoles(Set.of(), child, _other);

			inTransaction(() -> _group.addMember(_other));
			assertRoles(Set.of(_reader), child, _other);

			inTransaction(() -> _group.removeMember(_other));
			assertRoles(Set.of(), child, _other);
		});
	}

	public void testQueryCountIndependentOfObjectCount() {
		int groups = groupCount(_member);
		int fewQueries = roleQueriesForFreshCheck(_member, _children.subList(0, 5));
		int manyQueries = roleQueriesForFreshCheck(_member, _children);

		assertEquals("Role assignments are looked up once per group.", groups, fewQueries);
		assertEquals("The number of role queries must not grow with the number of checked objects.", fewQueries,
			manyQueries);
	}

	public void testGroupWithManyAssignments() {
		Group large = createGroupWithAssignments("roleLookupLarge", MANY_ASSIGNMENTS);
		Person largeMember = createPerson("roleLookupLargeMember");
		assertNotNull(largeMember.getRepresentativeGroup());
		inTransaction(() -> large.addMember(largeMember));
		try {
			List<TLObject> objects = new ArrayList<>(_children.subList(0, 20));
			TLObject assigned = _created.get(_created.size() - 1);
			objects.add(assigned);

			inInteraction(() -> {
				for (TLObject object : objects) {
					assertRoles(object == assigned ? Set.of(_direct) : Set.of(), object, largeMember);
				}
			});

			// One load per group; for the group with many assignments, one query per object and one
			// per role parent: the children of the test's parent and the container of the assigned
			// object.
			int expected = groupCount(largeMember) + objects.size() + 2;
			assertEquals("The objects of a group with many assignments are looked up one by one, but each role"
				+ " parent only once.", expected, roleQueriesForFreshCheck(largeMember, objects));
		} finally {
			inTransaction(() -> {
				large.removeMember(largeMember);
				largeMember.getRepresentativeGroup().tDelete();
				largeMember.tDelete();
			});
		}
	}

	/**
	 * Creates a group holding the direct role on the given number of new objects.
	 */
	private Group createGroupWithAssignments(String name, int count) {
		Group[] result = new Group[1];
		inTransaction(() -> {
			Group group = createGroup(name);
			TLObject container = create(COVERED);
			for (TLObject child : createChildren(container, count)) {
				BoundedRole.assignRole(child, group, _direct);
			}
			result[0] = group;
		});
		return result[0];
	}

	/**
	 * The number of queries of role assignments made by checking the roles of the given person on
	 * the given objects in a new revision.
	 */
	private int roleQueriesForFreshCheck(Person person, List<TLObject> objects) {
		// A new revision starts a new memo.
		inTransaction(() -> create(COVERED));

		ConnectionPool pool = ((DBKnowledgeBase) kb()).getConnectionPool();
		LoggingDataSourceProxy dataSource = (LoggingDataSourceProxy) pool.getDataSource();
		String roleTable =
			((DBTableMetaObject) kb().getMORepository().getMetaObject(BoundedRole.ROLE_ASSIGNMENT_OBJECT_NAME))
				.getDBName().toLowerCase(Locale.ROOT);
		AtomicInteger queries = new AtomicInteger();
		Thread testThread = Thread.currentThread();
		StatementAnalyzer counter = (sql, elapsed, rows, calls) -> {
			if (Thread.currentThread() == testThread && sql.toLowerCase(Locale.ROOT).contains(roleTable)) {
				queries.addAndGet(calls);
			}
		};

		StatementAnalyzer before = dataSource.getAnalyzer();
		dataSource.setAnalyzer(counter);
		// Only connections created while an analyzer is installed report their statements.
		pool.clear();
		try {
			inInteraction(() -> {
				for (TLObject object : objects) {
					BoundedRole.getLocalAndGlobalAndGroupRoles(object, person);
				}
			});
		} finally {
			dataSource.setAnalyzer(before);
			pool.clear();
		}
		return queries.get();
	}

	private static int groupCount(Person person) {
		return 1 + Group.getGroups(person, true, true).size();
	}

	private static void assertRoles(Set<BoundRole> expected, TLObject object, Person person) {
		assertRoles(null, expected, object, person);
	}

	private static void assertRoles(String message, Set<BoundRole> expected, TLObject object, Person person) {
		Set<BoundRole> roles = BoundedRole.getLocalAndGlobalAndGroupRoles(object, person);
		assertEquals(message, expected, roles);
		assertEquals("Roles differ from the roles found by a query per group: " + message,
			rolesQueriedPerGroup(object, person), roles);
	}

	/**
	 * The roles of the given person on the given object, looked up with a query per group, object
	 * and role parent.
	 */
	private static Set<BoundRole> rolesQueriedPerGroup(TLObject object, Person person) {
		Set<BoundRole> result = new HashSet<>(BoundedRole.getRoles(object, person.getRepresentativeGroup()));
		for (Group group : Group.getGroups(person, true, true)) {
			result.addAll(BoundedRole.getRoles(object, group));
		}
		return result;
	}

	private List<TLObject> createChildren(TLObject container, int count) {
		List<TLObject> children = new ArrayList<>(count);
		for (int n = 0; n < count; n++) {
			children.add(create(CHILD_OF_COVERED));
		}
		container.tUpdateByName(CHILDREN, children);
		return children;
	}

	private TLObject create(String qualifiedTypeName) {
		TLObject result = ModelService.getInstance().getFactory().createObject(type(qualifiedTypeName));
		_created.add(result);
		return result;
	}

	private Group createGroup(String name) {
		Group group = Group.createGroup(name);
		_createdGroups.add(group);
		return group;
	}

	private static Person createPerson(String name) {
		Person[] result = new Person[1];
		inTransaction(() -> {
			result[0] =
				Person.create(kb(), name, TLSecurityDeviceManager.getInstance().getAuthenticationDevice("dbSecurity"));
		});
		return result[0];
	}

	private static TLClass type(String qualifiedTypeName) {
		return (TLClass) TLModelUtil.findType(qualifiedTypeName);
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	private static void inInteraction(Runnable check) {
		ThreadContextManager.inInteraction(TestRoleLookupPerInteraction.class.getName(), check::run);
	}

	private static void inTransaction(Runnable modification) {
		ThreadContext.pushSuperUser();
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			modification.run();
			tx.commit();
		} finally {
			ThreadContext.popSuperUser();
		}
	}

	/** Return the suite of tests to perform. */
	public static Test suite() {
		Test suite = ServiceTestSetup.createSetup(new TestSuite(TestRoleLookupPerInteraction.class),
			TLSecurityDeviceManager.Module.INSTANCE, PersonManager.Module.INSTANCE);
		return ElementWebTestSetup.createElementWebTestSetup(suite);
	}

}
