/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import junit.framework.TestCase;

import com.top_logic.layout.view.channel.ChannelVetoException;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.VetoListener;
import com.top_logic.layout.view.form.StateHandler;

/**
 * Tests for the veto mechanism in {@link DefaultViewChannel}.
 */
public class TestChannelVeto extends TestCase {

	/**
	 * Creates a stub {@link StateHandler} for use in tests.
	 *
	 * @param dirty
	 *        Whether the handler reports dirty state.
	 * @param hasErrors
	 *        Whether the handler reports errors.
	 * @param description
	 *        Human-readable description.
	 * @return A new stub {@link StateHandler}.
	 */
	private static StateHandler stubHandler(boolean dirty, boolean hasErrors, String description) {
		return new StateHandler() {
			private boolean _dirty = dirty;

			@Override
			public boolean isDirty() {
				return _dirty;
			}

			@Override
			public boolean hasErrors() {
				return hasErrors;
			}

			@Override
			public void executeSave() {
				_dirty = false;
			}

			@Override
			public void executeDiscard() {
				_dirty = false;
			}

			@Override
			public String getDescription() {
				return description;
			}
		};
	}

	/**
	 * A veto listener returns a non-null handler, causing {@link ChannelVetoException} to be thrown
	 * and the channel value to remain unchanged.
	 */
	public void testVetoBlocksChange() {
		DefaultViewChannel channel = new DefaultViewChannel("test");
		channel.set("initial");

		StateHandler handler = stubHandler(true, false, "Form A");
		channel.addVetoListener((sender, oldValue, newValue) -> handler);

		try {
			channel.set("new");
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			// expected
		}

		assertEquals("Value must remain unchanged after veto", "initial", channel.get());
	}

	/**
	 * Regular {@link ViewChannel.ChannelListener}s must NOT be notified when a veto blocks the
	 * change.
	 */
	public void testListenersNotNotifiedOnVeto() {
		DefaultViewChannel channel = new DefaultViewChannel("test");
		channel.set("initial");

		int[] callCount = { 0 };
		channel.addListener((sender, oldVal, newVal) -> callCount[0]++);

		StateHandler handler = stubHandler(true, false, "Form A");
		channel.addVetoListener((sender, oldValue, newValue) -> handler);

		try {
			channel.set("new");
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			// expected
		}

		assertEquals("Listener must not be called when veto blocks", 0, callCount[0]);
	}

	/**
	 * When a veto listener returns {@code null}, the change is allowed to proceed normally.
	 */
	public void testNullVetoAllowsChange() {
		DefaultViewChannel channel = new DefaultViewChannel("test");
		channel.set("initial");

		channel.addVetoListener((sender, oldValue, newValue) -> null);

		assertTrue("Set should return true", channel.set("new"));
		assertEquals("new", channel.get());
	}

	/**
	 * When two veto listeners both return handlers, the exception carries both handlers.
	 */
	public void testMultipleVetosCollected() {
		DefaultViewChannel channel = new DefaultViewChannel("test");

		StateHandler handlerA = stubHandler(true, false, "Form A");
		StateHandler handlerB = stubHandler(true, true, "Form B");

		channel.addVetoListener((sender, oldValue, newValue) -> handlerA);
		channel.addVetoListener((sender, oldValue, newValue) -> handlerB);

		try {
			channel.set("new");
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			assertEquals("Both handlers must be collected", 2, ex.getDirtyHandlers().size());
			assertTrue(ex.getDirtyHandlers().contains(handlerA));
			assertTrue(ex.getDirtyHandlers().contains(handlerB));
		}
	}

	/**
	 * After clearing dirty state on all handlers, executing the continuation retries the blocked
	 * change successfully.
	 */
	public void testContinuationRetries() {
		DefaultViewChannel channel = new DefaultViewChannel("test");
		channel.set("initial");

		StateHandler handler = stubHandler(true, false, "Form A");
		VetoListener vetoListener = (sender, oldValue, newValue) -> handler.isDirty() ? handler : null;
		channel.addVetoListener(vetoListener);

		ChannelVetoException caught = null;
		try {
			channel.set("new");
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			caught = ex;
		}

		// Simulate user discarding changes
		handler.executeDiscard();
		assertFalse("Handler should no longer be dirty", handler.isDirty());

		// Retry via continuation
		caught.getContinuation().run();
		assertEquals("Value must be updated after continuation", "new", channel.get());
	}

	/**
	 * A removed veto listener no longer blocks changes.
	 */
	public void testRemoveVetoListener() {
		DefaultViewChannel channel = new DefaultViewChannel("test");
		channel.set("initial");

		StateHandler handler = stubHandler(true, false, "Form A");
		VetoListener vetoListener = (sender, oldValue, newValue) -> handler;
		channel.addVetoListener(vetoListener);

		// Verify it blocks first
		try {
			channel.set("blocked");
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			// expected
		}
		assertEquals("initial", channel.get());

		// Remove the veto listener
		channel.removeVetoListener(vetoListener);

		// Now the change should succeed
		assertTrue(channel.set("allowed"));
		assertEquals("allowed", channel.get());
	}
}
