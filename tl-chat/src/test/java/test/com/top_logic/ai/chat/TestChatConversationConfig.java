/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.util.List;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.ai.chat.view.ChatConversationElement;
import com.top_logic.ai.chat.view.ChatConversationElement.Config;
import com.top_logic.ai.chat.view.ChatConversationElement.Trace;
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
		assertNull(config.getBusy());
		assertNull(config.getAgent());
		assertNull(config.getModel());
		assertNull(config.getEffort());
		assertNull(config.getMcpServers());
		assertNull(config.getDraft());
		assertNull(config.getReference());

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
	 * The composer's two draft channels parse, each naming a channel of its own.
	 *
	 * <p>
	 * {@code draft} is the unsent text, two-way; {@code reference} takes a model object and turns it
	 * into one atomic token inside that text. Both are wiring: what they carry is pinned by the
	 * browser's own suite and by the control.
	 * </p>
	 */
	public void testDraftChannelsParse() throws Exception {
		String xml = "<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "'"
			+ " draft='composerDraft' reference='quotedObject'/>";
		Config config = (Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertEquals("composerDraft", config.getDraft().getChannelName());
		assertEquals("quotedObject", config.getReference().getChannelName());
	}

	/** Neither channel is required: an element that wires none is the ordinary case. */
	public void testDraftChannelsAreOptional() throws Exception {
		String xml = "<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "' draft='composerDraft'/>";
		Config config = (Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertEquals("composerDraft", config.getDraft().getChannelName());
		assertNull("A draft channel alone is enough; the reference one stays unwired.",
			config.getReference());
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

	/** Boots the module system so typed-configuration resolves nested config types via TypeIndex. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestChatConversationConfig.class));
	}
}
