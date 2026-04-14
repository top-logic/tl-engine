/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ContextMenuContribution;

/**
 * Tests for {@link ContextMenuContribution}.
 */
public class TestContextMenuContribution extends TestCase {

	public void testHoldsTargetAndCommands() {
		ViewChannel target = new DefaultViewChannel("target");
		CommandModel cmd = FakeCommandModels.contextMenu("edit", "Edit", true, true);
		ContextMenuContribution contribution =
			new ContextMenuContribution(target, List.of(cmd));

		assertSame(target, contribution.target());
		assertEquals(List.of(cmd), contribution.commands());
	}

	public void testExecutableCommandsFiltersDisabledAndInvisible() {
		ViewChannel target = new DefaultViewChannel("target");
		CommandModel visibleEnabled = FakeCommandModels.contextMenu("a", "A", true, true);
		CommandModel disabledVisible = FakeCommandModels.contextMenu("b", "B", true, false);
		CommandModel invisible = FakeCommandModels.contextMenu("c", "C", false, true);

		ContextMenuContribution contribution =
			new ContextMenuContribution(target, List.of(visibleEnabled, disabledVisible, invisible));

		assertEquals(
			List.of(visibleEnabled, disabledVisible),
			contribution.visibleCommands());
	}
}
