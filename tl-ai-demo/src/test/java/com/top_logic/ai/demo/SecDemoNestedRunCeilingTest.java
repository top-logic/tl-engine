/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import com.top_logic.ai.mcp.server.authentication.McpCallerContext;
import com.top_logic.ai.mcp.server.tools.ExposedTool;
import com.top_logic.ai.mcp.server.tools.McpExposure;
import com.top_logic.ai.mcp.server.tools.McpToolAdapter;
import com.top_logic.ai.tool.api.ToolGroups;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.ai.core.agent.TopLogicSecurityContext;
import com.top_logic.ai.core.scripting.AIScriptFunctions;
import com.top_logic.ai.core.security.AiToolAccessService;
import com.top_logic.ai.core.security.ToolAccessPolicy;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * TL-Script that a tool of a background run executes calls {@code aiCallTool} in a background run of
 * the same account: the {@code <background-allowlist>} bounds the nested call as it bounds the run.
 *
 * <p>
 * The carrier is {@code script-exec.changeData}, the shipped tool that runs a script with an effect.
 * Its real run needs {@code data.write}, which the demo policy keeps off the allowlist, so the tests of
 * a background run add it for their duration. The nested call asks for a tool of {@code agent.meta},
 * which is off every allowlist used here. The account is an administrator, whose roles bound nothing:
 * the allowlist is the only limit.
 * </p>
 */
public class SecDemoNestedRunCeilingTest extends SecDemoPersonaTest {

	/** In {@code agent.meta}, granted to nobody and off every allowlist used here. */
	private static final String AGENT_META_READ_TOOL = "script-tool-authoring.listScriptToolFactories";

	/** A script that calls {@link #AGENT_META_READ_TOOL} and returns its answer. */
	private static final String NESTED_CALL = "aiCallTool(\"" + AGENT_META_READ_TOOL + "\", null)";

	/** A part of the answer of {@link #AGENT_META_READ_TOOL}: the name of a script tool factory. */
	private static final String TOOL_ANSWER = "secdemo-reporting";

	/** What each refusal by the ceiling says. */
	private static final String ALLOWLIST = "background allowlist";

	/**
	 * In a background run, the nested call is refused the tool that the allowlist withholds from the
	 * run, and the ceiling ends with the tool call that executed the script.
	 */
	public void testANestedCallInABackgroundRunIsBoundedByTheAllowlist() throws Exception {
		Person administrator = anyAdministrator();
		actAs(administrator);
		withDataWriteOnTheAllowlist(() -> {
			TopLogicSecurityContext background = TopLogicSecurityContext.forRun(administrator, true);
			String direct = background.toolDenialReason(tool(AGENT_META_READ_TOOL));
			assertNotNull("Precondition: the background run itself is refused the agent.meta tool.", direct);

			assertRefusedByTheAllowlist(changeData(background, NESTED_CALL, false));

			String outside = AIScriptFunctions.callTool(AGENT_META_READ_TOOL, null);
			assertTrue("After the tool call, a script outside a run reaches the tool as before: " + outside,
				outside.contains(TOOL_ANSWER));
		});
	}

	/**
	 * An MCP client that calls {@code changeData} directly starts a background run, too: the MCP
	 * adapter runs the tool without the agent loop.
	 */
	public void testANestedCallOverMcpIsBoundedByTheAllowlist() throws Exception {
		Person administrator = anyAdministrator();
		actAs(administrator);
		withDataWriteOnTheAllowlist(() -> {
			ExposedTool entry = TypedConfiguration.newConfigItem(ExposedTool.class);
			entry.setTool(SCRIPT_TOOL);
			List<McpExposure.Published> published = McpExposure.resolve(List.of(entry));
			assertEquals("The live registry publishes " + SCRIPT_TOOL, 1, published.size());
			Map<String, Object> args = new HashMap<>();
			args.put("script", NESTED_CALL);

			McpSchema.CallToolResult result = McpCallerContext.withCaller(administrator,
				() -> McpToolAdapter.adapt(published.get(0)).callHandler()
					.apply(null, McpSchema.CallToolRequest.builder(SCRIPT_TOOL).arguments(args).build()));

			assertRefusedByTheAllowlist(((McpSchema.TextContent) result.content().get(0)).text());
		});
	}

	/** In a run with a person who can approve, the nested call reaches the tool as before. */
	public void testANestedCallInAnAttendedRunReachesTheTool() {
		Person administrator = anyAdministrator();
		actAs(administrator);

		String nested = changeData(TopLogicSecurityContext.forRun(administrator, false), NESTED_CALL, false);

		assertTrue("The nested call of an attended run is bounded by the roles only: " + nested,
			nested.contains(TOOL_ANSWER));
	}

	private static void assertRefusedByTheAllowlist(String result) {
		assertFalse("The nested call must not run the agent.meta tool: " + result, result.contains(TOOL_ANSWER));
		assertTrue("The refusal must name the allowlist: " + result, result.contains(ALLOWLIST));
	}

	/**
	 * Runs the given test with {@code data.write} on the background allowlist, so that a background
	 * run may execute a script with {@code changeData}.
	 */
	private static void withDataWriteOnTheAllowlist(Runnable test) throws Exception {
		ToolAccessPolicy.Grant dataWrite = TypedConfiguration.newConfigItem(ToolAccessPolicy.Grant.class);
		dataWrite.setGroup(ToolGroups.DATA_WRITE);
		List<ToolAccessPolicy.Grant> allowlist = AiToolAccessService.policy().getBackgroundAllowlist().getGrants();
		allowlist.add(dataWrite);
		ModuleUtil.INSTANCE.restart(AiToolAccessService.Module.INSTANCE, null);
		try {
			test.run();
		} finally {
			allowlist.remove(dataWrite);
			ModuleUtil.INSTANCE.restart(AiToolAccessService.Module.INSTANCE, null);
		}
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoNestedRunCeilingTest.class);
	}

}
