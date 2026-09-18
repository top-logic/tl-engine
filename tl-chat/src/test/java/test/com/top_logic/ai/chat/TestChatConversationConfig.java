/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.ai.chat.view.ChatConversationElement;
import com.top_logic.ai.chat.view.ChatConversationElement.Config;
import com.top_logic.ai.chat.view.ChatConversationElement.Trace;
import com.top_logic.ai.chat.view.ChatSessionIds;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * Defaults and the nested {@code <actions/>} and {@code <controls/>} groups of
 * {@code <chat-conversation/>}, plus the session-object to id mapping its {@code input} channel
 * uses - the part of it that does not need a KnowledgeBase.
 */
public class TestChatConversationConfig extends TestCase {

	/** Unconfigured attributes match the design: pieces find each other, every action and picker on. */
	public void testDefaults() {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		assertEquals("default", config.getChatId());
		assertNull(config.getInput());
		assertNull(config.getBusy());

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

	/** Boots the module system so typed-configuration resolves nested config types via TypeIndex. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestChatConversationConfig.class));
	}
}
