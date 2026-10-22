/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;

/**
 * Server side of {@link PageSelectionElement}: writes the selection of its page into a shell channel
 * while the page is on screen.
 *
 * <p>
 * A control that shows nothing. It exists because a control knows when its view is on screen: it
 * is attached when the view is displayed, and detached when the view leaves the screen, for example
 * when the user opens another nav item or a tile-stack frame covers the page.
 * </p>
 */
public class PageSelectionControl extends ReactControl {

	private final TypedAnnotatable _window;

	private final String _to;

	private final List<ViewChannel> _inputs;

	/**
	 * Creates a {@link PageSelectionControl}.
	 *
	 * @param window
	 *        The sub-session of the browser window. <code>null</code> writes nothing.
	 * @param to
	 *        The name of the shell channel in the window's {@link ChatChannels}.
	 * @param inputs
	 *        The channels of the page that hold its selection, the most specific first.
	 */
	public PageSelectionControl(ReactContext context, TypedAnnotatable window, String to,
			List<ViewChannel> inputs) {
		super(context, null, ChatChannelsControl.INVISIBLE);
		_window = window;
		_to = to;
		_inputs = inputs;

		ChannelListener listener = (sender, oldValue, newValue) -> {
			if (isAttached()) {
				select();
			}
		};
		for (ViewChannel input : inputs) {
			input.addListener(listener);
			addCleanupAction(() -> input.removeListener(listener));
		}
		addCleanupAction(this::deselect);
	}

	/**
	 * Writes the selection on attach, not in the constructor: a deep link or a push writes the
	 * page's channels while the view is built.
	 */
	@Override
	protected void onAttach() {
		super.onAttach();
		select();
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		deselect();
	}

	/** Structural: an invisible helper, left out of the headless projection. */
	@Override
	public boolean scriptingTransparent() {
		return true;
	}

	private void select() {
		if (_window == null) {
			return;
		}
		ChatChannels.select(_window, _to, this, selectionOf(_inputs.stream().map(ViewChannel::get).toList()));
	}

	private void deselect() {
		if (_window == null) {
			return;
		}
		ChatChannels.deselect(_window, _to, this);
	}

	/**
	 * The one object that the page has selected.
	 *
	 * <p>
	 * One object and never a list. The shell channel can be the input of other elements that expect
	 * one object. A list written into such a channel caused a {@link ClassCastException} in an
	 * application.
	 * </p>
	 *
	 * @param values
	 *        The values of the page's channels, the most specific first. Entries can be
	 *        <code>null</code>.
	 * @return The first value that is one object, or the element of the first set of exactly one
	 *         object. <code>null</code> when no value is one object.
	 */
	static Object selectionOf(List<?> values) {
		for (Object value : values) {
			if (value instanceof Collection<?> collection) {
				Object single = collection.size() == 1 ? collection.iterator().next() : null;
				if (single != null) {
					return single;
				}
			} else if (value != null) {
				return value;
			}
		}
		return null;
	}

}
