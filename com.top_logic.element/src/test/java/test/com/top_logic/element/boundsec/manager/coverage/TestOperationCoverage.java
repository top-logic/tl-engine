/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager.coverage;

import java.util.List;
import java.util.Locale;
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
import com.top_logic.element.boundsec.manager.coverage.OperationUse;
import com.top_logic.element.boundsec.manager.coverage.OperationUse.Kind;
import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageAnalysis;
import com.top_logic.element.boundsec.manager.coverage.TypeCoverage;
import com.top_logic.model.TLClass;
import com.top_logic.model.security.ApplicableOperations;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.Resources;

/**
 * Test of the operation related part of the {@link SecurityCoverageAnalysis}: the roles a user can
 * hold on a type, the operations no role holds, and the {@link OperationUse}s checking an operation
 * that does not apply or is unknown.
 *
 * <p>
 * Runs against the model module {@code TestSecurityCoverage} of the element test application.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestOperationCoverage extends BasicTestCase {

	private static final String MODULE = "TestSecurityCoverage";

	private static final String EXCLUDED_MODULE = "TestSecurityCoverageExcluded";

	/** Type with a role rule and grants of Read and Write. */
	private static final String COVERED = MODULE + ":Covered";

	/** Specialization of {@link #COVERED}. */
	private static final String COVERED_SUB = MODULE + ":CoveredSub";

	/** Type declaring Finish, which no role is granted. */
	private static final String WITH_OPERATIONS = MODULE + ":WithOperations";

	/** Type declaring Finish implied by Read. */
	private static final String IMPLIED_OPERATION = MODULE + ":ImpliedOperation";

	/** Type granting Finish, which TestCoverageShow is implied by. */
	private static final String GRANTED_OPERATION = MODULE + ":GrantedOperation";

	/** The operation implied by Finish on {@link #GRANTED_OPERATION}. */
	private static final String IMPLIED_BY_FINISH = "TestCoverageShow";

	/** Type delegating to its container. */
	private static final String EXPLICIT_PART = MODULE + ":ExplicitPart";

	/** Composition part of {@link #GRANTED_OPERATION}, delegating to its container by default. */
	private static final String PART_OF_GRANTED = MODULE + ":PartOfGranted";

	/** Composition part of {@link #COVERED}, delegating to its container by default. */
	private static final String PART_OF_COVERED = MODULE + ":PartOfCovered";

	/** Type with a grant to a role no rule delivers on it. */
	private static final String DEAD_GRANT = MODULE + ":DeadGrant";

	private static final String ROLE_READER = MODULE + ".Reader";

	private static final String ROLE_GHOST = MODULE + ".Ghost";

	/** Name of an operation that is no registered command group. */
	private static final String UNKNOWN = "TestNoSuchOperation";

	/** A registered operation that is not built-in. */
	private static final String FINISH_NAME = SimpleBoundCommandGroup.FINISH_NAME;

	public void testHoldableRoles() {
		SecurityCoverageAnalysis analysis = analysis(List.of());
		assertEquals(List.of(ROLE_READER), roleNames(analysis.getHoldableRoles(type(COVERED))));
		assertEquals("Inherited role rules deliver the role on the specialization.", List.of(ROLE_READER),
			roleNames(analysis.getHoldableRoles(type(COVERED_SUB))));
		assertFalse("A role no rule delivers cannot be held.",
			analysis.canHold(type(DEAD_GRANT), BoundedRole.getRoleByName(ROLE_GHOST)));
		assertTrue(analysis.canHold(type(DEAD_GRANT), BoundedRole.getRoleByName(ROLE_READER)));
	}

	public void testGrantedRolesWithoutImplication() {
		SecurityCoverageAnalysis analysis = analysis(List.of());
		TLClass implied = type(IMPLIED_OPERATION);
		assertEquals(Set.of(ROLE_READER), roleNames(analysis.getGrantedRoles(implied, SimpleBoundCommandGroup.READ)));
		assertEquals("A role holding Finish through Read is not granted Finish directly.", Set.of(),
			roleNames(analysis.getGrantedRoles(implied, finish())));
	}

	public void testUngrantedOperation() {
		Map<String, TypeCoverage> coverage = coverage(List.of());

		TypeCoverage withOperations = coverage.get(WITH_OPERATIONS);
		assertEquals(withOperations.toString(), CoverageStatus.INCOMPLETE, withOperations.status());
		CoverageFinding finding = single(withOperations, FindingKind.UNGRANTED_OPERATION);
		assertEquals(finish(), finding.getOperation());

		TypeCoverage implied = coverage.get(IMPLIED_OPERATION);
		assertEquals("An operation implied by a granted one is held.", CoverageStatus.COVERED, implied.status());

		assertFalse("A built-in operation without a grant is no finding.",
			coverage.get(COVERED).hasFinding(FindingKind.UNGRANTED_OPERATION));
	}

	/**
	 * A finding names an operation by its label, as the administration shows it.
	 */
	public void testFindingNamesTheOperationLabel() {
		CoverageFinding finding =
			single(coverage(List.of()).get(WITH_OPERATIONS), FindingKind.UNGRANTED_OPERATION);
		String german = Resources.getInstance(Locale.GERMAN).getString(finding.getMessage());
		assertTrue(german, german.contains(Resources.getInstance(Locale.GERMAN)
			.getString(CoverageFinding.operationLabel(finding.getOperation()))));
		assertFalse("The internal name is not shown where the label differs: " + german,
			german.contains("\"" + FINISH_NAME + "\""));
	}

	public void testGrantNothingChecksIsReported() {
		CoverageFinding finding = single(coverage(List.of()).get(GRANTED_OPERATION), FindingKind.UNCHECKED_GRANT);
		assertEquals(finish(), finding.getOperation());
		assertEquals(Set.of(ROLE_READER), roleNames(finding.getRoles()));
	}

	public void testCheckedGrantIsNoFinding() {
		OperationUse use = OperationUse.onType(Kind.COMMAND, FINISH_NAME, type(GRANTED_OPERATION), "test.view.xml:6");
		assertFalse(coverage(List.of(use)).get(GRANTED_OPERATION).hasFinding(FindingKind.UNCHECKED_GRANT));
	}

	public void testGrantCheckedWithoutDeclaredTypeIsNoFinding() {
		OperationUse script = OperationUse.onUnknown(Kind.SCRIPT, FINISH_NAME, "TL-Script finish()");
		assertFalse("A place not declaring its type may check every type.",
			coverage(List.of(script)).get(GRANTED_OPERATION).hasFinding(FindingKind.UNCHECKED_GRANT));
	}

	public void testCheckOfAnImpliedOperationUsesTheGrant() {
		OperationUse use =
			OperationUse.onType(Kind.VIEW, IMPLIED_BY_FINISH, type(GRANTED_OPERATION), "test.view.xml:7");
		assertFalse("Holding Finish means holding the operation it implies, which is checked.",
			coverage(List.of(use)).get(GRANTED_OPERATION).hasFinding(FindingKind.UNCHECKED_GRANT));
	}

	public void testCheckOnAnotherTypeDoesNotUseTheGrant() {
		OperationUse use = OperationUse.onType(Kind.COMMAND, FINISH_NAME, type(WITH_OPERATIONS), "test.view.xml:8");
		assertTrue(coverage(List.of(use)).get(GRANTED_OPERATION).hasFinding(FindingKind.UNCHECKED_GRANT));
	}

	/**
	 * A check on a composition part deciding by its container, the access parent it gets by
	 * default, is a use of the grants of the types that may contain it, and of no other type.
	 */
	public void testDefaultContainerIsFollowedToContainerTypes() {
		OperationUse onPart =
			OperationUse.onType(Kind.COMMAND, FINISH_NAME, type(PART_OF_GRANTED), "test.view.xml:9");
		assertFalse("The check on the part is decided on its container.",
			coverage(List.of(onPart)).get(GRANTED_OPERATION).hasFinding(FindingKind.UNCHECKED_GRANT));

		OperationUse onOtherPart =
			OperationUse.onType(Kind.COMMAND, FINISH_NAME, type(PART_OF_COVERED), "test.view.xml:10");
		assertTrue("A part of another container does not check the grant.",
			coverage(List.of(onOtherPart)).get(GRANTED_OPERATION).hasFinding(FindingKind.UNCHECKED_GRANT));
	}

	public void testBuiltInGrantIsNeverReported() {
		for (TypeCoverage coverage : coverage(List.of()).values()) {
			for (CoverageFinding finding : coverage.findings(FindingKind.UNCHECKED_GRANT)) {
				assertFalse(finding.toString(), ApplicableOperations.isBuiltIn(finding.getOperation()));
			}
		}
		assertFalse(coverage(List.of()).get(COVERED).hasFinding(FindingKind.UNCHECKED_GRANT));
	}

	public void testUnknownUsesReportNoUncheckedGrant() {
		Map<String, TypeCoverage> coverage = byType(SecurityCoverageAnalysis.newInstance(Set.of(EXCLUDED_MODULE)).analyze());
		assertFalse("Without knowing the places checking operations, no grant is reported as unchecked.",
			coverage.get(GRANTED_OPERATION).hasFinding(FindingKind.UNCHECKED_GRANT));
	}

	public void testInapplicableOperation() {
		OperationUse use = OperationUse.onType(Kind.COMMAND, FINISH_NAME, type(COVERED), "test.view.xml:1");
		Map<String, TypeCoverage> coverage = coverage(List.of(use));

		CoverageFinding finding = single(coverage.get(COVERED), FindingKind.INAPPLICABLE_OPERATION);
		assertEquals(finish(), finding.getOperation());
		assertEquals(List.of(use), finding.getUses());
		assertTrue("A use of a type checks the objects of its specializations as well.",
			coverage.get(COVERED_SUB).hasFinding(FindingKind.INAPPLICABLE_OPERATION));
	}

	public void testApplicableOperationIsNoFinding() {
		OperationUse declared =
			OperationUse.onType(Kind.VIEW, FINISH_NAME, type(WITH_OPERATIONS), "test.view.xml:2");
		OperationUse builtIn =
			OperationUse.onType(Kind.FORM, SimpleBoundCommandGroup.WRITE_NAME, type(COVERED), "test.view.xml:3");
		Map<String, TypeCoverage> coverage = coverage(List.of(declared, builtIn));

		assertFalse(coverage.get(WITH_OPERATIONS).hasFinding(FindingKind.INAPPLICABLE_OPERATION));
		assertEquals(coverage.get(COVERED).toString(), CoverageStatus.COVERED, coverage.get(COVERED).status());
	}

	public void testDelegatingTypeIsNotCheckedForApplicability() {
		OperationUse use =
			OperationUse.onType(Kind.COMMAND, FINISH_NAME, type(EXPLICIT_PART), "test.view.xml:4");
		Map<String, TypeCoverage> coverage = coverage(List.of(use));

		assertFalse("The operation is decided on the access parent.",
			coverage.get(EXPLICIT_PART).hasFinding(FindingKind.INAPPLICABLE_OPERATION));
	}

	public void testUnknownOperation() {
		OperationUse typed = OperationUse.onType(Kind.VIEW, UNKNOWN, type(COVERED), "test.view.xml:5");
		OperationUse untyped = OperationUse.onUnknown(Kind.SCRIPT, UNKNOWN, "TL-Script noSuch()");
		SecurityCoverageAnalysis analysis = analysis(List.of(typed, untyped));
		Map<String, TypeCoverage> coverage = byType(analysis.analyze());

		CoverageFinding finding = single(coverage.get(COVERED), FindingKind.UNKNOWN_OPERATION);
		assertNull(finding.getOperation());
		assertEquals(UNKNOWN, finding.getOperationName());

		List<CoverageFinding> unattached = analysis.getUnattachedFindings();
		assertEquals(1, unattached.size());
		assertEquals(FindingKind.UNKNOWN_OPERATION, unattached.get(0).getKind());
		assertNull("A use not declaring its type has no type to report the finding for.",
			unattached.get(0).getType());
		assertEquals(List.of(untyped), unattached.get(0).getUses());
	}

	public void testUsesOfAType() {
		OperationUse onType = OperationUse.onType(Kind.VIEW, FINISH_NAME, type(WITH_OPERATIONS), "a");
		OperationUse onCovered = OperationUse.onType(Kind.VIEW, FINISH_NAME, type(COVERED), "b");
		OperationUse untyped = OperationUse.onUnknown(Kind.SCRIPT, FINISH_NAME, "c");
		OperationUse root = OperationUse.onRoot(Kind.VIEW, FINISH_NAME, "d");
		OperationUse other = OperationUse.onType(Kind.VIEW, SimpleBoundCommandGroup.READ_NAME,
			type(WITH_OPERATIONS), "e");
		SecurityCoverageAnalysis analysis = analysis(List.of(onType, onCovered, untyped, root, other));

		assertEquals(List.of(onType, untyped), analysis.getUses(type(WITH_OPERATIONS), finish()));
		assertEquals(List.of(root), analysis.getRootUses(finish()));
	}

	private static BoundCommandGroup finish() {
		return CommandGroupRegistry.getInstance().getGroup(FINISH_NAME);
	}

	private static SecurityCoverageAnalysis analysis(List<OperationUse> uses) {
		return SecurityCoverageAnalysis.newInstance(Set.of(EXCLUDED_MODULE), uses);
	}

	private static Map<String, TypeCoverage> coverage(List<OperationUse> uses) {
		return byType(analysis(uses).analyze());
	}

	private static Map<String, TypeCoverage> byType(List<TypeCoverage> coverage) {
		return coverage.stream()
			.collect(Collectors.toMap(entry -> TLModelUtil.qualifiedName(entry.type()), Function.identity()));
	}

	private static CoverageFinding single(TypeCoverage coverage, FindingKind kind) {
		List<CoverageFinding> findings = coverage.findings(kind);
		assertEquals("Expected exactly one " + kind + " finding in " + coverage, 1, findings.size());
		return findings.get(0);
	}

	private static TLClass type(String name) {
		return (TLClass) TLModelUtil.findType(name);
	}

	private static List<String> roleNames(List<BoundedRole> roles) {
		return roles.stream().map(BoundedRole::getName).toList();
	}

	private static Set<String> roleNames(Set<BoundedRole> roles) {
		return roles.stream().map(BoundedRole::getName).collect(Collectors.toSet());
	}

	/** Return the suite of tests to perform. */
	public static Test suite() {
		return ElementWebTestSetup.createElementWebTestSetup(new TestSuite(TestOperationCoverage.class));
	}

}
