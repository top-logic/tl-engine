/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.tools.ChatPresentTool;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.tools.present.ChartKind;
import com.top_logic.tl.ai.core.tools.present.ChartSpec;
import com.top_logic.tl.ai.core.tools.present.PresentAs;
import com.top_logic.tl.ai.core.tools.present.Presentation;
import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * What the {@code present} tool does with a page, and what it refuses.
 *
 * <p>
 * The test drives the real path: a real sub-session, a window registered with
 * {@link AgentViewRefreshService} the way a rendering chat registers one, and a channel opened
 * through {@link ChatViewBinding} the way {@code <chat-conversation present="…">} opens one. What
 * stands in for the browser is the {@link ReactWindowRegistry} - the tool only ever asks it for an
 * interaction - and what stands in for the page is a listener on the channel.
 * </p>
 *
 * <p>
 * The cases live next to {@link ChatViewBinding} rather than next to {@link ChatPresentTool}, for the
 * reason {@code TestChatViewBinding} does: registering a channel without a live window needs the
 * binding's package-visible {@code bind}.
 * </p>
 */
public class TestPresentTool extends BasicTestCase {

	/** The channel the fixture's page offers. */
	private static final String PANE = "paneObject";

	/**
	 * A query a model would write. That a query finds the objects it names is the eval's to show -
	 * it runs against data committed the way an application's is (tl-ai-eval, show-scenarios); here
	 * it is only the shape of what comes back that counts.
	 */
	private static final String PERSONS = "all(`tl.accounts:Person`)";

	private static Person user;

	private static TLObject first;

	private static TLObject second;

	private ChatPresentTool _tool;

	private ViewChannel _channel;

