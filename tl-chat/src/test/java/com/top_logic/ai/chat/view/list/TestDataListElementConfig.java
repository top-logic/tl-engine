/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view.list;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.DefaultInstantiationContext;
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
		assertFalse("Expanded on arrival: a list starts showing what it holds.", config.getCollapsed());
		assertNull("No grouping until a group-by expression is set.", config.getGroupBy());
		assertNull("No lines configured by default.", config.getLines());
		assertNull("No selection channel by default.", config.getSelection());
		assertNull("No count channel by default.", config.getCount());
		assertNull("No row state until a row-css-class expression is set.", config.getRowCssClass());
		assertNull("No class on the list itself by default.", config.getCssClass());
		assertNull("No title by default.", config.getTitle());
		assertTrue("No header commands by default.", config.getCommands().isEmpty());
		assertTrue("No row commands by default.", config.getRowCommands().isEmpty());
		assertEquals("A row command binds the row through this channel.", "row", config.getRowChannel());
		assertEquals("A trigger on every row of a long list is noise; it waits for the pointer.",
			DataListElement.RowActions.HOVER, config.getRowActions());
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
			+ "<row-css-class>t -> \"is-done\"</row-css-class>"
			+ "</data-list>";
		DataListElement.Config config =
			(DataListElement.Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertNotNull("The count channel is configured.", config.getCount());
		assertEquals("progressBadge", config.getCount().getChannelName());
		assertNotNull("The row state expression is configured.", config.getRowCssClass());
	}

	/**
	 * A view written for tl-ai 2.0.5, whose {@code <css-class>} was the row function, is refused
	 * rather than read as the class of the list: the engine's {@code css-class} of every element
	 * would otherwise take the script text and the rows would lose their state without a word.
	 */
	public void testRowFunctionAsCssClassIsRefused() throws Exception {
		String xml = "<data-list xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + DataListElement.Config.class.getName() + "'>"
			+ "<rows>all(`tl.ai.execution:AgentTask`)</rows>"
			+ "<css-class>t -> \"is-done\"</css-class>"
			+ "</data-list>";
		DataListElement.Config config =
			(DataListElement.Config) TypedConfiguration.parse(CharacterContents.newContent(xml));

		BufferingProtocol log = new BufferingProtocol();
		new DefaultInstantiationContext(log).getInstance(config);

		assertFalse("The old spelling is reported.", log.getErrors().isEmpty());
		String errors = log.getErrors().toString();
		assertTrue(errors, errors.contains(DataListElement.Config.ROW_CSS_CLASS));
	}

	/** A plain class on the list itself is what css-class is for, and is accepted. */
	public void testCssClassOfTheList() throws Exception {
		String xml = "<data-list xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + DataListElement.Config.class.getName() + "'"
			+ " css-class='appRail'>"
			+ "<rows>all(`tl.ai.execution:AgentTask`)</rows>"
			+ "</data-list>";
		DataListElement.Config config =
			(DataListElement.Config) TypedConfiguration.parse(CharacterContents.newContent(xml));

		BufferingProtocol log = new BufferingProtocol();
		new DefaultInstantiationContext(log).getInstance(config);

		assertEquals(List.of(), log.getErrors());
		assertEquals("appRail", config.getCssClass());
	}

	/**
	 * A {@code <row-commands>} entry parses as a standard view command, with its own input and
	 * executability - which is what lets a row command decide on the row the menu was opened on.
	 */
	public void testRowCommandsParse() throws Exception {
		String xml = "<data-list xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + DataListElement.Config.class.getName() + "'"
			+ " row-channel='session' row-actions='always'>"
			+ "<rows>all(`tl.ai.execution:AgentSession`)</rows>"
			+ "<row-commands>"
			+ "<generic-command image='css:bi bi-pencil' input='session'>"
			+ "<open-dialog dialog-view='demo/rename.view.xml'/>"
			+ "</generic-command>"
			+ "<generic-command image='css:bi bi-trash' input='session'>"
			+ "<executability><visible-if expr='s -> true'/></executability>"
			+ "<execute-script function='s -> aiSessionDelete($s)'/>"
			+ "</generic-command>"
			+ "</row-commands>"
			+ "</data-list>";
		DataListElement.Config config =
			(DataListElement.Config) TypedConfiguration.parse(CharacterContents.newContent(xml));

		assertEquals(2, config.getRowCommands().size());
		assertEquals(GenericViewCommand.class, config.getRowCommands().get(0).getImplementationClass());
		assertEquals("session", config.getRowChannel());
		assertEquals(DataListElement.RowActions.ALWAYS, config.getRowActions());
		assertTrue("Row commands are not header commands.", config.getCommands().isEmpty());
	}

	/** Boots the module system so typed-configuration resolves nested config types via TypeIndex. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestDataListElementConfig.class));
	}
}
