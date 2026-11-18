/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.list;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.list.DataListElement;

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
		assertEquals("A column at every width until a drawer threshold is set.", 0, config.getDrawerBelow());
		assertNull("Nothing in front of the lines by default.", config.getLeading());
		assertNull("Nothing after the lines by default.", config.getTrailing());
		assertEquals(DataListElement.class, config.getImplementationClass());
	}

	/** A header {@code <commands>} entry parses as a standard view command with its actions. */
	public void testCommandParses() throws Exception {
		String xml = "<data-list xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + DataListElement.Config.class.getName() + "'>"
			+ "<rows>all(`tl.model:TLModule`)</rows>"
			+ "<commands>"
			+ "<generic-command image='css:bi bi-plus-lg'><write-channel name='selected'/></generic-command>"
			+ "</commands>"
			+ "</data-list>";
		DataListElement.Config config =
			(DataListElement.Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertEquals(1, config.getCommands().size());
		assertEquals(GenericViewCommand.class, config.getCommands().get(0).getImplementationClass());
	}

	/**
	 * The two hooks a section around the list needs: the channel the row count is written to, and
	 * the expression that gives a row its state class.
	 */
	public void testCountChannelAndRowStateParse() throws Exception {
		String xml = "<data-list xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + DataListElement.Config.class.getName() + "'"
			+ " count='badge'>"
			+ "<rows>all(`tl.model:TLModule`)</rows>"
			+ "<row-css-class>t -> \"is-done\"</row-css-class>"
			+ "</data-list>";
		DataListElement.Config config =
			(DataListElement.Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		assertNotNull("The count channel is configured.", config.getCount());
		assertEquals("badge", config.getCount().getChannelName());
		assertNotNull("The row state expression is configured.", config.getRowCssClass());
	}

	/**
	 * A function of a row written as {@code <css-class>} is refused rather than read as the class of
	 * the list: the {@code css-class} of every element would otherwise take the script text and the
	 * rows would lose their state without a word.
	 */
	public void testRowFunctionAsCssClassIsRefused() throws Exception {
		String xml = "<data-list xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + DataListElement.Config.class.getName() + "'>"
			+ "<rows>all(`tl.model:TLModule`)</rows>"
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
			+ " css-class='my-list'>"
			+ "<rows>all(`tl.model:TLModule`)</rows>"
			+ "</data-list>";
		DataListElement.Config config =
			(DataListElement.Config) TypedConfiguration.parse(CharacterContents.newContent(xml));

		BufferingProtocol log = new BufferingProtocol();
		new DefaultInstantiationContext(log).getInstance(config);

		assertEquals(List.of(), log.getErrors());
		assertEquals("my-list", config.getCssClass());
	}

	/**
	 * A {@code <row-commands>} entry parses as a standard view command, with its own inputs and
	 * executability - which is what lets a row command decide on the row the menu was opened on.
	 */
	public void testRowCommandsParse() throws Exception {
		String xml = "<data-list xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + DataListElement.Config.class.getName() + "'"
			+ " row-channel='item' row-actions='always'>"
			+ "<rows>all(`tl.model:TLModule`)</rows>"
			+ "<row-commands>"
			+ "<generic-command image='css:bi bi-pencil' inputs='item'>"
			+ "<open-dialog dialog-view='my/rename.view.xml'/>"
			+ "</generic-command>"
			+ "<generic-command image='css:bi bi-trash' inputs='item'>"
			+ "<executability><visible-if expr='true'/></executability>"
			+ "<execute-script function='$item.delete()'/>"
			+ "</generic-command>"
			+ "</row-commands>"
			+ "</data-list>";
		DataListElement.Config config =
			(DataListElement.Config) TypedConfiguration.parse(CharacterContents.newContent(xml));

		assertEquals(2, config.getRowCommands().size());
		assertEquals(GenericViewCommand.class, config.getRowCommands().get(0).getImplementationClass());
		assertEquals("item", config.getRowChannel());
		assertEquals(DataListElement.RowActions.ALWAYS, config.getRowActions());
		assertTrue("Row commands are not header commands.", config.getCommands().isEmpty());
	}

	/**
	 * The layout editor offers every option of the list but the two every element hides: the
	 * implementation class the tag fixes and the personalization key the view context derives.
	 */
	public void testHiddenOptions() {
		assertHidden(UIElement.Config.PERSONALIZATION_KEY);
		assertHidden(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME);
		for (String name : List.of(DataListElement.Config.ROWS, DataListElement.Config.SELECTION,
			DataListElement.Config.SEARCH, DataListElement.Config.COUNT, DataListElement.Config.CSS_CLASS,
			DataListElement.Config.COLLAPSED, DataListElement.Config.DRAWER_BELOW, DataListElement.Config.ROW_COMMANDS,
			DataListElement.Config.ROW_CHANNEL, DataListElement.Config.ROW_ACTIONS, DataListElement.Config.LEADING,
			DataListElement.Config.TRAILING)) {
			Hidden hidden = property(name).getAnnotation(Hidden.class);
			assertTrue("Option '" + name + "' is a designer option and must stay visible.",
				hidden == null || !hidden.value());
		}
	}

	private static void assertHidden(String name) {
		Hidden hidden = property(name).getAnnotation(Hidden.class);
		assertTrue("Option '" + name + "' must be hidden from the layout editor.", hidden != null && hidden.value());
	}

	private static PropertyDescriptor property(String name) {
		PropertyDescriptor property =
			TypedConfiguration.getConfigurationDescriptor(DataListElement.Config.class).getProperty(name);
		assertNotNull("No option '" + name + "'.", property);
		return property;
	}

	/** Boots the module system so typed-configuration resolves nested config types via TypeIndex. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestDataListElementConfig.class));
	}
}
