/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.tools.interaction.DelegationTool;

/**
 * Tests for {@link DelegationTool}.
 */
public class TestDelegationTool extends TestCase {

	/**
	 * name() returns the tool name.
	 */
	public void testName() {
		DelegationTool tool = new DelegationTool("delegate_to_explorer", "Explores stuff", "explorer", "orchestrator",
			name -> null);
		assertEquals("delegate_to_explorer", tool.name());
	}

	/**
	 * description includes target agent name and description.
	 */
	public void testDescription() {
		DelegationTool tool = new DelegationTool("delegate_to_explorer", "Explores the model", "explorer",
			"orchestrator", name -> null);
		assertTrue(tool.description().contains("explorer"));
		assertTrue(tool.description().contains("Explores the model"));
	}

	/**
	 * schema has correct name and required task parameter.
	 */
	public void testSchema() {
		DelegationTool tool = new DelegationTool("delegate_to_explorer", "desc", "explorer", "orchestrator",
			name -> null);
		assertEquals("delegate_to_explorer", tool.schema().name());
		assertTrue(tool.schema().inputSchemaJson().contains("\"task\""));
		assertTrue(tool.schema().inputSchemaJson().contains("\"required\""));
	}

	/**
	 * isConcurrencySafe returns true.
	 */
	public void testIsConcurrencySafe() {
		DelegationTool tool = new DelegationTool("delegate_to_x", "desc", "x", "parent", name -> null);
		assertTrue(tool.isConcurrencySafe());
	}

	/**
	 * execute returns error when task parameter is missing.
	 */
	public void testExecuteWithoutTask() {
		DelegationTool tool = new DelegationTool("delegate_to_x", "desc", "x", "parent", name -> null);
		String result = tool.execute(Map.of(), ToolContext.EMPTY);
		assertTrue(result.startsWith("ERROR:"));
		assertTrue(result.contains("task"));
	}

	/**
	 * execute returns error at max delegation depth.
	 */
	public void testExecuteAtMaxDepth() {
		DelegationTool tool = new DelegationTool("delegate_to_x", "desc", "x", "parent", name -> null);
		ToolContext deepContext = ToolContext.EMPTY;
		for (int i = 0; i < DelegationTool.MAX_DELEGATION_DEPTH; i++) {
			deepContext = deepContext.forDelegation(TransactionScope.NOOP);
		}
		String result = tool.execute(Map.of("task", "do something"), deepContext);
		assertTrue(result.startsWith("ERROR:"));
		assertTrue(result.contains("depth"));
	}

	/**
	 * null target description falls back to agent name.
	 */
	public void testNullDescriptionFallback() {
		DelegationTool tool = new DelegationTool("delegate_to_x", null, "explorer", "parent", name -> null);
		assertTrue(tool.description().contains("explorer"));
	}
}
