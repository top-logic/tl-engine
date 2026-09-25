/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.overlay;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.json.JSON;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactToolbarControl;
import com.top_logic.layout.react.control.layout.ToolbarGroupDisplay;
import com.top_logic.layout.react.control.layout.ToolbarOverflow;
import com.top_logic.layout.react.control.overlay.ReactWindowControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.WindowState;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests the footer of a {@link ReactWindowControl}: one collapsing toolbar carrying the window's
 * actions ahead of its button-bar commands, however the two are handed to the window.
 */
public class TestReactWindowControl extends TestCase {

	/** State key holding the client state of a control descriptor. */
	private static final String STATE = "state";

	private ReactContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/** A window given neither actions nor a button bar has no footer. */
	public void testAWindowWithoutCommandsHasNoFooter() {
		assertNull("Nothing to show in the footer.", state(window()).get(WindowState.FOOTER__PROP));
	}

	/** Actions alone are shown in a footer toolbar the window builds, collapsing from its leading end. */
	public void testActionsAloneFormTheFooter() {
		ReactWindowControl window = window();
		window.setActions(List.of(item()));

		Map<?, ?> footer = footer(window);
		assertEquals(List.of(ReactWindowControl.ACTIONS_CLIQUE), groupNames(footer));
		assertEquals("The footer folds its leading end first, keeping the primary command visible.",
			ToolbarOverflow.LEADING.getExternalName(), footer.get(ReactToolbarControl.OVERFLOW));
	}

	/** The button bar set before the actions carries them: the footer is one toolbar. */
	public void testTheButtonBarTakesTheActions() {
		ReactWindowControl window = window();
		window.setButtonBar(buttonBar("apply"));
		window.setActions(List.of(item()));

		assertEquals("The actions lead the footer, the scope's commands follow.",
			List.of(ReactWindowControl.ACTIONS_CLIQUE, "apply"), groupNames(footer(window)));
	}

	/** Actions set before the button bar move on to it: the footer is still one toolbar. */
	public void testActionsSetBeforeTheButtonBarMoveOnToIt() {
		ReactWindowControl window = window();
		window.setActions(List.of(item()));
		window.setButtonBar(buttonBar("apply"));

		assertEquals(List.of(ReactWindowControl.ACTIONS_CLIQUE, "apply"), groupNames(footer(window)));
	}

	/** The button bar rebuilt for a changed command scope keeps the window's actions. */
	public void testARebuiltButtonBarKeepsTheActions() {
		ReactWindowControl window = window();
		ReactToolbarControl buttonBar = buttonBar("apply");
		window.setButtonBar(buttonBar);
		window.setActions(List.of(item()));

		buttonBar.replaceGroups(buttonBar("save"));

		assertEquals("The rebuild swaps the scope's commands and keeps the actions.",
			List.of(ReactWindowControl.ACTIONS_CLIQUE, "save"), groupNames(footer(window)));
		assertEquals(ToolbarOverflow.LEADING.getExternalName(),
			footer(window).get(ReactToolbarControl.OVERFLOW));
	}

	/** The client state of the window's footer toolbar. */
	private Map<?, ?> footer(ReactWindowControl window) {
		Object footer = state(window).get(WindowState.FOOTER__PROP);
		assertNotNull("The window shows a footer.", footer);
		return (Map<?, ?>) ((Map<?, ?>) footer).get(STATE);
	}

	/** The clique names of the groups in the given toolbar state, in display order. */
	private static List<String> groupNames(Map<?, ?> toolbarState) {
		List<?> groups = (List<?>) toolbarState.get(ReactToolbarControl.GROUPS);
		return groups.stream()
			.map(group -> (String) ((Map<?, ?>) group).get(ReactToolbarControl.GROUP_NAME))
			.toList();
	}

	private static Map<?, ?> state(ReactWindowControl window) {
		try {
			return (Map<?, ?>) JSON.fromString(window.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

	/** A toolbar as the command scope builds it for the window's button-bar commands. */
	private ReactToolbarControl buttonBar(String clique) {
		ReactToolbarControl result = new ReactToolbarControl(_context);
		result.setOverflow(ToolbarOverflow.LEADING);
		result.addGroup(clique, ToolbarGroupDisplay.INLINE, null, null, List.of(item()));
		return result;
	}

	private ReactWindowControl window() {
		return new ReactWindowControl(_context, "test", DisplayDimension.px(400), () -> {
			// The test never closes the window.
		});
	}

	/** A control standing for a command button. */
	private ReactControl item() {
		return new ReactControl(_context, null, "TLText");
	}

}
