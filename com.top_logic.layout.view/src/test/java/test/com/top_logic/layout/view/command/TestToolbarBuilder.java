/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.json.JSON;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;

import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.CommandPlacement;
import com.top_logic.layout.react.control.layout.ReactToolbarControl;
import com.top_logic.layout.react.control.layout.ToolbarGroupDisplay;
import com.top_logic.layout.react.control.layout.ToolbarOverflow;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.command.CliqueRegistry;
import com.top_logic.layout.view.command.CliqueRegistry.CliqueInfo;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.command.ToolbarBuilder;

/**
 * Tests the {@link ToolbarBuilder}: the {@link ToolbarOverflow} it derives from the placement its
 * toolbar is built for, and the groups it forms from the cliques of its commands.
 */
public class TestToolbarBuilder extends TestCase {

	private static final String FIRST = "first";

	private static final String SECOND = "second";

	private static final String MENU = "menu";

	private static final String UNKNOWN = "unknown";

	private static final String MENU_LABEL = "Menu label";

	/** The cliques the toolbars of this test are built with, in display order. */
	private static final CliqueRegistry REGISTRY = new CliqueRegistry(List.of(
		new CliqueInfo(FIRST, ToolbarGroupDisplay.INLINE, null, null),
		new CliqueInfo(SECOND, ToolbarGroupDisplay.INLINE, null, null),
		new CliqueInfo(MENU, ToolbarGroupDisplay.MENU, ResKey.text(MENU_LABEL), null)));

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
			CommandPlacement.BUTTON_BAR, REGISTRY, null);

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

	/**
	 * The groups follow the order of the registered cliques, not the order of the commands.
	 */
	public void testGroupsFollowTheCliqueOrder() {
		ReactToolbarControl toolbar = build(command("m", MENU), command("s", SECOND), command("f", FIRST));

		assertEquals(List.of(FIRST, SECOND, MENU), names(groups(toolbar)));
	}

	/**
	 * A clique registered as menu yields a menu group labelled in the language of the user.
	 */
	public void testMenuCliqueIsLabelled() {
		ReactToolbarControl toolbar = build(command("m", MENU));

		Map<?, ?> group = groups(toolbar).get(0);
		assertEquals(ToolbarGroupDisplay.MENU.getExternalName(), group.get(ReactToolbarControl.GROUP_DISPLAY));
		assertEquals(MENU_LABEL, group.get(ReactToolbarControl.GROUP_LABEL));
	}

	/**
	 * A clique that is not registered yields an inline group without label after the registered
	 * ones.
	 */
	public void testUnregisteredCliqueIsAppendedInline() {
		ReactToolbarControl toolbar = build(command("u", UNKNOWN), command("m", MENU), command("f", FIRST));

		List<Map<?, ?>> groups = groups(toolbar);
		assertEquals(List.of(FIRST, MENU, UNKNOWN), names(groups));
		Map<?, ?> unknown = groups.get(2);
		assertEquals(ToolbarGroupDisplay.INLINE.getExternalName(), unknown.get(ReactToolbarControl.GROUP_DISPLAY));
		assertNull(unknown.get(ReactToolbarControl.GROUP_LABEL));
		assertNull(unknown.get(ReactToolbarControl.GROUP_ICON));
	}

	private ReactToolbarControl build(CommandModel... commands) {
		ReactToolbarControl result = ToolbarBuilder.build(_context, new CommandScope(List.of(commands)),
			CommandPlacement.TOOLBAR, REGISTRY, null);
		assertNotNull("Commands of the requested placement yield a toolbar.", result);
		return result;
	}

	/** The clique names of the given groups. */
	private static List<Object> names(List<Map<?, ?>> groups) {
		return groups.stream().map(group -> (Object) group.get(ReactToolbarControl.GROUP_NAME)).toList();
	}

	/** The groups the given toolbar publishes to the client, in display order. */
	private static List<Map<?, ?>> groups(ReactToolbarControl toolbar) {
		Map<?, ?> state;
		try {
			state = (Map<?, ?>) JSON.fromString(toolbar.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("Not a state object.", ex);
		}
		return ((List<?>) state.get(ReactToolbarControl.GROUPS)).stream().<Map<?, ?>> map(g -> (Map<?, ?>) g).toList();
	}

	private ReactToolbarControl build(CommandPlacement placement) {
		CommandScope scope = new CommandScope(List.of(command("first", placement), command("second", placement)));

		ReactToolbarControl result =
			ToolbarBuilder.build(_context, scope, placement, REGISTRY, null);
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

	private static CommandModel command(String name, String clique) {
		return new FakeCommandModelBase(name) {
			@Override
			public CommandPlacement getPlacement() {
				return CommandPlacement.TOOLBAR;
			}

			@Override
			public String getClique() {
				return clique;
			}
		};
	}

	/**
	 * Test suite providing the services that turn the label of a menu clique into the text
	 * displayed.
	 */
	public static Test suite() throws ModuleException {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestToolbarBuilder.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}
}
