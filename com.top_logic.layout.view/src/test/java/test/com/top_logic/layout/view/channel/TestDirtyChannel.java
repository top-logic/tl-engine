/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import junit.framework.TestCase;

import com.top_logic.layout.view.channel.DirtyChannel;
import com.top_logic.layout.view.form.StateHandler;

/**
 * Tests for {@link DirtyChannel}.
 */
public class TestDirtyChannel extends TestCase {

	/**
	 * A new {@link DirtyChannel} has no dirty handlers.
	 */
	public void testInitiallyClean() {
		DirtyChannel channel = new DirtyChannel();
		assertFalse(channel.hasDirtyHandlers());
		assertTrue(channel.getDirtyHandlers().isEmpty());
	}

	/**
	 * After registering a handler as dirty, the channel reports dirty handlers.
	 */
	public void testRegisterDirty() {
		DirtyChannel channel = new DirtyChannel();
		StateHandler handler = stubHandler("form-1");
		channel.updateState(handler, true);
		assertTrue(channel.hasDirtyHandlers());
		assertEquals(1, channel.getDirtyHandlers().size());
		assertSame(handler, channel.getDirtyHandlers().get(0));
	}

	/**
	 * After marking a handler dirty then clean, no dirty handlers remain.
	 */
	public void testRegisterClean() {
		DirtyChannel channel = new DirtyChannel();
		StateHandler handler = stubHandler("form-1");
		channel.updateState(handler, true);
		channel.updateState(handler, false);
		assertFalse(channel.hasDirtyHandlers());
		assertTrue(channel.getDirtyHandlers().isEmpty());
	}

	/**
	 * With two dirty handlers, cleaning one leaves only the other.
	 */
	public void testMultipleHandlers() {
		DirtyChannel channel = new DirtyChannel();
		StateHandler h1 = stubHandler("form-1");
		StateHandler h2 = stubHandler("form-2");
		channel.updateState(h1, true);
		channel.updateState(h2, true);
		assertEquals(2, channel.getDirtyHandlers().size());

		channel.updateState(h1, false);
		assertTrue(channel.hasDirtyHandlers());
		assertEquals(1, channel.getDirtyHandlers().size());
		assertSame(h2, channel.getDirtyHandlers().get(0));
	}

	/**
	 * Calling updateState(handler, false) for an unknown handler is a no-op.
	 */
	public void testCleanUnregisteredIsNoOp() {
		DirtyChannel channel = new DirtyChannel();
		StateHandler unknown = stubHandler("unknown");
		// Must not throw
		channel.updateState(unknown, false);
		assertFalse(channel.hasDirtyHandlers());
	}

	/**
	 * removeHandler removes a dirty handler regardless of state.
	 */
	public void testRemoveHandler() {
		DirtyChannel channel = new DirtyChannel();
		StateHandler h1 = stubHandler("form-1");
		StateHandler h2 = stubHandler("form-2");
		channel.updateState(h1, true);
		channel.updateState(h2, true);

		channel.removeHandler(h1);
		assertTrue(channel.hasDirtyHandlers());
		assertEquals(1, channel.getDirtyHandlers().size());
		assertSame(h2, channel.getDirtyHandlers().get(0));

		channel.removeHandler(h2);
		assertFalse(channel.hasDirtyHandlers());
	}

	private StateHandler stubHandler(String description) {
		return new StateHandler() {
			@Override
			public boolean isDirty() {
				return false;
			}

			@Override
			public boolean hasErrors() {
				return false;
			}

			@Override
			public void executeSave() {
				// no-op
			}

			@Override
			public void executeDiscard() {
				// no-op
			}

			@Override
			public String getDescription() {
				return description;
			}
		};
	}
}
