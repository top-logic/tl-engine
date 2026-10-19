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
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.layout.ReactSplitPanelControl;
import com.top_logic.layout.react.control.layout.ReactSplitPanelControl.ChildConstraint;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.structure.OrientationAware.Orientation;

/**
 * Tests that a double click on the handle of a {@link ReactSplitPanelControl} gives the panes the sizes they started
 * with, and that the split reports the reset so that a personalization forgets the dragged sizes.
 */
public class TestSplitPanelReset extends TestCase {

	/** A drag turns the panes into pixel sizes; the reset gives them their configured percentages back. */
	public void testResetGivesTheConfiguredSizesBack() {
		ReactContext context = context();
		int[] resets = { 0 };
		ReactSplitPanelControl split = new ReactSplitPanelControl(context, Orientation.HORIZONTAL, true, null, null,
			() -> resets[0]++);
		ReactTextControl a = new ReactTextControl(context, "a");
		ReactTextControl b = new ReactTextControl(context, "b");
		split.addChild(a, ChildConstraint.percent(30));
		split.addChild(b, ChildConstraint.percent(70));

		split.executeCommand("updateSizes", Map.of("sizes", Map.of(a.getID(), 400, b.getID(), 600)));
		assertEquals(DisplayUnit.PIXEL.getExternalName(), child(split, 0).get("unit"));

		split.executeCommand("resetSizes", Map.of());
		assertEquals(30.0, ((Number) child(split, 0).get("size")).doubleValue(), 0.001);
		assertEquals(DisplayUnit.PERCENT.getExternalName(), child(split, 0).get("unit"));
		assertEquals(70.0, ((Number) child(split, 1).get("size")).doubleValue(), 0.001);
		assertEquals("The personalization must learn of the reset.", 1, resets[0]);
	}

	/** A pane added with a remembered size resets to the default given for it, not to the remembered one. */
	public void testResetUsesTheDefaultOfAPersonalizedPane() {
		ReactContext context = context();
		ReactSplitPanelControl split = new ReactSplitPanelControl(context, Orientation.HORIZONTAL, true);
		ReactTextControl a = new ReactTextControl(context, "a");
		split.addChild(a, ChildConstraint.pixels(420));
		split.setDefaultSize(a, 25, DisplayUnit.PERCENT);

		split.executeCommand("resetSizes", Map.of());
		assertEquals(25.0, ((Number) child(split, 0).get("size")).doubleValue(), 0.001);
		assertEquals(DisplayUnit.PERCENT.getExternalName(), child(split, 0).get("unit"));
	}

	private static Map<?, ?> child(ReactSplitPanelControl split, int index) {
		try {
			Map<?, ?> state = (Map<?, ?>) JSON.fromString(split.stateAsJSON());
			return (Map<?, ?>) ((List<?>) state.get("children")).get(index);
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

	private static ReactContext context() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}
}
