/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager.explain;

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
import com.top_logic.element.boundsec.manager.explain.AccessExplanation;
import com.top_logic.element.boundsec.manager.explain.EffectiveAccess;
import com.top_logic.element.boundsec.manager.explain.HeldRole;
import com.top_logic.element.boundsec.manager.explain.OperationDecision;
import com.top_logic.element.boundsec.manager.explain.RoleAssignment;
import com.top_logic.element.boundsec.manager.explain.RoleSource;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.model.ModelService;

/**
 * Test for {@link EffectiveAccess} against the model module {@code TestSecurityCoverage} of the
 * element test application.
 * <p>
 * The role rule {@code TestSecurityCoverage_covered} gives the owner of a {@code Covered} object
 * the reader role on it, which is granted read and write. A {@code ChildOfCovered} object has the
 * {@code Covered} object holding it as role parent and grants read to the reader role, a
 * {@code PartOfCovered} object delegates to its container as access parent, and a
 * {@code DirectlyAssigned} object grants read to a role no rule delivers.
 * </p>
 * <p>
 * The analysis runs in the system context of the test, in which the security checks are bypassed:
 * the decisions must nevertheless be made for the explained account.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestEffectiveAccess extends BasicTestCase {

	private static final String MODULE = "TestSecurityCoverage";

	private static final String COVERED = MODULE + ":Covered";

	private static final String CHILD_OF_COVERED = MODULE + ":ChildOfCovered";

	private static final String PART_OF_COVERED = MODULE + ":PartOfCovered";

	private static final String DIRECTLY_ASSIGNED = MODULE + ":DirectlyAssigned";

	private static final String UNSECURED = MODULE + ":Unsecured";

	private static final String CHILDREN = "children";

	private static final String PARTS = "parts";

	private static final String OWNER = "owner";

	private static final String ROLE_READER = MODULE + ".Reader";

	private static final String ROLE_DIRECT = MODULE + ".Direct";

	private static final String RULE_COVERED = "TestSecurityCoverage_covered";

	private BoundedRole _reader;

	private BoundedRole _direct;

	private Group _group;

	private Person _owner;

	private Person _member;

	private Person _directHolder;

	private Person _other;

	private TLObject _covered;

	private TLObject _child;

	private TLObject _part;

	private TLObject _assigned;

	private TLObject _unsecured;

	private final List<TLObject> _created = new ArrayList<>();

	private final List<Person> _createdPersons = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_owner = createPerson("effectiveAccessOwner");
		_member = createPerson("effectiveAccessMember");
		_directHolder = createPerson("effectiveAccessDirect");
		_other = createPerson("effectiveAccessOther");
		inTransaction(() -> {
			_reader = BoundedRole.getRoleByName(ROLE_READER);
			_direct = BoundedRole.getRoleByName(ROLE_DIRECT);
			assertNotNull(_reader);
			assertNotNull(_direct);

			_group = Group.createGroup("effectiveAccessGroup");
			_group.addMember(_member);

			_covered = create(COVERED);
			_covered.tUpdateByName(OWNER, _owner);
			_child = create(CHILD_OF_COVERED);
			_covered.tUpdateByName(CHILDREN, List.of(_child));
			_part = create(PART_OF_COVERED);
			_covered.tUpdateByName(PARTS, List.of(_part));

			_assigned = create(DIRECTLY_ASSIGNED);
			_unsecured = create(UNSECURED);

			BoundedRole.assignRole(_covered, _group, _reader);
			BoundedRole.assignRole(_assigned, _directHolder, _direct);
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

	public void testSourcesAvailable() {
		assertTrue("The test application uses a storage access manager.",
			inInteraction(() -> analysis().isSourcesAvailable()));
	}

	public void testDirectRoleAssignment() {
		AccessExplanation explanation = explain(_directHolder, _assigned);

		assertTrue(explanation.sourcesAvailable());
		assertFalse(explanation.isDelegating());
		assertSame(_assigned, explanation.decidingObject());

		OperationDecision read = decision(explanation, SimpleBoundCommandGroup.READ);
		assertTrue(read.allowed());
		assertEquals(Set.of(_direct), read.grantingRoles());
		assertFalse("Only read is granted.", decision(explanation, SimpleBoundCommandGroup.WRITE).allowed());

		RoleAssignment source = singleSource(explanation, _direct);
		assertEquals(RoleSource.DIRECT, source.source());
		assertNull(source.rule());
		assertSame(_assigned, source.object());
		assertEquals(_directHolder.getRepresentativeGroup(), source.group());
		assertFalse(explanation.isInherited(source));
	}

	public void testRoleRule() {
		AccessExplanation explanation = explain(_owner, _covered);

		assertFalse(explanation.isDelegating());
		assertTrue(decision(explanation, SimpleBoundCommandGroup.READ).allowed());
		assertEquals(Set.of(_reader), decision(explanation, SimpleBoundCommandGroup.READ).grantingRoles());
		assertTrue(decision(explanation, SimpleBoundCommandGroup.WRITE).allowed());
		assertFalse("Delete is granted to nobody.", decision(explanation, SimpleBoundCommandGroup.DELETE).allowed());
		assertEquals(Set.of(), decision(explanation, SimpleBoundCommandGroup.DELETE).grantingRoles());

		RoleAssignment source = singleSource(explanation, _reader);
		assertEquals(RoleSource.RULE, source.source());
		assertEquals(RULE_COVERED, source.ruleId());
		assertSame(_covered, source.object());
		assertEquals(_owner.getRepresentativeGroup(), source.group());
	}

	public void testRoleThroughGroup() {
		AccessExplanation explanation = explain(_member, _covered);

		assertTrue(decision(explanation, SimpleBoundCommandGroup.READ).allowed());
		RoleAssignment source = singleSource(explanation, _reader);
		assertEquals(RoleSource.DIRECT, source.source());
		assertEquals("The role reaches the account through the group it is a member of.", _group, source.group());
	}

	public void testRoleParent() {
		AccessExplanation explanation = explain(_owner, _child);

		assertFalse("A role parent passes on roles, not the decision.", explanation.isDelegating());
		assertSame(_child, explanation.decidingObject());
		assertEquals(List.of(_covered), explanation.roleParents());

		OperationDecision read = decision(explanation, SimpleBoundCommandGroup.READ);
		assertTrue(read.allowed());
		assertEquals(Set.of(_reader), read.grantingRoles());
		assertFalse("The child grants read only, whatever the role parent grants.",
			decision(explanation, SimpleBoundCommandGroup.WRITE).allowed());

		RoleAssignment source = singleSource(explanation, _reader);
		assertTrue(explanation.isInherited(source));
		assertSame("The role is held on the role parent.", _covered, source.object());
		assertEquals(RoleSource.RULE, source.source());
		assertEquals(RULE_COVERED, source.ruleId());
	}

	public void testAccessParent() {
		AccessExplanation explanation = explain(_owner, _part);

		assertTrue(explanation.isDelegating());
		assertSame("The container decides for the part.", _covered, explanation.decidingObject());

		OperationDecision read = decision(explanation, SimpleBoundCommandGroup.READ);
		assertTrue(read.allowed());
		assertEquals(Set.of(_reader), read.admittingRoles());
		assertEquals(Set.of(_reader), read.grantingRoles());

		OperationDecision delete = decision(explanation, SimpleBoundCommandGroup.DELETE);
		assertTrue("Deleting a part is writing its container.", delete.allowed());
		assertEquals(Set.of(_reader), delete.admittingRoles());

		RoleAssignment source = singleSource(explanation, _reader);
		assertFalse(explanation.isInherited(source));
		assertSame(_covered, source.object());
		assertEquals(RULE_COVERED, source.ruleId());
	}

	public void testDenied() {
		AccessExplanation explanation = explain(_other, _covered);

		assertFalse(explanation.administrator());
		assertEquals(List.of(), explanation.heldRoles());
		OperationDecision read = decision(explanation, SimpleBoundCommandGroup.READ);
		assertFalse("The account holds no role on the object.", read.allowed());
		assertEquals(Set.of(_reader), read.admittingRoles());
		assertEquals(Set.of(), read.grantingRoles());
	}

	public void testWithoutSecurity() {
		AccessExplanation explanation = explain(_other, _unsecured);

		assertTrue(explanation.withoutSecurity());
		assertFalse(explanation.internal());
		OperationDecision read = decision(explanation, SimpleBoundCommandGroup.READ);
		assertTrue("A type without security is not access controlled.", read.allowed());
		assertEquals(Set.of(), read.grantingRoles());
	}

	public void testRestrictedUser() {
		inTransaction(() -> _owner.setRestrictedUser(Boolean.TRUE));
		AccessExplanation explanation = explain(_owner, _covered);

		assertTrue(explanation.restrictedUser());
		OperationDecision write = decision(explanation, SimpleBoundCommandGroup.WRITE);
		assertFalse("A restricted user must not modify, whatever roles it holds.", write.allowed());
		assertEquals("The role granting write is held nevertheless.", Set.of(_reader), write.grantingRoles());
		assertTrue(write.restricted());

		OperationDecision read = decision(explanation, SimpleBoundCommandGroup.READ);
		assertTrue(read.allowed());
		assertFalse(read.restricted());
	}

	public void testUnrestrictedUser() {
		AccessExplanation explanation = explain(_owner, _covered);

		assertFalse(explanation.restrictedUser());
		assertFalse(decision(explanation, SimpleBoundCommandGroup.WRITE).restricted());
	}

	public void testAdministrator() {
		Person root = PersonManager.getManager().getRoot();
		AccessExplanation explanation = explain(root, _covered);

		assertTrue(explanation.administrator());
		assertTrue(decision(explanation, SimpleBoundCommandGroup.DELETE).allowed());
	}

	public void testAssignmentsOfAccount() {
		List<RoleAssignment> owned = inInteraction(() -> analysis().assignments(_owner, null));
		RoleAssignment rule = find(owned, _reader, _covered);
		assertNotNull("The rule gives the owner the reader role on the covered object.", rule);
		assertEquals(RoleSource.RULE, rule.source());
		assertEquals(RULE_COVERED, rule.ruleId());
		assertEquals(_owner.getRepresentativeGroup(), rule.group());
		assertNull("An inherited role is reported on the role parent only.", find(owned, _reader, _child));

		List<RoleAssignment> direct = inInteraction(() -> analysis().assignments(_directHolder, List.of()));
		RoleAssignment assignment = find(direct, _direct, _assigned);
		assertNotNull(assignment);
		assertEquals(RoleSource.DIRECT, assignment.source());
	}

	public void testAssignmentsWithRoleFilter() {
		List<RoleAssignment> readers = inInteraction(() -> analysis().assignments(_member, List.of(_reader)));
		RoleAssignment membership = find(readers, _reader, _covered);
		assertNotNull(membership);
		assertEquals(RoleSource.DIRECT, membership.source());
		assertEquals(_group, membership.group());
		for (RoleAssignment entry : readers) {
			assertEquals(_reader, entry.role());
		}

		assertEquals("The member holds the direct role nowhere.", List.of(),
			inInteraction(() -> analysis().assignments(_member, List.of(_direct))));
		assertEquals("The other account holds none of the roles of the test.", List.of(),
			inInteraction(() -> analysis().assignments(_other, List.of(_reader, _direct))));
	}

	private static RoleAssignment find(List<RoleAssignment> assignments, BoundedRole role, TLObject object) {
		for (RoleAssignment assignment : assignments) {
			if (role.equals(assignment.role()) && object.equals(assignment.object())) {
				return assignment;
			}
		}
		return null;
	}

	private static RoleAssignment singleSource(AccessExplanation explanation, BoundedRole role) {
		HeldRole held = null;
		for (HeldRole candidate : explanation.heldRoles()) {
			if (candidate.role().equals(role)) {
				held = candidate;
			}
		}
		assertNotNull("The account holds " + role.getName() + ".", held);
		assertEquals("Sources of " + role.getName() + ": " + held.sources(), 1, held.sources().size());
		return held.sources().get(0);
	}

	private static OperationDecision decision(AccessExplanation explanation, BoundCommandGroup operation) {
		OperationDecision result = explanation.getDecision(operation);
		assertNotNull("Operation " + operation + " applies.", result);
		return result;
	}

	private static AccessExplanation explain(Person person, TLObject object) {
		return inInteraction(() -> analysis().explain(person, object));
	}

	private static EffectiveAccess analysis() {
		return EffectiveAccess.getInstance();
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

	private static <T> T inInteraction(Supplier<T> check) {
		return ThreadContext.inSystemContext(TestEffectiveAccess.class, check::get);
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
		Test suite = ServiceTestSetup.createSetup(new TestSuite(TestEffectiveAccess.class),
			TLSecurityDeviceManager.Module.INSTANCE, PersonManager.Module.INSTANCE);
		return ElementWebTestSetup.createElementWebTestSetup(suite);
	}

}
