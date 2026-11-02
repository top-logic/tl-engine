/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.slot;

import java.io.IOException;
import java.util.List;

import junit.framework.Test;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.sidebar.ReactSidebarControl;
import com.top_logic.layout.view.slot.SlotContribution;
import com.top_logic.layout.view.slot.SlotElement;
import com.top_logic.layout.view.slot.control.SlotPlaceholderControl;

/**
 * The {@link SlotElement.Config#getEmpty() fallback} of a {@code <slot>} is displayed while no
 * contribution is routed to the slot, gives way to a contribution and comes back when the
 * contribution leaves the display.
 *
 * <p>
 * The fixture holds the slot in the header of a sidebar rail, and a contribution to it in the
 * second item only: while the first item is displayed, the slot shows its fallback.
 * </p>
 */
public class TestSlotFallback extends AbstractSlotViewTest {

	private static final String FALLBACK_VIEW = "slot-fallback.view.xml";

	/** The ID of the item of {@link #FALLBACK_VIEW} without contribution. */
	private static final String FIRST_ITEM = "first";

	/** The ID of the item of {@link #FALLBACK_VIEW} contributing to the slot. */
	private static final String SECOND_ITEM = "second";

	@Override
	protected String[] fixtures() {
		return new String[] { FALLBACK_VIEW };
	}

	/**
	 * The slot switches between its fallback and the contribution as the contribution enters and
	 * leaves the display, and the fallback is attached exactly while it is displayed.
	 */
	public void testSwitchBetweenFallbackAndContribution() throws IOException, ConfigurationException {
		createView(FALLBACK_VIEW);
		loadPage("");

		SlotPlaceholderControl slot = find(root(), SlotPlaceholderControl.class);
		ReactControl fallback = assertShowsFallback(slot);

		sidebar().selectItem(SECOND_ITEM);

		assertEquals("Contributions displayed.", 1, contributions().size());
		SlotContribution contribution = contributions().get(0);
		assertEquals("The contribution replaces the fallback.", contribution.getControls(),
			slot.displayedChildren());
		assertFalse("The hidden fallback is detached.", fallback.isAttached());
		assertFalse("The hidden fallback is kept.", fallback.isDisposed());

		sidebar().selectItem(FIRST_ITEM);

		assertEquals("Contributions displayed after leaving the item.", 0, contributions().size());
		assertSame("The fallback is shown again.", fallback, assertShowsFallback(slot));
	}

	/**
	 * Disposing the view disposes the fallback while it is displayed.
	 */
	public void testDisposeDisplayedFallback() throws IOException, ConfigurationException {
		createView(FALLBACK_VIEW);
		loadPage("");

		ReactControl fallback = assertShowsFallback(find(root(), SlotPlaceholderControl.class));

		root().cleanupTree();

		assertTrue("The fallback is disposed with the view.", fallback.isDisposed());
	}

	/**
	 * Disposing the view disposes the fallback also while a contribution covers it.
	 */
	public void testDisposeHiddenFallback() throws IOException, ConfigurationException {
		createView(FALLBACK_VIEW);
		loadPage("");

		SlotPlaceholderControl slot = find(root(), SlotPlaceholderControl.class);
		ReactControl fallback = assertShowsFallback(slot);
		sidebar().selectItem(SECOND_ITEM);
		assertFalse("The fallback is hidden.", slot.displayedChildren().contains(fallback));
		List<ReactControl> contributed = contributions().get(0).getControls();

		root().cleanupTree();

		assertTrue("The hidden fallback is disposed with the view.", fallback.isDisposed());
		for (ReactControl control : contributed) {
			assertTrue("The contribution is disposed by its declaring view.", control.isDisposed());
		}
	}

	/**
	 * Asserts that the given slot displays exactly one attached fallback control and returns it.
	 */
	private static ReactControl assertShowsFallback(SlotPlaceholderControl slot) {
		List<ReactControl> displayed = slot.displayedChildren();
		assertEquals("The slot displays its fallback.", 1, displayed.size());
		ReactControl fallback = displayed.get(0);
		assertTrue("The displayed fallback is attached.", fallback.isAttached());
		assertFalse("The displayed fallback is not disposed.", fallback.isDisposed());
		return fallback;
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
		return createSuite(TestSlotFallback.class);
	}
}
