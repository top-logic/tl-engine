/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.eval;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.ai.tool.api.Tool;
import com.top_logic.ai.tool.api.ToolGroups;
import com.top_logic.ai.tool.api.ToolMode;
import com.top_logic.ai.tool.api.ToolSchema;
import com.top_logic.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.ai.core.agent.AgentApplicationService;
import com.top_logic.ai.core.agent.AgentPolicyService;
import com.top_logic.ai.core.agent.AgentRuntimeService;
import com.top_logic.ai.core.agent.SecurityContext;
import com.top_logic.ai.core.agent.ToolContext;
import com.top_logic.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.ai.core.agent.subagent.SubagentWorkers;
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
import com.top_logic.ai.core.security.SecurityContextProxy;
import com.top_logic.ai.core.security.ToolAccessPolicy;
import com.top_logic.ai.core.tools.ToolExecutions;
import com.top_logic.ai.core.tools.registry.ScriptToolFactory;
import com.top_logic.ai.core.tools.registry.ScriptToolFactory.ScriptToolConfig;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * TL-Script that a background agent run executes is bounded by the {@code <background-allowlist>}
 * when it starts a nested run with {@code aiCallTool}, {@code aiAgentChat} or {@code aiChat} with
 * tools: the script of a tool, and a fact of the agent of the run.
 *
 * <p>
 * The background run is a real agent loop, started as an MCP request starts one
 * ({@code ToolContext.forBackgroundRun}), on the scripted model. Its model calls {@code changeData}
 * with a script that starts the nested run. For the test, the allowlist of the eval also holds
 * {@code script} and {@code data.write}, so that {@code changeData} may run the script;
 * {@code agent.meta} and {@code workspace} stay off it. The agent {@code Eval_Facts} of the test
 * application has a fact that calls a tool of {@code agent.meta}.
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

	/** An agent whose fact calls {@link #AGENT_META_TOOL}. */
	private static final String FACTS_AGENT = "Eval_Facts";

	/** How the environment block of a run of {@link #FACTS_AGENT} starts the line of its fact. */
	private static final String FACT_LINE = "- Framework agents:";

	/** An agent that spawns a subagent. */
	private static final String LEAD_AGENT = "Eval_Lead";

	/** What the run that {@code aiAgentChatAsync} starts is asked. */
	private static final String ASYNC_TASK = "ASYNC-TASK: create a ticket.";

	/** A script that changes the application. */
	private static final String CREATE_TICKET = "new(`pm:Ticket`)..set(`pm:Ticket#title`, \"Nested async ticket\")";

	/** The refusal of the limit of {@link NoWrites}. */
	private static final String NO_WRITES = "This run may not change the application.";

	private static volatile String _outerScript;

	private static final BlockingQueue<String> ASYNC_TOOL_RESULTS = new LinkedBlockingQueue<>();

	private static final List<String> FACTS = new CopyOnWriteArrayList<>();

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
		FACTS.clear();
		ASYNC_TOOL_RESULTS.clear();
		ScriptedProvider.install(TestNestedRunCeiling::reply);
		// The saved policy, not the running one: a restart reads the saved one, and in a suite that
		// shares its services with other suites the running service can still carry an older item.
		List<ToolAccessPolicy.Grant> allowlist = AiToolAccessService.savedPolicy().getBackgroundAllowlist().getGrants();
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
		AiToolAccessService.savedPolicy().getBackgroundAllowlist().getGrants().removeAll(_added);
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
		assertTrue("The nested run must be refused the workspace tool, naming the allowlist: " + nested
			+ ", outer run: " + OUTER_TOOL_RESULTS, nested.contains(ALLOWLIST));
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
		assertTrue("The refusal must name the allowlist: " + nested + ", outer run: " + OUTER_TOOL_RESULTS,
			nested.contains(ALLOWLIST));
	}

	/**
	 * The facts of the agent of a background run are computed while the run is built, before any tool
	 * call: a fact that calls {@code aiCallTool} is refused a tool off the allowlist, too.
	 */
	public void testAFactOfABackgroundRunIsBoundedByTheAllowlist() throws Throwable {
		Person root = PersonManager.getManager().getRoot();
		assertRefusedInBackground(root, AGENT_META_TOOL);

		run(root, FACTS_AGENT, () -> ToolContext.forBackgroundRun(root));

		String fact = theFact();
		assertFalse("The fact must not run the agent.meta tool: " + fact, fact.contains(NESTED_AGENT));
		assertTrue("The refusal must name the allowlist: " + fact, fact.contains(ALLOWLIST));
	}

	/** In a run that a person can approve, the fact reaches the tool as before. */
	public void testAFactOfAnAttendedRunReachesTheTool() throws Throwable {
		Person root = PersonManager.getManager().getRoot();

		run(root, FACTS_AGENT, ToolContext::forCurrentUser);

		String fact = theFact();
		assertTrue("The fact of an attended run is bounded by the roles only: " + fact, fact.contains(NESTED_AGENT));
	}

	/**
	 * A detached subagent is a background run, also when the run that spawned it is attended: the
	 * facts of its agent are refused a tool off the allowlist.
	 */
	public void testAFactOfADetachedSubagentIsBoundedByTheAllowlist() throws Throwable {
		Person root = PersonManager.getManager().getRoot();
		ScriptedProvider.install(TestNestedRunCeiling::spawningReply);

		run(root, LEAD_AGENT, ToolContext::forCurrentUser);

		String fact = theFact();
		assertFalse("The fact of the subagent must not run the agent.meta tool: " + fact, fact.contains(NESTED_AGENT));
		assertTrue("The refusal must name the allowlist: " + fact, fact.contains(ALLOWLIST));
	}

	/**
	 * {@code aiAgentChatAsync} in the script of a run starts a detached run with every limit of that
	 * run: a run that may not change the application, as an A2A run with allow-writes="false", starts
	 * no run that may.
	 *
	 * <p>
	 * The script is the one of a script tool that only reads, which such a run may call. The allowlist
	 * of the test holds {@code script} and {@code data.write}, so only the limit of the outer run can
	 * refuse the write of the detached run.
	 * </p>
	 */
	public void testAiAgentChatAsyncKeepsTheLimitsOfTheRun() throws Throwable {
		Person root = PersonManager.getManager().getRoot();
		Tool reading = readingScriptTool("aiAgentChatAsync(\"" + OUTER_AGENT + "\", \"" + ASYNC_TASK + "\")");

		TLContextManager.<Void, Throwable, RuntimeException> inPersonContext(root, () -> {
			SecurityContext noWrites = new NoWrites(ToolContext.forBackgroundRun(root).security());
			String started = ToolExecutions.execute(reading, Map.of(),
				new ToolContext(noWrites, ApprovalCallback.AUTO_APPROVE)).text();
			log("aiAgentChatAsync answered: " + started);
			return null;
		});

		String result = ASYNC_TOOL_RESULTS.poll(60, TimeUnit.SECONDS);
		assertNotNull("The detached run must call changeData and end.", result);
		assertFalse("The detached run must not change the application: " + result, result.contains("Done"));
		assertTrue("The limit of the outer run must refuse the write: " + result, result.contains(NO_WRITES));
	}

	/** A script tool that only reads, as an application configures one, with the given script. */
	private static Tool readingScriptTool(String script) throws ConfigurationException {
		ScriptToolConfig tool = TypedConfiguration.newConfigItem(ScriptToolConfig.class);
		tool.setName("startAsync");
		tool.setImplementation(ExprFormat.INSTANCE.getValue(ScriptToolConfig.IMPLEMENTATION, script));
		tool.update(tool.descriptor().getProperty(ScriptToolConfig.MODE), ToolMode.READ);
		ScriptToolFactory.Config factory = TypedConfiguration.newConfigItem(ScriptToolFactory.Config.class);
		factory.getTools().put(tool.getName(), tool);
		return new ScriptToolFactory(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, factory)
			.createTools().get(0);
	}

	/**
	 * The limit of an A2A run with allow-writes="false": no tool that changes the application. A
	 * proxy of the same kind as the guard of the A2A module, which this module does not see.
	 */
	private static final class NoWrites extends SecurityContextProxy {

		NoWrites(SecurityContext impl) {
			super(impl);
		}

		@Override
		protected SecurityContext copy(SecurityContext narrowed) {
			return new NoWrites(narrowed);
		}

		@Override
		public boolean hasToolPermission(Tool tool) {
			return !tool.mode().writesApplicationState() && super.hasToolPermission(tool);
		}

		@Override
		public String toolDenialReason(Tool tool) {
			return tool.mode().writesApplicationState() ? NO_WRITES : super.toolDenialReason(tool);
		}
	}

	/** The fact that the one run of {@link #FACTS_AGENT} was told. */
	private static String theFact() {
		assertEquals("The agent with the fact must have run once: " + FACTS, 1, FACTS.size());
		return FACTS.get(0);
	}

	/** The background run itself is refused the given tool, so a nested run must be too. */
	private static void assertRefusedInBackground(Person account, String toolName) {
		String direct = ToolContext.forBackgroundRun(account).security().toolDenialReason(tool(toolName));
		assertNotNull("Precondition: the background run itself is refused " + toolName + ".", direct);
		assertTrue(direct, direct.contains(ALLOWLIST));
	}

	/** Runs the outer agent as an MCP request runs it: unattended, auto-approving, as root. */
	private static void runInBackground(Person root) throws Throwable {
		run(root, OUTER_AGENT, () -> ToolContext.forBackgroundRun(root));
	}

	/** Runs the given agent as root, in the context the given function builds in the context of root. */
	private static void run(Person root, String agent, Supplier<ToolContext> newContext) throws Throwable {
		AgentSession session =
			AgentExecutionStore.getInstance().createSession(SessionKind.BACKGROUND, root, "TestNestedRunCeiling");
		TLContextManager.<Void, Throwable, RuntimeException> inPersonContext(root, () -> {
			ToolContext context = newContext.get();
			Conversation conversation = new Conversation();
			try (AgentEnvironment env = AgentEnvironment.openEphemeral()) {
				conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
					List.of(new TextContent("Run the script."))));
				AgentRunner.RecordedRun run = AgentRunner.run(agent, conversation, null, context, event -> {
					// Only the tool results matter here.
				}, null, env.logListener().log(), session, null);
				log(agent + " ended: " + run.result().stopReason() + " " + run.result().errorMessage());
			} finally {
				context.transaction().close();
				HistoryUtils.updateSessionRevision();
			}
			return null;
		});
	}

	private static Reply reply(ProviderRequest request, int turn) {
		if (hasFact(request)) {
			return factsReply(request);
		}
		List<Message> messages = request.messages();
		String last = String.valueOf(messages.get(messages.size() - 1));
		String first = String.valueOf(messages.get(0));
		if (first.contains("NESTED-TASK")) {
			return nestedReply(request, turn, "listTasks", last);
		}
		if (first.contains("NESTED-CHAT")) {
			return nestedReply(request, turn, AGENT_META_TOOL_NAME, last);
		}
		if (first.contains("ASYNC-TASK")) {
			if (turn == 0) {
				return Reply.of(new ToolCall("call-" + UUID.randomUUID(), "changeData",
					"{\"script\": \"" + CREATE_TICKET.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}"));
			}
			ASYNC_TOOL_RESULTS.add(last);
			return Reply.text("Async run done.");
		}
		if (turn == 0) {
			return Reply.of(new ToolCall("call-" + UUID.randomUUID(), "changeData",
				"{\"script\": \"" + _outerScript.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}"));
		}
		OUTER_TOOL_RESULTS.add(last);
		return Reply.text("Outer run done.");
	}

	/**
	 * The model of a lead that spawns a subagent of {@link #FACTS_AGENT}, waits for it and ends; and
	 * of that subagent.
	 */
	private static Reply spawningReply(ProviderRequest request, int turn) {
		if (hasFact(request)) {
			return factsReply(request);
		}
		switch (turn) {
			case 0:
				return Reply.of(new ToolCall("call-" + UUID.randomUUID(), "spawn_subagent",
					"{\"agent\": \"" + FACTS_AGENT + "\", \"task\": \"Say what you were told.\"}"));
			case 1:
				return Reply.of(new ToolCall("call-" + UUID.randomUUID(), "wait_for_subagent", "{}"));
			default:
				return Reply.text("Lead done.");
		}
	}

	/** Whether the request is one of a run of {@link #FACTS_AGENT}, whose environment block states its fact. */
	private static boolean hasFact(ProviderRequest request) {
		return String.valueOf(request.systemPrompt()).contains(FACT_LINE);
	}

	/** The model of a run of {@link #FACTS_AGENT}: it notes the line of its fact and ends. */
	private static Reply factsReply(ProviderRequest request) {
		String prompt = request.systemPrompt();
		int start = prompt.indexOf(FACT_LINE);
		int end = prompt.indexOf('\n', start);
		FACTS.add(prompt.substring(start, end < 0 ? prompt.length() : end));
		return Reply.text("Facts noted.");
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
				AgentPolicyService.Module.INSTANCE,
				SubagentWorkers.Module.INSTANCE,
				SchedulerService.Module.INSTANCE));
	}
}
