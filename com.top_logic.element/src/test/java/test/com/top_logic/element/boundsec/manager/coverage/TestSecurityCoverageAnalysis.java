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

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.element.boundsec.manager.coverage.CoverageFinding;
import com.top_logic.element.boundsec.manager.coverage.CoverageStatus;
import com.top_logic.element.boundsec.manager.coverage.FindingKind;
import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageAnalysis;
import com.top_logic.element.boundsec.manager.coverage.TypeCoverage;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.element.boundsec.manager.rule.config.NavigationRuleConfig;
import com.top_logic.element.boundsec.manager.rule.config.PathElementConfig;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * Test for the {@link SecurityCoverageAnalysis} against the model module
 * {@code TestSecurityCoverage} of the element test application.
 *
 * <p>
 * The module declares one type per case the analysis must distinguish; the role rules, the security
 * parent rules and the grants are declared in {@code element.test.config.xml}.
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

	/** Type whose role source is a security parent rule pointing to {@link #COVERED}. */
	private static final String CHILD_OF_COVERED = MODULE + ":ChildOfCovered";

	/** Type without a role source and without a container. */
	private static final String ORPHAN = MODULE + ":Orphan";

	/** Type contained in exactly one composition. */
	private static final String SINGLE_CONTAINED = MODULE + ":SingleContained";

	/** Type contained in two compositions. */
	private static final String DOUBLE_CONTAINED = MODULE + ":DoubleContained";

	/** Type with a read grant to a role that no rule delivers on it. */
	private static final String DEAD_GRANT = MODULE + ":DeadGrant";

	/** Type excluded from access control. */
	private static final String UNSECURED = MODULE + ":Unsecured";

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

	public void testSecurityParentIsRoleSource() {
		TypeCoverage coverage = coverage(CHILD_OF_COVERED);
		assertEquals(coverage.toString(), CoverageStatus.COVERED, coverage.status());
		assertTrue("The type has no role rule of its own.", coverage.roleRules().isEmpty());
		assertEquals(1, coverage.securityParentRules().size());
	}

	public void testTypeWithoutRoleSourceAndWithoutGrant() {
		TypeCoverage coverage = coverage(ORPHAN);
		assertEquals(coverage.toString(), CoverageStatus.INCOMPLETE, coverage.status());
		assertEquals(Set.of(FindingKind.NO_ROLE_SOURCE, FindingKind.NO_READ_GRANT), findingKinds(coverage));

		CoverageFinding finding = singleFinding(coverage, FindingKind.NO_ROLE_SOURCE);
		assertFalse("The element test application does not use the global default security parent.",
			finding.isRootFallbackActive());
		assertNotNull(finding.getMessage());
	}

	public void testSuggestedSecurityParent() {
		TypeCoverage coverage = coverage(SINGLE_CONTAINED);
		assertTrue(coverage.toString(), coverage.hasFinding(FindingKind.NO_ROLE_SOURCE));

		CoverageFinding finding = singleFinding(coverage, FindingKind.SUGGESTED_PARENT);
		assertEquals(List.of(SINGLES_REFERENCE), qualifiedNames(finding.getContainerReferences()));

		NavigationRuleConfig rule = finding.getSuggestedRule();
		assertNotNull(rule);
		assertEquals("SingleContained" + SecurityCoverageAnalysis.SUGGESTED_RULE_ID_SUFFIX, rule.getId());
		assertEquals(SINGLE_CONTAINED, rule.getMetaElement());
		assertTrue("The proposed rule applies to the specializations, too.", rule.isInherit());

		List<PolymorphicConfiguration<? extends PathElement>> path = rule.getPathElements();
		assertEquals(path.toString(), 1, path.size());
		PathElementConfig step = (PathElementConfig) path.get(0);
		assertEquals(SINGLES_REFERENCE, step.getAttribute().qualifiedName());
		assertTrue("The composition is navigated backwards to reach the container.", step.isInverse());
	}

	public void testAmbiguousSecurityParent() {
		TypeCoverage coverage = coverage(DOUBLE_CONTAINED);
		assertTrue(coverage.toString(), coverage.hasFinding(FindingKind.NO_ROLE_SOURCE));
		assertFalse(coverage.toString(), coverage.hasFinding(FindingKind.SUGGESTED_PARENT));

		CoverageFinding finding = singleFinding(coverage, FindingKind.AMBIGUOUS_PARENT);
		assertEquals(List.of(DOUBLES_A_REFERENCE, DOUBLES_B_REFERENCE),
			qualifiedNames(finding.getContainerReferences()));
		assertNull("An ambiguous container yields no proposal.", finding.getSuggestedRule());
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
		assertEquals(coverage.toString(), CoverageStatus.COVERED, coverage.status());
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
