/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.mcp;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.PermissionDecision;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolPermissionFilter;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMode;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * Tests that every tool of an external MCP server is classified {@link ToolMode#EXTERNAL},
 * whatever the server declares about it.
 *
 * <p>
 * The stub MCP server here is the {@link io.modelcontextprotocol.spec.McpSchema.Tool} record
 * itself - verbatim what a server returns from {@code tools/list} - handed to the same
 * {@link McpServerRegistry#createTool} the connected registry uses. A definition built without
 * annotations is therefore exactly the wire situation of a server that annotates nothing.
 * </p>
 *
 * @see McpTool#mode()
 */
public class TestMcpToolMode extends TestCase {

	/** A server that says nothing about its tool. */
	public void testNoAnnotationsIsExternal() {
		assertExternal(definition("createIssue", null));
	}

	/** An {@code annotations} object with every hint absent. */
	public void testEmptyAnnotationsIsExternal() {
		assertExternal(definition("createIssue",
			new McpSchema.ToolAnnotations(null, null, null, null, null, null)));
	}

	/**
	 * {@code readOnlyHint: true} does not relax anything.
	 *
	 * <p>
	 * The case the whole design turns on. A server's hints are its claims about itself, and the MCP
	 * specification tells clients not to base trust decisions on them. If they were read, a hostile
	 * or merely careless server would obtain auto-approval for every one of its tools by sending
	 * five characters.
	 * </p>
	 */
	public void testReadOnlyHintIsStillExternal() {
		assertExternal(definition("searchIssues",
			new McpSchema.ToolAnnotations(null, Boolean.TRUE, Boolean.FALSE, null, null, null)));
	}

	/** {@code destructiveHint: true} does not escalate anything either. */
	public void testDestructiveHintIsStillExternal() {
		assertExternal(definition("deleteProject",
			new McpSchema.ToolAnnotations(null, Boolean.FALSE, Boolean.TRUE, null, null, null)));
	}

	/** A self-contradictory declaration is not a special case, because none of it is read. */
	public void testContradictoryHintsAreStillExternal() {
		assertExternal(definition("mystery",
			new McpSchema.ToolAnnotations(null, Boolean.TRUE, Boolean.TRUE, null, null, null)));
	}

	/**
	 * Every combination of the two hints resolves to the same mode.
	 *
	 * <p>
	 * Stated as one assertion over the whole space rather than case by case, because the property is
	 * that the hints have <em>no</em> influence - which a finite list of examples only illustrates.
	 * </p>
	 */
	public void testHintsHaveNoInfluence() {
		Boolean[] values = { null, Boolean.TRUE, Boolean.FALSE };
		for (Boolean readOnly : values) {
			for (Boolean destructive : values) {
				McpTool tool = McpServerRegistry.createTool("jira", definition("t",
					new McpSchema.ToolAnnotations(null, readOnly, destructive, null, null, null)),
					null);
				assertEquals("readOnlyHint=" + readOnly + ", destructiveHint=" + destructive,
					ToolMode.EXTERNAL, tool.mode());
			}
		}
	}

	/** The extracted construction path still carries name, description and schema through. */
	public void testWrappedToolIdentity() {
		McpSchema.Tool definition = new McpSchema.Tool("createIssue", null, "Files an issue",
			new McpSchema.JsonSchema("object", Map.of("title", Map.of("type", "string")),
				List.of("title"), null, null, null),
			null, null, null);

		McpTool tool = McpServerRegistry.createTool("jira", definition, null);

		assertEquals("mcp__jira__createIssue", tool.name());
		assertEquals("Files an issue", tool.description());
		assertEquals("mcp__jira__createIssue", tool.schema().name());
		assertTrue("Expected the declared property in " + tool.schema().inputSchemaJson(),
			tool.schema().inputSchemaJson().contains("\"title\""));
	}

	/** A server that sends no description at all yields the empty string, not {@code null}. */
	public void testMissingDescription() {
		McpSchema.Tool definition = new McpSchema.Tool("bare", null, null,
			new McpSchema.JsonSchema("object", Map.of(), List.of(), null, null, null),
			null, null, null);

		McpTool tool = McpServerRegistry.createTool("jira", definition, null);

		assertEquals("", tool.description());
		assertEquals(ToolMode.EXTERNAL, tool.mode());
	}

	/**
	 * The mode, and every gate that reads it.
	 *
	 * <p>
	 * Asserted together because the mode exists only in order to reach these gates: it has to be
	 * {@code EXTERNAL}, that has to produce an approval prompt, it must not be excluded by the
	 * execution-mode filter in production, and it must not join a concurrent batch.
	 * </p>
	 */
	private static void assertExternal(McpSchema.Tool definition) {
		McpTool tool = McpServerRegistry.createTool("jira", definition, null);

		assertEquals(ToolMode.EXTERNAL, tool.mode());
		assertFalse("An external call is put in front of a human, so it cannot share a batch.",
			tool.isConcurrencySafe());

		ToolCall call = new ToolCall("tc1", tool.name(), "{}");
		PermissionDecision decision = permissive().checkApproval(tool, call);
		assertTrue("Expected NeedsApproval but got " + decision.getClass().getSimpleName(),
			decision instanceof PermissionDecision.NeedsApproval);

		assertTrue("An external tool is not what the production ban is about.",
			new ToolPermissionFilter(permissive()).test(tool));
	}

	/**
	 * A context that grants every tool and reports the most restricted execution mode, so that
	 * nothing but the mode can be the reason a gate answers as it does.
	 */
	private static SecurityContext permissive() {
		return new SecurityContext() {
			@Override
			public boolean hasToolPermission(Tool tool) {
				return true;
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.PRODUCTION;
			}
		};
	}

	private static McpSchema.Tool definition(String name, McpSchema.ToolAnnotations annotations) {
		return new McpSchema.Tool(name, null, "Does something",
			new McpSchema.JsonSchema("object", Map.of(), List.of(), null, null, null),
			null, annotations, null);
	}
}
