/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.view.command.CommandScope;

/**
 * Tests the order of the commands in a {@link CommandScope}, which is the order the toolbar shows
 * them in within a group.
 */
public class TestCommandScope extends TestCase {

	/**
	 * Tests that a command added first precedes the scope's own commands, while a command added
	 * normally follows them.
	 */
	public void testAddFirst() {
		CommandModel own = new FakeCommandModelBase("own");
		CommandModel later = new FakeCommandModelBase("later");
		CommandModel primary = new FakeCommandModelBase("primary");
		CommandScope scope = new CommandScope(List.of(own));
		int[] changes = { 0 };
		scope.addListener(() -> changes[0]++);

		scope.addCommand(later);
		scope.addCommandFirst(primary);

		assertEquals(List.of(primary, own, later), scope.getAllCommands());
		assertEquals("Every addition is reported.", 2, changes[0]);
	}

}
