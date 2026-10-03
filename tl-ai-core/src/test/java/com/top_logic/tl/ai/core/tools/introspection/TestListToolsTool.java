/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.introspection;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * What {@code listTools} says about a tool that cannot act in the run it is asked in.
 *
 * <p>
 * The model is not offered such a tool (#649), so the listing must not call it callable: the model
 * asks {@code listTools} exactly when it doubts what it has, and an entry it would follow into a
 * refused call is the wrong answer to that doubt. It is not withheld from the user either - no grant
 * changes it - so it stands under a heading of its own, with the tool's reason.
 * </p>
 */
public class TestListToolsTool extends TestCase {

	private static final String CALLABLE = "## Own tools - callable right now";

	private static final String UNABLE = "## Own tools - carried by this agent, unable to act in this run";

	/** A tool that cannot act is listed apart from the callable ones, with its reason. */
	public void testAToolThatCannotActIsNotListedAsCallable() {
		Tool search = ToolStubs.tool("search", ToolMode.READ);
		Tool show = ToolStubs.builder("show")
			.unavailable(context -> "This page has no place to show anything.")
			.build();
		ListToolsTool listTools = new ListToolsTool("agent", List.of(search, show), List.of(), null);

		String listing = listTools.execute(Map.of(), new ToolContext(everything(), ApprovalCallback.AUTO_APPROVE))
			.text();

		String callable = section(listing, CALLABLE);
		assertTrue(listing, callable.contains("search"));
		assertFalse(listing, callable.contains("show"));
		assertTrue(listing, section(listing, UNABLE).contains("`show` - This page has no place to show anything."));
	}

	/** A run in which every tool can act has no such heading. */
	public void testWithoutSuchAToolThereIsNoHeading() {
		ListToolsTool listTools =
			new ListToolsTool("agent", List.of(ToolStubs.tool("search", ToolMode.READ)), List.of(), null);

		String listing = listTools.execute(Map.of(), new ToolContext(everything(), ApprovalCallback.AUTO_APPROVE))
			.text();

		assertFalse(listing, listing.contains(UNABLE));
	}

	/** The text below the given heading, up to the next one. */
	private static String section(String listing, String heading) {
		int start = listing.indexOf(heading);
		assertTrue("No section '" + heading + "' in:\n" + listing, start >= 0);
		int end = listing.indexOf("\n## ", start + heading.length());
		return listing.substring(start + heading.length(), end < 0 ? listing.length() : end);
	}

	/** A run whose principal may use every tool, so that only the run itself decides. */
	private static SecurityContext everything() {
		return new SecurityContext() {
			@Override
			public boolean hasToolPermission(Tool tool) {
				return true;
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.DEVELOPMENT;
			}
		};
	}
}
