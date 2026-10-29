/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager;

import java.util.ArrayList;
import java.util.Collection;
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
import com.top_logic.element.boundsec.manager.StorageAccessManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.model.ModelService;

/**
 * Test for {@link AccessManager#hasRoleOnAnyObject(Person, Collection)} of the
 * {@link StorageAccessManager}.
 * <p>
 * The test uses the model module {@code TestSecurityCoverage} of the element test application,
 * where a {@code ChildOfCovered} object has the {@code Covered} object holding it as role parent,
 * and the role rule {@code TestSecurityCoverage_covered} gives the owner of a {@code Covered}
 * object the reader role on it. Each positive answer is backed by
 * {@link AccessManager#hasRole(Person, BoundObject, Collection)} on the object holding the role.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestRoleOnAnyObject extends BasicTestCase {

	private static final String MODULE = "TestSecurityCoverage";

	private static final String COVERED = MODULE + ":Covered";

	private static final String CHILD_OF_COVERED = MODULE + ":ChildOfCovered";

	private static final String CHILDREN = "children";

	private static final String OWNER = "owner";

	private static final String ROLE_READER = MODULE + ".Reader";

	private static final String ROLE_DIRECT = MODULE + ".Direct";

	private BoundedRole _reader;

	private BoundedRole _direct;

	private Group _group;

	private Person _member;

	private Person _directHolder;

	private Person _other;

	private TLObject _parent;

	private TLObject _child;

	private final List<TLObject> _created = new ArrayList<>();

	private final List<Person> _createdPersons = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_member = createPerson("roleAnywhereMember");
		_directHolder = createPerson("roleAnywhereDirect");
		_other = createPerson("roleAnywhereOther");
		inTransaction(() -> {
			_reader = BoundedRole.getRoleByName(ROLE_READER);
			_direct = BoundedRole.getRoleByName(ROLE_DIRECT);
			assertNotNull(_reader);
			assertNotNull(_direct);

			_group = Group.createGroup("roleAnywhereGroup");
			_group.addMember(_member);

			_parent = create(COVERED);
			_child = create(CHILD_OF_COVERED);
			_parent.tUpdateByName(CHILDREN, List.of(_child));

			BoundedRole.assignRole(_parent, _group, _reader);
			BoundedRole.assignRole(_child, _directHolder, _direct);
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
			_group.tDelete();
			for (Person person : _createdPersons) {
				person.getRepresentativeGroup().tDelete();
				person.tDelete();
			}
		});
		_created.clear();
		_createdPersons.clear();

		super.tearDown();
	}

	public void testDirectAssignment() {
		inInteraction(() -> {
			assertTrue(accessManager().hasRole(_directHolder, (BoundObject) _child, Set.of(_direct)));
			assertTrue(anywhere(_directHolder, _direct));
			assertTrue(anywhere(_directHolder, _direct, _reader));
			assertFalse("No assignment of the reader role.", anywhere(_directHolder, _reader));
		});
	}

	public void testGroupMembership() {
		inInteraction(() -> {
			assertTrue(accessManager().hasRole(_member, (BoundObject) _child, Set.of(_reader)));
			assertTrue(anywhere(_member, _reader));
			assertFalse(anywhere(_member, _direct));
		});
	}

	public void testWithoutAssignment() {
		inInteraction(() -> {
			assertFalse(anywhere(_other, _reader));
			assertFalse(anywhere(_other, _direct));
			assertFalse(anywhere(_other, _reader, _direct));
		});
	}

	public void testNoRoles() {
		inInteraction(() -> {
			assertFalse(accessManager().hasRoleOnAnyObject(_directHolder, Set.of()));
			assertFalse(accessManager().hasRoleOnAnyObject(_directHolder, null));
		});
	}

	public void testNoPerson() {
		inInteraction(() -> assertFalse(accessManager().hasRoleOnAnyObject(null, Set.of(_reader))));
	}

	public void testAdmin() {
		Person admin = PersonManager.getManager().getRoot();
		assertTrue(admin.isAdmin());
		inInteraction(() -> {
			assertTrue(anywhere(admin, _reader));
			assertTrue(accessManager().hasRoleOnAnyObject(admin, Set.of()));
		});
	}

	public void testOutsideInteraction() {
		assertTrue(anywhere(_member, _reader));
		assertFalse(anywhere(_other, _reader));
	}

	public void testAssignmentCommittedInSameInteraction() {
		inInteraction(() -> {
			assertFalse(anywhere(_other, _reader));
			// Answered from the memo.
			assertFalse(anywhere(_other, _reader));

			inTransaction(() -> BoundedRole.assignRole(_parent, _other, _reader));
			assertTrue(anywhere(_other, _reader));

			inTransaction(() -> BoundedRole.removeRoleAssignments(_parent, _other.getRepresentativeGroup(), _reader));
			assertFalse(anywhere(_other, _reader));
		});
	}

	public void testMembershipChangedInSameInteraction() {
		inInteraction(() -> {
			assertFalse(anywhere(_other, _reader));

			inTransaction(() -> _group.addMember(_other));
			assertTrue(anywhere(_other, _reader));

			inTransaction(() -> _group.removeMember(_other));
			assertFalse(anywhere(_other, _reader));
		});
	}

	public void testRoleFromRule() {
		inInteraction(() -> {
			assertFalse(anywhere(_other, _reader));

			TLObject[] owned = new TLObject[1];
			inTransaction(() -> {
				owned[0] = create(COVERED);
				owned[0].tUpdateByName(OWNER, _other);
			});
			assertTrue("The role rule gives the owner the reader role.",
				accessManager().hasRole(_other, (BoundObject) owned[0], Set.of(_reader)));
			assertTrue(anywhere(_other, _reader));
			assertFalse(anywhere(_other, _direct));
		});
	}

	private static boolean anywhere(Person person, BoundRole... roles) {
		return accessManager().hasRoleOnAnyObject(person, Set.of(roles));
	}

	private static AccessManager accessManager() {
		AccessManager result = AccessManager.getInstance();
		assertTrue(result instanceof StorageAccessManager);
		return result;
	}

	private TLObject create(String qualifiedTypeName) {
		TLObject result = ModelService.getInstance().getFactory().createObject(type(qualifiedTypeName));
		_created.add(result);
		return result;
	}

	private Person createPerson(String name) {
		Person[] result = new Person[1];
		inTransaction(() -> {
			result[0] =
				Person.create(kb(), name, TLSecurityDeviceManager.getInstance().getAuthenticationDevice("dbSecurity"));
		});
		assertNotNull(result[0].getRepresentativeGroup());
		_createdPersons.add(result[0]);
		return result[0];
	}

	private static TLClass type(String qualifiedTypeName) {
		return (TLClass) TLModelUtil.findType(qualifiedTypeName);
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	private static void inInteraction(Runnable check) {
		ThreadContext.inSystemContext(TestRoleOnAnyObject.class, check::run);
	}

	private static void inTransaction(Runnable modification) {
		ThreadContext.withoutSecurityChecks(() -> {
			try (Transaction tx =
				kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
				modification.run();
				tx.commit();
			}
		});
	}

	/** Return the suite of tests to perform. */
	public static Test suite() {
		Test suite = ServiceTestSetup.createSetup(new TestSuite(TestRoleOnAnyObject.class),
			TLSecurityDeviceManager.Module.INSTANCE, PersonManager.Module.INSTANCE);
		return ElementWebTestSetup.createElementWebTestSetup(suite);
	}

}
