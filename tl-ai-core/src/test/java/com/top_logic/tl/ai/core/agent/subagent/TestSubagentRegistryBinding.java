/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import junit.framework.TestCase;

/**
 * Test for {@link SubagentRegistryBinding} thread-local bind/restore.
 */
public class TestSubagentRegistryBinding extends TestCase {

	public void testBindAndRestore() {
		assertNull(SubagentRegistryBinding.getRegistry());
		SubagentJobRegistry registry = new SubagentJobRegistry();
		try (SubagentRegistryBinding.Binding b = SubagentRegistryBinding.bind(registry)) {
			assertSame(registry, SubagentRegistryBinding.getRegistry());
		}
		assertNull("restored to null after close", SubagentRegistryBinding.getRegistry());
	}

	public void testNestedBindRestoresPrevious() {
		SubagentJobRegistry outer = new SubagentJobRegistry();
		SubagentJobRegistry inner = new SubagentJobRegistry();
		try (SubagentRegistryBinding.Binding b1 = SubagentRegistryBinding.bind(outer)) {
			try (SubagentRegistryBinding.Binding b2 = SubagentRegistryBinding.bind(inner)) {
				assertSame(inner, SubagentRegistryBinding.getRegistry());
			}
			assertSame(outer, SubagentRegistryBinding.getRegistry());
		}
	}
}
