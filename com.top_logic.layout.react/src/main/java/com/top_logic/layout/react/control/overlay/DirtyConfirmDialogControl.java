/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackDirection;
import com.top_logic.layout.react.control.table.ReactTextCellControl;
import com.top_logic.layout.react.dirty.StateHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Builds and opens a dirty-check confirmation dialog composed of standard React controls.
 *
 * <p>
 * Uses {@link ReactWindowControl} for the visual frame with {@link ReactButtonControl}s in the
 * footer. The dialog asks the user whether to save, discard, or cancel when navigating away from
 * dirty forms. The "save" option is hidden when any handler has validation errors.
 * </p>
 */
public class DirtyConfirmDialogControl {

	/**
	 * Opens a dirty-check confirmation dialog.
	 *
	 * @param context
	 *        The React context.
	 * @param dialogManager
	 *        The dialog manager for opening/closing the dialog.
	 * @param dirtyHandlers
	 *        The dirty state handlers.
	 * @param continuation
	 *        The action to execute after save/discard.
	 * @param rollback
	 *        Optional action to revert optimistic UI changes on cancel (e.g. table selection
	 *        rollback), or {@code null}.
	 */
	public static void openDialog(ReactContext context, DialogManager dialogManager,
			List<StateHandler> dirtyHandlers, Runnable continuation, Runnable rollback) {
		Resources resources = Resources.getInstance();

		String title = resources.getString(I18NConstants.DIRTY_CONFIRM_TITLE);
		String message = resources.getString(I18NConstants.DIRTY_CONFIRM_MESSAGE);
		String saveLabel = resources.getString(I18NConstants.DIRTY_CONFIRM_SAVE);
		String discardLabel = resources.getString(I18NConstants.DIRTY_CONFIRM_DISCARD);
		String cancelLabel = resources.getString(I18NConstants.DIRTY_CONFIRM_CANCEL);

		boolean canSave = dirtyHandlers.stream().noneMatch(StateHandler::hasErrors);

		// Body: message text + list of dirty handler descriptions.
		List<ReactControl> bodyChildren = new ArrayList<>();
		bodyChildren.add(new ReactTextCellControl(context, message));
		for (StateHandler handler : dirtyHandlers) {
			bodyChildren.add(new ReactTextCellControl(context, "\u2022 " + handler.getDescription()));
		}
		ReactStackControl body = new ReactStackControl(context, bodyChildren);

		// Close handler for window X button — delegates to DialogManager (result handler does rollback).
		Runnable closeHandler = () -> dialogManager.closeTopDialog(DialogResult.cancelled());

		// Window chrome.
		ReactWindowControl window = new ReactWindowControl(context, title,
			DisplayDimension.px(420), closeHandler);
		window.setChild(body);

		// Footer action buttons.
		List<ReactControl> actions = new ArrayList<>();

		ReactButtonControl cancelBtn = new ReactButtonControl(context, cancelLabel, ctx -> {
			dialogManager.closeTopDialog(DialogResult.cancelled());
			return HandlerResult.DEFAULT_RESULT;
		});
		actions.add(cancelBtn);

		ReactButtonControl discardBtn = new ReactButtonControl(context, discardLabel, ctx -> {
			for (StateHandler handler : dirtyHandlers) {
				handler.executeDiscard();
			}
			dialogManager.closeTopDialog(DialogResult.ok(null));
			continuation.run();
			return HandlerResult.DEFAULT_RESULT;
		});
		actions.add(discardBtn);

		if (canSave) {
			ReactButtonControl saveBtn = new ReactButtonControl(context, saveLabel, ctx -> {
				for (StateHandler handler : dirtyHandlers) {
					handler.executeSave();
				}
				dialogManager.closeTopDialog(DialogResult.ok(null));
				continuation.run();
				return HandlerResult.DEFAULT_RESULT;
			});
			actions.add(saveBtn);
		}

		window.setActions(actions);

		// Open via DialogManager. The result handler runs rollback on cancel — this covers ALL
		// cancel paths: Cancel button, X button, Escape key, and backdrop click.
		dialogManager.openDialog(false, window, result -> {
			if (result.isCancelled() && rollback != null) {
				rollback.run();
			}
		});
	}
}
