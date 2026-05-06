/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Tests for collision diagnostics in {@link ToolCatalog}.
 */
public class TestToolCatalogCollision extends TestCase {

	/** Registering two tools with the same name produces a descriptive error. */
	public void testCollisionMessageContainsClassNames() {
		ToolCatalog catalog = new ToolCatalog();

		Tool first = new FirstTool();
		Tool second = new SecondTool();

		catalog.register(first);

		try {
			catalog.register(second);
			fail("Expected IllegalArgumentException for name collision.");
		} catch (IllegalArgumentException e) {
			String msg = e.getMessage();
			assertTrue("Should contain tool name, got: " + msg,
				msg.contains("'search'"));
			assertTrue("Should contain existing class name, got: " + msg,
				msg.contains("FirstTool"));
			assertTrue("Should contain new class name, got: " + msg,
				msg.contains("SecondTool"));
		}
	}

	/** A tool named the same as itself cannot be registered twice. */
	public void testSameToolRegisteredTwice() {
		ToolCatalog catalog = new ToolCatalog();
		Tool tool = new FirstTool();
		catalog.register(tool);

		try {
			catalog.register(tool);
			fail("Expected IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("'search'"));
		}
	}

	private static class FirstTool implements Tool {
		@Override public String name() { return "search"; }
		@Override public String description() { return "first"; }
		@Override public ToolSchema schema() { return new ToolSchema("search", "first", "{}"); }
		@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext ctx) {
			return ToolResult.of("");
		}
		@Override public ToolMode mode() { return ToolMode.READ; }
	}

	private static class SecondTool implements Tool {
		@Override public String name() { return "search"; }
		@Override public String description() { return "second"; }
		@Override public ToolSchema schema() { return new ToolSchema("search", "second", "{}"); }
		@Override public ToolResult execute(Map<String, Object> args, ToolExecutionContext ctx) {
			return ToolResult.of("");
		}
		@Override public ToolMode mode() { return ToolMode.READ; }
	}
}
