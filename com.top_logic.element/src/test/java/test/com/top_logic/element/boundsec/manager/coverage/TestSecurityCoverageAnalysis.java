/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager.coverage;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.element.boundsec.manager.coverage.CoverageFinding;
import com.top_logic.element.boundsec.manager.coverage.CoverageStatus;
import com.top_logic.element.boundsec.manager.coverage.FindingKind;
import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageAnalysis;
import com.top_logic.element.boundsec.manager.coverage.TypeCoverage;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.security.AccessParent;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * Test for the {@link SecurityCoverageAnalysis} against the model module
 * {@code TestSecurityCoverage} of the element test application.
 *
 * <p>
 * The module declares one type per case the analysis must distinguish; the role rules, the role
 * parent rules, the access parents and the grants are declared in {@code element.test.config.xml}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestSecurityCoverageAnalysis extends BasicTestCase {

	/** Name of the analyzed test model module. */
	private static final String MODULE = "TestSecurityCoverage";

	/** Name of the module that is kept out of the analysis. */
	private static final String EXCLUDED_MODULE = "TestSecurityCoverageExcluded";

	/** Type with a role rule and a read grant. */
	private static final String COVERED = MODULE + ":Covered";

	/** Specialization inheriting the role rule and the grant of {@link #COVERED}. */
	private static final String COVERED_SUB = MODULE + ":CoveredSub";

	/** Type whose role source is a role parent rule pointing to {@link #COVERED}. */
	private static final String CHILD_OF_COVERED = MODULE + ":ChildOfCovered";

	/** Type without a role source and without a container. */
	private static final String ORPHAN = MODULE + ":Orphan";

	/** Type contained in exactly one composition. */
	private static final String SINGLE_CONTAINED = MODULE + ":SingleContained";

	/** Type contained in two compositions. */
	private static final String DOUBLE_CONTAINED = MODULE + ":DoubleContained";

	/** Composition part of {@link #COVERED} without any definition. */
	private static final String PART_OF_COVERED = MODULE + ":PartOfCovered";

	/** Composition part of {@link #PART_OF_COVERED}. */
	private static final String SUB_PART = MODULE + ":SubPart";

	/** Type with a configured access parent navigating a composition backwards, and a shadowed rule. */
	private static final String EXPLICIT_PART = MODULE + ":ExplicitPart";

	/** Type with a configured access parent navigating its own reference forwards. */
	private static final String LINKED = MODULE + ":Linked";

	/** Composition containing {@link #EXPLICIT_PART}. */
	private static final String EXPLICIT_PARTS_REFERENCE = MODULE + ":Covered#explicitParts";

	/** Reference of {@link #LINKED} leading to its access parent. */
	private static final String TARGET_REFERENCE = MODULE + ":Linked#target";

	/** The role rule shadowed by the access parent of {@link #EXPLICIT_PART}. */
	private static final String SHADOWED_RULE_ID = "TestSecurityCoverage_explicitPart";

	/** Type with a read grant to a role that no rule delivers on it. */
	private static final String DEAD_GRANT = MODULE + ":DeadGrant";

	/** Type excluded from access control. */
	private static final String UNSECURED = MODULE + ":Unsecured";

	/** Type used by the application's code only. */
	private static final String INTERNAL = MODULE + ":Internal";

	/** The only type of {@link #EXCLUDED_MODULE}. */
	private static final String IGNORED = EXCLUDED_MODULE + ":Ignored";

	/** Composition containing {@link #SINGLE_CONTAINED}. */
	private static final String SINGLES_REFERENCE = MODULE + ":Container#singles";

	/** First composition containing {@link #DOUBLE_CONTAINED}. */
	private static final String DOUBLES_A_REFERENCE = MODULE + ":Container#doublesA";

	/** Second composition containing {@link #DOUBLE_CONTAINED}. */
	private static final String DOUBLES_B_REFERENCE = MODULE + ":OtherContainer#doublesB";

	/** Role delivered by the role rules of the test model. */
	private static final String ROLE_READER = MODULE + ".Reader";

	/** Role that is granted but delivered by no rule. */
	private static final String ROLE_GHOST = MODULE + ".Ghost";

	private Map<String, TypeCoverage> _coverage;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_coverage = coverageByType(Set.of(EXCLUDED_MODULE));
	}

	@Override
	protected void tearDown() throws Exception {
		_coverage = null;

		super.tearDown();
	}

	private static Map<String, TypeCoverage> coverageByType(Set<String> excludedModules) {
		return SecurityCoverageAnalysis.newInstance(excludedModules).analyze()
			.stream()
			.collect(Collectors.toMap(coverage -> TLModelUtil.qualifiedName(coverage.type()), Function.identity()));
	}

	public void testCoveredTypeHasNoFinding() {
		TypeCoverage coverage = coverage(COVERED);
		assertEquals(coverage.toString(), CoverageStatus.COVERED, coverage.status());
		assertTrue(coverage.toString(), coverage.findings().isEmpty());
		assertFalse(coverage.roleRules().isEmpty());
		assertEquals(Set.of(ROLE_READER), roleNames(coverage.readRoles()));
	}

	public void testInheritedRoleRuleCoversSpecialization() {
		TypeCoverage coverage = coverage(COVERED_SUB);
		assertEquals(coverage.toString(), CoverageStatus.COVERED, coverage.status());
		assertFalse("The role rule of the generalization is inherited.", coverage.roleRules().isEmpty());
		assertEquals(Set.of(ROLE_READER), roleNames(coverage.readRoles()));
	}

	public void testRoleParentIsRoleSource() {
		TypeCoverage coverage = coverage(CHILD_OF_COVERED);
		assertEquals(coverage.toString(), CoverageStatus.COVERED, coverage.status());
		assertTrue("The type has no role rule of its own.", coverage.roleRules().isEmpty());
		assertEquals(1, coverage.roleParentRules().size());
		assertNull("A type with a role parent rule decides for itself.", coverage.accessParent());
	}

	public void testTypeWithoutRoleSourceAndWithoutGrant() {
		TypeCoverage coverage = coverage(ORPHAN);
		assertEquals(coverage.toString(), CoverageStatus.INCOMPLETE, coverage.status());
		assertEquals(Set.of(FindingKind.NO_ROLE_SOURCE, FindingKind.NO_READ_GRANT), findingKinds(coverage));
		assertNull("A type held in no composition has no default access parent.", coverage.accessParent());

		CoverageFinding finding = singleFinding(coverage, FindingKind.NO_ROLE_SOURCE);
		assertFalse("The element test application does not use the global default role parent.",
			finding.isRootFallbackActive());
		assertNotNull(finding.getMessage());
	}

	public void testCompositionPartDelegatesToContainerByDefault() {
		TypeCoverage coverage = coverage(SINGLE_CONTAINED);
		assertEquals(coverage.toString(), CoverageStatus.DELEGATED, coverage.status());
		assertTrue(coverage.toString(), coverage.findings().isEmpty());
		AccessParent parent = coverage.accessParent();
		assertNotNull("A composition part without a role source has its container as access parent.", parent);
		assertTrue("The default relation is the container, whichever composition holds the object.",
			parent.isContainer());
		assertFalse("The default relation is not configured.", parent.explicit());
		assertEquals(List.of(SINGLES_REFERENCE), qualifiedNames(coverage.containerReferences()));
	}

	public void testPartOfSeveralCompositionsDelegatesToContainer() {
		TypeCoverage coverage = coverage(DOUBLE_CONTAINED);
		assertEquals(coverage.toString(), CoverageStatus.DELEGATED, coverage.status());
		assertTrue("Several compositions are no ambiguity: the container holding the object decides.",
			coverage.accessParent().isContainer());
		assertEquals(List.of(DOUBLES_A_REFERENCE, DOUBLES_B_REFERENCE),
			qualifiedNames(coverage.containerReferences()));
	}

	public void testChainOfCompositionParts() {
		assertEquals(CoverageStatus.DELEGATED, coverage(PART_OF_COVERED).status());
		assertEquals(CoverageStatus.DELEGATED, coverage(SUB_PART).status());
	}

	public void testConfiguredAccessParentBackwards() {
		TypeCoverage coverage = coverage(EXPLICIT_PART);
		AccessParent parent = coverage.accessParent();
		assertNotNull(parent);
		assertTrue(parent.explicit());
		assertTrue("A composition is navigated backwards to the container.", parent.inverse());
		assertEquals(EXPLICIT_PARTS_REFERENCE, TLModelUtil.qualifiedName(parent.reference()));

		assertEquals("The role rule applying to the type is shadowed by the access parent: " + coverage,
			CoverageStatus.INCOMPLETE, coverage.status());
		CoverageFinding finding = singleFinding(coverage, FindingKind.SHADOWED_RULES);
		assertEquals(List.of(SHADOWED_RULE_ID), finding.getRuleIds());
	}

	public void testConfiguredAccessParentForwards() {
		TypeCoverage coverage = coverage(LINKED);
		assertEquals(coverage.toString(), CoverageStatus.DELEGATED, coverage.status());
		AccessParent parent = coverage.accessParent();
		assertNotNull(parent);
		assertTrue(parent.explicit());
		assertFalse("A to-one reference of the type is navigated forwards.", parent.inverse());
		assertEquals(TARGET_REFERENCE, TLModelUtil.qualifiedName(parent.reference()));
		assertTrue("The type is held in no composition.", coverage.containerReferences().isEmpty());
	}

	public void testDeadGrant() {
		TypeCoverage coverage = coverage(DEAD_GRANT);
		assertEquals(coverage.toString(), Set.of(FindingKind.DEAD_GRANT), findingKinds(coverage));

		CoverageFinding finding = singleFinding(coverage, FindingKind.DEAD_GRANT);
		assertEquals(SimpleBoundCommandGroup.READ, finding.getOperation());
		assertEquals(Set.of(ROLE_GHOST), roleNames(finding.getRoles()));
	}

	public void testTypeWithoutSecurityHasNoFinding() {
		TypeCoverage coverage = coverage(UNSECURED);
		assertTrue("The type is excluded from access control.", coverage.withoutSecurity());
		assertEquals(coverage.toString(), CoverageStatus.EXEMPT, coverage.status());
		assertTrue(coverage.toString(), coverage.findings().isEmpty());
	}

	public void testInternalTypeHasNoFinding() {
		TypeCoverage coverage = coverage(INTERNAL);
		assertTrue("The type is used by the application's code only.", coverage.internal());
		assertFalse("An internal type is access controlled.", coverage.withoutSecurity());
		assertEquals(coverage.toString(), CoverageStatus.EXEMPT, coverage.status());
		assertTrue(coverage.toString(), coverage.findings().isEmpty());
	}

	public void testExcludedModuleIsNotAnalyzed() {
		assertFalse("The type of an excluded module must not be part of the result.",
			_coverage.containsKey(IGNORED));

		Map<String, TypeCoverage> all = coverageByType(Set.of());
		assertTrue("Without the exclusion the type is analyzed.", all.containsKey(IGNORED));
		assertTrue(all.get(IGNORED).hasFinding(FindingKind.NO_ROLE_SOURCE));
	}

	public void testAbstractTypesAreNotAnalyzed() {
		for (TypeCoverage coverage : _coverage.values()) {
			assertFalse("An abstract type has no objects: " + coverage, coverage.type().isAbstract());
		}
	}

	private TypeCoverage coverage(String qualifiedTypeName) {
		TypeCoverage result = _coverage.get(qualifiedTypeName);
		assertNotNull("No coverage computed for '" + qualifiedTypeName + "'.", result);
		assertEquals(qualifiedTypeName, TLModelUtil.qualifiedName(result.type()));
		return result;
	}

	private static CoverageFinding singleFinding(TypeCoverage coverage, FindingKind kind) {
		List<CoverageFinding> findings = coverage.findings(kind);
		assertEquals("Expected exactly one " + kind + " finding in " + coverage, 1, findings.size());
		return findings.get(0);
	}

	private static Set<FindingKind> findingKinds(TypeCoverage coverage) {
		return coverage.findings().stream().map(CoverageFinding::getKind).collect(Collectors.toSet());
	}

	private static Set<String> roleNames(Set<BoundedRole> roles) {
		return roles.stream().map(BoundedRole::getName).collect(Collectors.toSet());
	}

	private static List<String> qualifiedNames(List<? extends TLModelPart> parts) {
		return parts.stream().map(TLModelUtil::qualifiedName).toList();
	}

	/** Return the suite of tests to perform. */
	public static Test suite() {
		return ElementWebTestSetup.createElementWebTestSetup(new TestSuite(TestSecurityCoverageAnalysis.class));
	}

}
