/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.TooltipProvider;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.protocol.PatchEvent;
import com.top_logic.layout.react.protocol.SSEEvent;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.TextState;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that a {@link ReactTextControl} whose tooltip changes tells the client so: the client keeps
 * fetched tooltip content as long as the state of the control is unchanged (see
 * {@link TooltipProvider}), so a changed tooltip must change the state although the text still has
 * a tooltip.
 */
public class TestReactTextControlTooltip extends TestCase {

	/** The key the client asks a text for its tooltip with. */
	private static final String TOOLTIP_KEY = "tooltip";

	private CapturingQueue _queue;

	private ReactTextControl _text;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_queue = new CapturingQueue();
		_text = new ReactTextControl(
			new DefaultReactContext("", "test", _queue, new ReactWindowRegistry("test")), "Open");
		_text.setTooltip("Nobody works on it.", null, false);
		// Render, so state changes produce patch events.
		_text.write(new TagWriter());
		_queue.clear();
	}

	/** A changed tooltip is published as a state change, and the new content is served. */
	public void testChangedTooltipChangesTheState() {
		_text.setTooltip("Somebody works on it.", null, false);

		assertTrue("A changed tooltip reaches the client as a patch: " + _queue.events(),
			_queue.patches().stream().anyMatch(patch -> patch.contains(TextState.HAS_TOOLTIP__PROP)));
		assertEquals("Somebody works on it.", _text.getTooltipContent(TOOLTIP_KEY).getHtml());
	}

	/** A removed tooltip is published as well, and no content is served any more. */
	public void testRemovedTooltipChangesTheState() {
		_text.setTooltip(null, null, false);

		assertTrue(_queue.patches().stream().anyMatch(patch -> patch.contains(TextState.HAS_TOOLTIP__PROP)));
		assertNull(_text.getTooltipContent(TOOLTIP_KEY));
	}

	/**
	 * An {@link SSEUpdateQueue} that captures enqueued events instead of flushing them to a
	 * connection.
	 */
	private static final class CapturingQueue extends SSEUpdateQueue {

		private final List<SSEEvent> _events = new ArrayList<>();

		@Override
		public void enqueue(SSEEvent event) {
			_events.add(event);
		}

		List<SSEEvent> events() {
			return _events;
		}

		List<String> patches() {
			return _events.stream()
				.filter(PatchEvent.class::isInstance)
				.map(event -> ((PatchEvent) event).getPatch())
				.toList();
		}

		void clear() {
			_events.clear();
		}
	}
}
