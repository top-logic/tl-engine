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
