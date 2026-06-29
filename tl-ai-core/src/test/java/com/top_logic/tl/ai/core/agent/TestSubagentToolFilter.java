/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Map;

import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

import junit.framework.TestCase;

/**
 * Tests the read-only tool filter used by {@link AgentFactory#buildSubagentLoop}.
 *
 * <p>The structural depth-1 guarantee (no {@code delegate_to_*} / {@code wait_for_subagent} tools)
 * and the end-to-end read-only catalog are verified by the Task 15 integration test.</p>
 */
public class TestSubagentToolFilter extends TestCase {

	/** */
	public void testReadModeIsAllowed() {
		assertTrue(AgentFactory.isReadOnlyTool(fakeTool("read_tool", ToolMode.READ)));
	}

	/** */
	public void testUserScopedModeIsAllowed() {
		assertTrue(AgentFactory.isReadOnlyTool(fakeTool("user_tool", ToolMode.USER_SCOPED)));
	}

	/** */
	public void testDataWriteModeIsExcluded() {
		assertFalse(AgentFactory.isReadOnlyTool(fakeTool("write_tool", ToolMode.DATA_WRITE)));
	}

	/** */
	public void testStructuralModeIsExcluded() {
		assertFalse(AgentFactory.isReadOnlyTool(fakeTool("structural_tool", ToolMode.STRUCTURAL)));
	}

	private static Tool fakeTool(String name, ToolMode mode) {
		return new Tool() {
			@Override
			public String name() {
				return name;
			}

			@Override
			public String description() {
				return name;
			}

			@Override
			public ToolSchema schema() {
				return new ToolSchema(name, name, "{}");
			}

			@Override
			public ToolMode mode() {
				return mode;
			}

			@Override
			public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				return ToolResult.of("");
			}
		};
	}

}
