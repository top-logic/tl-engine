/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import junit.framework.TestCase;

import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;

/**
 * Tests for {@link DefaultViewChannel}.
 */
public class TestDefaultViewChannel extends TestCase {

	/**
	 * Tests that a new channel starts with a null value.
	 */
	public void testInitialValueIsNull() {
		ViewChannel channel = new DefaultViewChannel("test");
		assertNull(channel.get());
	}

	/**
	 * Tests basic get/set behavior.
	 */
	public void testSetAndGet() {
		ViewChannel channel = new DefaultViewChannel("test");

		assertTrue("First set should return true", channel.set("hello"));
		assertEquals("hello", channel.get());

		assertTrue("Different value should return true", channel.set("world"));
		assertEquals("world", channel.get());
	}

	/**
	 * Tests that setting the same value returns false and does not fire listeners.
	 */
	public void testSetSameValueReturnsFalse() {
		ViewChannel channel = new DefaultViewChannel("test");

		channel.set("value");
		assertFalse("Same value should return false", channel.set("value"));
	}

	/**
	 * Tests that setting null on a null channel returns false.
	 */
	public void testSetNullOnNullReturnsFalse() {
		ViewChannel channel = new DefaultViewChannel("test");
		assertFalse("Null to null should return false", channel.set(null));
	}

	/**
	 * Tests that listeners are notified on value change.
	 */
	public void testListenerNotified() {
		ViewChannel channel = new DefaultViewChannel("test");

		Object[] captured = new Object[3];
		ChannelListener listener = (sender, oldVal, newVal) -> {
			captured[0] = sender;
			captured[1] = oldVal;
			captured[2] = newVal;
		};

		channel.addListener(listener);
		channel.set("first");

		assertSame("Sender should be the channel", channel, captured[0]);
		assertNull("Old value should be null", captured[1]);
		assertEquals("New value should be 'first'", "first", captured[2]);
	}

	/**
	 * Tests that listeners are NOT notified when value does not change.
	 */
	public void testListenerNotNotifiedOnSameValue() {
		ViewChannel channel = new DefaultViewChannel("test");
		channel.set("value");

		int[] callCount = {0};
		channel.addListener((sender, oldVal, newVal) -> callCount[0]++);

		channel.set("value");
		assertEquals("Listener should not be called", 0, callCount[0]);
	}

	/**
	 * Tests that a removed listener is no longer notified.
	 */
	public void testRemoveListener() {
		ViewChannel channel = new DefaultViewChannel("test");

		int[] callCount = {0};
		ChannelListener listener = (sender, oldVal, newVal) -> callCount[0]++;

		channel.addListener(listener);
		channel.set("a");
		assertEquals(1, callCount[0]);

		channel.removeListener(listener);
		channel.set("b");
		assertEquals("Listener should not be called after removal", 1, callCount[0]);
	}
}
