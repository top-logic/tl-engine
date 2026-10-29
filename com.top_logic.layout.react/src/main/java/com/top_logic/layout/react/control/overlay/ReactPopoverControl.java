/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import java.util.function.Supplier;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.ReactImages;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.state.PopoverState;
import com.top_logic.layout.react.state.PopoverState.Mode;
import com.top_logic.layout.react.state.PopoverState.Placement;

/**
 * A non-modal surface of arbitrary content, anchored to the trigger that opens it.
 *
 * <p>
 * The control renders its trigger button in place and, while it is {@link #isOpen() open}, its
 * content on a surface placed at the trigger. The trigger button shows the trigger content the
 * popover was created with, or else its {@link #setIcon(ThemeImage) icon} or
 * {@link #setLabel(String) label}. The content stays interactive with the page around it: the
 * surface has no backdrop and does not trap the focus. The state is described by
 * {@link PopoverState}.
 * </p>
 *
 * <p>
 * The content is created by the supplier the popover is constructed with when the popover opens for
 * the first time. Closing the popover detaches the content but keeps it, so that it shows what the
 * user left there when the popover opens again; disposing the popover disposes the content.
 * </p>
 *
 * <p>
 * The client asks for the popover to open and close through the {@link #CMD_OPEN} and
 * {@link #CMD_CLOSE} commands, according to its {@link Mode}. The server opens and closes it
 * with {@link #open()} and {@link #close()}.
 * </p>
 */
public class ReactPopoverControl extends ReactControl {

	/** The command the client sends to open the popover. */
	public static final String CMD_OPEN = "open";

	/** The command the client sends to close the popover. */
	public static final String CMD_CLOSE = "close";

	private static final String REACT_MODULE = "TLPopover";

	private final Supplier<? extends ReactControl> _contentFactory;

	private ReactControl _content;

	private boolean _open;

	/**
	 * Creates a closed {@link ReactPopoverControl} whose trigger button shows its
	 * {@link #setLabel(String) label} and {@link #setIcon(ThemeImage) icon}.
	 *
	 * @param context
	 *        The context of the control.
	 * @param contentFactory
	 *        Creates the content of the surface when the popover opens for the first time. The
	 *        popover owns the created content and disposes it with itself.
	 * @param placement
	 *        Where the surface stands at the trigger.
	 * @param mode
	 *        The gesture opening the popover.
	 */
	public ReactPopoverControl(ReactContext context, Supplier<? extends ReactControl> contentFactory,
			Placement placement, Mode mode) {
		this(context, null, contentFactory, placement, mode);
	}

	/**
	 * Creates a closed {@link ReactPopoverControl}.
	 *
	 * @param context
	 *        The context of the control.
	 * @param trigger
	 *        The content shown inside the trigger button, or {@code null} for a trigger button
	 *        showing the {@link #setLabel(String) label} and {@link #setIcon(ThemeImage) icon}. The
	 *        content is presentational - text, an icon, a badge - and holds no interactive
	 *        controls: the trigger button itself is the interactive element, and a control inside it
	 *        (a button in a button) is neither valid HTML nor reachable. The popover owns the
	 *        content and disposes it with itself.
	 * @param contentFactory
	 *        Creates the content of the surface when the popover opens for the first time. The
	 *        popover owns the created content and disposes it with itself.
	 * @param placement
	 *        Where the surface stands at the trigger.
	 * @param mode
	 *        The gesture opening the popover.
	 */
	public ReactPopoverControl(ReactContext context, ReactControl trigger,
			Supplier<? extends ReactControl> contentFactory, Placement placement, Mode mode) {
		super(context, null, REACT_MODULE);
		_contentFactory = contentFactory;
		if (trigger != null) {
			putState(PopoverState.TRIGGER__PROP, trigger);
		}
		putState(PopoverState.PLACEMENT__PROP, placement.protocolName());
		putState(PopoverState.MODE__PROP, mode.protocolName());
		putState(PopoverState.OPEN__PROP, false);
	}

	/**
	 * Sets the label of the trigger button.
	 *
	 * <p>
	 * The label is shown when the trigger button has neither trigger content nor an icon, and is the
	 * accessible name of the trigger button and of the surface in every case.
	 * </p>
	 *
	 * @param label
	 *        The label, or {@code null} for none.
	 */
	public void setLabel(String label) {
		putState(PopoverState.LABEL__PROP, label);
	}

	/**
	 * Sets the icon of the trigger button, shown alone when the trigger button has no trigger
	 * content.
	 *
	 * @param icon
	 *        The icon, or {@code null} for none.
	 */
	public void setIcon(ThemeImage icon) {
		putState(PopoverState.ICON__PROP, ReactImages.encode(getReactContext(), icon));
	}

	/**
	 * Whether the surface is shown.
	 */
	public boolean isOpen() {
		return _open;
	}

	/**
	 * Shows the surface, creating its content when the popover opens for the first time.
	 *
	 * <p>
	 * Opening an open popover changes nothing.
	 * </p>
	 */
	public void open() {
		if (_open) {
			return;
		}
		_open = true;
		if (_content == null) {
			_content = _contentFactory.get();
		}

		Object tx = beginUpdate();
		putState(PopoverState.CONTENT__PROP, _content);
		putState(PopoverState.OPEN__PROP, true);
		commitUpdate(tx);

		if (isAttached()) {
			_content.attach();
		}
	}

	/**
	 * Hides the surface. Its content is detached and kept for the next {@link #open()}.
	 *
	 * <p>
	 * Closing a closed popover changes nothing.
	 * </p>
	 */
	public void close() {
		if (!_open) {
			return;
		}
		_open = false;

		Object tx = beginUpdate();
		putState(PopoverState.OPEN__PROP, false);
		putState(PopoverState.CONTENT__PROP, null);
		commitUpdate(tx);

		_content.detach();
	}

	/**
	 * Also disposes the content while the popover is closed: only the content of an open popover
	 * is part of the state.
	 */
	@Override
	protected void cleanupChildren() {
		super.cleanupChildren();
		if (_content != null) {
			_content.cleanupTree();
		}
	}

	/**
	 * Handles the {@link #CMD_OPEN} command of the client.
	 *
	 * <p>
	 * Opening is a gesture of its own, recorded and offered to the headless interface: what is done
	 * inside the surface can only be replayed on an open popover. Closing is
	 * {@link ReactCommandHandler#technical() technical}, like the closing of a menu.
	 * </p>
	 */
	@ReactCommandHandler(CMD_OPEN)
	void handleOpen() {
		open();
	}

	/**
	 * Handles the {@link #CMD_CLOSE} command of the client.
	 */
	@ReactCommandHandler(value = CMD_CLOSE, technical = true)
	void handleClose() {
		close();
	}

}
