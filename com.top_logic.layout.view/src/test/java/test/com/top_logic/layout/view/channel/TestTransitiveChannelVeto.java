/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.layout.view.channel.ChannelVetoException;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.DerivedViewChannel;
import com.top_logic.layout.view.channel.VetoForwarder;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.StateHandler;

/**
 * Tests for the veto of a channel that is written in reaction to another channel changing, asked
 * through a {@link VetoForwarder} before the first channel is written.
 */
public class TestTransitiveChannelVeto extends TestCase {

	/**
	 * A {@link StateHandler} stub whose dirty state is switched by the test.
	 */
	private static class StubHandler implements StateHandler {

		boolean _dirty = true;

		private final String _description;

		StubHandler(String description) {
			_description = description;
		}

		@Override
		public boolean isDirty() {
			return _dirty;
		}

		@Override
		public boolean hasErrors() {
			return false;
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
			return _description;
		}
	}

	/**
	 * Registers a veto listener answering with the given handler while it is dirty.
	 */
	private static void vetoWhileDirty(ViewChannel channel, StubHandler handler) {
		channel.addVetoListener(
			(sender, oldValue, newValue) -> handler.isDirty() ? List.of(handler) : List.<StateHandler> of());
	}

	/**
	 * The unsaved changes blocking the write of the channel that a listener writes in reaction stop
	 * the write of the notifying channel, before any of its listeners runs.
	 */
	public void testNestedWriteVetoedBeforeOuterWrite() {
		DefaultViewChannel source = new DefaultViewChannel("source");
		DefaultViewChannel target = new DefaultViewChannel("target");
		source.set("s0");
		target.set("t0");

		List<String> notifications = new ArrayList<>();
		source.addListener((sender, oldValue, newValue) -> {
			notifications.add("writer:" + newValue);
			target.set("t-" + newValue);
		});

		StubHandler handler = new StubHandler("targetForm");
		vetoWhileDirty(target, handler);
		VetoForwarder.forward(source, target);

		// A listener registered after the writing one must be notified by the retry as well.
		source.addListener((sender, oldValue, newValue) -> notifications.add("late:" + newValue));

		ChannelVetoException caught = null;
		try {
			source.set("s1");
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			caught = ex;
		}

		assertEquals("The handler of the written channel blocks the change", List.of(handler),
			caught.getDirtyHandlers());
		assertEquals("Source must not be written when the nested write is vetoed", "s0", source.get());
		assertEquals("No listener of the source must run when the change is vetoed", List.of(), notifications);
		assertEquals("Target must not be written when the change is vetoed", "t0", target.get());

		handler.executeDiscard();
		caught.getContinuation().run();

		assertEquals("Source must hold the new value after the continuation", "s1", source.get());
		assertEquals("Every listener of the source must be notified exactly once",
			List.of("writer:s1", "late:s1"), notifications);
		assertEquals("Target must hold what the listener computed", "t-s1", target.get());
	}

	/**
	 * Without a forwarder, the unsaved changes of the written channel are not visible on the
	 * notifying channel.
	 */
	public void testWithoutForwarderNoTransitiveVeto() {
		DefaultViewChannel source = new DefaultViewChannel("source");
		DefaultViewChannel target = new DefaultViewChannel("target");
		source.set("s0");
		target.set("t0");

		source.addListener((sender, oldValue, newValue) -> target.set("t-" + newValue));

		StubHandler handler = new StubHandler("targetForm");
		vetoWhileDirty(target, handler);
		Runnable removeForwarder = VetoForwarder.forward(source, target);

		assertEquals("The forwarder makes the handler visible on the source", List.of(handler),
			source.dirtyHandlers());

		removeForwarder.run();

		assertEquals("Without the forwarder the source does not ask the target", List.of(),
			source.dirtyHandlers());

		handler.executeDiscard();
		assertTrue("A clean handler must not block the change", source.set("s1"));
		assertEquals("s1", source.get());
		assertEquals("t-s1", target.get());
	}

