/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.element.ContentControls;

/**
 * Tests {@link ContentControls#retire(ReactControl)}, the step every container takes for the content
 * it stops displaying: a switch swapping cases, a master-detail exchanging its presentation, a tile
 * stack dropping the frames a shortened path no longer names.
 *
 * <p>
 * The scenario the split between detaching and disposing answers to is a channel with two listeners:
 * one is the container exchanging its content, the other updates a control of the content just
 * replaced. The second listener runs while the notification is still in progress.
 * </p>
 */
public class TestContentControls extends TestCase {

	private SSEUpdateQueue _queue;

	private ReactControl _content;

	/** Set when {@link #_content} is disposed. */
	private boolean _disposed;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_queue = new SSEUpdateQueue();
		ReactContext context = new DefaultReactContext("", "test", _queue);
		_content = new ReactControl(context, null, "TLPanel");
		_content.write(new TagWriter());
		_content.addCleanupAction(() -> _disposed = true);
	}

	/**
	 * Retired content stops being displayed at once, while its disposal waits for the notification
	 * that retired it - and a state change reaching it in between sends nothing.
	 */
	public void testRetiringFromWithinANotification() {
		DefaultViewChannel channel = new DefaultViewChannel("input");

		// The container exchanging its content.
		channel.addListener((sender, oldValue, newValue) -> ContentControls.retire(_content));

		// The observer that was already in the channel's listener snapshot and updates a control of
		// the content just retired.
		boolean[] displayed = { true };
		boolean[] disposedAlready = { true };
		int[] sent = { -1 };
		channel.addListener((sender, oldValue, newValue) -> {
			displayed[0] = _content.isAttached();
			disposedAlready[0] = _disposed;
			int before = _queue.pendingEventCount();
			_content.setHidden(true);
			sent[0] = _queue.pendingEventCount() - before;
		});

		channel.set("x");

		assertFalse("Retired content stops being displayed at once.", displayed[0]);
		assertFalse("Disposal waits until the notification has unwound.", disposedAlready[0]);
		assertEquals("A state change of retired content must not reach the client.", 0, sent[0]);
		assertTrue("The notification has unwound, so the content is disposed.", _disposed);
	}

	/**
	 * Without a notification in progress there is nothing to wait for, so retiring disposes right
	 * away.
	 */
	public void testRetiringOutsideANotification() {
		ContentControls.retire(_content);

		assertFalse("Retired content stops being displayed.", _content.isAttached());
		assertTrue("Nothing defers the disposal.", _disposed);
	}

	/** The precondition: displayed content does send its state changes. */
	public void testDisplayedContentSendsItsStateChanges() {
		int before = _queue.pendingEventCount();
		_content.setHidden(true);

		assertEquals("The state change reaches the client.", before + 1, _queue.pendingEventCount());
	}

}
