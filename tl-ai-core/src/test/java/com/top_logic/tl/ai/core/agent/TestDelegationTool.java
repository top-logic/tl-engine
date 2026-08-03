/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.interaction.DelegationTool;
import com.top_logic.tl.ai.tool.api.ToolResult;

/**
 * Tests for {@link DelegationTool}.
 */
public class TestDelegationTool extends TestCase {

	/**
	 * name() returns the tool name.
	 */
	public void testName() {
		DelegationTool tool = new DelegationTool("delegate_to_explorer", "Explores stuff", "explorer", "orchestrator",
			(name, security) -> null);
		assertEquals("delegate_to_explorer", tool.name());
	}

	/**
	 * description includes target agent name and description.
	 */
	public void testDescription() {
		DelegationTool tool = new DelegationTool("delegate_to_explorer", "Explores the model", "explorer",
			"orchestrator", (name, security) -> null);
		assertTrue(tool.description().contains("explorer"));
		assertTrue(tool.description().contains("Explores the model"));
	}

	/**
	 * schema has correct name and required task parameter.
	 */
	public void testSchema() {
		DelegationTool tool = new DelegationTool("delegate_to_explorer", "desc", "explorer", "orchestrator",
			(name, security) -> null);
		assertEquals("delegate_to_explorer", tool.schema().name());
		assertTrue(tool.schema().inputSchemaJson().contains("\"task\""));
		assertTrue(tool.schema().inputSchemaJson().contains("\"required\""));
	}

	/**
	 * isConcurrencySafe returns true.
	 */
	public void testIsConcurrencySafe() {
		DelegationTool tool = new DelegationTool("delegate_to_x", "desc", "x", "parent", (name, security) -> null);
		assertTrue(tool.isConcurrencySafe());
	}

	/**
	 * execute returns error when task parameter is missing.
	 */
	public void testExecuteWithoutTask() {
		DelegationTool tool = new DelegationTool("delegate_to_x", "desc", "x", "parent", (name, security) -> null);
		ToolResult result = tool.execute(Map.of(), ToolContext.EMPTY);
		assertTrue(result.text().startsWith("ERROR:"));
		assertTrue(result.text().contains("task"));
	}

	/**
	 * execute returns error at max delegation depth.
	 */
	public void testExecuteAtMaxDepth() {
		DelegationTool tool = new DelegationTool("delegate_to_x", "desc", "x", "parent", (name, security) -> null);
		ToolContext deepContext = ToolContext.EMPTY;
		for (int i = 0; i < DelegationTool.MAX_DELEGATION_DEPTH; i++) {
			deepContext = deepContext.forDelegation(TransactionScope.NOOP);
		}
		ToolResult result = tool.execute(Map.of("task", "do something"), deepContext);
		assertTrue(result.text().startsWith("ERROR:"));
		assertTrue(result.text().contains("depth"));
	}

	/**
	 * null target description falls back to agent name.
	 */
	public void testNullDescriptionFallback() {
		DelegationTool tool = new DelegationTool("delegate_to_x", null, "explorer", "parent", (name, security) -> null);
		assertTrue(tool.description().contains("explorer"));
	}

	/**
	 * The child agent loop must receive its own {@link TransactionScope}, not the
	 * parent's. This isolates the child's begin/commit/rollback lifecycle from the
	 * parent and prevents "Invalid nesting of transactions" errors when the child
	 * commits before the parent's turn ends.
	 */
	public void testDelegationGivesChildOwnTransactionScope() {
		TransactionScope parentScope = new TransactionScope();
		ToolContext parentContext = new ToolContext(
			SecurityContext.UNRESTRICTED, ApprovalCallback.AUTO_APPROVE, parentScope);

		AtomicReference<TransactionScope> childScopeCapture = new AtomicReference<>();

		// Minimal provider stub — the child's run() is overridden so the provider is never invoked.
		LLMProvider neverCalled = (request, handler) -> {
			throw new AssertionError("Provider should not be reached when run() is overridden.");
		};
		ToolCatalog emptyCatalog = new ToolCatalog();
		emptyCatalog.freeze();

		AgentLoop child = new AgentLoop(testAgentParams(), neverCalled, emptyCatalog) {
			@Override
			public AgentResult run(Conversation conv, ToolContext ctx, AgentEventListener listener) {
				childScopeCapture.set(ctx.transaction());
				return new AgentResult(StopReason.COMPLETE, 0, TokenUsage.EMPTY);
			}
		};

		DelegationTool tool = new DelegationTool(
			"delegate_to_x", "child", "x", "parent",
			(name, security) -> child);

		tool.execute(Map.of("task", "do the thing"), parentContext);

		assertNotNull("Child received a TransactionScope", childScopeCapture.get());
		assertNotSame(
			"Child must NOT receive parent's TransactionScope",
			parentScope, childScopeCapture.get());
	}

	private static AgentParams testAgentParams() {
		return new AgentParams("test-model", "You are a test assistant.",
			AgentParams.DEFAULT_MAX_TURNS, AgentParams.DEFAULT_MAX_OUTPUT_TOKENS);
	}
}
