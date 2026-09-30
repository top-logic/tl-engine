/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.CommandPlacement;
import com.top_logic.layout.react.control.layout.ReactToolbarControl;
import com.top_logic.layout.react.control.layout.ToolbarOverflow;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.command.CliqueRegistry;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.command.ToolbarBuilder;

/**
 * Tests the {@link ToolbarBuilder}, in particular the {@link ToolbarOverflow} it derives from the
 * placement its toolbar is built for.
 */
public class TestToolbarBuilder extends TestCase {

	private ReactContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/**
	 * A toolbar reads from the left, so it collapses from its trailing end.
	 */
	public void testToolbarCollapsesFromTheTrailingEnd() {
		ReactToolbarControl toolbar = build(CommandPlacement.TOOLBAR);

		assertEquals(ToolbarOverflow.TRAILING, toolbar.getOverflow());
	}

	/**
	 * A button bar ends with the action committing the form, so it collapses from its leading end.
	 */
	public void testButtonBarCollapsesFromTheLeadingEnd() {
		ReactToolbarControl toolbar = build(CommandPlacement.BUTTON_BAR);

		assertEquals(ToolbarOverflow.LEADING, toolbar.getOverflow());
	}

	/**
	 * A toolbar without commands is displayed all the same (it is the target of the reactive
	 * rebuilds), so it must carry the collapsing behavior of its placement from the start.
	 */
	public void testEmptyToolbarKeepsThePlacementBehavior() {
		CommandScope scope = new CommandScope(List.of(command("other", CommandPlacement.CONTEXT_MENU)));

		ReactToolbarControl toolbar = ToolbarBuilder.buildOrEmpty(_context, scope,
			CommandPlacement.BUTTON_BAR, new CliqueRegistry(), null);

		assertTrue(toolbar.isEmpty());
		assertEquals(ToolbarOverflow.LEADING, toolbar.getOverflow());
	}

	/**
	 * A rebuilt toolbar replaces the groups of the one on display, which keeps collapsing the way
	 * its placement asks for.
	 */
	public void testRebuildKeepsTheCollapsingBehavior() {
		ReactToolbarControl displayed = build(CommandPlacement.TOOLBAR);

		displayed.replaceGroups(build(CommandPlacement.TOOLBAR));

		assertEquals(ToolbarOverflow.TRAILING, displayed.getOverflow());
	}

	/**
	 * A toolbar built for a placement that is not displayed in one does not collapse.
	 */
	public void testUnplacedCommandsYieldNoCollapsing() {
		ReactToolbarControl toolbar = build(CommandPlacement.CONTEXT_MENU);

		assertEquals(ToolbarOverflow.NONE, toolbar.getOverflow());
	}

	private ReactToolbarControl build(CommandPlacement placement) {
		CommandScope scope = new CommandScope(List.of(command("first", placement), command("second", placement)));

		ReactToolbarControl result =
			ToolbarBuilder.build(_context, scope, placement, new CliqueRegistry(), null);
		assertNotNull("Commands of the requested placement yield a toolbar.", result);
		return result;
	}

	private static CommandModel command(String name, CommandPlacement placement) {
		return new FakeCommandModelBase(name) {
			@Override
			public CommandPlacement getPlacement() {
				return placement;
			}
		};
	}
}
