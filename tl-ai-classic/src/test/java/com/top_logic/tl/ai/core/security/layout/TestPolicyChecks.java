/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * The warnings {@link PolicyChecks} shows an administrator about a policy.
 */
public class TestPolicyChecks extends TestCase {

	/** The checks resolve configuration types through the type index. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestPolicyChecks.class)));
	}

	/**
	 * A role whose data tools reach the types without security is not told they see nothing; it is
	 * told what they reach. Without such types the original sentence stands (#609).
	 */
	public void testDataRoleWarningNamesWhatWithoutSecurityReaches() {
		ResKey precise = PolicyChecks.dataRoleWithoutModelRights("lookupEditor",
			Set.of(ToolGroups.DATA_WRITE), Set.of("pm.lookup", "pm:Note"));
		assertEquals(I18NConstants.WARN_DATA_ROLE_ONLY_WITHOUT_SECURITY__ROLE_GROUPS_PARTS.getKey(),
			precise.getKey());
		assertEquals(List.of("lookupEditor", ToolGroups.DATA_WRITE, "pm.lookup, pm:Note"),
			List.of(precise.arguments()));

		ResKey nothing = PolicyChecks.dataRoleWithoutModelRights("dora",
			Set.of(ToolGroups.DATA_WRITE), Set.of());
		assertEquals(I18NConstants.WARN_DATA_ROLE_WITHOUT_MODEL_RIGHTS__ROLE_GROUPS.getKey(),
			nothing.getKey());
	}

}
