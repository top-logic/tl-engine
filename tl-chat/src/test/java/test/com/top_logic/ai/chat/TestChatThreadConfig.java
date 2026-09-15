/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.ai.chat.view.AgentChatElement.TraceMode;
import com.top_logic.ai.chat.view.ChatThreadElement;
import com.top_logic.ai.chat.view.ChatThreadElement.Config;
import com.top_logic.ai.chat.view.ChatThreadElement.RunSummary;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * Defaults and nested {@code <actions/>} of {@code <chat-thread/>}.
 */
public class TestChatThreadConfig extends TestCase {

	/** Unconfigured attributes match the design: pieces find each other and every action is on. */
	public void testDefaults() {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		assertEquals("default", config.getChatId());
		assertNull(config.getInput());
		assertNull(config.getBusy());
		assertEquals(TraceMode.OVERVIEW, config.getTraceMode());
		assertEquals(RunSummary.COLLAPSED, config.getRunSummary());
		assertTrue(config.getCitationMarkers());
		assertTrue(config.getRailToggle());
		assertNull(config.getAnnotations());
		assertTrue(config.getActions().getCopy());
		assertTrue(config.getActions().getRate());
		assertTrue(config.getActions().getRegenerate());
		assertTrue(config.getActions().getHost() == null || config.getActions().getHost().isEmpty());
		assertEquals("TLChatThread", config.getReactElement());
	}

	/** XML and React both consume the run-summary's lowercase external names. */
	public void testRunSummaryExternalNames() {
		assertEquals("collapsed", RunSummary.COLLAPSED.getExternalName());
		assertEquals("expanded", RunSummary.EXPANDED.getExternalName());
		assertEquals("hidden", RunSummary.HIDDEN.getExternalName());
	}

	/** A nested {@code <actions/>} group parses. */
	public void testActionsGroupParses() throws Exception {
		String xml = "<chat-thread xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "'>"
			+ "<actions copy='false' rate='false' regenerate='false' host='open-in-editor'/>"
			+ "</chat-thread>";
		Config config = (Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertFalse(config.getActions().getCopy());
		assertFalse(config.getActions().getRate());
		assertFalse(config.getActions().getRegenerate());
		assertEquals(List.of("open-in-editor"), config.getActions().getHost());
	}

	/**
	 * An unknown host action name does not fail the view; it is dropped with a debug line when
	 * resolved against the registered names.
	 */
	public void testUnknownHostActionIsIgnored() throws Exception {
		String xml = "<chat-thread xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "'>"
			+ "<actions host='not-a-real-action'/>"
			+ "</chat-thread>";
		Config config = (Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertEquals("The view still loads.", List.of("not-a-real-action"), config.getActions().getHost());
		assertEquals(1, ChatThreadElement.ignoreUnknownHostActions(config.getActions().getHost(), List.of()));
		assertEquals(0, ChatThreadElement.ignoreUnknownHostActions(List.of("open-in-editor"),
			List.of("open-in-editor")));
	}
}
