/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.ai.chat.view.ChatComposerElement.Config;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * Defaults and nested {@code <controls/>} of {@code <chat-composer/>}.
 */
public class TestChatComposerConfig extends TestCase {

	/** Unconfigured attributes match the design: pieces find each other and every picker is on. */
	public void testDefaults() {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		assertEquals("default", config.getChatId());
		assertNull(config.getInput());
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
		assertFalse("Since #416 a slash types a slash.", config.getControls().getAgentSlash());
		assertEquals("TLChatComposer", config.getReactElement());
	}

	/** A nested {@code <controls/>} group parses, including every picker switched off. */
	public void testControlsGroupParses() throws Exception {
		String xml = "<chat-composer xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + Config.class.getName() + "'"
			+ " chat-id='main' prompt-improver-agent='TL_PromptImprover'"
			+ " reference-source='invoices' menu-items='export,print'>"
			+ "<controls auto-approve='false' enter-hint='false' agent-picker='false'"
			+ " model-picker='false' thinking='false' mcp='false' attachments='false'"
			+ " trace-picker='false' agent-slash='true'/>"
			+ "</chat-composer>";
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
		assertTrue(config.getControls().getAgentSlash());
	}
}
