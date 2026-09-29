/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.layout.view.channel.ChannelNotificationScope;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Tests for {@link ChannelNotificationScope}: actions deferred from inside a channel notification
 * run only after the outermost notification has unwound.
 */
public class TestChannelNotificationScope extends TestCase {

	/**
	 * Tests that an action registered outside any notification runs immediately.
	 */
	public void testImmediateWhenIdle() {
		boolean[] ran = {false};
		ChannelNotificationScope.current().afterNotification(() -> ran[0] = true);
		assertTrue("Action must run immediately outside a notification", ran[0]);
	}

	/**
	 * Tests that an action deferred from inside a listener runs only after all listeners of the
	 * notification have been called.
	 */
	public void testDeferredUntilNotificationUnwinds() {
		ViewChannel channel = new DefaultViewChannel("test");
		List<String> log = new ArrayList<>();

		// First listener defers an action (like a control disposing its replaced subtree).
		channel.addListener((sender, oldVal, newVal) -> {
			log.add("first");
			ChannelNotificationScope.current().afterNotification(() -> log.add("deferred"));
		});
		// Second listener must run before the deferred action, even though it is notified later.
		channel.addListener((sender, oldVal, newVal) -> log.add("second"));

		channel.set("value");

		assertEquals(List.of("first", "second", "deferred"), log);
	}

	/**
	 * Tests that with nested channel writes (a listener setting another channel), deferred actions
	 * of the inner notification run when the inner notification unwinds, and actions of the outer
	 * one when the outer unwinds.
	 */
	public void testNestedNotifications() {
		ViewChannel outer = new DefaultViewChannel("outer");
		ViewChannel inner = new DefaultViewChannel("inner");
		List<String> log = new ArrayList<>();

		inner.addListener((sender, oldVal, newVal) -> {
			log.add("inner-listener");
			ChannelNotificationScope.current().afterNotification(() -> log.add("inner-deferred"));
		});
		outer.addListener((sender, oldVal, newVal) -> {
			log.add("outer-listener");
			ChannelNotificationScope.current().afterNotification(() -> log.add("outer-deferred"));
			inner.set("cascade");
		});
		outer.addListener((sender, oldVal, newVal) -> log.add("outer-second"));

		outer.set("value");

		assertEquals(
			List.of("outer-listener", "inner-listener", "outer-second", "outer-deferred", "inner-deferred"),
			log);
	}

	/**
	 * Tests the failure scenario behind the deferral: a listener that tears down another listener's
	 * owner must not prevent that listener (already in the notification's snapshot) from completing
	 * before the teardown runs.
	 */
	public void testDisposalDeferredAcrossListenerSnapshot() {
		ViewChannel channel = new DefaultViewChannel("selection");
		boolean[] disposed = {false};
		boolean[] ranAfterDisposal = {false};

		// Listener 1: rebuilds its presentation and defers disposal of the old subtree.
		channel.addListener((sender, oldVal, newVal) ->
			ChannelNotificationScope.current().afterNotification(() -> disposed[0] = true));
		// Listener 2: belongs to the old subtree; still in the snapshot and must run undisposed.
		channel.addListener((sender, oldVal, newVal) -> ranAfterDisposal[0] = disposed[0]);

		channel.set("selected");

		assertTrue("Deferred disposal must run after the notification", disposed[0]);
		assertFalse("The snapshot listener must run before the deferred disposal", ranAfterDisposal[0]);
	}

	/**
	 * Tests that a failing deferred action neither prevents the other deferred actions from running
	 * nor leaves the scope in a notification: an action registered afterwards, outside any
	 * notification, runs immediately.
	 */
	public void testFailingDeferredActionDoesNotSkipOthers() {
		ViewChannel channel = new DefaultViewChannel("test");
		List<String> log = new ArrayList<>();

		channel.addListener((sender, oldVal, newVal) -> {
			ChannelNotificationScope scope = ChannelNotificationScope.current();
			scope.afterNotification(() -> log.add("before"));
			scope.afterNotification(() -> {
				throw new IllegalStateException("Expected failure of a deferred action.");
			});
			scope.afterNotification(() -> log.add("after"));
		});

		channel.set("value");

		assertEquals(List.of("before", "after"), log);

		ChannelNotificationScope.current().afterNotification(() -> log.add("idle"));
		assertEquals("The scope must not remain in a notification after a failing action.",
			List.of("before", "after", "idle"), log);
	}

	/**
	 * Tests that a listener failure still runs the actions deferred before it and leaves the scope
	 * clean, while the listener's exception reaches the writer of the channel.
	 */
	public void testFailingListenerRunsDeferredActions() {
		ViewChannel channel = new DefaultViewChannel("test");
		List<String> log = new ArrayList<>();

		channel.addListener((sender, oldVal, newVal) -> {
			ChannelNotificationScope.current().afterNotification(() -> log.add("deferred"));
			throw new IllegalStateException("Expected listener failure.");
		});

		try {
			channel.set("value");
			fail("The listener failure must reach the writer.");
		} catch (IllegalStateException ex) {
			assertEquals("Expected listener failure.", ex.getMessage());
		}
		assertEquals(List.of("deferred"), log);

		ChannelNotificationScope.current().afterNotification(() -> log.add("idle"));
		assertEquals(List.of("deferred", "idle"), log);
	}

}
