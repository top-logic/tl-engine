/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager.coverage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DeactivatedTest;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.util.ElementTestCollector;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.element.boundsec.manager.coverage.CoverageFinding;
import com.top_logic.element.boundsec.manager.coverage.FindingKind;
import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageCheck;
import com.top_logic.element.boundsec.manager.coverage.TypeCoverage;

/**
 * Test that the model based access definition of the application under test is complete.
 *
 * <p>
 * The test reports every type whose objects no user can see: a type without a role source, a type
 * nobody may read, and a type carrying a grant to a role that no rule delivers on it. It runs
 * against the definitions of the application under test, therefore it is added to the test suite of
 * an application only when the application switches it on, see
 * {@link ElementTestCollector.GlobalConfig#getTestSecurityCoverage()}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
@DeactivatedTest("Checks the application under test, not the module the test is part of. "
	+ "Applications activate it through their test configuration.")
public class TestSecurityCoverage extends BasicTestCase {

	/**
	 * The kinds of finding that make the access definition of a type incomplete: every kind the
	 * analysis reports.
	 */
	private static final Set<FindingKind> FAILING_KINDS = Set.of(FindingKind.values());

	public void testSecurityCoverage() {
		List<String> problems = new ArrayList<>();
		for (TypeCoverage coverage : SecurityCoverageCheck.getInstance().analyze()) {
			if (!fails(coverage)) {
				continue;
			}
			for (CoverageFinding finding : coverage.findings()) {
				problems.add(SecurityCoverageCheck.describe(finding));
			}
		}
		if (!problems.isEmpty()) {
			fail("The access definition is incomplete for " + problems.size() + " finding(s):\n"
				+ String.join("\n", problems));
		}
	}

	private static boolean fails(TypeCoverage coverage) {
		return coverage.findings().stream().anyMatch(finding -> FAILING_KINDS.contains(finding.getKind()));
	}

	/** Return the suite of tests to perform. */
	public static Test suite() {
		Test suite = ServiceTestSetup.createSetup(new TestSuite(TestSecurityCoverage.class),
			SecurityCoverageCheck.Module.INSTANCE);
		return ElementWebTestSetup.createElementWebTestSetup(suite);
	}

}
