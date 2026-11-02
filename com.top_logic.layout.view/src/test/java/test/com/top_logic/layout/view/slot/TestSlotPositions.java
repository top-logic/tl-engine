/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.slot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactSplitPanelControl;
import com.top_logic.layout.react.control.sidebar.ReactSidebarControl;
import com.top_logic.layout.view.slot.SlotContribution;
import com.top_logic.layout.view.slot.SlotPlaceholder;

/**
 * The regions of a container that displays several of them side by side are positions of their own
 * for slots: a {@code <slot-content>} in a region reaches the {@code <slot>} of the same name in its
 * own region, not one in a neighbouring region.
 *
 * <p>
 * Each region of a fixture holds a slot and a contribution of the same name. Were the regions one
 * position, all slots would be equally near to every contribution, and the slot registered first
 * would receive all of them.
 * </p>
 */
public class TestSlotPositions extends AbstractSlotViewTest {

	private static final String SPLIT_VIEW = "slot-positions-split.view.xml";

	private static final String SIDEBAR_VIEW = "slot-positions-sidebar.view.xml";

	/** The ID of the second item of {@link #SIDEBAR_VIEW}. */
	private static final String SECOND_ITEM = "second";

	@Override
	protected String[] fixtures() {
		return new String[] { SPLIT_VIEW, SIDEBAR_VIEW };
	}

	/**
	 * Each pane of a split panel - one with an id, one without - displays the contribution of its own
	 * pane.
	 */
	public void testSplitPanelPanes() throws IOException, ConfigurationException {
		createView(SPLIT_VIEW);
		loadPage("");

		List<ReactControl> panes = find(root(), ReactSplitPanelControl.class).displayedChildren();
		assertEquals("Panes of the split panel.", 2, panes.size());
		assertRoutedWithinRegions(panes);
	}

	/**
	 * The header of the rail and the displayed item of a sidebar each display the contribution of
	 * their own, also after the selection moved to another item.
	 */
	public void testSidebarItems() throws IOException, ConfigurationException {
		createView(SIDEBAR_VIEW);
		loadPage("");

		ReactSidebarControl sidebar = find(root(), ReactSidebarControl.class);
		List<ReactControl> regions = sidebar.displayedChildren();
		assertEquals("Header and displayed item of the sidebar.", 2, regions.size());
		assertRoutedWithinRegions(regions);

		sidebar.selectItem(SECOND_ITEM);

		regions = sidebar.displayedChildren();
		assertEquals("Header and displayed item of the sidebar after the switch.", 2, regions.size());
		assertRoutedWithinRegions(regions);
	}

	/**
	 * Each of the given regions holds one slot and one contribution, and the slot displays exactly
	 * the controls of the contribution of its own region.
	 */
	private void assertRoutedWithinRegions(List<ReactControl> regions) {
		assertEquals("Slots displayed.", regions.size(), registry().getPlaceholders().size());
		assertEquals("Contributions displayed.", regions.size(), contributions().size());

		for (ReactControl region : regions) {
			Set<ReactControl> contents = Collections.newSetFromMap(new IdentityHashMap<>());
			collectOwn(region, contents);

			List<SlotPlaceholder> placeholders = new ArrayList<>();
			for (SlotPlaceholder placeholder : registry().getPlaceholders()) {
				if (contents.contains(placeholder)) {
					placeholders.add(placeholder);
				}
			}
			List<SlotContribution> contributions = new ArrayList<>();
			for (SlotContribution contribution : contributions()) {
				if (contents.contains(contribution)) {
					contributions.add(contribution);
				}
			}
			assertEquals("Slots in region " + region.getID() + ".", 1, placeholders.size());
			assertEquals("Contributions in region " + region.getID() + ".", 1, contributions.size());

			assertEquals("The slot of region " + region.getID() + " displays the contribution of its region.",
				contributions.get(0).getControls(), ((ReactControl) placeholders.get(0)).displayedChildren());
		}
	}

	/**
	 * Adds the given control and the controls it displays, transitively, to the given set, without
	 * descending into what a slot displays: those controls are borrowed from the contribution routed
	 * to the slot and belong to wherever that contribution is written.
	 */
	private static void collectOwn(ReactControl control, Set<ReactControl> result) {
		if (!result.add(control) || control instanceof SlotPlaceholder) {
			return;
		}
		for (ReactControl child : control.displayedChildren()) {
			collectOwn(child, result);
		}
	}

	private static <T extends ReactControl> T find(ReactControl control, Class<T> type) {
		if (type.isInstance(control)) {
			return type.cast(control);
		}
		for (ReactControl child : control.displayedChildren()) {
			T found = find(child, type);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/**
	 * Test suite with the services of {@link AbstractSlotViewTest#createSuite(Class)}.
	 */
	public static Test suite() {
		return createSuite(TestSlotPositions.class);
	}
}
