/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import java.util.Locale;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;

/**
 * Slide-in panel from the edge of the viewport or of an enclosing container.
 *
 * <p>
 * Slides in via CSS transform. No backdrop, the content it overlays stays interactive. Optional
 * title bar with close button. Escape sends the {@link #CMD_CLOSE close} command, which runs the
 * close handler the drawer was created with.
 * </p>
 *
 * <p>
 * The {@link #setChild(ReactControl) child} is rendered whenever the drawer holds one: opening and
 * closing is a transform of the panel, not an exchange of its contents. A drawer whose contents
 * follow a model therefore keeps showing what that model says, and the caller replaces the child
 * only when it displays something else.
 * </p>
 */
public class ReactDrawerControl extends ReactControl {

	/**
	 * Command the client sends when the drawer is closed, either through its close button or by
	 * pressing Escape.
	 */
	public static final String CMD_CLOSE = "close";

	private static final String REACT_MODULE = "TLDrawer";

	private static final String TITLE = "title";

	private static final String POSITION = "position";

	private static final String SIZE = "size";

	private static final String ANCHOR = "anchor";

	private static final String WIDTH = "width";

	private static final String OPEN = "open";

	private static final String CHILD = "child";

	/**
	 * The edge the drawer slides in from.
	 */
	public enum Position {

		/** Slides in from the left edge, spanning the full height. */
		LEFT,

		/** Slides in from the right edge, spanning the full height. */
		RIGHT,

		/** Slides up from the bottom edge, spanning the full width. */
		BOTTOM;

		/**
		 * The spelling the client receives for this constant: its name in lower case.
		 */
		String wireName() {
			return wireNameOf(this);
		}
	}

	/**
	 * The extent of the drawer across the edge it slides in from: the width of a
	 * {@link Position#LEFT} or {@link Position#RIGHT} drawer, the height of a
	 * {@link Position#BOTTOM} one.
	 *
	 * <p>
	 * A named size is a preference the client caps at the extent of the area the drawer is anchored
	 * in. An {@link ReactDrawerControl#setWidth(int) explicit pixel width} overrides it.
	 * </p>
	 */
	public enum Size {

		/** The narrowest size, for a drawer holding little more than a list of actions. */
		NARROW,

		/** The medium size, for a drawer holding a form or a detail view. */
		MEDIUM,

		/** The widest size, for a drawer holding content that needs room. */
		WIDE;

		/**
		 * The spelling the client receives for this constant: its name in lower case.
		 */
		String wireName() {
			return wireNameOf(this);
		}
	}

	/**
	 * The area the drawer is positioned in.
	 */
	public enum Anchor {

		/**
		 * The drawer overlays the whole page, anchored at the edges of the viewport.
		 */
		VIEWPORT,

		/**
		 * The drawer overlays only the element it is rendered in, anchored at the edges of the
		 * nearest positioned ancestor.
		 *
		 * <p>
		 * The surrounding chrome - app bar, sidebar, the title of the enclosing panel - stays
		 * uncovered, and the drawer's layer sits above the container's content but below the window
		 * and dialog layers.
		 * </p>
		 */
		CONTAINER;

		/**
		 * The spelling the client receives for this constant: its name in lower case.
		 */
		String wireName() {
			return wireNameOf(this);
		}
	}

	/**
	 * The wire name of an enum constant: its name in lower case, which is the spelling the client
	 * state contract uses.
	 */
	static String wireNameOf(Enum<?> value) {
		return value.name().toLowerCase(Locale.ROOT);
	}

	private final Runnable _closeHandler;

	private boolean _open;

	/**
	 * Creates a drawer sliding in from the right edge of the viewport in
	 * {@link Size#MEDIUM medium} size, without a title bar.
	 *
	 * @param closeHandler
	 *        Called when the drawer is closed.
	 */
	public ReactDrawerControl(ReactContext context, Runnable closeHandler) {
		this(context, null, Position.RIGHT, Size.MEDIUM, closeHandler);
	}

	/**
	 * Creates a drawer anchored at the {@link Anchor#VIEWPORT viewport}.
	 *
	 * @param title
	 *        Optional title; {@code null} renders no header (and therefore no close button).
	 * @param position
	 *        The edge the drawer slides in from.
	 * @param size
	 *        The named extent of the drawer.
	 * @param closeHandler
	 *        Called when the drawer is closed.
	 */
	public ReactDrawerControl(ReactContext context, String title, Position position, Size size,
			Runnable closeHandler) {
		this(context, title, position, size, Anchor.VIEWPORT, closeHandler);
	}

	/**
	 * Creates a drawer with full configuration.
	 *
	 * @param title
	 *        Optional title; {@code null} renders no header (and therefore no close button).
	 * @param position
	 *        The edge the drawer slides in from.
	 * @param size
	 *        The named extent of the drawer, overridden by {@link #setWidth(int)}.
	 * @param anchor
	 *        The area the drawer is positioned in.
	 * @param closeHandler
	 *        Called when the drawer is closed.
	 */
	public ReactDrawerControl(ReactContext context, String title, Position position, Size size, Anchor anchor,
			Runnable closeHandler) {
		super(context, null, REACT_MODULE);
		_closeHandler = closeHandler;
		if (title != null) {
			putState(TITLE, title);
		}
		putState(POSITION, position.wireName());
		putState(SIZE, size.wireName());
		putState(ANCHOR, anchor.wireName());
		putState(OPEN, false);
	}

	/**
	 * Opens the drawer.
	 */
	public void open() {
		if (_open) {
			return;
		}
		_open = true;
		putState(OPEN, true);
	}

	/**
	 * Closes the drawer.
	 */
	public void close() {
		if (!_open) {
			return;
		}
		_open = false;
		putState(OPEN, false);
	}

	/**
	 * Whether the drawer is currently {@link #open() open}.
	 */
	public boolean isOpen() {
		return _open;
	}

	/**
	 * Sets the title.
	 *
	 * @param title
	 *        The new title, or {@code null} to remove the header.
	 */
	public void setTitle(String title) {
		putState(TITLE, title);
	}

	/**
	 * Sets an explicit extent in pixels, overriding the named {@link Size}.
	 *
	 * <p>
	 * The client still caps the drawer at the extent of the area it is anchored in, so an explicit
	 * width never makes a {@link Anchor#CONTAINER container-anchored} drawer wider than its
	 * container.
	 * </p>
	 *
	 * @param width
	 *        The width in pixels of a {@link Position#LEFT} or {@link Position#RIGHT} drawer, the
	 *        height of a {@link Position#BOTTOM} one. A value of {@code 0} or less restores the
	 *        named {@link Size}.
	 */
	public void setWidth(int width) {
		putState(WIDTH, width > 0 ? Integer.valueOf(width) : null);
	}

	/**
	 * Sets the child content.
	 *
	 * @param child
	 *        The content control to display in the drawer body, or {@code null} for an empty
	 *        drawer. A control that replaces a previously set one is disposed by the caller that
	 *        built it.
	 */
	public void setChild(ReactControl child) {
		putState(CHILD, child);
	}

	/**
	 * Handles the {@link #CMD_CLOSE} command sent when the drawer is closed.
	 *
	 * @implNote The close handler runs before the drawer closes, so that a handler refusing the
	 *           close (e.g. by vetoing the channel write it performs) leaves the drawer open. A
	 *           handler that closes the drawer itself makes the subsequent {@link #close()} a
	 *           no-op.
	 */
	@ReactCommandHandler(value = CMD_CLOSE, technical = true)
	void handleClose() {
		_closeHandler.run();
		close();
	}

}
