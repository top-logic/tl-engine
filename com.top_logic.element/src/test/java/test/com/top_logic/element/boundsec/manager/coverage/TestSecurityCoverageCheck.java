/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager.coverage;

import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.tools.CollectingLogListener;
import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageCheck;
import com.top_logic.element.boundsec.manager.coverage.TypeCoverage;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for the {@link SecurityCoverageCheck} service against the model module
 * {@code TestSecurityCoverage} of the element test application.
 *
 * <p>
 * The service is configured in {@code element.test.config.xml} with the module
 * {@code TestSecurityCoverageExcluded} excluded and with the startup logging switched off, so the
 * element test application starts without the findings the test model provokes; the logging is
 * exercised by an explicit call. The findings are written at the informational level, because they
 * report the state of the access definition rather than a failure.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestSecurityCoverageCheck extends BasicTestCase {

	/** Name of the analyzed test model module. */
	private static final String MODULE = "TestSecurityCoverage";

	/** The only type of the module excluded in the service configuration. */
	private static final String IGNORED = "TestSecurityCoverageExcluded:Ignored";

	/** Type with a role rule and a read grant. */
	private static final String COVERED = MODULE + ":Covered";

	/** Type without a role source and without a container. */
	private static final String ORPHAN = MODULE + ":Orphan";

	/** Type contained in exactly one composition. */
	private static final String SINGLE_CONTAINED = MODULE + ":SingleContained";

	/** Type contained in two compositions. */
	private static final String DOUBLE_CONTAINED = MODULE + ":DoubleContained";

	/** Type with a read grant to a role that no rule delivers on it. */
	private static final String DEAD_GRANT = MODULE + ":DeadGrant";

	/** Part of the summary line written after the findings. */
	private static final String SUMMARY = "types analysed";

	public void testConfiguredExclusion() {
		List<String> types = analyzedTypes();
		assertFalse("The type of the excluded module must not be analyzed: " + IGNORED, types.contains(IGNORED));
		assertTrue("The types of the analyzed module are part of the result.", types.contains(ORPHAN));
	}

	public void testLogFindings() {
		CollectingLogListener listener = new CollectingLogListener(Set.of(Level.INFO), true);
		List<String> messages;
		try {
			SecurityCoverageCheck check = SecurityCoverageCheck.getInstance();
			check.logCoverage(check.analyze());

			messages = listener.getAndClearLogEntries().stream().map(LogEntry::getMessage).toList();
		} finally {
			listener.deactivate();
		}

		assertLogged(messages, ORPHAN);
		assertLogged(messages, DEAD_GRANT);

		assertFalse("A composition part delegates to its container and produces no finding: " + SINGLE_CONTAINED,
			hasFindingFor(messages, SINGLE_CONTAINED));
		assertFalse("A composition part delegates to its container and produces no finding: " + DOUBLE_CONTAINED,
			hasFindingFor(messages, DOUBLE_CONTAINED));

		assertFalse("A covered type produces no finding: " + COVERED, hasFindingFor(messages, COVERED));
		assertTrue("The summary of the analysis is logged: " + messages,
			messages.stream().anyMatch(message -> message.contains(SUMMARY)));
	}

	private static void assertLogged(List<String> messages, String qualifiedTypeName) {
		assertTrue("Expected a finding for '" + qualifiedTypeName + "' in " + messages,
			hasFindingFor(messages, qualifiedTypeName));
	}

	private static boolean hasFindingFor(List<String> messages, String qualifiedTypeName) {
		return messages.stream().anyMatch(message -> message.startsWith(qualifiedTypeName + ":"));
	}

	private static List<String> analyzedTypes() {
		List<TypeCoverage> coverage = SecurityCoverageCheck.getInstance().analyze();
		return coverage.stream().map(entry -> TLModelUtil.qualifiedName(entry.type())).toList();
	}

	/** Return the suite of tests to perform. */
	public static Test suite() {
		Test suite = ServiceTestSetup.createSetup(new TestSuite(TestSecurityCoverageCheck.class),
			SecurityCoverageCheck.Module.INSTANCE);
		return ElementWebTestSetup.createElementWebTestSetup(suite);
	}

}
