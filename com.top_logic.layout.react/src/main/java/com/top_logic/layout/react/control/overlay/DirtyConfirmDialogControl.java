/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.dirty.StateHandler;

/**
 * Content control for the dirty-check confirmation dialog.
 *
 * <p>
 * Renders a dialog asking the user whether to save, discard, or cancel when navigating away from
 * dirty forms. The "save" option is hidden when any handler has validation errors.
 * </p>
 *
 * <p>
 * State sent to React:
 * </p>
 * <ul>
 * <li>{@code descriptions} - list of handler descriptions (strings)</li>
 * <li>{@code canSave} - whether save is available (no validation errors)</li>
 * </ul>
 */
public class DirtyConfirmDialogControl extends ReactControl {

	private static final String REACT_MODULE = "TLDirtyConfirmDialog";

	private static final String DESCRIPTIONS = "descriptions";

	private static final String CAN_SAVE = "canSave";

	private final List<StateHandler> _dirtyHandlers;

	private final Runnable _continuation;

	private final DialogManager _dialogManager;

	/**
	 * Creates a new {@link DirtyConfirmDialogControl}.
	 *
	 * @param context
	 *        The React context.
	 * @param dirtyHandlers
	 *        The dirty state handlers.
	 * @param continuation
	 *        The action to execute after save/discard.
	 * @param dialogManager
	 *        The dialog manager for closing this dialog.
	 */
	public DirtyConfirmDialogControl(ReactContext context, List<StateHandler> dirtyHandlers,
			Runnable continuation, DialogManager dialogManager) {
		super(context, null, REACT_MODULE);
		_dirtyHandlers = dirtyHandlers;
		_continuation = continuation;
		_dialogManager = dialogManager;

		List<String> descriptions = new ArrayList<>();
		boolean canSave = true;
		for (StateHandler handler : dirtyHandlers) {
			descriptions.add(handler.getDescription());
			if (handler.hasErrors()) {
				canSave = false;
			}
		}
		putState(DESCRIPTIONS, descriptions);
		putState(CAN_SAVE, Boolean.valueOf(canSave));
	}

	/**
	 * Saves all dirty handlers, closes the dialog, and continues.
	 */
	@ReactCommand("save")
	void handleSave() {
		for (StateHandler handler : _dirtyHandlers) {
			handler.executeSave();
		}
		_dialogManager.closeTopDialog(DialogResult.ok(null));
		_continuation.run();
	}

	/**
	 * Discards all dirty handlers, closes the dialog, and continues.
	 */
	@ReactCommand("discard")
	void handleDiscard() {
		for (StateHandler handler : _dirtyHandlers) {
			handler.executeDiscard();
		}
		_dialogManager.closeTopDialog(DialogResult.ok(null));
		_continuation.run();
	}

	/**
	 * Cancels the navigation, keeping dirty state intact.
	 */
	@ReactCommand("cancel")
	void handleCancel() {
		_dialogManager.closeTopDialog(DialogResult.cancelled());
	}
}
