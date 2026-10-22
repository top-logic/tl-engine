/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.ai.chat.view.ChatConversationElement;
import com.top_logic.ai.chat.view.ChatConversationElement.Config;
import com.top_logic.ai.chat.view.ChatConversationElement.Trace;
import com.top_logic.ai.chat.view.ChatQuotes;
import com.top_logic.ai.chat.view.ChatSessionIds;
import com.top_logic.ai.chat.view.ChatTurnChannels;
import com.top_logic.ai.chat.view.ChatViewBinding;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;

/**
 * Defaults and the nested {@code <actions/>} and {@code <controls/>} groups of
 * {@code <chat-conversation/>}, plus the session-object to id mapping its {@code input} channel
 * uses - the part of it that does not need a KnowledgeBase.
 */
public class TestChatConversationConfig extends TestCase {

	/** Unconfigured attributes match the design: pieces find each other, every action and picker on. */
	public void testDefaults() {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		assertNull("No id of its own: the enclosing <chat> names it, else it is the default chat.",
			config.getChatId());
		assertNull(config.getInput());
		assertNull("A conversation is about nothing until a page says what.", config.getSubject());
		assertNull(config.getBusy());
		assertNull(config.getAgent());
		assertNull(config.getModel());
		assertNull(config.getEffort());
		assertNull(config.getMcpServers());
		assertNull(config.getDraft());
		assertNull(config.getReference());
		assertNull(config.getQuote());

		// Transcript.
		assertEquals(Trace.SUMMARY, config.getTrace());
		assertTrue(config.getCitations());
		assertNull(config.getCitationRenderer());
		assertTrue(config.getActions().getCopy());
		assertTrue(config.getActions().getRate());
		assertTrue(config.getActions().getRegenerate());
		assertTrue(config.getActions().getTimestamp());
		assertTrue(config.getActions().getHost() == null || config.getActions().getHost().isEmpty());

		// Composer.
		assertNull(config.getPromptImproverAgent());
		assertNull(config.getPlaceholder());
		assertNull(config.getReferenceSource());
		assertTrue(config.getMenuItems() == null || config.getMenuItems().isEmpty());
		assertTrue(config.getControls().getAutoApprove());
		assertTrue(config.getControls().getEnterHint());
		assertTrue(config.getControls().getAgentPicker());
		assertTrue(config.getControls().getModelPicker());
		assertTrue(config.getControls().getThinking());
		assertTrue(config.getControls().getMcp());
		assertTrue(config.getControls().getAttachments());
		assertTrue(config.getControls().getTracePicker());

		assertEquals("TLChatConversation", config.getReactElement());
	}

	/** XML and React both consume the trace level's lowercase external names. */
	public void testTraceExternalNames() {
		assertEquals("none", Trace.NONE.getExternalName());
		assertEquals("summary", Trace.SUMMARY.getExternalName());
		assertEquals("detailed", Trace.DETAILED.getExternalName());
	}