	private List<Object> _written;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (user == null) {
			user = TestPerson.createPerson("presenter");
			// Any persisted object will do: what is under test is the identifier, the rights check
			// and the write, none of which know what an invoice is.
			first = ChatHistoryService.createSession("First", user);
			second = ChatHistoryService.createSession("Second", user);
			// The store commits on its writer thread; the ids resolve at this thread's revision.
			HistoryUtils.updateSessionRevision();
		}
		_tool = new ChatPresentTool();
		_written = new ArrayList<>();
		_channel = new DefaultViewChannel(PANE);
		_channel.addListener((sender, oldValue, newValue) -> _written.add(newValue));
	}

	@Override
	protected void tearDown() throws Exception {
		ChatViewBinding.bind(subSession(), windowName(), "/app", "main").denyPresenting(PANE, _channel);
		super.tearDown();
	}

	/**
	 * The tool is offered to a run only where it can show something (#649): a run without a window
	 * is not offered it, nor is a run on a page that names no channel - its model answers a request
	 * to see objects in text, instead of with a call that is refused.
	 */
	public void testTheToolIsOfferedOnlyWhereThePageHasAPlace() {
		Tool tool = toolOf(_tool);
		ToolContext run = context(ModelSecurity.UNCHECKED);

		assertSays("No page is attached", tool.unavailableReason(run));

		withWindow();
		assertSays("offers no place", tool.unavailableReason(run));

		allow();
		assertNull("A page that names a channel is offered the tool.", tool.unavailableReason(run));
	}

	/**
	 * A page's channel counts while the conversation that names it is displayed (#649): a page of
	 * the shell the user left, a classic tab that is not selected, the React page a browser tab
	 * showed before the classic UI - each is detached, offers nothing, and is not where a run puts a
	 * table nobody sees.
	 */
	public void testAChannelCountsWhileItsConversationIsDisplayed() throws Exception {
		ReactWindowRegistry registry = new ReactWindowRegistry("test-session");
		String window = "displayedWindow";
		DefaultViewContext view = new DefaultViewContext(
			new DefaultReactContext("/app", window, registry.getOrCreateQueue(window), registry));
		view.registerChannel(PANE, _channel);
		ChatConversationControl conversation = new ChatConversationControl(view, (ChatConversationElement.Config)
			TypedConfiguration.parse(CharacterContents.newContent(
				"<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
					+ " config:interface='" + ChatConversationElement.Config.class.getName() + "'"
					+ " present='" + PANE + "'/>")));
		Tool tool = toolOf(_tool);
		ToolContext run = context(ModelSecurity.UNCHECKED);
		try {
			assertEquals("Built but not displayed yet.", Set.of(), ChatViewBinding.presentableNames(subSession()));
			assertSays("offers no place", tool.unavailableReason(run));

			conversation.attach();
			assertEquals(Set.of(PANE), ChatViewBinding.presentableNames(subSession()));
			assertNull(tool.unavailableReason(run));

			conversation.detach();
			assertEquals("Left, not gone.", Set.of(), ChatViewBinding.presentableNames(subSession()));
			assertSays("offers no place", tool.unavailableReason(run));

			conversation.attach();
			assertEquals("Displayed again.", Set.of(PANE), ChatViewBinding.presentableNames(subSession()));
		} finally {
			conversation.cleanupTree();
		}
		assertEquals("Gone.", Set.of(), ChatViewBinding.presentableNames(subSession()));
	}

	/** A run with no browser window has no page, and says so rather than blaming the allow list. */
	public void testWithoutAWindowNothingIsPresented() {
		allow();

		String result = present(List.of(tid(first)), PANE);

		assertSays("No page is attached", result);
		assertEquals("Nothing reached the channel.", List.of(), _written);
	}

	/**
	 * The model need not know the channel's name: a page that offers one place is written without
	 * one (#616), which is what makes "show me" a single call.
	 */
	public void testAPageWithOnePlaceNeedsNoChannelName() {
		withWindow();
		allow();

		assertSays("The user now sees the form", present(List.of(tid(first)), "  "));
		assertEquals(List.of(first), _written);
	}

	/** A page that offers several places makes the model choose, and names them. */
	public void testAPageWithSeveralPlacesNeedsAChannelName() {
		withWindow();
		allow();
		ViewChannel other = new DefaultViewChannel("dockPane");
		ChatViewBinding.bind(subSession(), windowName(), "/app", "main").allowPresenting("dockPane", other);
		try {
			String result = present(List.of(tid(first)), "");

			assertSays("several places", result);
			assertSays(PANE, result);
			assertSays("dockPane", result);
			assertEquals(List.of(), _written);
		} finally {
			ChatViewBinding.bind(subSession(), windowName(), "/app", "main").denyPresenting("dockPane", other);
		}
	}

	/** A call that names neither a query nor an object is answered before anything is resolved. */
	public void testWithoutIdsOrQueryNothingHappens() {
		withWindow();
		allow();

		assertSays("nothing to show", present(List.of(), PANE));
		assertSays("nothing to show", present(null, PANE));
		assertEquals(List.of(), _written);
	}

	/** Deny by default: a page that opened no channel is not writable, window or no window. */
	public void testAPageThatOpensNothingIsNotWritable() {
		withWindow();

		String result = present(List.of(tid(first)), PANE);

		assertSays("offers no place", result);
		assertEquals(List.of(), _written);
	}

	/** A channel the page did not name is refused, and the refusal says which ones exist. */
	public void testAnotherChannelIsRefused() {
		withWindow();
		allow();

		String result = present(List.of(tid(first)), "secretChannel");

		assertSays("cannot be written here", result);
		assertSays(PANE, result);
		assertEquals(List.of(), _written);
	}

	/**
	 * An object the acting user may not read is refused - and is refused exactly like one that does
	 * not exist, because saying which of the two it is discloses that it exists.
	 */
	public void testAnObjectTheUserMayNotSeeIsRefused() {
		withWindow();
		allow();

		String result = _tool.present(null, List.of(tid(first)), null, null, null, PANE, context(new NoReadSecurity()));

		assertSays("Nothing was presented", result);
		assertSays("not found", result);
		assertEquals(List.of(), _written);
	}

	/** An id that names nothing refuses the call the same way. */
	public void testAnUnknownIdIsRefused() {
		withWindow();
		allow();

		assertSays("Nothing was presented", present(List.of("GenericObject:no-such-object"), PANE));
		assertEquals(List.of(), _written);
	}

	/** All or nothing: one bad id among good ones presents none of them. */
	public void testOneBadIdRefusesTheWholeCall() {
		withWindow();
		allow();

		String result = present(List.of(tid(first), "GenericObject:no-such-object"), PANE);

		assertSays("Nothing was presented", result);
		assertEquals("A page showing half of what was asked for would be a page that lies.",
			List.of(), _written);
	}

	/**
	 * One object stands on the channel as itself - what every element reading "the open object"
	 * understands - and the answer names it the way every tool does.
	 */
	public void testOneObjectReachesTheChannel() {
		withWindow();
		allow();

		String result = present(List.of(tid(first)), PANE);

		assertSays("The user now sees the form", result);
		assertSays("[id:" + tid(first) + "]", result);
		assertEquals(List.of(first), _written);
		assertEquals(first, _channel.get());
	}

	/** Several stand on it as a table, in the order the model named them. */
	public void testSeveralObjectsReachTheChannelAsATable() {
		withWindow();
		allow();

		String result = present(List.of(tid(second), tid(first)), PANE);

		assertSays("a table of 2 objects", result);
		Presentation shown = (Presentation) _channel.get();
		assertEquals(PresentAs.TABLE, shown.as());
		assertEquals(List.of(second, first), shown.objects());
	}

	/** A query that fails, or finds something other than objects, says so and shows nothing. */
	public void testABrokenQueryIsAnsweredWithItsError() {
		withWindow();
		allow();

		assertSays("the query failed", present("all(`no.such:Type`)"));
		assertSays("the query returned a number", present(PERSONS + ".size()"));
		assertSays("either 'query' or 'ids'", _tool.present(PERSONS, List.of(tid(first)), null, null, null, "",
			context(ModelSecurity.UNCHECKED)));
		assertEquals(List.of(), _written);
	}

	/** A form shows one object, and a request for a form of several is refused, not guessed at. */
	public void testAFormOfSeveralIsRefused() {
		withWindow();
		allow();

		assertSays("a FORM shows exactly one object", _tool.present(null, List.of(tid(first), tid(second)),
			PresentAs.FORM, null, null, "", context(ModelSecurity.UNCHECKED)));
		assertEquals(List.of(), _written);
	}

	/**
	 * A chart's functions are tried on every object before anything is shown, so a mistake is the
	 * model's to correct rather than an empty chart in front of the user.
	 */
	public void testAChartIsCheckedBeforeItIsShown() {
		withWindow();
		allow();

		String label = "s -> \"row\"";
		assertSays("a CHART needs 'chart'", chart(null));
		assertSays("value function does not compile", chart(new ChartSpec(ChartKind.BAR, label, "s -> $s.(")));
		assertSays("which is not a number", chart(new ChartSpec(ChartKind.BAR, label, "s -> \"x\"")));
		assertEquals(List.of(), _written);

		String result = chart(new ChartSpec(ChartKind.BAR, label, "s -> 7"));

		assertSays("a bar chart of", result);
		assertSays("row: 7 [id:" + tid(first) + "]", result);
		Presentation shown = (Presentation) _channel.get();
		assertEquals(PresentAs.CHART, shown.as());
	}

	/** A point of a chart of objects opens its object, which is what its dataset's metadata says. */
	public void testAChartOfObjectsOpensItsPoints() {
		withWindow();
		allow();

		chart(new ChartSpec(ChartKind.BAR, "s -> \"row\"", "s -> 7"));

		Presentation shown = (Presentation) _channel.get();
		Map<String, Object> config = PaneCharts.config(shown.chartData(), "open");
		Map<?, ?> dataset = (Map<?, ?>) ((List<?>) ((Map<?, ?>) config.get("data")).get("datasets")).get(0);
		assertEquals("open", dataset.get("onClick"));
		assertEquals(List.of(first, second), dataset.get("metadata"));
		assertEquals(List.of("row", "row"), shown.chartData().labels());
	}

	/**
	 * "How many per group" is one call (#655): the query computes the numbers, and they stand on the
	 * channel as a chart of data - labels and values, no object behind a point.
	 */
	public void testACountPerGroupIsOneChart() {
		withWindow();
		allow();

		String result = present("{\"Open\": 3, \"Closed\": 5}");

		assertSays("The user now sees a bar chart of 2 labels:", result);
		assertSays("Open: 3\nClosed: 5", result);
		Presentation shown = (Presentation) _channel.get();
		assertTrue(shown.isData());
		assertEquals(PresentAs.CHART, shown.as());
		assertEquals(List.of("Open", "Closed"), shown.chartData().labels());
		assertEquals(List.of(), shown.objects());

		String grouped = present(PERSONS + ".groupBy(p -> \"all\", g -> $g.size())");
		assertSays("a bar chart of 1 label:", grouped);
		assertSays("all: ", grouped);
	}

	/** Several numbers per label are several series, and a table shows them as columns. */
	public void testSeveralSeriesAsATable() {
		withWindow();
		allow();

		String result = _tool.present("list({\"label\": \"Jan\", \"planned\": 5, \"actual\": 3}, "
			+ "{\"label\": \"Feb\", \"planned\": 4, \"actual\": 6})", null, PresentAs.TABLE, null,
			"Plan und Ist", "", context(ModelSecurity.UNCHECKED));

		assertSays("The user now sees a table of 2 labels in 2 series:", result);
		assertSays("Jan: planned 5, actual 3\nFeb: planned 4, actual 6", result);
		Presentation shown = (Presentation) _channel.get();
		assertEquals(PresentAs.TABLE, shown.as());
		assertEquals("Plan und Ist", shown.title());
	}

	/**
	 * A query may return a whole Chart.js configuration, as a chart component's script does - which
	 * is what draws a chart no short form can say, a line over stacked bars.
	 */
	public void testAWholeChartConfigurationIsShown() {
		withWindow();
		allow();

		String result = present("{'type': 'bar', 'data': {'labels': list('Q1', 'Q2'), 'datasets': list("
			+ "{'label': 'A', 'data': list(1, 2), 'stack': 's'}, {'label': 'B', 'data': list(3, 4), 'stack': 's'},"
			+ " {'label': 'Total', 'data': list(4, 6), 'type': 'line'})},"
			+ " 'options': {'scales': {'x': {'stacked': true}, 'y': {'stacked': true}}}}");

		assertSays("a bar chart of 2 labels in 3 series:", result);
		assertSays("Q1: A 1, B 3, Total 4", result);
		Map<String, Object> config = ((Presentation) _channel.get()).chartData().config();
		assertEquals(Map.of("x", Map.of("stacked", true), "y", Map.of("stacked", true)),
			((Map<?, ?>) config.get("options")).get("scales"));
	}

	/** Data that cannot be shown as asked is refused with a sentence the model can act on. */
	public void testDataIsRefusedWhereItCannotBeShown() {
		withWindow();
		allow();
		ToolContext run = context(ModelSecurity.UNCHECKED);

		assertSays("a FORM shows one object", _tool.present("{\"a\": 1}", null, PresentAs.FORM, null, null, "", run));
		assertSays("'label' and 'value' are functions of one object", _tool.present("{\"a\": 1}", null,
			PresentAs.CHART, new ChartSpec(ChartKind.BAR, "x -> $x", "x -> 1"), null, "", run));
		assertSays("The value for 'a' is the text 'x', not a number", present("{\"a\": \"x\"}"));
		assertSays("the query returned a list containing a number", present("list(1, {\"a\": 1})"));
		assertSays("a TABLE shows a label and numbers per row", _tool.present("{'type': 'scatter', 'data':"
			+ " {'datasets': list({'data': list({'x': 1, 'y': 2})})}}", null, PresentAs.TABLE, null, null, "", run));
		assertEquals(List.of(), _written);
	}

	/**
	 * The pane builds a step for data, chart and table alike. It swallows what it cannot build - a
	 * page must not break over one presentation - so only a test sees a step that is never built.
	 */
	public void testThePaneShowsData() throws Exception {
		withWindow();
		allow();
		ReactWindowRegistry registry = new ReactWindowRegistry("test-session");
		String window = "paneWindow";
		DefaultReactContext react = new DefaultReactContext("/app", window, registry.getOrCreateQueue(window), registry);
		DefaultViewContext view = new DefaultViewContext(react);
		ViewChannel input = new DefaultViewChannel("paneInput");
		PresentationPane pane = new PresentationPane(view, input, new ReactControl(react, null, "TLTest"), () -> {
			// Nothing to tell a browser.
		});
		try {
			present("{\"Open\": 3, \"Closed\": 5}");
			input.set(_channel.get());
			_tool.present("{\"Open\": {\"count\": 3, \"sum\": 2.5}}", null, PresentAs.TABLE, null, null, "",
				context(ModelSecurity.UNCHECKED));
			input.set(_channel.get());

			List<Map<String, Object>> steps = pane.state();
			assertEquals(2, steps.size());
			assertEquals("chart", steps.get(0).get("kind"));
			assertEquals("table", steps.get(1).get("kind"));
		} finally {
			pane.dispose();
		}
	}

	/** The same object named twice is one object: the page shows what the model meant. */
	public void testADuplicateIdIsOneObject() {
		withWindow();
		allow();

		present(List.of(tid(first), tid(first)), PANE);

		assertEquals(List.of(first), _written);
	}

	/**
	 * A whole {@code label [id:TID]} entry, as a text tool result writes it, names the object it
	 * carries - a model that copies what it just read is right rather than wrong.
	 */
	public void testAnIdCopiedOutOfATextResultWorks() {
		withWindow();
		allow();

		present(List.of("First [id:" + tid(first) + "]"), PANE);

		assertEquals(List.of(first), _written);
	}

	// --- Fixture ---

	/** Registers the window the run belongs to, as a rendering chat element does. */
	private void withWindow() {
		AgentViewRefreshService.registerWindow(subSession(), windowName(), new ReactWindowRegistry("test-session"));
	}

	/** Opens the fixture's channel to the agent, as {@code <chat-conversation present="…">} does. */
	private void allow() {
		ChatViewBinding.bind(subSession(), windowName(), "/app", "main").allowPresenting(PANE, _channel);
	}

	private String present(List<String> ids, String channel) {
		return _tool.present(null, ids, null, null, null, channel, context(ModelSecurity.UNCHECKED));
	}

	private String present(String query) {
		return _tool.present(query, null, null, null, null, "", context(ModelSecurity.UNCHECKED));
	}

	private String chart(ChartSpec spec) {
		return _tool.present(null, List.of(tid(first), tid(second)), PresentAs.CHART, spec, null, "",
			context(ModelSecurity.UNCHECKED));
	}

	private static ToolContext context(ModelSecurity security) {
		return new ToolContext(new RunAs(security), ApprovalCallback.AUTO_APPROVE);
	}

	/** The tool as the agent's catalog holds it: what the schema filter asks before every request. */
	private static Tool toolOf(ChatPresentTool provider) {
		List<Tool> tools = AnnotatedTools.from(provider);
		assertEquals(1, tools.size());
		return tools.get(0);
	}

	private static TLSubSessionContext subSession() {
		return TLContextManager.getSubSession();
	}

	private static String windowName() {
		return "presentWindow";
	}

	private static String tid(TLObject object) {
		return object.tId().asString();
	}

	private static void assertSays(String expected, String actual) {
		assertTrue("Expected '" + expected + "' in: " + actual, actual.contains(expected));
	}

	/** A run acting with the given model rights and no tool restrictions. */
	private static class RunAs implements SecurityContext {

		private final ModelSecurity _security;

		RunAs(ModelSecurity security) {
			_security = security;
		}

		@Override
		public boolean hasToolPermission(Tool tool) {
			return true;
		}

		@Override
		public ExecutionMode executionMode() {
			return ExecutionMode.DEVELOPMENT;
		}

		@Override
		public ModelSecurity modelSecurity() {
			return _security;
		}
	}

	/** Rights that read nothing; everything else is the unchecked answer, which is not under test. */
	private static class NoReadSecurity implements ModelSecurity {

		@Override
		public boolean canRead(TLObject obj) {
			return false;
		}

		@Override
		public boolean canRead(TLObject obj, TLStructuredTypePart attribute) {
			return false;
		}

		@Override
		public boolean canWrite(TLObject obj) {
			return false;
		}

		@Override
		public boolean canWrite(TLObject obj, TLStructuredTypePart attribute) {
			return false;
		}

		@Override
		public boolean canDelete(TLObject obj) {
			return false;
		}

		@Override
		public boolean canCreate(TLClass type, TLObject context) {
			return false;
		}

		@Override
		public boolean canCreate(TLObject parent, TLStructuredTypePart compositionAttribute) {
			return false;
		}

		@Override
		public boolean canPerform(TLObject obj, BoundCommandGroup operation) {
			return false;
		}

		@Override
		public <T extends TLObject> List<T> filterReadable(Collection<T> objects) {
			return List.of();
		}

		@Override
		public Set<TLClass> accessibleTypes(BoundCommandGroup operation) {
			return Set.of();
		}
	}

	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestPresentTool.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				AgentViewRefreshService.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE));
	}
}
