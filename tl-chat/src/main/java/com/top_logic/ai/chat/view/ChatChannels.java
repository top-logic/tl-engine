/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.util.TLContextManager;

/**
 * The shell channels that the rest of one browser window can reach by name.
 *
 * <h2>Why this exists</h2>
 *
 * <p>
 * A channel resolves in the view that declares it. A page of the shell reaches a shell channel only
 * through a {@code <bind>} on its {@code <view-ref>}. Two kinds of page cannot do that (#928):
 * </p>
 * <ul>
 * <li>A <em>tile-stack frame</em>. {@code ReactTileStackControl.buildFrame} gives a frame a new
 * context, with copies of its push parameters only.</li>
 * <li>A page that a {@code ReactControl} builds itself. It has only the channels that its own
 * element was configured with.</li>
 * </ul>
 *
 * <p>
 * So the shell names its channels once, in {@code <chat-channels>} ({@link ChatChannelsElement}),
 * and this register keeps them for the browser window. {@code <page-selection>}
 * ({@link PageSelectionElement}) writes into a channel of this register, and Java code of the same
 * window can get one with {@link #channel(String)}.
 * </p>
 *
 * <h2>Scope</h2>
 *
 * <p>
 * One register per browser window: it is a property of the window's sub-session, and the engine
 * gives each browser window a sub-session of its own ({@code ViewServlet.ensureSubSession}). A new
 * window has an empty register.
 * </p>
 *
 * <h2>Page selections</h2>
 *
 * <p>
 * Each channel keeps a list of the pages that write a selection into it, most recent last. The
 * channel holds the value of the most recent page. When a page withdraws, the channel holds the
 * value of the page before it, or <code>null</code> when no page is left. So the channel always
 * names the selection of a page that is on screen.
 * </p>
 */
public final class ChatChannels {

	private static final Property<ChatChannels> REGISTER =
		TypedAnnotatable.property(ChatChannels.class, "tl.chat.channels");

	private final Map<String, Slot> _slots = new HashMap<>();

	private ChatChannels() {
		// Created by register(TypedAnnotatable) only.
	}

	/**
	 * A shell channel of the current browser window.
	 *
	 * <p>
	 * For Java code that runs in a request of the window, for example a command of a control. Write
	 * to the channel with {@link ViewChannel#set(Object)}.
	 * </p>
	 *
	 * @param name
	 *        The name in {@code <chat-channels channels="...">}.
	 * @return The channel, or <code>null</code> when the window has no shell channel of that name.
	 */
	public static ViewChannel channel(String name) {
		TypedAnnotatable window = TLContextManager.getSubSession();
		if (window == null || name == null) {
			return null;
		}
		ChatChannels register = window.get(REGISTER);
		if (register == null) {
			return null;
		}
		synchronized (register) {
			Slot slot = register._slots.get(name);
			return slot == null ? null : slot._channel;
		}
	}

	/**
	 * Makes a shell channel reachable under its name.
	 *
	 * <p>
	 * When pages already wrote a selection for this name, the channel gets the value of the most
	 * recent page.
	 * </p>
	 */
	static void publish(TypedAnnotatable window, String name, ViewChannel channel) {
		ChatChannels register = register(window);
		synchronized (register) {
			Slot slot = register.slot(name);
			slot._channel = channel;
			if (!slot._writers.isEmpty()) {
				slot.apply();
			}
		}
	}

	/**
	 * Undoes {@link #publish(TypedAnnotatable, String, ViewChannel)}, when the shell goes away.
	 */
	static void withdraw(TypedAnnotatable window, String name, ViewChannel channel) {
		ChatChannels register = window.get(REGISTER);
		if (register == null) {
			return;
		}
		synchronized (register) {
			Slot slot = register._slots.get(name);
			if (slot != null && slot._channel == channel) {
				slot._channel = null;
			}
		}
	}

	/**
	 * Records the selection of a page that is on screen.
	 *
	 * @param page
	 *        The page that writes. Identity counts.
	 * @param value
	 *        The selection. <code>null</code> removes the page, as {@link #deselect} does.
	 */
	static void select(TypedAnnotatable window, String name, Object page, Object value) {
		ChatChannels register = register(window);
		synchronized (register) {
			Slot slot = register.slot(name);
			slot.remove(page);
			if (value != null) {
				slot._writers.add(new Writer(page, value));
			}
			slot.apply();
		}
	}

	/**
	 * Removes the selection of a page that left the screen.
	 *
	 * <p>
	 * The channel then names the selection of the page before it. A page that wrote nothing changes
	 * nothing, so it cannot clear the value of another page.
	 * </p>
	 */
	static void deselect(TypedAnnotatable window, String name, Object page) {
		ChatChannels register = window.get(REGISTER);
		if (register == null) {
			return;
		}
		synchronized (register) {
			Slot slot = register._slots.get(name);
			if (slot != null && slot.remove(page)) {
				slot.apply();
			}
		}
	}

	private Slot slot(String name) {
		return _slots.computeIfAbsent(name, x -> new Slot());
	}

	private static synchronized ChatChannels register(TypedAnnotatable window) {
		ChatChannels register = window.get(REGISTER);
		if (register == null) {
			register = new ChatChannels();
			window.set(REGISTER, register);
		}
		return register;
	}

	private static final class Slot {

		ViewChannel _channel;

		final List<Writer> _writers = new ArrayList<>();

		boolean remove(Object page) {
			return _writers.removeIf(writer -> writer.page() == page);
		}

		void apply() {
			if (_channel != null) {
				_channel.set(_writers.isEmpty() ? null : _writers.get(_writers.size() - 1).value());
			}
		}
	}

	private record Writer(Object page, Object value) {
		// Data only.
	}

}
