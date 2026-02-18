/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.skills;

import junit.framework.TestCase;

public class TestSkillTier extends TestCase {

	public void testFromStringCore() {
		assertEquals(SkillTier.CORE, SkillTier.fromString("core"));
	}

	public void testFromStringOnDemand() {
		assertEquals(SkillTier.ON_DEMAND, SkillTier.fromString("on-demand"));
	}

	public void testFromStringCaseInsensitive() {
		assertEquals(SkillTier.CORE, SkillTier.fromString("CORE"));
		assertEquals(SkillTier.ON_DEMAND, SkillTier.fromString("ON-DEMAND"));
	}

	public void testFromStringInvalid() {
		try {
			SkillTier.fromString("unknown");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// ok
		}
	}

	public void testDefaultIsCoreWhenNull() {
		assertEquals(SkillTier.CORE, SkillTier.fromString(null));
	}
}
