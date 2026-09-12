/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.servlet;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.Interaction;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that an {@link Interaction} delivers the updates it produced for the controls it leaves
 * displayed.
 *
 * <p>
 * An interaction updates controls and decides which of them stay displayed, and it does so in that
 * order: a control is patched by the listener chain of the channel a command wrote, while the
 * container replacing it is notified later in the same chain. The update that is on its way to a
 * control the client is about to unmount would send the browser looking for data the server no
 * longer serves, so delivery is settled when the interaction closes.
 * </p>
 */
public class TestInteractionDelivery extends TestCase {

	private ReactWindowRegistry _registry;

	private SSEUpdateQueue _queue;

	private ReactControl _control;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_registry = new ReactWindowRegistry("test");
		_queue = new SSEUpdateQueue();
		ReactContext context = new DefaultReactContext("", "test", _queue);
		_control = new ReactControl(context, null, "TLPanel");

		// Rendering is what displays the control, which is the precondition for it being updated.
		_control.write(new TagWriter());
	}

	/**
	 * The events the queue has been handed; without a connection none of them is written, so the
	 * count reports exactly what is waiting for the client.
	 */
	private int pending() {
		return _queue.pendingEventCount();
	}

	/** An update to a control the interaction stops displaying is not delivered. */
	public void testAnUpdateToADetachedControlIsDropped() {
		int before = pending();
		try (Interaction interaction = _registry.beginInteraction()) {
			_control.setHidden(true);
			assertEquals("The update waits for the interaction to complete.", before + 1, pending());

			_control.detach();
		}

		assertEquals("A control the client unmounts must not be addressed.", before, pending());
	}

	/** An update to a control that stays displayed is delivered. */
	public void testAnUpdateToADisplayedControlIsDelivered() {
		int before = pending();
		try (Interaction interaction = _registry.beginInteraction()) {
			_control.setHidden(true);
		}

		assertEquals("A displayed control receives what the interaction produced for it.",
			before + 1, pending());
	}

	/** An update to a control the interaction disposes is not delivered. */
	public void testAnUpdateToADisposedControlIsDropped() {
		int before = pending();
		try (Interaction interaction = _registry.beginInteraction()) {
			_control.setHidden(true);

			_control.cleanupTree();
		}

		assertEquals("A control that no longer exists must not be addressed.", before, pending());
	}

	/** An event that addresses no control is delivered whatever happened to the control tree. */
	public void testAnEventWithoutAControlIsDelivered() {
		int before = pending();
		try (Interaction interaction = _registry.beginInteraction()) {
			_queue.enqueue(JSSnipplet.create().setCode("window.location.reload();"));

			_control.cleanupTree();
		}

		assertEquals("Only the updates of a control depend on that control being displayed.",
			before + 1, pending());
	}

	/** Outside an interaction an update is handed on as it is produced. */
	public void testAnUpdateOutsideAnInteractionIsDeliveredImmediately() {
		int before = pending();
		_control.setHidden(true);

		assertEquals("Nothing holds the update back.", before + 1, pending());

		_control.detach();

		assertEquals("What was handed on is not taken back.", before + 1, pending());
	}

	/** Nested interactions deliver once, when the outermost one completes. */
	public void testNestedInteractionsDeliverAtTheOutermostClose() {
		int before = pending();
		try (Interaction outer = _registry.beginInteraction()) {
			try (Interaction inner = _registry.beginInteraction()) {
				_control.setHidden(true);
			}

			assertEquals("The inner interaction is part of the outer one.", before + 1, pending());

			_control.detach();
		}

		assertEquals("The outer interaction decides what is still displayed.", before, pending());
	}

}