	/** The nested {@code <actions/>} group parses, including every action switched off. */
	public void testActionsGroupParses() throws Exception {
		String xml = "<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "'>"
			+ "<actions copy='false' rate='false' regenerate='false' timestamp='false' host='open-in-editor'/>"
			+ "</chat-conversation>";
		Config config = (Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertFalse(config.getActions().getCopy());
		assertFalse(config.getActions().getRate());
		assertFalse(config.getActions().getRegenerate());
		assertFalse(config.getActions().getTimestamp());
		assertEquals(List.of("open-in-editor"), config.getActions().getHost());
	}

	/**
	 * An unknown host action name does not fail the view; it is dropped with a debug line when
	 * resolved against the registered names.
	 */
	public void testUnknownHostActionIsIgnored() throws Exception {
		String xml = "<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "'>"
			+ "<actions host='not-a-real-action'/>"
			+ "</chat-conversation>";
		Config config = (Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertEquals("The view still loads.", List.of("not-a-real-action"), config.getActions().getHost());
		assertEquals(1, ChatConversationElement.ignoreUnknownHostActions(config.getActions().getHost(), List.of()));
		assertEquals(0, ChatConversationElement.ignoreUnknownHostActions(List.of("open-in-editor"),
			List.of("open-in-editor")));
	}

	/** The nested {@code <controls/>} group parses, including every picker switched off. */
	public void testControlsGroupParses() throws Exception {
		String xml = "<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "'"
			+ " chat-id='main' prompt-improver-agent='TL_PromptImprover'"
			+ " reference-source='invoices' menu-items='export,print'>"
			+ "<controls auto-approve='false' enter-hint='false' agent-picker='false'"
			+ " model-picker='false' thinking='false' mcp='false' attachments='false'"
			+ " trace-picker='false'/>"
			+ "</chat-conversation>";
		Config config = (Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertEquals("main", config.getChatId());
		assertEquals("TL_PromptImprover", config.getPromptImproverAgent());
		assertEquals("invoices", config.getReferenceSource());
		assertEquals(List.of("export", "print"), config.getMenuItems());
		assertFalse(config.getControls().getAutoApprove());
		assertFalse(config.getControls().getEnterHint());
		assertFalse(config.getControls().getAgentPicker());
		assertFalse(config.getControls().getModelPicker());
		assertFalse(config.getControls().getThinking());
		assertFalse(config.getControls().getMcp());
		assertFalse(config.getControls().getAttachments());
		assertFalse(config.getControls().getTracePicker());
	}

	/**
	 * The four turn-setting channels parse, each naming a channel of its own.
	 *
	 * <p>
	 * Wiring, not data: what they carry - an agent, a model, an effort level, a set of MCP servers -
	 * is pinned by {@code TestChatTurnChannels} and by the browser's own suite. What this holds is
	 * that an application can name a channel for each.
	 * </p>
	 */
	public void testTurnSettingChannelsParse() throws Exception {
		String xml = "<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "'"
			+ " agent='pickedAgent' model='pickedModel' effort='pickedEffort'"
			+ " mcp-servers='attachedServers'/>";
		Config config = (Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertEquals("pickedAgent", config.getAgent().getChannelName());
		assertEquals("pickedModel", config.getModel().getChannelName());
		assertEquals("pickedEffort", config.getEffort().getChannelName());
		assertEquals("attachedServers", config.getMcpServers().getChannelName());
	}

	/**
	 * The composer's three draft channels parse, each naming a channel of its own.
	 *
	 * <p>
	 * {@code draft} is the unsent text, two-way; {@code reference} takes a model object and turns it
	 * into one atomic token inside that text; {@code quote} does the same for a marked passage
	 * <em>of</em> an object (Issue #509). All three are wiring: what they carry is pinned by the
	 * browser's own suite and, for the quote, by {@link ChatQuotes}.
	 * </p>
	 */
	public void testDraftChannelsParse() throws Exception {
		String xml = "<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "'"
			+ " draft='composerDraft' reference='quotedObject' quote='markedPassage'/>";
		Config config = (Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertEquals("composerDraft", config.getDraft().getChannelName());
		assertEquals("quotedObject", config.getReference().getChannelName());
		assertEquals("markedPassage", config.getQuote().getChannelName());
	}

	/**
	 * The {@code quote} channel is independent of the other two: a viewer that marks passages needs
	 * neither a draft channel nor a reference one.
	 */
	public void testQuoteChannelStandsAlone() throws Exception {
		String xml = "<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "' quote='markedPassage'/>";
		Config config = (Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertEquals("markedPassage", config.getQuote().getChannelName());
		assertNull(config.getDraft());
		assertNull("A marked passage carries its own object; it needs no reference channel.",
			config.getReference());
	}

	/**
	 * The two channels of a per-object chat parse: which conversation is shown, and what a new one
	 * is about.
	 *
	 * <p>
	 * They are independent halves of one page's rule (Issue #508). {@code input} is the conversation
	 * the page shows - written by the {@code <data-list>} that scoped its rows by the same object -
	 * and {@code subject} is what a conversation started here is stamped with. Nothing on the way
	 * in: picking a session stays the list's job.
	 * </p>
	 */
	public void testSubjectChannelParses() throws Exception {
		String xml = "<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "'"
			+ " input='chatSession' subject='project'/>";
		Config config = (Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertEquals("chatSession", config.getInput().getChannelName());
		assertEquals("project", config.getSubject().getChannelName());
	}

	/**
	 * A subject needs no session list: an element may stamp what it creates and show nothing else.
	 *
	 * <p>
	 * The two are configured apart because they answer different questions, and a page that scopes
	 * its list elsewhere - a rail section, another view - still wants its conversations stamped.
	 * </p>
	 */
	public void testSubjectChannelIsIndependentOfTheInputChannel() throws Exception {
		String xml = "<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "' subject='project'/>";
		Config config = (Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertEquals("project", config.getSubject().getChannelName());
		assertNull("No session list on this page.", config.getInput());
	}

	/** Neither channel is required: an element that wires none is the ordinary case. */
	public void testDraftChannelsAreOptional() throws Exception {
		String xml = "<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "' draft='composerDraft'/>";
		Config config = (Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertEquals("composerDraft", config.getDraft().getChannelName());
		assertNull("A draft channel alone is enough; the reference one stays unwired.",
			config.getReference());
		assertNull(config.getQuote());
	}

	/**
	 * What the {@code quote} channel carries: the passage, the object it is from, and what the
	 * token says about the two.
	 *
	 * <p>
	 * The object is normally written as the model object itself, which the element turns into the
	 * token's kind, stable id and label - the seam this test stands in for, because an application
	 * has no way of spelling that id. Whatever the map says wins over what the object says: the host
	 * showed the passage and knows how to name it.
	 * </p>
	 */
	public void testQuoteTakesThePassageAndTheObjectItIsFrom() {
		Object marked = new Object();
		Function<Object, Map<String, Object>> tokenOf = value -> value == marked
			? Map.of("kind", "GenericObject", "id", "6881", "label", "Angebot Alpha")
			: Map.of();

		Map<String, Object> token = ChatQuotes.token(
			mapOf("id", marked, "location", "02 Kontext", "text", "the marked passage"), tokenOf);
		assertEquals("GenericObject", token.get("kind"));
		assertEquals("6881", token.get("id"));
		assertEquals("The object's own label, no <mention> having said otherwise.",
			"Angebot Alpha", token.get("label"));
		assertEquals("02 Kontext", token.get("location"));
		assertEquals("the marked passage", token.get("text"));

		Map<String, Object> named = ChatQuotes.token(
			mapOf("id", marked, "kind", "section", "label", "Angebot Alpha, Kontext",
				"text", "the marked passage"),
			tokenOf);
		assertEquals("section", named.get("kind"));
		assertEquals("Angebot Alpha, Kontext", named.get("label"));
		assertEquals("A passage with no location is still a passage.", "", named.get("location"));
	}

	/**
	 * A host that does know the id writes it as text, and is then the only one saying anything about
	 * the object - the element is never asked.
	 */
	public void testQuoteTakesAPrintedIdWithoutAskingTheElement() {
		Function<Object, Map<String, Object>> never = value -> {
			throw new AssertionError("A printed id is the id; nothing to resolve.");
		};
		Map<String, Object> token =
			ChatQuotes.token(mapOf("id", " 6881 ", "text", "the marked passage"), never);
		assertEquals("6881", token.get("id"));
		assertEquals("Nothing else said what to show, so the id is the label.",
			"6881", token.get("label"));
		assertEquals("", token.get("kind"));
	}

	/**
	 * The naming fields are trimmed; the passage is not.
	 *
	 * <p>
	 * A label or a location is a name for the passage, and padding in one is noise the reader sees
	 * on the token. The passage itself is what the reader marked, so it travels as marked.
	 * </p>
	 */
	public void testQuoteTrimsTheNamesAndKeepsThePassageAsMarked() {
		Map<String, Object> token = ChatQuotes.token(
			mapOf("id", "6881", "kind", " section ", "label", "  Angebot Alpha  ",
				"location", " 02 Kontext ", "text", "  the marked passage\n"),
			value -> Map.of());
		assertEquals("section", token.get("kind"));
		assertEquals("Angebot Alpha", token.get("label"));
		assertEquals("02 Kontext", token.get("location"));
		assertEquals("  the marked passage\n", token.get("text"));
	}

	/**
	 * What the channel does not insert: everything that is not a passage of something.
	 *
	 * <p>
	 * A bare object is the loudest of them. Inserting it would quote the whole of it back, which is
	 * what the {@code reference} channel is for and what the agent's instructions say not to do.
	 * </p>
	 */
	public void testQuoteInsertsNothingWithoutAPassageAndAnObject() {
		Object marked = new Object();
		Function<Object, Map<String, Object>> tokenOf = value -> Map.of("id", "6881");

		assertNull("No passage.", ChatQuotes.token(mapOf("id", marked), tokenOf));
		assertNull("A blank passage is no passage.",
			ChatQuotes.token(mapOf("id", marked, "text", "   "), tokenOf));
		assertNull("No object.", ChatQuotes.token(mapOf("text", "the marked passage"), tokenOf));
		assertNull("An object with no stable id.",
			ChatQuotes.token(mapOf("id", marked, "text", "x"), value -> Map.of()));
		assertNull("A bare object is a reference, not a quote.", ChatQuotes.token(marked, tokenOf));
		assertNull("An unwired channel.", ChatQuotes.token(null, tokenOf));
		assertNull("A sentence is a draft, not a quote.", ChatQuotes.token("some text", tokenOf));
	}

	/** A single-row selection writes its one map, the way it writes its one object. */
	public void testQuoteReadsAOneRowSelection() {
		Function<Object, Map<String, Object>> tokenOf = value -> Map.of();
		Map<String, Object> written = mapOf("id", "6881", "text", "the marked passage");
		assertEquals("6881", ChatQuotes.token(List.of(written), tokenOf).get("id"));
		assertNull("Several rows name no single passage.",
			ChatQuotes.token(List.of(written, written), tokenOf));
		assertNull("No row at all.", ChatQuotes.token(List.of(), tokenOf));
	}

	/**
	 * A field written as a {@link ResKey} is resolved, not printed - the same rule the {@code draft}
	 * channel reads by, because the way to write a sentence in TL-Script is
	 * {@code {"en": …, "de": …}.internationalize()}.
	 */
	public void testQuoteResolvesAnInternationalizedField() {
		Map<String, Object> token = ChatQuotes.token(
			mapOf("id", "6881", "location", ResKey.text("02 Kontext"),
				"text", ResKey.text("die markierte Passage")),
			value -> Map.of());
		assertEquals("02 Kontext", token.get("location"));
		assertEquals("die markierte Passage", token.get("text"));

		assertEquals("An unwritten field is the empty text, never \"null\".", "",
			ChatQuotes.text(null));
		assertEquals("plain", ChatQuotes.text("plain"));
	}

	/**
	 * The composer's invitation is a text per language, and it reaches the browser resolved.
	 *
	 * <p>
	 * Written as a literal here, which is the form an application uses in its view - the other one
	 * is the key of a text in its own bundle. What the browser receives is the sentence, never the
	 * key, because it has no bundle of the application's to look one up in
	 * ({@link ChatViewBinding#resolveText(ResKey)}).
	 * </p>
	 */
	public void testPlaceholderParsesAndResolves() throws Exception {
		String xml = "<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "'>"
			+ "<placeholder><en>Ask about this project.</en><de>Fragen Sie zu diesem Projekt.</de></placeholder>"
			+ "</chat-conversation>";
		Config config = (Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		ResKey placeholder = config.getPlaceholder();
		assertNotNull("The literal parsed.", placeholder);
		assertEquals("Ask about this project.",
			Resources.getInstance(Locale.ENGLISH).getString(placeholder));
		assertEquals("Fragen Sie zu diesem Projekt.",
			Resources.getInstance(Locale.GERMAN).getString(placeholder));
	}

	/** An element that configures none pushes the empty string, which is the browser's "not set". */
	public void testUnsetPlaceholderIsTheEmptyString() {
		assertEquals("", ChatViewBinding.resolveText(null));
	}

	/** A model-bound {@code <mention>} parses: rows plus the optional label/text and the default limit. */
	public void testMentionGroupParses() throws Exception {
		String xml = "<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "'>"
			+ "<mention observed-types='tl.ai.execution:AgentSession'>"
			+ "<rows>all(`tl.ai.execution:AgentSession`)</rows>"
			+ "<label>x -> $x.get(`tl.ai.execution:AgentSession#title`)</label>"
			+ "</mention>"
			+ "</chat-conversation>";
		Config config = (Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertNotNull("Mention is configured.", config.getMention());
		assertNotNull("Rows expression parsed.", config.getMention().getRows());
		assertNotNull("Label expression parsed.", config.getMention().getLabel());
		assertNull("Text defaults to null (falls back to the label).", config.getMention().getText());
		assertEquals("Default candidate limit.", 50, config.getMention().getLimit());
		assertEquals(1, config.getMention().getObservedTypes().size());
	}

	/** With no {@code <mention>}, the binding is absent and {@code @} uses the registered source name. */
	public void testMentionAbsentByDefault() {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		assertNull(config.getMention());
	}

	/** No session maps to an empty id, never {@code null}. */
	public void testEmptyIdWhenThereIsNoSession() {
		assertEquals("", ChatSessionIds.idOf(null));
		assertNull(ChatSessionIds.sessionOf(null));
		assertEquals("", ChatSessionIds.selectedId(null, null));
		assertNull(ChatSessionIds.ownedSession("", null));
		assertNull(ChatSessionIds.ownedSession(null, null));
	}

	/** A one-item collection is the session a multi-select table writes for a single row. */
	public void testOneItemCollectionIsTheSession() {
		assertNull(ChatSessionIds.sessionOf(List.of()));
		assertNull(ChatSessionIds.sessionOf(List.of("not-a-session")));
	}

	/**
	 * What a turn-setting channel says when it holds nothing, or something that is not a name.
	 *
	 * <p>
	 * {@code null} is the answer that leaves the composer alone, and a blank string is the same
	 * answer: there is no agent, model or effort level whose name is empty. An empty <em>list</em> of
	 * servers is the opposite - an instruction to attach none - which is why the two are told apart.
	 * </p>
	 */
	public void testAnEmptyTurnChannelSaysNothing() {
		assertNull(ChatTurnChannels.name(null));
		assertNull(ChatTurnChannels.name(""));
		assertNull(ChatTurnChannels.name("   "));
		assertNull(ChatTurnChannels.name(List.of()));
		assertNull(ChatTurnChannels.names(null));
		assertEquals(List.of(), ChatTurnChannels.names(List.of()));
	}

	/** A name, however a host wrote it: on its own, padded, or as the one row a list selected. */
	public void testTurnChannelReadsTheName() {
		assertEquals("TL_ModelExplorer", ChatTurnChannels.name("TL_ModelExplorer"));
		assertEquals("TL_ModelExplorer", ChatTurnChannels.name("  TL_ModelExplorer  "));
		assertEquals("TL_ModelExplorer", ChatTurnChannels.name(List.of("TL_ModelExplorer")));
		assertNull("Several rows name no single agent.",
			ChatTurnChannels.name(List.of("one", "two")));
	}

	/** The server channel takes a collection, or a single name as a collection of one. */
	public void testServerNamesFromACollectionOrOne() {
		assertEquals(List.of("files", "issues"), ChatTurnChannels.names(List.of("files", "issues")));
		assertEquals(List.of("files"), ChatTurnChannels.names("files"));
		assertEquals("A blank entry names no server.", List.of("files"),
			ChatTurnChannels.names(java.util.Arrays.asList("files", "  ")));
	}

	/** A map written the way TL-Script writes one: string keys, values of whatever type. */
	private static Map<String, Object> mapOf(Object... pairs) {
		Map<String, Object> map = new LinkedHashMap<>();
		for (int n = 0; n < pairs.length; n += 2) {
			map.put(String.valueOf(pairs[n]), pairs[n + 1]);
		}
		return map;
	}

	/** Boots the module system so typed-configuration resolves nested config types via TypeIndex. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestChatConversationConfig.class));
	}
}
