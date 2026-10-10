/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.citation.ArchivedPageServlet;
import com.top_logic.tl.ai.core.citation.CitationValidator;
import com.top_logic.tl.ai.core.citation.CitationKinds;
import com.top_logic.tl.ai.core.citation.PageArchive;
import com.top_logic.tl.ai.core.citation.ReadingList;
import com.top_logic.tl.ai.core.citation.ReadingListBinding;
import com.top_logic.tl.ai.core.execution.AgentRunner;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.StreamHandler;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.session.Citation;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.model.InstanceBrowserTool;
import com.top_logic.tl.ai.core.tools.script.QueryTool;
import com.top_logic.tl.ai.core.tools.web.FilteringProxy.Target;
import com.top_logic.tl.ai.core.tools.web.RecordingServer.Reply;
import com.top_logic.tl.ai.tool.api.ReadSource;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * An answer cites what its run read without a citation tool, and a cited web page can still be
 * looked at after it changed (#617).
 *
 * <p>
 * The real loop, the real runner, the real read tools - {@code getInstance} on three objects of the
 * knowledge base, {@code webFetch} on a page a local fixture server serves - and the real archive
 * and its route. Only the model is scripted: it reads, then writes its answer with links, and calls
 * nothing to cite.
 * </p>
 */
public class TestCitationsFromReadingList extends BasicTestCase {

	/** A globally routable address, so the fetch's SSRF check has nothing to refuse. */
	private static final byte[] PUBLIC = { 93, (byte) 184, (byte) 216, 34 };

	private static final String PAGE = "http://fixture.invalid/sommerfest";

	private static final String BEFORE = "Das Sommerfest findet am 4. Juli im Stadtpark statt.";

	private static final String AFTER = "Das Sommerfest ist abgesagt.";

	private static Person anna;

	private static Person ben;

	private static Person carla;

	private static ServletRunner runner;

	/** What the fixture server answers at the moment. */
	private final AtomicReference<String> _live = new AtomicReference<>(BEFORE);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("anna-who-reads");
			ben = TestPerson.createPerson("ben-who-does-not");
			carla = TestPerson.createPerson("carla-the-third");
			runner = new ServletRunner();
			runner.registerServlet("archived-page/*", ArchivedPageServlet.class.getName());
		}
		TLContext.getContext().setPerson(anna);
		_live.set(BEFORE);
	}

	/**
	 * The acceptance case: an answer resting on three objects and a web page needs no citation call
	 * and shows exactly these four sources - the objects by their ids, the page by its address and
	 * its archived copy. A link to an object nobody read loses its link.
	 */
	public void testThreeObjectsAndAPageAreCitedWithoutACiteCall() throws Exception {
		List<String> ids = List.of(ReadFilter.idOf(anna), ReadFilter.idOf(ben), ReadFilter.idOf(carla));
		String answer = "[Anna](tl:" + ids.get(0) + "), [Ben](tl:" + ids.get(1) + ") und [Carla](tl:" + ids.get(2)
			+ ") kommen; das Fest ist [im Stadtpark](" + PAGE + "). [Dora](tl:Person:does-not-exist) fehlt.";

		try (RecordingServer origin = startOrigin(); FilteringProxy egress = egressTo(origin)) {
			ConversationLog log = new ConversationLog();
			Conversation conversation = run(egress, log, (request, turn) -> turn == 0
				? List.of(new TextContent("Ich lese nach."),
					new ToolCall("c1", "getInstance", "{\"tids\": [\"" + String.join("\", \"", ids) + "\"]}"),
					new ToolCall("c2", "webFetch", "{\"url\": \"" + PAGE + "\"}"))
				: List.of(new TextContent(answer)));

			assertEquals("The run read with two tools and cited with none.", List.of("getInstance", "webFetch"),
				toolsCalled(conversation));
			Turn turn = log.turns().get(log.turns().size() - 1);
			Part.TextPart text = lastText(turn);
			assertEquals("Anna[1], Ben[2] und Carla[3] kommen; das Fest ist im Stadtpark[4]. Dora fehlt.",
				text.text());
			List<Citation> citations = text.citations();
			List<String> cited = new ArrayList<>(ids);
			cited.add(PAGE);
			assertEquals(cited, citations.stream().map(Citation::id).toList());
			assertEquals(CitationKinds.OBJECT, citations.get(0).kind());
			assertEquals("The label is the object's own.", MetaLabelProvider.INSTANCE.getLabel(anna),
				citations.get(0).label());
			Citation page = citations.get(3);
			assertEquals(CitationKinds.WEB, page.kind());
			assertEquals(PAGE, page.url());
			assertTrue("The page's footnote offers its archived copy: " + page.archiveUrl(),
				page.archiveUrl().contains(PageArchive.URL_PREFIX));
			assertTrue("Everything read is on the transcript's reading list.",
				turn.sources().stream().map(ReadSource::id).toList().containsAll(cited));
		}
	}

	/**
	 * An object a {@code query} answered with is a source as well: the query renders its result
	 * itself - {@code label [type:...] [id:...]} - and that rendering puts it on the reading list.
	 * Found in the live check, where a model linked the events a query had listed and every link was
	 * dropped.
	 */
	public void testAnObjectAQueryListedIsCitable() throws Exception {
		String id = ReadFilter.idOf(anna);
		try (RecordingServer origin = startOrigin(); FilteringProxy egress = egressTo(origin)) {
			ConversationLog log = new ConversationLog();
			Conversation conversation = run(egress, log, (request, turn) -> turn == 0
				? List.of(new ToolCall("q1", "query",
					"{\"expression\": \"all(`tl.accounts:Person`)\"}"))
				: List.of(new TextContent("Es kommt [Anna](tl:" + id + ").")));

			Part.TextPart text = lastText(log.turns().get(log.turns().size() - 1));
			assertEquals("The query answered: " + toolResults(conversation), "Es kommt Anna[1].", text.text());
			assertEquals(id, text.citations().get(0).id());
		}
	}

	/**
	 * The page changed after the agent read it: its citation's archived copy still shows what the
	 * agent read, to the account whose run read it and to nobody else.
	 */
	public void testTheArchivedCopyShowsThePageAsItWasRead() throws Exception {
		try (RecordingServer origin = startOrigin(); FilteringProxy egress = egressTo(origin)) {
			WebFetchTool fetch = new WebFetchTool(publicAnswer(), egress);
			ReadingList first = new ReadingList();
			try (ReadingListBinding.Binding bound = ReadingListBinding.bind(first)) {
				assertTrue(fetch.webFetch(PAGE, null).contains(BEFORE));
			}
			String archived = first.find(PAGE).archiveUrl();

			_live.set(AFTER);
			ReadingList second = new ReadingList();
			try (ReadingListBinding.Binding bound = ReadingListBinding.bind(second)) {
				assertTrue("The live page has changed.", fetch.webFetch(PAGE, null).contains(AFTER));
			}

			Recording owner = get(handleOf(archived), anna);
			assertEquals(HttpServletResponse.SC_OK, owner._status);
			assertTrue("The copy shows the page as it was read: " + owner.body(), owner.body().contains(BEFORE));
			assertFalse("not as it is now", owner.body().contains(AFTER));
			assertTrue("and says where it came from", owner.body().contains(PAGE));
			assertTrue("The newer reading has a copy of its own.",
				get(handleOf(second.find(PAGE).archiveUrl()), anna).body().contains(AFTER));

			assertEquals("Another account does not get it.", HttpServletResponse.SC_NOT_FOUND,
				get(handleOf(archived), ben)._status);
		}
	}

	/** A long report, read in windows: 60 pages of some 5,000 characters, about 300,000 in all. */
	private static final String REPORT = "http://fixture.invalid/annual-report.pdf";

	/**
	 * A run that reads a long report through to its end can quote its last page: the reading list
	 * holds every window read, and the archived copy shows them - one copy, extended as the run reads
	 * on. Both used to stop at the first 200,000 characters, so the end of a long report was neither
	 * checkable nor archived.
	 */
	public void testTheLastPageOfALongReportReadThroughIsCitable() throws Exception {
		try (RecordingServer origin = serveReport(); FilteringProxy egress = egressTo(origin)) {
			WebFetchTool fetch = new WebFetchTool(publicAnswer(), egress);
			ReadingList list = new ReadingList();
			List<String> copies = new ArrayList<>();
			try (ReadingListBinding.Binding bound = ReadingListBinding.bind(list)) {
				Integer offset = null;
				do {
					String answer = fetch.webFetch(REPORT, offset);
					copies.add(list.find(REPORT).archiveUrl());
					offset = nextOffset(answer);
				} while (offset != null);
			}

			ReadSource report = list.find(REPORT);
			assertTrue("more than the old bound was read: " + report.text().length(),
				report.text().length() > 200_000);
			assertEquals("one copy, extended window by window", 1, copies.stream().distinct().count());
			String lastPage = finding(60);
			CitationValidator.Result cited = CitationValidator.validate(
				"Es steht [im Bericht](" + REPORT + " \"" + lastPage + "\").", list, List.of());
			assertEquals("the last page is citable by its words", lastPage, cited.citations().get(0).passage());
			Recording copy = get(handleOf(report.archiveUrl()), anna);
			assertEquals(HttpServletResponse.SC_OK, copy._status);
			assertTrue("and the archived copy shows it", copy.body().contains(lastPage));
		}
	}

	/**
	 * A run that read the first window and the last one has not read the middle: a passage from it
	 * is not shown as the report's words, and the copy marks the part that was left out.
	 */
	public void testAPartThatWasNotReadIsNotCitable() throws Exception {
		try (RecordingServer origin = serveReport(); FilteringProxy egress = egressTo(origin)) {
			WebFetchTool fetch = new WebFetchTool(publicAnswer(), egress);
			ReadingList list = new ReadingList();
			try (ReadingListBinding.Binding bound = ReadingListBinding.bind(list)) {
				String first = fetch.webFetch(REPORT, null);
				int length = totalLength(first);
				fetch.webFetch(REPORT, length - 8_000);
			}

			ReadSource report = list.find(REPORT);
			assertTrue(report.text().contains(finding(1)));
			assertTrue(report.text().contains(finding(60)));
			assertFalse(report.text().contains(finding(30)));
			assertTrue("the gap is marked", report.text().contains("[...]"));
			CitationValidator.Result cited = CitationValidator.validate(
				"Es steht [im Bericht](" + REPORT + " \"" + finding(30) + "\").", list, List.of());
			assertEquals("a page the run did not read is not quoted", "", cited.citations().get(0).passage());
		}
	}

	private static RecordingServer serveReport() throws java.io.IOException {
		String filler = " The committee reviewed the figures of the quarter and found them consistent with the plan.";
		byte[] pdf = FixturePdf.of(60, page -> finding(page) + filler.repeat(50));
		return RecordingServer.start(target -> Reply.binary("application/pdf", pdf));
	}

	/** The one sentence of a report page that is only on that page. */
	private static String finding(int page) {
		return "Finding " + page + ": the measured output of plant " + page + " was " + (1000 + page * 7) + " MWh.";
	}

	/** The offset a {@code webFetch} answer names for reading on, or {@code null} at the end. */
	private static Integer nextOffset(String answer) {
		java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("offset=(\\d+)\\.\\]$").matcher(answer);
		return matcher.find() ? Integer.valueOf(matcher.group(1)) : null;
	}

	/** The length of the whole document a {@code webFetch} answer names. */
	private static int totalLength(String answer) {
		java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(" to \\d+ of (\\d+)").matcher(answer);
		assertTrue(answer, matcher.find());
		return Integer.parseInt(matcher.group(1));
	}

	/** Runs a scripted model over the two read tools, recording into {@code log}. */
	private Conversation run(FilteringProxy egress, ConversationLog log, Script script) {
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(new InstanceBrowserTool());
		catalog.register(new QueryTool());
		catalog.register(new WebFetchTool(publicAnswer(), egress));
		catalog.freeze();
		AgentLoop loop = new AgentLoop(new AgentParams("test-model", "You cite with links.", 5,
			AgentParams.DEFAULT_MAX_OUTPUT_TOKENS), new ScriptedModel(script), catalog);

		String question = "Wer kommt zum Sommerfest, und wo ist es?";
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(), List.of(new TextContent(question))));
		log.startTurn().setUserParts(List.of(new Part.TextPart(UUID.randomUUID().toString(), Instant.now(), question)));
		AgentRunner.run(loop, "citing-agent", conversation, ToolContext.unrestricted(), event -> {
			// Only the outcome matters.
		}, log, null, null);
		return conversation;
	}

	private RecordingServer startOrigin() {
		return RecordingServer.start(target -> Reply.html("<html><head><title>Sommerfest 2026</title></head>"
			+ "<body><h1>Sommerfest</h1><p>" + _live.get() + "</p></body></html>"));
	}

	private static FilteringProxy egressTo(RecordingServer origin) {
		return FilteringProxy.start(host -> null,
			(host, port) -> Target.to(new InetSocketAddress(InetAddress.getLoopbackAddress(), origin.port())));
	}

	private static UrlSafety.NameLookup publicAnswer() {
		return name -> new InetAddress[] { InetAddress.getByAddress(name, PUBLIC) };
	}

	private static List<String> toolsCalled(Conversation conversation) {
		List<String> result = new ArrayList<>();
		for (Message message : conversation.messages()) {
			if (message instanceof ToolResultMessage toolResult) {
				result.add(toolResult.toolName());
			}
		}
		return result;
	}

	private static String toolResults(Conversation conversation) {
		StringBuilder result = new StringBuilder();
		for (Message message : conversation.messages()) {
			if (message instanceof ToolResultMessage toolResult) {
				result.append(toolResult.toolName()).append(": ").append(toolResult.state()).append('\n');
			}
		}
		return result.toString();
	}

	private static Part.TextPart lastText(Turn turn) {
		Part.TextPart result = null;
		for (Part part : turn.assistantParts()) {
			if (part instanceof Part.TextPart text) {
				result = text;
			}
		}
		assertNotNull("The turn has an answer.", result);
		return result;
	}

	private static String handleOf(String archiveUrl) {
		return archiveUrl.substring(archiveUrl.indexOf(PageArchive.URL_PREFIX) + PageArchive.URL_PREFIX.length());
	}

	private static Recording get(String handle, Person caller) throws Exception {
		Recording response = new Recording();
		InvocationContext invocation =
			runner.newClient().newInvocation(new GetMethodWebRequest("http://localhost/archived-page/" + handle));
		HttpServletRequest request = invocation.getRequest();
		ThreadContext.pushSuperUser();
		try {
			SessionService.getInstance().loginUser(request, response.asResponse(), caller);
		} finally {
			ThreadContext.popSuperUser();
		}
		new Probe().get(request, response.asResponse());
		return response;
	}

	/** Exposes the servlet's own dispatch to the test. */
	static final class Probe extends ArchivedPageServlet {

		void get(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			doGet(req, resp);
		}
	}

	/** What the servlet answered. */
	static final class Recording {

		int _status = HttpServletResponse.SC_OK;

		private final StringWriter _text = new StringWriter();

		String body() {
			return _text.toString();
		}

		HttpServletResponse asResponse() {
			return (HttpServletResponse) Proxy.newProxyInstance(HttpServletResponse.class.getClassLoader(),
				new Class<?>[] { HttpServletResponse.class }, (proxy, method, args) -> {
					switch (method.getName()) {
						case "sendError":
						case "setStatus":
							_status = ((Integer) args[0]).intValue();
							return null;
						case "getWriter":
							return new PrintWriter(_text);
						default:
							return null;
					}
				});
		}
	}

	/** What the scripted model answers in a turn. */
	@FunctionalInterface
	interface Script {
		List<ContentPart> reply(ProviderRequest request, int turn);
	}

	/** A model whose answers the test writes, tool calls included. */
	private static final class ScriptedModel implements LLMProvider {

		private final Script _script;

		private int _turn;

		ScriptedModel(Script script) {
			_script = script;
		}

		@Override
		public void stream(ProviderRequest request, StreamHandler handler) {
			List<ContentPart> content = _script.reply(request, _turn++);
			handler.onEvent(new StreamEvent.StreamStart());
			boolean calls = false;
			for (int i = 0; i < content.size(); i++) {
				ContentPart part = content.get(i);
				if (part instanceof TextContent text) {
					handler.onEvent(new StreamEvent.TextStart(i));
					handler.onEvent(new StreamEvent.TextDelta(i, text.text()));
					handler.onEvent(new StreamEvent.TextEnd(i));
				} else if (part instanceof ToolCall call) {
					calls = true;
					handler.onEvent(new StreamEvent.ToolCallStart(i, call.id(), call.name()));
					handler.onEvent(new StreamEvent.ToolCallDelta(i, call.arguments()));
					handler.onEvent(new StreamEvent.ToolCallEnd(i));
				}
			}
			handler.onEvent(new StreamEvent.MessageComplete("scripted-" + _turn, request.model(),
				calls ? "tool_use" : "end_turn", TokenUsage.EMPTY, content));
		}
	}

	/**
	 * The services an agent run and the archive's route use: accounts and login, the model, the
	 * archive and the declared kinds.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestCitationsFromReadingList.class),
				ModelService.Module.INSTANCE,
				PageArchive.Module.INSTANCE,
				CitationKinds.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE));
	}
}
