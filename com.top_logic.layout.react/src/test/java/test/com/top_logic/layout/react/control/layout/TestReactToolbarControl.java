/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.layout;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.json.JSON;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactToolbarControl;
import com.top_logic.layout.react.control.layout.ToolbarGroupDisplay;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests the group composition of a {@link ReactToolbarControl}: the pinned group leads the groups
 * the client receives and stays through a rebuild of the others.
 */
public class TestReactToolbarControl extends TestCase {

	private ReactContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/** The pinned group is published before the groups added for the toolbar's commands. */
	public void testThePinnedGroupLeadsTheGroups() {
		ReactToolbarControl toolbar = new ReactToolbarControl(_context);
		toolbar.addGroup("edit", ToolbarGroupDisplay.INLINE, null, null, List.of(item()));
		toolbar.setPinnedGroup("pinned", ToolbarGroupDisplay.INLINE, List.of(item()));

		assertEquals(List.of("pinned", "edit"), groupNames(toolbar));
	}

	/** A rebuild swaps the added groups and keeps the pinned one at the leading end. */
	public void testThePinnedGroupSurvivesARebuild() {
		ReactToolbarControl toolbar = new ReactToolbarControl(_context);
		toolbar.setPinnedGroup("pinned", ToolbarGroupDisplay.INLINE, List.of(item()));
		toolbar.addGroup("edit", ToolbarGroupDisplay.INLINE, null, null, List.of(item()));

		ReactToolbarControl rebuilt = new ReactToolbarControl(_context);
		rebuilt.addGroup("view", ToolbarGroupDisplay.INLINE, null, null, List.of(item()));
		toolbar.replaceGroups(rebuilt);

		assertEquals("The rebuild replaces the added groups and keeps the pinned one.",
			List.of("pinned", "view"), groupNames(toolbar));
	}

	/** A rebuild that finds no command leaves the pinned group as the toolbar's only content. */
	public void testARebuildWithoutCommandsKeepsThePinnedGroup() {
		ReactToolbarControl toolbar = new ReactToolbarControl(_context);
		toolbar.setPinnedGroup("pinned", ToolbarGroupDisplay.INLINE, List.of(item()));
		toolbar.addGroup("edit", ToolbarGroupDisplay.INLINE, null, null, List.of(item()));

		toolbar.replaceGroups(new ReactToolbarControl(_context));

		assertEquals(List.of("pinned"), groupNames(toolbar));
		assertFalse("The pinned items are content of the toolbar.", toolbar.isEmpty());
	}

	/** A toolbar whose only content is its pinned group is not empty. */
	public void testAPinnedGroupIsContent() {
		ReactToolbarControl toolbar = new ReactToolbarControl(_context);
		assertTrue(toolbar.isEmpty());

		toolbar.setPinnedGroup("pinned", ToolbarGroupDisplay.INLINE, List.of(item()));

		assertFalse(toolbar.isEmpty());
	}

	/** Pinning nothing leaves the toolbar without a pinned group. */
	public void testPinningNothingDropsTheGroup() {
		ReactToolbarControl toolbar = new ReactToolbarControl(_context);
		toolbar.setPinnedGroup("pinned", ToolbarGroupDisplay.INLINE, List.of(item()));

		toolbar.setPinnedGroup("pinned", ToolbarGroupDisplay.INLINE, List.of());

		assertEquals(List.of(), groupNames(toolbar));
		assertTrue(toolbar.isEmpty());
	}

	/** The clique names of the groups the given toolbar publishes, in display order. */
	private static List<String> groupNames(ReactToolbarControl toolbar) {
		List<?> groups = (List<?>) state(toolbar).get(ReactToolbarControl.GROUPS);
		return groups.stream()
			.map(group -> (String) ((Map<?, ?>) group).get(ReactToolbarControl.GROUP_NAME))
			.toList();
	}

	/** The client state of the given toolbar. */
	private static Map<?, ?> state(ReactToolbarControl toolbar) {
		try {
			return (Map<?, ?>) JSON.fromString(toolbar.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

	/** A control standing for a command button. */
	private ReactControl item() {
		return new ReactControl(_context, null, "TLText");
	}

}
