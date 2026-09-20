/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.tools;

import junit.framework.TestCase;

import com.top_logic.tl.ai.tool.api.ToolResult;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * What an external MCP client is told about a tool call that this application ran.
 */
public class TestMcpToolAdapter extends TestCase {

	/**
	 * A tool that reports a failure is published with {@code isError: true}.
	 *
	 * <p>
	 * The four framework refusals - no authenticated caller, a denied tool, a pre-commit violation,
	 * a thrown exception - were flagged all along. A failure the tool itself returned was not, so it
	 * left this server as a successful call whose text happened to read like an error, and a client
	 * had nothing but the English to go on (Gitea #387).
	 * </p>
	 */
	public void testAReportedFailureIsPublishedAsAnError() {
		McpSchema.CallToolResult published =
			McpToolAdapter.published(ToolResult.failure("No ticket with that number."));

		assertEquals(Boolean.TRUE, published.isError());
		assertEquals("No ticket with that number.", text(published));
	}

	/** A successful answer is published unflagged, exactly as before. */
	public void testASuccessfulAnswerIsPublishedUnflagged() {
		McpSchema.CallToolResult published = McpToolAdapter.published(ToolResult.of("Ticket 7 is open."));

		assertFalse("Nothing failed here: " + published.isError(),
			Boolean.TRUE.equals(published.isError()));
		assertEquals("Ticket 7 is open.", text(published));
	}

	private static String text(McpSchema.CallToolResult result) {
		assertEquals("One text block is expected.", 1, result.content().size());
		return ((McpSchema.TextContent) result.content().get(0)).text();
	}
}
