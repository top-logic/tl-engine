/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import java.util.List;
import java.util.function.Function;

import junit.framework.TestCase;

import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.DerivedViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;

/**
 * Tests for {@link DerivedViewChannel}.
 */
public class TestDerivedViewChannel extends TestCase {

	/**
	 * Tests that the derived channel computes its initial value eagerly.
	 */
	public void testInitialValueComputed() {
		DefaultViewChannel input = new DefaultViewChannel("input");
		input.set("hello");

		DerivedViewChannel derived = new DerivedViewChannel("derived");
		derived.bind(List.of(input), args -> args[0]);

		assertEquals("hello", derived.get());
	}

	/**
	 * Tests that the derived channel recomputes when an input changes.
	 */
	public void testRecomputesOnInputChange() {
		DefaultViewChannel input = new DefaultViewChannel("input");
		input.set("initial");

		DerivedViewChannel derived = new DerivedViewChannel("derived");
		derived.bind(List.of(input), args -> args[0]);

		assertEquals("initial", derived.get());

		input.set("updated");
		assertEquals("updated", derived.get());
	}

	/**
	 * Tests that set() throws IllegalStateException.
	 */
	public void testSetThrows() {
		DerivedViewChannel derived = new DerivedViewChannel("derived");
		derived.bind(List.of(), args -> null);

		try {
			derived.set("value");
			fail("Expected IllegalStateException");
		} catch (IllegalStateException expected) {
			assertTrue(expected.getMessage().contains("derived"));
			assertTrue(expected.getMessage().contains("read-only"));
		}
	}

	/**
	 * Tests that listeners are notified when the derived value changes.
	 */
	public void testListenerNotifiedOnChange() {
		DefaultViewChannel input = new DefaultViewChannel("input");
		input.set(null);

		// Computes: input != null
		Function<Object[], Object> isNotNull = args -> args[0] != null;

		DerivedViewChannel derived = new DerivedViewChannel("derived");
		derived.bind(List.of(input), isNotNull);

		assertEquals(Boolean.FALSE, derived.get());

		Object[] captured = new Object[3];
		ChannelListener listener = (sender, oldVal, newVal) -> {
			captured[0] = sender;
			captured[1] = oldVal;
			captured[2] = newVal;
		};
		derived.addListener(listener);

		input.set("something");

		assertSame(derived, captured[0]);
		assertEquals(Boolean.FALSE, captured[1]);
		assertEquals(Boolean.TRUE, captured[2]);
	}

	/**
	 * Tests that listeners are NOT notified when the recomputed value equals the current value.
	 */
	public void testNoNotificationOnEqualValue() {
		DefaultViewChannel input1 = new DefaultViewChannel("input1");
		DefaultViewChannel input2 = new DefaultViewChannel("input2");
		input1.set("a");
		input2.set("b");

		// Always returns constant "fixed" regardless of inputs.
		DerivedViewChannel derived = new DerivedViewChannel("derived");
		derived.bind(List.of(input1, input2), args -> "fixed");

		assertEquals("fixed", derived.get());

		int[] callCount = {0};
		derived.addListener((sender, oldVal, newVal) -> callCount[0]++);

		// Change input -- recomputed value is still "fixed".
		input1.set("changed");
		assertEquals(0, callCount[0]);
	}

	/**
	 * Tests multi-input derived channels.
	 */
	public void testMultiInput() {
		DefaultViewChannel input1 = new DefaultViewChannel("a");
		DefaultViewChannel input2 = new DefaultViewChannel("b");
		input1.set(null);
		input2.set(null);

		// Returns true if both inputs are non-null.
		Function<Object[], Object> bothNonNull = args -> args[0] != null && args[1] != null;

		DerivedViewChannel derived = new DerivedViewChannel("derived");
		derived.bind(List.of(input1, input2), bothNonNull);

		assertEquals(Boolean.FALSE, derived.get());

		input1.set("x");
		assertEquals(Boolean.FALSE, derived.get());

		input2.set("y");
		assertEquals(Boolean.TRUE, derived.get());
	}

	/**
	 * Tests that removeListener prevents further notifications.
	 */
	public void testRemoveListener() {
		DefaultViewChannel input = new DefaultViewChannel("input");
		input.set(null);

		DerivedViewChannel derived = new DerivedViewChannel("derived");
		derived.bind(List.of(input), args -> args[0]);

		int[] callCount = {0};
		ChannelListener listener = (sender, oldVal, newVal) -> callCount[0]++;
		derived.addListener(listener);

		input.set("a");
		assertEquals(1, callCount[0]);

		derived.removeListener(listener);
		input.set("b");
		assertEquals("Listener should not be called after removal", 1, callCount[0]);
	}
}
