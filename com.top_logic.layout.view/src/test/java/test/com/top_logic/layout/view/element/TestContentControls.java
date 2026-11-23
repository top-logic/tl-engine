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
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.element.ContentControls;

/**
 * Tests {@link ContentControls#retire(ReactControl)}, the step every container takes for the content
 * it stops displaying: a switch swapping cases, a master-detail exchanging its presentation, a tile
 * stack dropping the frames a shortened path no longer names.
 *
 * <p>
 * The scenario is a channel the container listens to, along with a listener further down in the
 * notification: either one of the retired content, which must not hear the new value anymore, or
 * one holding a reference to a control of the retired content, whose state change must not reach
 * the client.
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
		ReactContext context = new DefaultReactContext("", "test", _queue, new ReactWindowRegistry("test"));
		_content = new ReactControl(context, null, "TLPanel");
		_content.write(new TagWriter());
		_content.addCleanupAction(() -> _disposed = true);
	}

	/**
	 * Retired content is disposed at once, also from within a notification - and a state change
	 * reaching it later in that notification sends nothing.
	 */
	public void testRetiringFromWithinANotification() {
		DefaultViewChannel channel = new DefaultViewChannel("input");

		// The container exchanging its content.
		channel.addListener((sender, oldValue, newValue) -> ContentControls.retire(_content));

		// The observer that was already in the channel's listener snapshot and updates a control of
		// the content just retired.
		boolean[] displayed = { true };
		boolean[] disposedAlready = { false };
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
		assertTrue("Retired content is disposed at once.", disposedAlready[0]);
		assertEquals("A state change of retired content must not reach the client.", 0, sent[0]);
	}

	/**
	 * A listener of the retired content that is still pending in the notification that retired it
	 * is not called anymore: the content does not hear a value it was not built for.
	 */
	public void testRetiredContentMissesTheNotification() {
		DefaultViewChannel channel = new DefaultViewChannel("input");

		// The container exchanging its content.
		channel.addListener((sender, oldValue, newValue) -> ContentControls.retire(_content));

		// The retired content's own listener, ending with its disposal.
		int[] heard = { 0 };
		_content.addCleanupAction(channel.addListener((sender, oldValue, newValue) -> heard[0]++)::dispose);

		channel.set("x");

		assertEquals("The retired content must not hear the value it was retired for.", 0, heard[0]);
	}

	/**
	 * Without a notification in progress, retiring disposes the content as well.
	 */
	public void testRetiringOutsideANotification() {
		ContentControls.retire(_content);

		assertFalse("Retired content stops being displayed.", _content.isAttached());
		assertTrue("Retired content is disposed.", _disposed);
	}

	/** The precondition: displayed content does send its state changes. */
	public void testDisplayedContentSendsItsStateChanges() {
		int before = _queue.pendingEventCount();
		_content.setHidden(true);

		assertEquals("The state change reaches the client.", before + 1, _queue.pendingEventCount());
	}

}
