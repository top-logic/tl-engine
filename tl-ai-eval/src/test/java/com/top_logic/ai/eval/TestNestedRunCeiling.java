/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.eval;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.ai.tool.api.Tool;
import com.top_logic.ai.tool.api.ToolGroups;
import com.top_logic.ai.tool.api.ToolSchema;
import com.top_logic.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.ai.core.agent.AgentApplicationService;
import com.top_logic.ai.core.agent.AgentPolicyService;
import com.top_logic.ai.core.agent.AgentRuntimeService;
import com.top_logic.ai.core.agent.ToolContext;
import com.top_logic.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.ai.core.execution.AgentEnvironment;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.ai.core.execution.AgentRunner;
import com.top_logic.ai.core.execution.model.AgentSession;
import com.top_logic.ai.core.message.Conversation;
import com.top_logic.ai.core.message.Message;
import com.top_logic.ai.core.message.TextContent;
import com.top_logic.ai.core.message.ToolCall;
import com.top_logic.ai.core.message.UserMessage;
import com.top_logic.ai.core.provider.LLMProviderService;
import com.top_logic.ai.core.provider.ProviderRequest;
import com.top_logic.ai.core.provider.ScriptedModel.Reply;
import com.top_logic.ai.core.provider.ScriptedProvider;
import com.top_logic.ai.core.security.AiToolAccessService;
import com.top_logic.ai.core.security.ToolAccessPolicy;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * TL-Script that a tool of a background agent run executes is bounded by the
 * {@code <background-allowlist>} when it starts a nested run with {@code aiCallTool},
 * {@code aiAgentChat} or {@code aiChat} with tools.
 *
 * <p>
 * The background run is a real agent loop, started as an MCP request starts one
 * ({@code ToolContext.forBackgroundRun}), on the scripted model. Its model calls {@code changeData}
 * with a script that starts the nested run. For the test, the allowlist of the eval also holds
 * {@code script} and {@code data.write}, so that {@code changeData} may run the script;
 * {@code agent.meta} and {@code workspace} stay off it.
 * </p>
 */
public class TestNestedRunCeiling extends BasicTestCase {

	private static final String OUTER_AGENT = "Eval_Scripted";

	/** A framework agent whose own tool list has {@code workspace} tools. */
	private static final String NESTED_AGENT = "TL_Orchestrator";

	private static final String NESTED_TASK = "NESTED-TASK: list the open tasks.";

	private static final String NESTED_CHAT = "NESTED-CHAT: list the framework agents.";

	/** In {@code agent.meta}, off the allowlist. Its answer names {@link #NESTED_AGENT}. */
	private static final String AGENT_META_TOOL = "framework-wiring.listFrameworkAgents";

	/** The name the model calls {@link #AGENT_META_TOOL} by. */
	private static final String AGENT_META_TOOL_NAME = "listFrameworkAgents";

	/** What each refusal by the ceiling says. */
	private static final String ALLOWLIST = "background allowlist";

	/** In {@code workspace}, off the allowlist, on the tool list of {@link #NESTED_AGENT}. */
	private static final String WORKSPACE_TOOL = "task.listTasks";

	private static volatile String _outerScript;

	private static final List<String> OUTER_TOOL_RESULTS = new CopyOnWriteArrayList<>();

	private static final List<String> NESTED_OFFERED = new CopyOnWriteArrayList<>();

	private static final List<String> NESTED_TOOL_RESULTS = new CopyOnWriteArrayList<>();

