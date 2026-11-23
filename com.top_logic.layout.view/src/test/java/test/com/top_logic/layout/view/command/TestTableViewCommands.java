/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.table.TableViewPreferences;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.command.TableViewCommands;

/**
 * Tests for {@link TableViewCommands}: two switches that follow the stored table view of the person
 * and turn it over.
 */
public class TestTableViewCommands extends TestCase {

	/** Both settings are offered as switches. */
	public void testTwoToggles() {
		List<CommandModel> commands = commands();

		assertEquals(2, commands.size());
		assertTrue(commands.get(0).isToggle());
		assertTrue(commands.get(1).isToggle());
	}

	/** The position of a switch is the stored setting; running the command turns it over and stores it. */
	public void testExecutingTurnsTheSettingOverAndStoresIt() {
		ThreadContext.inSystemContext(TestTableViewCommands.class, () -> {
			List<CommandModel> commands = commands();
			CommandModel striped = commands.get(0);
			CommandModel columnLines = commands.get(1);
			ReactContext context =
				new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));

			assertFalse(striped.isActive());
			assertFalse(columnLines.isActive());

			striped.perform(context);

			assertTrue(striped.isActive());
			assertTrue(TableViewPreferences.striped());
			assertFalse("The other setting is left alone.", columnLines.isActive());
			assertFalse(TableViewPreferences.columnLines());

			columnLines.perform(context);
			striped.perform(context);

			assertFalse(striped.isActive());
			assertFalse(TableViewPreferences.striped());
			assertTrue(columnLines.isActive());
			assertTrue(TableViewPreferences.columnLines());
		});
	}

	/** The script sets or removes the attribute at the element carrying the table view, if there is one. */
	public void testClientCode() {
		assertEquals("(function(e){if(e)e.toggleAttribute('data-tl-table-striped',true);})"
			+ "(document.querySelector('[data-tl-table-view]'));",
			TableViewCommands.clientCode(TableViewPreferences.ATTR_STRIPED, true));
		assertEquals("(function(e){if(e)e.toggleAttribute('data-tl-table-column-lines',false);})"
			+ "(document.querySelector('[data-tl-table-view]'));",
			TableViewCommands.clientCode(TableViewPreferences.ATTR_COLUMN_LINES, false));
	}

	private static List<CommandModel> commands() {
		return new TableViewCommands(null, TypedConfiguration.newConfigItem(TableViewCommands.Config.class))
			.getCommands(null);
	}

	/** Suite of tests. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableViewCommands.class, ThreadContextManager.Module.INSTANCE));
	}
}
