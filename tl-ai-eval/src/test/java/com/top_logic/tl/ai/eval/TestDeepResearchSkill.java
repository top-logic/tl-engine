/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.AgentApplicationService;
import com.top_logic.tl.ai.core.agent.AgentPolicyService;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobStatus;
import com.top_logic.tl.ai.core.agent.subagent.SubagentProgress;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistries;
import com.top_logic.tl.ai.core.execution.AgentEnvironment;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.AgentRunner;
import com.top_logic.tl.ai.core.execution.ExecutionRecorder;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.execution.model.ResearchRun;
import com.top_logic.tl.ai.core.execution.model.ResearchSource;
import com.top_logic.tl.ai.core.execution.model.TlAiExecutionFactory;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.ToolChoice;
import com.top_logic.tl.ai.core.research.ResearchRuns;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.RunEntryPoint;
import com.top_logic.tl.ai.core.security.RunPrincipal;
import com.top_logic.tl.ai.core.security.RunSecurity;
import com.top_logic.tl.ai.core.session.Citation;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.tools.web.WebSearchHit;
import com.top_logic.tl.ai.core.tools.web.WebToolsService;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * Albert's {@code TL_deepResearch} skill with the shipped {@code TL_ResearchWorker}, run end to end
 * on a scripted model and the {@link FixtureWeb} (#723).
 *
 * <p>
 * What the model answers is scripted; everything those answers set in motion is the real thing: the
 * shipped agent definitions, the skill's tools, {@code spawn_subagent} starting one background run
 * per sub-question, the workers' web tools, the delivery of their results to the waiting lead, the
 * join of their reading lists into the lead's, and the citation check of the report. The shape this
 * pins is the one the skill prescribes: the lead loads the skill, starts its workers in one turn,
 * waits, lists the findings the workers recorded, never reads a page itself, keeps the run with its
 * report, opens it where it can, and answers with one report whose links point at the pages the
 * findings rest on (#794).
 * </p>
 *
 * <p>
 * Whether a real model follows the skill is not something a scripted model can say; that is what
 * the research eval with real models measures.
 * </p>
 */
public class TestDeepResearchSkill extends BasicTestCase {

	private static final String ALBERT = "TL_SingleAgent";

	private static final String WORKER = "TL_ResearchWorker";

	private static final String QUESTION = "Compare the heat pump subsidies of Germany, Austria and Denmark.";

	private static final String INVENTED = "https://invented.test/never-read";

	/** The plan the lead shows and keeps with the run. */
	private static final String PLAN = "1. Germany\n2. Austria\n3. Denmark";

	/** The id of the kept run in the answer of {@code saveReport}. */
	private static final Pattern KEPT = Pattern.compile("Kept research run (\\S+?): ");

	private static final List<Topic> TOPICS = List.of(
		new Topic("Germany", "heat pump subsidy Germany 2026", "https://bafa.de.test/heat-pumps",
			"Germany pays up to 70 percent of the cost of a heat pump."),
		new Topic("Austria", "heat pump subsidy Austria 2026", "https://kesseltausch.at.test/heat-pumps",
			"Austria pays up to 7500 euros for a heat pump."),
		new Topic("Denmark", "heat pump subsidy Denmark 2026", "https://ens.dk.test/heat-pumps",
			"Denmark pays a fixed subsidy for heat pumps that replace oil boilers."));

	/**
	 * One sub-question, the query its worker searches with, and the page it reads.
	 */
	private record Topic(String country, String query, String url, String fact) {

		String task() {
			return "Sub-question: What heat pump subsidy does " + country + " pay in 2026?\n"
				+ "Purpose: one section of the comparison.\nScope: 2026, " + country + ".\n"
				+ "Budget: at most 15 tool calls.";
		}

		String findings() {
			return "## Answer\n" + fact + "\n\n## Findings\n- F1: " + fact + " - [" + country + " subsidy page]("
				+ url + ")\n\n## Conflicts and gaps\n- None.";
		}
	}

	private String _registryKey;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// The account the run acts for, bound as a chat request binds it: the kept run's owner.
		TLContext.getContext().setPerson(PersonManager.getManager().getRoot());
		for (Topic topic : TOPICS) {
			FixtureWeb.search(topic.query(),
				new WebSearchHit(topic.country() + " subsidy page", topic.url(), topic.fact(), ""));
			FixtureWeb.page(topic.url(), topic.country() + " subsidy page", "# Subsidies\n\n" + topic.fact());
		}
	}

	@Override
	protected void tearDown() throws Exception {
		FixtureWeb.clear();
		if (_registryKey != null) {
			// The registries belong to the process, and the next suite's fresh database hands out the
			// same session ids: a registry left here would be found by the next test's run.
			SubagentRegistries.remove(_registryKey);
		}
		super.tearDown();
	}

	/**
	 * The whole run: the lead plans and fans out, every worker searches and reads, the lead reads no
	 * page itself, and its one report cites exactly the pages the workers read.
	 */
	public void testTheLeadDelegatesTheReadingAndCitesWhatTheWorkersRead() {
		ScriptedProvider.install(TestDeepResearchSkill::reply);
		Person root = PersonManager.getManager().getRoot();
		AgentSession session = AgentExecutionStore.getInstance().createSession(SessionKind.SCRIPT, root,
			"TestDeepResearchSkill");

		_registryKey = SubagentRegistries.conversationKey(ExecutionRecorder.conversationIdOf(session));
		Run run = run(root, session);

		assertEquals(StopReason.COMPLETE, run.stopReason());
		List<String> leadCalls = run.toolCalls();
		assertEquals("The lead loads the skill, starts one worker per sub-question in one turn, waits,"
			+ " lists the findings and keeps the run before it answers. Without a page there is no present.",
			List.of("loadSkill", "spawn_subagent", "spawn_subagent", "spawn_subagent", "wait_for_subagent",
				"listFindings", "saveReport"),
			leadCalls);
		String findings = run.result("listFindings");
		assertTrue("The lead sees every worker's finding: " + findings, findings.startsWith("3 findings:"));
		for (Topic topic : TOPICS) {
			assertTrue(findings, findings.contains(topic.url()) && findings.contains(topic.fact()));
		}

		SubagentJobRegistry registry = SubagentRegistries.find(_registryKey);
		assertNotNull("The conversation's registry ran the workers.", registry);
		List<SubagentJob> jobs = registry.byStatus(SubagentJobStatus.values());
		assertEquals("Spawn answers: " + run.conversation().messages(), TOPICS.size(), jobs.size());
		for (SubagentJob job : jobs) {
			assertEquals(WORKER, job.targetAgentName());
			assertEquals("Worker failed: " + job.subagentId(), SubagentJobStatus.DONE, job.status());
			assertEquals("Every worker searches, reads the page it found, and records what it found there.",
				List.of("webSearch", "webFetch", "recordFinding"), toolNames(job.progress()));
			for (SubagentProgress.Step step : job.progress().steps()) {
				assertEquals(step.toolName(), SubagentProgress.Outcome.OK, step.outcome());
			}
		}

		Set<String> cited = new TreeSet<>();
		for (Citation citation : run.citations()) {
			cited.add(citation.url());
		}
		Set<String> read = new TreeSet<>();
		for (Topic topic : TOPICS) {
			read.add(topic.url());
		}
		assertEquals("The report cites exactly the pages the workers read.", read, cited);
		assertEquals("The report is written once.", 1, run.reports());
		assertFalse("A link to a page nobody read keeps no address: " + run.response(),
			run.response().contains(INVENTED));
		assertTrue("The statement of the invented link stays as text: " + run.response(),
			run.response().contains("Sweden pays nothing"));
		assertTrue("The answer is the report: " + run.response(),
			run.response().startsWith("# Heat pump subsidies compared"));

		String kept = run.result("saveReport");
		Matcher id = KEPT.matcher(kept);
		assertTrue("The run is kept: " + kept, id.find());
		ResearchRun researchRun = newestResearchRun();
		assertEquals(QUESTION, researchRun.getQuestion());
		assertEquals(root, researchRun.getOwner());
		assertEquals("standard", researchRun.getEffort());
		assertEquals(PLAN, researchRun.getPlan());
		Set<String> keptSources = new TreeSet<>();
		for (ResearchSource source : researchRun.getCitedSources()) {
			keptSources.add(source.getUrl());
		}
		assertEquals("The kept run cites the pages the report cites.", read, keptSources);
		assertEquals("Every worker's finding is kept with it.", TOPICS.size(), researchRun.getFindings().size());
	}

	/** The research run kept last. */
	private static ResearchRun newestResearchRun() {
		HistoryUtils.updateSessionRevision();
		ResearchRun newest = null;
		for (TLObject object : MetaElementUtil.getAllInstancesOf(TlAiExecutionFactory.getResearchRunType(),
			TLObject.class)) {
			ResearchRun run = (ResearchRun) object;
			if (newest == null || !run.getFinished().before(newest.getFinished())) {
				newest = run;
			}
		}
		assertNotNull("A research run was kept.", newest);
		return newest;
	}

	/**
	 * The lead loads the skill, plans and spawns, waits, writes; a worker searches, reads, reports.
	 */
	private static List<ContentPart> reply(ProviderRequest request, int turn) {
		String prompt = String.valueOf(request.systemPrompt());
		if (prompt.startsWith("You are a research worker.")) {
			return worker(request, turn);
		}
		switch (turn) {
			case 0:
				return List.of(new TextContent("I will research this."),
					call("loadSkill", "{\"skillId\": \"TL_deepResearch\"}"));
			case 1: {
				List<ContentPart> parts = new ArrayList<>();
				parts.add(new TextContent("Effort: standard. Plan:\n1. Germany\n2. Austria\n3. Denmark\n"
					+ "The workers start now."));
				for (Topic topic : TOPICS) {
					parts.add(call("spawn_subagent",
						"{\"agent\": \"" + WORKER + "\", \"task\": " + json(topic.task()) + "}"));
				}
				return parts;
			}
			case 2:
				return List.of(call("wait_for_subagent", "{\"timeoutMs\": 120000}"));
			case 3:
				return List.of(call("listFindings", "{}"));
			case 4:
				return List.of(call("saveReport", "{\"question\": " + json(QUESTION) + ", \"report\": "
					+ json(report()) + ", \"effort\": \"standard\", \"plan\": " + json(PLAN) + "}"));
			default:
				return List.of(new TextContent(report()));
		}
	}

	/** The report the lead keeps and answers with. */
	private static String report() {
		StringBuilder report = new StringBuilder("# Heat pump subsidies compared\n\n");
		for (Topic topic : TOPICS) {
			report.append("## ").append(topic.country()).append("\n[").append(topic.fact())
				.append("](").append(topic.url()).append(" \"").append(topic.fact()).append("\")\n\n");
		}
		report.append("## Open points\n[Sweden pays nothing](").append(INVENTED).append(").\n");
		return report.toString();
	}

	/** The task of the worker that would search forever. */
	private static final String ENDLESS = "Sub-question: Find every heat pump subsidy of every country in the world.";

	/**
	 * A worker that would search forever ends at the budget its lead gave it, with its report, and
	 * the lead learns that it stopped there and says so (#775). A worker spawned without a budget
	 * gets the research worker's own limit.
	 */
	public void testAWorkerEndsAtItsBudgetWithAReport() {
		for (int n = 0; n < 10; n++) {
			FixtureWeb.search("heat pump subsidy country " + n,
				new WebSearchHit("Country " + n, "https://country" + n + ".test/", "A subsidy.", ""));
		}
		ScriptedProvider.install(TestDeepResearchSkill::budgetReply);
		Person root = PersonManager.getManager().getRoot();
		AgentSession session = AgentExecutionStore.getInstance().createSession(SessionKind.SCRIPT, root,
			"TestDeepResearchSkill-budget");
		_registryKey = SubagentRegistries.conversationKey(ExecutionRecorder.conversationIdOf(session));

		Run run = run(root, session);

		assertEquals(StopReason.COMPLETE, run.stopReason());
		SubagentJob endless = job(ENDLESS);
		assertEquals(SubagentJobStatus.DONE, endless.status());
		assertEquals(StopReason.MAX_TOOL_CALLS, endless.result().stopReason());
		assertEquals(3, endless.toolCallLimit());
		assertEquals("The worker reports in its last turn.", "## Answer\nNot all countries could be covered.",
			endless.result().finalText().substring(0, 45));
		String delivered = run.result("wait_for_subagent");
		assertTrue("The lead is told the worker stopped at its budget: " + delivered,
			delivered.contains("Stopped at its limit: it used up its budget of 3 tool calls"));
		assertTrue("The lead's report names the worker: " + run.response(),
			run.response().contains(endless.subagentId()));

		for (Turn turn : run.log().turns()) {
			for (Part part : turn.assistantParts()) {
				assertFalse("The worker's last-turn notice stays in the worker's transcript, out of the lead's.",
					part instanceof Part.SystemPart system
						&& system.eventType() == Part.SystemEventType.CONTINUATION_REQUESTED);
			}
		}

		SubagentJob germany = job(TOPICS.get(0).task());
		assertEquals("Without a budget of its own the research worker has its agent's limit.", 25,
			germany.toolCallLimit());
		assertEquals(StopReason.COMPLETE, germany.result().stopReason());
	}

	private SubagentJob job(String task) {
		return SubagentRegistries.find(_registryKey).byStatus(SubagentJobStatus.values()).stream()
			.filter(j -> j.taskBrief().contains(task.substring(0, 40))).findFirst().orElseThrow();
	}

	/** The lead of the budget scenario: one endless worker with a budget of 3, one ordinary worker. */
	private static List<ContentPart> budgetReply(ProviderRequest request, int turn) {
		String prompt = String.valueOf(request.systemPrompt());
		if (prompt.startsWith("You are a research worker.")) {
			String brief = String.valueOf(request.messages().get(0));
			if (!brief.contains(ENDLESS)) {
				return worker(request, turn);
			}
			if (request.toolChoice() == ToolChoice.NONE) {
				return List.of(new TextContent("## Answer\nNot all countries could be covered.\n\n## Findings\n-"
					+ " none\n\n## Conflicts and gaps\n- The budget ended the search."));
			}
			return List.of(call("webSearch", "{\"query\": \"heat pump subsidy country " + turn + "\"}"));
		}
		switch (turn) {
			case 0:
				return List.of(call("loadSkill", "{\"skillId\": \"TL_deepResearch\"}"));
			case 1:
				return List.of(
					call("spawn_subagent", "{\"agent\": \"" + WORKER + "\", \"task\": " + json(ENDLESS)
						+ ", \"toolCallBudget\": 3}"),
					call("spawn_subagent", "{\"agent\": \"" + WORKER + "\", \"task\": "
						+ json(TOPICS.get(0).task()) + "}"));
			case 2:
				return List.of(call("wait_for_subagent", "{\"timeoutMs\": 120000}"));
			default:
				String seen = String.valueOf(request.messages());
				int at = seen.lastIndexOf("_Stopped at its limit");
				String id = at < 0 ? "?" : seen.substring(seen.lastIndexOf("### Subagent ", at) + 13).split(" ")[0];
				return List.of(new TextContent("# Heat pump subsidies\n\n[" + TOPICS.get(0).fact() + "]("
					+ TOPICS.get(0).url() + ")\n\n## Open points\nWorker " + id
					+ " stopped at its budget; not every country is covered."));
		}
	}

	private static List<ContentPart> worker(ProviderRequest request, int turn) {
		String brief = String.valueOf(request.messages().get(0));
		Topic topic = TOPICS.stream().filter(t -> brief.contains(t.task().substring(0, 50)))
			.findFirst().orElseThrow(() -> new IllegalStateException("Unknown worker task: " + brief));
		switch (turn) {
			case 0:
				return List.of(call("webSearch", "{\"query\": " + json(topic.query()) + "}"));
			case 1:
				return List.of(call("webFetch", "{\"url\": " + json(topic.url()) + "}"));
			case 2:
				return List.of(call("recordFinding", "{\"claim\": " + json(topic.fact()) + ", \"source\": "
					+ json(topic.url()) + ", \"quote\": " + json(topic.fact()) + "}"));
			default:
				return List.of(new TextContent(topic.findings()));
		}
	}

	private record Run(StopReason stopReason, Conversation conversation, String response, ConversationLog log) {

		List<String> toolCalls() {
			List<String> result = new ArrayList<>();
			for (Message message : conversation.messages()) {
				if (message instanceof ToolResultMessage toolResult) {
					result.add(toolResult.toolName());
				}
			}
			return result;
		}

		String result(String tool) {
			for (Message message : conversation.messages()) {
				if (message instanceof ToolResultMessage toolResult && tool.equals(toolResult.toolName())
					&& toolResult.state() instanceof ToolState.Completed completed) {
					return completed.result();
				}
			}
			return "";
		}

		List<Citation> citations() {
			List<Citation> result = new ArrayList<>();
			for (Part.TextPart text : texts()) {
				result.addAll(text.citations());
			}
			return result;
		}

		int reports() {
			int result = 0;
			for (Part.TextPart text : texts()) {
				if (text.text() != null && text.text().startsWith("# ")) {
					result++;
				}
			}
			return result;
		}

		private List<Part.TextPart> texts() {
			List<Part.TextPart> result = new ArrayList<>();
			for (Turn turn : log.turns()) {
				for (Part part : turn.assistantParts()) {
					if (part instanceof Part.TextPart text) {
						result.add(text);
					}
				}
			}
			return result;
		}
	}

	private static Run run(Person root, AgentSession session) {
		CancellationToken cancellation = new CancellationToken();
		ToolContext context = new ToolContext(
			RunSecurity.contextFor(new RunPrincipal(root, RunEntryPoint.CHAT, false)),
			ApprovalCallback.AUTO_APPROVE, new TransactionScope(cancellation), cancellation);
		Conversation conversation = new Conversation();
		try (AgentEnvironment env = AgentEnvironment.openEphemeral()) {
			conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
				List.of(new TextContent(QUESTION))));
			ConversationLog log = env.logListener().log();
			log.startTurn().setUserParts(
				List.of(new Part.TextPart(UUID.randomUUID().toString(), Instant.now(), QUESTION)));
			AgentRunner.RecordedRun run = AgentRunner.run(ALBERT, conversation, null, context, event -> {
				// Only the outcome matters here.
			}, null, log, session, null);
			return new Run(run.result().stopReason(), conversation, String.valueOf(run.response()), log);
		} finally {
			context.transaction().close();
			HistoryUtils.updateSessionRevision();
		}
	}

	private static List<String> toolNames(SubagentProgress progress) {
		return progress.steps().stream().map(SubagentProgress.Step::toolName).toList();
	}

	private static ToolCall call(String tool, String arguments) {
		return new ToolCall("call-" + UUID.randomUUID(), tool, arguments);
	}

	private static String json(String text) {
		return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
	}

	/**
	 * The suite, on the services a chat request passes through, and the web tools.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestDeepResearchSkill.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ToolRegistryService.Module.INSTANCE,
				TopLogicAgentDefinitions.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				WebToolsService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE,
				ResearchRuns.Module.INSTANCE));
	}
}