	private final List<ToolAccessPolicy.Grant> _added = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		OUTER_TOOL_RESULTS.clear();
		NESTED_OFFERED.clear();
		NESTED_TOOL_RESULTS.clear();
		ScriptedProvider.install(TestNestedRunCeiling::reply);
		List<ToolAccessPolicy.Grant> allowlist = AiToolAccessService.policy().getBackgroundAllowlist().getGrants();
		for (String group : List.of(ToolGroups.SCRIPT, ToolGroups.DATA_WRITE)) {
			ToolAccessPolicy.Grant grant = TypedConfiguration.newConfigItem(ToolAccessPolicy.Grant.class);
			grant.setGroup(group);
			allowlist.add(grant);
			_added.add(grant);
		}
		ModuleUtil.INSTANCE.restart(AiToolAccessService.Module.INSTANCE, null);
	}

	@Override
	protected void tearDown() throws Exception {
		AiToolAccessService.policy().getBackgroundAllowlist().getGrants().removeAll(_added);
		_added.clear();
		ModuleUtil.INSTANCE.restart(AiToolAccessService.Module.INSTANCE, null);
		super.tearDown();
	}

	/** {@code aiCallTool} in a script of a background run is refused a tool off the allowlist. */
	public void testAiCallToolInsideABackgroundRun() throws Throwable {
		Person root = PersonManager.getManager().getRoot();
		assertRefusedInBackground(root, AGENT_META_TOOL);

		_outerScript = "aiCallTool(\"" + AGENT_META_TOOL + "\", null)";
		runInBackground(root);

		String outer = String.valueOf(OUTER_TOOL_RESULTS);
		assertFalse("The nested call must not run the agent.meta tool: " + outer, outer.contains(NESTED_AGENT));
		assertTrue("changeData must run the script, and the nested call must be refused: " + outer,
			outer.contains("Error invoking tool '" + AGENT_META_TOOL + "'"));
		assertTrue("The refusal must name the allowlist: " + outer, outer.contains(ALLOWLIST));
	}

	/**
	 * {@code aiAgentChat} in a script of a background run starts a background run: its agent is not
	 * offered the tools off the allowlist, and a call of one is refused.
	 */
	public void testAiAgentChatInsideABackgroundRun() throws Throwable {
		Person root = PersonManager.getManager().getRoot();
		assertRefusedInBackground(root, WORKSPACE_TOOL);

		_outerScript = "aiAgentChat(\"" + NESTED_AGENT + "\", \"" + NESTED_TASK + "\")";
		runInBackground(root);

		assertFalse("The nested run must not be offered the workspace tool: " + NESTED_OFFERED,
			NESTED_OFFERED.contains("listTasks"));
		String nested = String.valueOf(NESTED_TOOL_RESULTS);
		assertTrue("The nested run must be refused the workspace tool, naming the allowlist: " + nested,
			nested.contains(ALLOWLIST));
	}

	/** {@code aiChat} with tools in a script of a background run is refused a tool off the allowlist. */
	public void testAiChatWithToolsInsideABackgroundRun() throws Throwable {
		Person root = PersonManager.getManager().getRoot();
		assertRefusedInBackground(root, AGENT_META_TOOL);

		_outerScript = "aiChat([\"" + NESTED_CHAT + "\"], null, null, [\"" + AGENT_META_TOOL + "\"])";
		runInBackground(root);

		assertFalse("The nested chat must not be offered the agent.meta tool: " + NESTED_OFFERED,
			NESTED_OFFERED.contains(AGENT_META_TOOL_NAME));
		String nested = String.valueOf(NESTED_TOOL_RESULTS);
		assertFalse("The nested chat must not run the agent.meta tool: " + nested, nested.contains(NESTED_AGENT));
		assertTrue("The refusal must name the allowlist: " + nested, nested.contains(ALLOWLIST));
	}

	/** The background run itself is refused the given tool, so a nested run must be too. */
	private static void assertRefusedInBackground(Person account, String toolName) {
		String direct = ToolContext.forBackgroundRun(account).security().toolDenialReason(tool(toolName));
		assertNotNull("Precondition: the background run itself is refused " + toolName + ".", direct);
		assertTrue(direct, direct.contains(ALLOWLIST));
	}

	/** Runs the outer agent as an MCP request runs it: unattended, auto-approving, as root. */
	private static void runInBackground(Person root) throws Throwable {
		AgentSession session =
			AgentExecutionStore.getInstance().createSession(SessionKind.BACKGROUND, root, "TestNestedRunCeiling");
		TLContextManager.<Void, Throwable, RuntimeException> inPersonContext(root, () -> {
			ToolContext context = ToolContext.forBackgroundRun(root);
			Conversation conversation = new Conversation();
			try (AgentEnvironment env = AgentEnvironment.openEphemeral()) {
				conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
					List.of(new TextContent("Run the script."))));
				AgentRunner.RecordedRun run = AgentRunner.run(OUTER_AGENT, conversation, null, context, event -> {
					// Only the tool results matter here.
				}, null, env.logListener().log(), session, null);
				log("background run ended: " + run.result().stopReason() + " " + run.result().errorMessage());
			} finally {
				context.transaction().close();
				HistoryUtils.updateSessionRevision();
			}
			return null;
		});
	}

	private static Reply reply(ProviderRequest request, int turn) {
		List<Message> messages = request.messages();
		String last = String.valueOf(messages.get(messages.size() - 1));
		String first = String.valueOf(messages.get(0));
		if (first.contains("NESTED-TASK")) {
			return nestedReply(request, turn, "listTasks", last);
		}
		if (first.contains("NESTED-CHAT")) {
			return nestedReply(request, turn, AGENT_META_TOOL_NAME, last);
		}
		if (turn == 0) {
			return Reply.of(new ToolCall("call-" + UUID.randomUUID(), "changeData",
				"{\"script\": \"" + _outerScript.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}"));
		}
		OUTER_TOOL_RESULTS.add(last);
		return Reply.text("Outer run done.");
	}

	/**
	 * The model of a nested run: it notes the tools it is offered, then calls the given tool whether
	 * it was offered or not, as a model that guesses a tool name does.
	 */
	private static Reply nestedReply(ProviderRequest request, int turn, String toolName, String last) {
		if (turn == 0) {
			for (ToolSchema schema : request.tools()) {
				NESTED_OFFERED.add(schema.name());
			}
			return Reply.of(new ToolCall("call-" + UUID.randomUUID(), toolName, "{}"));
		}
		NESTED_TOOL_RESULTS.add(last);
		return Reply.text("Nested run done.");
	}

	private static Tool tool(String name) {
		Tool tool = ToolRegistryService.getInstance().getTool(name);
		assertNotNull("The tool '" + name + "' must be registered.", tool);
		return tool;
	}

	private static void log(String line) {
		System.out.println("[TestNestedRunCeiling] " + line);
	}

	/**
	 * The suite, on the services a run passes through.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestNestedRunCeiling.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ToolRegistryService.Module.INSTANCE,
				TopLogicAgentDefinitions.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE));
	}
}
