/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;

/**
 * Tests that a {@link ReactControl} sends a state change exactly while it is displayed.
 *
 * <p>
 * A container renders its active content and {@link ReactControl#detach() detaches} what it
 * replaces, so a control that is not {@link ReactControl#isAttached() attached} has no mounted
 * component on the client. An update addressed to it would arrive for a control the client has
 * unmounted and send the browser looking for data that is no longer served. Nothing is lost by
 * dropping it: a control that becomes displayed again is serialized with its full state.
 * </p>
 */
public class TestDetachedControlUpdates extends TestCase {

	private SSEUpdateQueue _queue;

	private ReactControl _control;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_queue = new SSEUpdateQueue();
		ReactContext context = new DefaultReactContext("", "test", _queue);
		_control = new ReactControl(context, null, "TLPanel");
	}

	/**
	 * Rendering the control, which is what establishes that it is displayed.
	 */
	private void render() throws Exception {
		_control.write(new TagWriter());
	}

	/**
	 * The events the queue has been handed; without a connection none of them is written, so the
	 * count only grows.
	 */
	private int sent() {
		return _queue.pendingEventCount();
	}

	/** A displayed control sends its state change - the precondition every other test rests on. */
	public void testADisplayedControlSendsItsStateChange() throws Exception {
		render();
		assertTrue("Rendering displays the control.", _control.isAttached());

		int before = sent();
		_control.setHidden(true);

		assertEquals("The state change reaches the client.", before + 1, sent());
	}

	/** A control that is no longer displayed sends nothing. */
	public void testADetachedControlSendsNothing() throws Exception {
		render();
		_control.detach();

		int before = sent();
		_control.setHidden(true);

		assertEquals("A control the client has unmounted must not be addressed.", before, sent());
	}

	/** Displaying it again lets its state changes flow again. */
	public void testDisplayingAgainSendsChangesAgain() throws Exception {
		render();
		_control.detach();
		_control.setHidden(true);
		_control.attach();

		int before = sent();
		_control.setHidden(false);

		assertEquals("A control that is displayed again is addressable again.", before + 1, sent());
	}

	/**
	 * A control that was never rendered sends nothing either: its state becomes part of the initial
	 * render.
	 */
	public void testAnUnrenderedControlSendsNothing() {
		int before = sent();
		_control.setHidden(true);

		assertEquals("Nothing is sent before the first rendering.", before, sent());
	}

	/**
	 * A disposed control sends nothing: the stale reference the disposing interaction may continue
	 * on must not produce an update.
	 */
	public void testADisposedControlSendsNothing() throws Exception {
		render();
		_control.cleanupTree();

		int before = sent();
		_control.setHidden(true);

		assertEquals("A disposed control must not be addressed.", before, sent());
	}

}