	/**
	 * A handler reachable both directly and over a forwarder is reported once.
	 */
	public void testDuplicateHandlerReportedOnce() {
		DefaultViewChannel source = new DefaultViewChannel("source");
		DefaultViewChannel target = new DefaultViewChannel("target");
		source.set("s0");

		StubHandler handler = new StubHandler("sharedForm");
		vetoWhileDirty(source, handler);
		vetoWhileDirty(target, handler);
		VetoForwarder.forward(source, target);

		assertEquals("The handler must be reported once", List.of(handler), source.dirtyHandlers());

		try {
			source.set("s1");
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			assertEquals("The handler must be reported once", List.of(handler), ex.getDirtyHandlers());
		}
	}

	/**
	 * The question is asked over a chain of forwarded channels.
	 */
	public void testDirtyHandlersAreTransitiveOverTwoLevels() {
		DefaultViewChannel first = new DefaultViewChannel("first");
		DefaultViewChannel second = new DefaultViewChannel("second");
		DefaultViewChannel third = new DefaultViewChannel("third");

		StubHandler secondHandler = new StubHandler("secondForm");
		StubHandler thirdHandler = new StubHandler("thirdForm");
		vetoWhileDirty(second, secondHandler);
		vetoWhileDirty(third, thirdHandler);

		VetoForwarder.forward(first, second);
		VetoForwarder.forward(second, third);

		assertEquals("The handler of the last channel must be reachable from the second",
			List.of(secondHandler, thirdHandler), second.dirtyHandlers());
		assertEquals("The handlers of the whole chain must be reachable from the first",
			List.of(secondHandler, thirdHandler), first.dirtyHandlers());

		try {
			first.set("f1");
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			assertEquals("The handlers of the whole chain must block the change",
				List.of(secondHandler, thirdHandler), ex.getDirtyHandlers());
		}
		assertNull("The first channel must not be written", first.get());
	}

	/**
	 * The unsaved changes of a form bound to a derived channel block the write of the input the
	 * derived value is computed from.
	 */
	public void testDerivedChannelVetoesInputWrite() {
		DefaultViewChannel input = new DefaultViewChannel("input");
		input.set("a");

		DerivedViewChannel derived = new DerivedViewChannel("derived");
		derived.bind(List.of(input), args -> args[0] + "!");

		assertEquals("a!", derived.get());

		StubHandler handler = new StubHandler("derivedForm");
		vetoWhileDirty(derived, handler);

		assertEquals("The input must report the handler of the derived channel", List.of(handler),
			input.dirtyHandlers());

		ChannelVetoException caught = null;
		try {
			input.set("b");
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			caught = ex;
		}

		assertEquals(List.of(handler), caught.getDirtyHandlers());
		assertEquals("Input must not be written when the derived channel vetoes", "a", input.get());
		assertEquals("Derived value must not change when the write is vetoed", "a!", derived.get());

		handler.executeDiscard();
		caught.getContinuation().run();

		assertEquals("b", input.get());
		assertEquals("b!", derived.get());
	}

	/**
	 * Writing a bidirectional derived channel asks its own veto listeners, since the write reaches
	 * the input the derived channel forwards from.
	 */
	public void testBidirectionalDerivedChannelVeto() {
		DefaultViewChannel input = new DefaultViewChannel("input");
		input.set("a");

		DerivedViewChannel derived = new DerivedViewChannel("derived");
		derived.bind(List.of(input), args -> args[0] + "!",
			value -> ((String) value).substring(0, ((String) value).length() - 1));

		assertEquals("a!", derived.get());

		StubHandler handler = new StubHandler("derivedForm");
		vetoWhileDirty(derived, handler);

		ChannelVetoException caught = null;
		try {
			derived.set("b!");
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			caught = ex;
		}

		assertEquals(List.of(handler), caught.getDirtyHandlers());
		assertEquals("Input must not be written when the derived channel vetoes", "a", input.get());
		assertEquals("Derived value must not change when the write is vetoed", "a!", derived.get());

		handler.executeDiscard();
		caught.getContinuation().run();

		assertEquals("Input must hold the reverse-mapped value after the continuation", "b", input.get());
		assertEquals("Derived value must be recomputed after the continuation", "b!", derived.get());
	}

}
