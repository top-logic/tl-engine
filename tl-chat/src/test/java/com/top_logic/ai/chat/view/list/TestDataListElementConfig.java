/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view.list;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.layout.view.command.GenericViewCommand;

/**
 * Checks the defaults of the {@code <data-list>} configuration: search and collapse on, no grouping
 * or selection until configured.
 */
public class TestDataListElementConfig extends TestCase {

	/** An unconfigured {@code <data-list>} has the list defaults. */
	public void testDefaults() {
		DataListElement.Config config = TypedConfiguration.newConfigItem(DataListElement.Config.class);

		assertTrue("Search on by default.", config.getSearch());
		assertTrue("Collapsible by default.", config.getCollapsible());
		assertNull("No grouping until a group-by expression is set.", config.getGroupBy());
		assertNull("No lines configured by default.", config.getLines());
		assertNull("No selection channel by default.", config.getSelection());
		assertNull("No count channel by default.", config.getCount());
		assertNull("No row state until a css-class expression is set.", config.getCssClass());
		assertNull("No title by default.", config.getTitle());
		assertTrue("No header commands by default.", config.getCommands().isEmpty());
		assertTrue("No input channels by default.", config.getInputs().isEmpty());
		assertTrue("A static list by default (no observed types).", config.getObservedTypes().isEmpty());
		assertEquals("TLDataList", config.getReactElement());
		assertEquals(DataListElement.class, config.getImplementationClass());
	}

	/** A header {@code <commands>} entry parses as a standard view command with its actions. */
	public void testCommandParses() throws Exception {
		String xml = "<data-list xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + DataListElement.Config.class.getName() + "'>"
			+ "<rows>all(`tl.ai.execution:AgentSession`)</rows>"
			+ "<commands>"
			+ "<generic-command image='css:bi bi-plus-lg'><write-channel name='chatSession'/></generic-command>"
			+ "</commands>"
			+ "</data-list>";
		DataListElement.Config config =
			(DataListElement.Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertEquals(1, config.getCommands().size());
		assertEquals(GenericViewCommand.class, config.getCommands().get(0).getImplementationClass());
	}

	/**
	 * The two hooks a rail section needs: the channel the row count is written to, and the
	 * expression that gives a row its state class.
	 */
	public void testCountChannelAndRowStateParse() throws Exception {
		String xml = "<data-list xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + DataListElement.Config.class.getName() + "'"
			+ " count='progressBadge'>"
			+ "<rows>all(`tl.ai.execution:AgentTask`)</rows>"
			+ "<css-class>t -> \"is-done\"</css-class>"
			+ "</data-list>";
		DataListElement.Config config =
			(DataListElement.Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertNotNull("The count channel is configured.", config.getCount());
		assertEquals("progressBadge", config.getCount().getChannelName());
		assertNotNull("The row state expression is configured.", config.getCssClass());
	}

	/** Boots the module system so typed-configuration resolves nested config types via TypeIndex. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestDataListElementConfig.class));
	}
}
