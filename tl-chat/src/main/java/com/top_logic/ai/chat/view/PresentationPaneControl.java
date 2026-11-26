/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * React control of {@code <presentation-pane>}, the component {@code TLPresentationPane}.
 *
 * <p>
 * Shows the history of a {@link PresentationPane}: every step is a child control, all of them
 * mounted and the top one shown, so that going back shows a step exactly as it was left.
 * </p>
 */
public class PresentationPaneControl extends ReactControl {

	private static final String REACT_MODULE = "TLPresentationPane";

	/**
	 * State: the steps of the history, bottom first, each with {@code id}, {@code kind}
	 * ({@code table}, {@code chart} or {@code form}), {@code title} and {@code control}; the last one
	 * is shown. Empty while the channel holds nothing.
	 */
	public static final String STEPS = "steps";

	/**
	 * Command of the back button: returns to the step below, or closes the pane.
	 *
	 * @see #back()
	 */
	public static final String CMD_BACK = "back";

	/**
	 * Command of the close button: writes {@code null} to the channel.
	 *
	 * @see #close()
	 */
	public static final String CMD_CLOSE = "close";

	private final PresentationPane _pane;

	/**
	 * Creates a {@link PresentationPaneControl}.
	 *
	 * @param context
	 *        The context the steps are built in.
	 * @param input
	 *        The channel whose values the pane shows.
	 */
	public PresentationPaneControl(ViewContext context, ViewChannel input) {
		super(context, null, REACT_MODULE);
		_pane = new PresentationPane(context, input, this, this::writeSteps);
		addCleanupAction(_pane::dispose);
		writeSteps();
	}

	/**
	 * Whether the pane shows anything.
	 */
	public boolean isOpen() {
		return _pane.isOpen();
	}

	/** Returns to the step below the top one, or closes the pane when there is none. */
	@ReactCommandHandler(CMD_BACK)
	public void back() {
		_pane.back();
	}

	/** Closes the pane, which is writing {@code null} to its channel. */
	@ReactCommandHandler(CMD_CLOSE)
	public void close() {
		_pane.close();
	}

	private void writeSteps() {
		if (_pane == null) {
			// Called by the pane while it is built; the steps are written right after.
			return;
		}
		putState(STEPS, _pane.state());
	}
}
