/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;

/**
 * Tests for {@link AiSecurityMode}.
 */
public class TestAiSecurityMode extends TestCase {

	/** Instantiating a {@link ToolAccessPolicy} resolves nested config types through the TypeIndex. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestAiSecurityMode.class)));
	}

	/**
	 * An application that configures no policy at all is unenforced.
	 *
	 * <p>
	 * The one place this design deliberately does not fail closed: the engine's model security denies
	 * by default, so enforcing it where no {@code <security-config>} was ever written would leave the
	 * agent unable to read anything.
	 * </p>
	 */
	public void testAbsentPolicyIsLegacy() {
		assertEquals(AiSecurityMode.LEGACY, AiSecurityMode.resolve(null));
	}

	/** A policy that names no mode is still an unmigrated application. */
	public void testPolicyWithoutModeIsLegacy() {
		assertEquals(AiSecurityMode.LEGACY, AiSecurityMode.resolve(policy(null)));
	}

	/** A mode the administrator named is the mode of the run. */
	public void testConfiguredModeIsUsed() {
		assertEquals(AiSecurityMode.ENFORCED, AiSecurityMode.resolve(policy(AiSecurityMode.ENFORCED)));
		assertEquals(AiSecurityMode.SYSTEM, AiSecurityMode.resolve(policy(AiSecurityMode.SYSTEM)));
		assertEquals(AiSecurityMode.LEGACY, AiSecurityMode.resolve(policy(AiSecurityMode.LEGACY)));
	}

	/** An agent that declares nothing leaves the deployment's decision alone. */
	public void testAbsentFloorLeavesTheCeilingAlone() {
		for (AiSecurityMode ceiling : AiSecurityMode.values()) {
			assertEquals(ceiling, AiSecurityMode.stricter(ceiling, null));
		}
		assertEquals(AiSecurityMode.LEGACY, AiSecurityMode.resolve(policy(null), null));
		assertEquals(AiSecurityMode.ENFORCED,
			AiSecurityMode.resolve(policy(AiSecurityMode.ENFORCED), null));
	}

	/** A floor stricter than the deployment wins - the whole point of having one. */
	public void testStricterFloorWins() {
		assertEquals(AiSecurityMode.ENFORCED,
			AiSecurityMode.stricter(AiSecurityMode.LEGACY, AiSecurityMode.ENFORCED));
		assertEquals(AiSecurityMode.SYSTEM,
			AiSecurityMode.stricter(AiSecurityMode.LEGACY, AiSecurityMode.SYSTEM));
		assertEquals(AiSecurityMode.ENFORCED,
			AiSecurityMode.stricter(AiSecurityMode.SYSTEM, AiSecurityMode.ENFORCED));
	}

	/**
	 * A floor laxer than the deployment is ignored.
	 *
	 * <p>
	 * The direction that must not exist: editing an agent definition is itself a privileged action,
	 * and it must not be a way around what the deployment decided.
	 * </p>
	 */
	public void testLaxerFloorIsIgnored() {
		assertEquals(AiSecurityMode.ENFORCED,
			AiSecurityMode.stricter(AiSecurityMode.ENFORCED, AiSecurityMode.LEGACY));
		assertEquals(AiSecurityMode.ENFORCED,
			AiSecurityMode.stricter(AiSecurityMode.ENFORCED, AiSecurityMode.SYSTEM));
		assertEquals(AiSecurityMode.SYSTEM,
			AiSecurityMode.stricter(AiSecurityMode.SYSTEM, AiSecurityMode.LEGACY));
	}

	/** An equal floor changes nothing, whatever the mode. */
	public void testEqualFloorChangesNothing() {
		for (AiSecurityMode mode : AiSecurityMode.values()) {
			assertSame(mode, AiSecurityMode.stricter(mode, mode));
		}
	}

	/**
	 * An agent demanding {@link AiSecurityMode#ENFORCED} in an unmigrated deployment really is
	 * enforced.
	 *
	 * <p>
	 * The case the whole feature exists for: a deployment that has written no policy is
	 * {@link AiSecurityMode#LEGACY}, and one agent inside it can still insist on being checked.
	 * </p>
	 */
	public void testEnforcedAgentInALegacyDeployment() {
		AiSecurityMode effective = AiSecurityMode.resolve(null, AiSecurityMode.ENFORCED);

		assertEquals(AiSecurityMode.ENFORCED, effective);
		assertTrue(effective.enforcesToolAccess());
		assertTrue(effective.enforcesModelAccess());
		assertTrue(effective.requiresUser());
	}

	/** Only ENFORCED enforces; only SYSTEM is audited for existing. */
	public void testCapabilityFlags() {
		assertTrue(AiSecurityMode.ENFORCED.requiresUser());
		assertTrue(AiSecurityMode.ENFORCED.enforcesToolAccess());
		assertTrue(AiSecurityMode.ENFORCED.enforcesModelAccess());
		assertFalse(AiSecurityMode.ENFORCED.requiresBypassAudit());

		assertFalse(AiSecurityMode.SYSTEM.requiresUser());
		assertFalse(AiSecurityMode.SYSTEM.enforcesToolAccess());
		assertFalse(AiSecurityMode.SYSTEM.enforcesModelAccess());
		assertTrue(AiSecurityMode.SYSTEM.requiresBypassAudit());

		assertFalse(AiSecurityMode.LEGACY.requiresUser());
		assertFalse(AiSecurityMode.LEGACY.enforcesToolAccess());
		assertFalse(AiSecurityMode.LEGACY.enforcesModelAccess());
		assertFalse(AiSecurityMode.LEGACY.requiresBypassAudit());
	}

	private static ToolAccessPolicy policy(AiSecurityMode mode) {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		if (mode != null) {
			policy.setSecurityMode(mode);
		}
		return policy;
	}
}
