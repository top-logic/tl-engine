/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.slot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.sidebar.ReactSidebarControl;
import com.top_logic.layout.react.protocol.PatchEvent;
import com.top_logic.layout.react.protocol.SSEEvent;
import com.top_logic.layout.react.protocol.StateEvent;
import com.top_logic.layout.react.servlet.DeliveryScope;
import com.top_logic.layout.react.state.ChildControl;
import com.top_logic.layout.view.slot.SlotRegistry;
import com.top_logic.layout.view.slot.control.SlotPlaceholderControl;

/**
 * A slot switches from the contribution of the content a container leaves to the contribution of
 * the content it displays in one step, and a page is written with its slots filled.
 *
 * <p>
 * The fixture holds the slot in the header of a sidebar rail, and a contribution to it in each of
 * the two items: switching the item exchanges the contribution the slot shows.
 * </p>
 *
 * @see SlotRegistry#settleRouting()
 */
public class TestSlotSwitch extends AbstractSlotViewTest {

	private static final String SWITCH_VIEW = "slot-switch.view.xml";

	/** The ID of the item of {@link #SWITCH_VIEW} displayed first. */
	private static final String FIRST_ITEM = "first";

	/** The ID of the other item of {@link #SWITCH_VIEW}. */
	private static final String SECOND_ITEM = "second";

	/** The title of the fallback panel of the slot of {@link #SWITCH_VIEW}. */
	private static final String FALLBACK_TITLE = "Fallback";

	/** The title of the panel the item {@link #FIRST_ITEM} contributes to the slot. */
	private static final String FIRST_TITLE = "First action";

	@Override
	protected String[] fixtures() {
		return new String[] { SWITCH_VIEW };
	}

	/**
	 * The rendered page shows the contribution of the displayed item in the slot, not the fallback.
	 */
	public void testPageShowsRoutedContribution() throws IOException, ConfigurationException {
		createView(SWITCH_VIEW);
		String page = renderPage("");

		assertEquals("Contributions displayed.", 1, contributions().size());
		assertEquals("The slot shows the contribution.", contributions().get(0).getControls(),
			slot().displayedChildren());
		assertTrue("The page shows the contribution: " + page, page.contains(FIRST_TITLE));
		assertFalse("The page does not show the fallback: " + page, page.contains(FALLBACK_TITLE));
	}

	/**
	 * Switching the item within one interaction updates the slot once, from the contribution of the
	 * item left to the one of the item displayed.
	 */
	public void testSwitchInOneStep() throws IOException, ConfigurationException {
		createView(SWITCH_VIEW);
		loadPage("");

		SlotPlaceholderControl slot = slot();
		List<ReactControl> left = contributions().get(0).getControls();
		int eventsBefore = sentEvents().size();

		DeliveryScope.enter();
		try {
			sidebar().selectItem(SECOND_ITEM);
		} finally {
			DeliveryScope.exit();
		}

		assertEquals("Contributions displayed.", 1, contributions().size());
		List<ReactControl> displayed = contributions().get(0).getControls();
		assertEquals("The slot shows the contribution of the displayed item.", displayed,
			slot.displayedChildren());

		List<String> updates = updatesOf(slot, sentEvents().subList(eventsBefore, sentEvents().size()));
		assertEquals("Updates of the slot: " + updates, 1, updates.size());
		String update = updates.get(0);
		for (ReactControl control : displayed) {
			assertTrue("The update shows the contribution of the displayed item: " + update,
				update.contains(childReference(control)));
		}
		for (ReactControl control : left) {
			assertFalse("The update drops the contribution of the item left: " + update,
				update.contains(childReference(control)));
		}

		DeliveryScope.enter();
		try {
			sidebar().selectItem(FIRST_ITEM);
		} finally {
			DeliveryScope.exit();
		}
		assertEquals("The slot shows the contribution of the item displayed again.",
			contributions().get(0).getControls(), slot.displayedChildren());
	}

	/**
	 * The patches and states of the given events that address the given control.
	 */
	private static List<String> updatesOf(ReactControl control, List<SSEEvent> events) {
		List<String> result = new ArrayList<>();
		for (SSEEvent event : events) {
			if (event instanceof PatchEvent patch && patch.getControlId().equals(control.getID())) {
				result.add(patch.getPatch());
			} else if (event instanceof StateEvent state && state.getControlId().equals(control.getID())) {
				result.add(state.getState());
			}
		}
		return result;
	}

	/**
	 * The JSON fragment by which a serialized state embeds the given control.
	 */
	private static String childReference(ReactControl control) {
		return "\"" + ChildControl.CONTROL_ID__PROP + "\":\"" + control.getID() + "\"";
	}

	private SlotPlaceholderControl slot() {
		return find(root(), SlotPlaceholderControl.class);
	}

	private ReactSidebarControl sidebar() {
		return find(root(), ReactSidebarControl.class);
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
		return createSuite(TestSlotSwitch.class);
	}
}
