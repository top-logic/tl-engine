/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager.coverage;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.element.boundsec.manager.coverage.CoverageFinding;
import com.top_logic.element.boundsec.manager.coverage.FindingKind;
import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageAnalysis;
import com.top_logic.element.boundsec.manager.coverage.TypeCoverage;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.model.ModelService;

/**
 * Test for the {@link SecurityCoverageAnalysis} recognizing a role that is assigned directly on an
 * object instead of being computed by a rule.
 *
 * <p>
 * The test model declares the type {@code TestSecurityCoverage:DirectlyAssigned} with a read grant
 * to the role {@code TestSecurityCoverage.Direct}, which no rule delivers. The grant is dead as
 * long as nothing assigns the role; an assignment on an object of the type makes it effective.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestSecurityCoverageDirectRoleAssignment extends BasicTestCase {

	/** Name of the analyzed test model module. */
	private static final String MODULE = "TestSecurityCoverage";

	/** Type whose grant is only delivered by a direct role assignment. */
	private static final String DIRECTLY_ASSIGNED = MODULE + ":DirectlyAssigned";

	/** Type with a read grant to a role that neither a rule nor an assignment delivers. */
	private static final String DEAD_GRANT = MODULE + ":DeadGrant";

	/** Role granted on {@link #DIRECTLY_ASSIGNED} and assigned directly by this test. */
	private static final String ROLE_DIRECT = MODULE + ".Direct";

	/** Role granted on {@link #DEAD_GRANT} that nothing delivers. */
	private static final String ROLE_GHOST = MODULE + ".Ghost";

	/** Name of the group the role is assigned to. */
	private static final String GROUP_NAME = "testSecurityCoverageDirect";

	private BoundedRole _role;

	private Group _group;

	private TLObject _object;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		inTransaction(() -> {
			_role = BoundedRole.getRoleByName(ROLE_DIRECT);
			assertNotNull("The role '" + ROLE_DIRECT + "' is created by the test configuration.", _role);
			_group = Group.createGroup(GROUP_NAME);
			_object = ModelService.getInstance().getFactory().createObject(type(DIRECTLY_ASSIGNED));
			BoundedRole.assignRole(_object, _group, _role);
		});
	}

	@Override
	protected void tearDown() throws Exception {
		inTransaction(() -> {
			BoundedRole.removeRoleAssignments(_object);
			_object.tDelete();
			_group.tDelete();
		});
		_object = null;
		_group = null;
		_role = null;

		super.tearDown();
	}

	public void testDirectAssignmentDeliversRole() {
		TypeCoverage coverage = coverage(DIRECTLY_ASSIGNED);
		assertFalse("The assigned role delivers the grant: " + coverage,
			coverage.hasFinding(FindingKind.DEAD_GRANT));

		TypeCoverage deadGrant = coverage(DEAD_GRANT);
		assertEquals("A role that no assignment delivers stays dead: " + deadGrant, Set.of(ROLE_GHOST),
			roleNames(singleDeadGrant(deadGrant)));
	}

	public void testGrantIsDeadWithoutAssignment() {
		inTransaction(() -> BoundedRole.removeRoleAssignments(_object, _group, _role));
		try {
			TypeCoverage coverage = coverage(DIRECTLY_ASSIGNED);
			assertEquals("Without the assignment nothing delivers the role: " + coverage, Set.of(ROLE_DIRECT),
				roleNames(singleDeadGrant(coverage)));
		} finally {
			inTransaction(() -> BoundedRole.assignRole(_object, _group, _role));
		}
	}

	private static CoverageFinding singleDeadGrant(TypeCoverage coverage) {
		assertEquals("Expected exactly one dead grant in " + coverage, 1,
			coverage.findings(FindingKind.DEAD_GRANT).size());
		return coverage.findings(FindingKind.DEAD_GRANT).get(0);
	}

	private static Set<String> roleNames(CoverageFinding finding) {
		return finding.getRoles().stream().map(BoundedRole::getName).collect(Collectors.toSet());
	}

	private static TypeCoverage coverage(String qualifiedTypeName) {
		Map<String, TypeCoverage> coverage = SecurityCoverageAnalysis.newInstance(Set.of()).analyze()
			.stream()
			.collect(Collectors.toMap(entry -> TLModelUtil.qualifiedName(entry.type()), Function.identity()));
		TypeCoverage result = coverage.get(qualifiedTypeName);
		assertNotNull("No coverage computed for '" + qualifiedTypeName + "'.", result);
		return result;
	}

	private static TLClass type(String qualifiedTypeName) {
		return (TLClass) TLModelUtil.findType(qualifiedTypeName);
	}

	private static void inTransaction(Runnable modification) {
		ThreadContext.pushSuperUser();
		try (Transaction tx = PersistencyLayer.getKnowledgeBase()
			.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			modification.run();
			tx.commit();
		} finally {
			ThreadContext.popSuperUser();
		}
	}

	/** Return the suite of tests to perform. */
	public static Test suite() {
		return ElementWebTestSetup
			.createElementWebTestSetup(new TestSuite(TestSecurityCoverageDirectRoleAssignment.class));
	}

}
