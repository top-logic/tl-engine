/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.layout.view.channel.ChannelVetoException;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.InitializedViewChannel;
import com.top_logic.layout.view.form.StateHandler;

/**
 * Tests for {@link InitializedViewChannel}: the wiring to its inputs, with Java functions instead
 * of TL-Script.
 *
 * @see TestInitializedViewChannelObservation
 */
public class TestInitializedViewChannel extends TestCase {

	/**
	 * Tests that the channel is filled from its inputs when it is bound.
	 */
	public void testInitialFill() {
		DefaultViewChannel input = new DefaultViewChannel("input");
		input.set("p1");

		InitializedViewChannel channel = channel(input);

		assertEquals("budget-p1", channel.get());
	}

	/**
	 * Tests that a new input value fills the channel again and reports the new value.
	 */
	public void testRefillOnInputChange() {
		DefaultViewChannel input = new DefaultViewChannel("input");
		input.set("p1");
		InitializedViewChannel channel = channel(input);
		List<Object> reported = new ArrayList<>();
		channel.addListener((sender, oldValue, newValue) -> reported.add(newValue));

		input.set("p2");

		assertEquals("budget-p2", channel.get());
		assertEquals(List.of("budget-p2"), reported);
	}

	/**
	 * Tests that the channel is writable and keeps a written value until the next input change,
	 * which replaces it, even if the input value results in the same initial value as before.
	 */
	public void testWrittenValueKeptUntilInputChange() {
		DefaultViewChannel input = new DefaultViewChannel("input");
		input.set("p1");
		InitializedViewChannel channel = new InitializedViewChannel("channel");
		channel.bind(List.of(input), args -> "same", Set.of());

		assertTrue("The channel is writable.", channel.set("written"));
		assertEquals("written", channel.get());

		input.set("p2");

		assertEquals("A new input value fills the channel again.", "same", channel.get());
	}

	/**
	 * Tests that the unsaved changes bound to the channel block the write of its input, before the
	 * input is written.
	 */
	public void testInputWriteAsksVetoListenersOfChannel() {
		DefaultViewChannel input = new DefaultViewChannel("input");
		input.set("p1");
		InitializedViewChannel channel = channel(input);
		channel.set("written");

		StateHandler handler = new DirtyHandler();
		channel.addVetoListener((sender, oldValue, newValue) -> List.of(handler));

		try {
			input.set("p2");
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			assertEquals(List.of(handler), ex.getDirtyHandlers());
		}
		assertEquals("The input was not written.", "p1", input.get());
		assertEquals("The written value is kept.", "written", channel.get());
	}

	/**
	 * Tests that a channel without inputs is filled once with the result of the function.
	 */
	public void testInitWithoutInputs() {
		InitializedViewChannel channel = new InitializedViewChannel("channel");
		channel.bind(List.of(), args -> args.length, Set.of());

		assertEquals(0, channel.get());
	}

	/**
	 * Tests that a released channel no longer follows its input.
	 */
	public void testReleaseStopsRefill() {
		DefaultViewChannel input = new DefaultViewChannel("input");
		input.set("p1");
		InitializedViewChannel channel = channel(input);

		channel.release();
		input.set("p2");

		assertEquals("budget-p1", channel.get());
	}

	private static InitializedViewChannel channel(DefaultViewChannel input) {
		InitializedViewChannel result = new InitializedViewChannel("channel");
		result.bind(List.of(input), args -> args[0] == null ? null : "budget-" + args[0], Set.of());
		return result;
	}

	/**
	 * A {@link StateHandler} with unsaved changes.
	 */
	private static class DirtyHandler implements StateHandler {

		@Override
		public boolean isDirty() {
			return true;
		}

		@Override
		public boolean hasErrors() {
			return false;
		}

		@Override
		public void executeSave() {
			// Nothing to save.
		}

		@Override
		public void executeDiscard() {
			// Nothing to discard.
		}

		@Override
		public String getDescription() {
			return "dirty";
		}
	}

}
