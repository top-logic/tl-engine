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
import com.top_logic.layout.react.control.button.MessageButtons;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.layout.ReactInsetControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.dirty.ChannelVetoException;
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
	 * Applies a change of the display that unsaved changes may refuse, asking the user about them
	 * and applying it again once they answered.
	 *
	 * <p>
	 * A change the user has to be asked about is carried out in two goes: the first one ends in the
	 * {@link ChannelVetoException} that names the unsaved changes standing in its way, and the
	 * second one - run from the dialog button the user pressed - finds them saved or discarded and
	 * goes through. What refuses the second go is another form deeper in the display, which is asked
	 * about exactly like the first one.
	 * </p>
	 *
	 * @param context
	 *        The context the dialog is opened in.
	 * @param dialogManager
	 *        Where the question is put to the user, or {@code null} where nothing can ask.
	 * @param step
	 *        The change to apply. Leaves the display consistent where it is refused, because what it
	 *        lets through is a question to the user, not a half-applied change.
	 * @param onRefusedWithoutDialog
	 *        Run instead of asking when there is no {@link DialogManager}, so that the caller can
	 *        give up what the refused change was part of. May be {@code null}.
	 */
	public static void guard(ReactContext context, DialogManager dialogManager, Runnable step,
			Runnable onRefusedWithoutDialog) {
		try {
			step.run();
		} catch (ChannelVetoException veto) {
			if (context == null || dialogManager == null) {
				if (onRefusedWithoutDialog != null) {
					onRefusedWithoutDialog.run();
				}
				return;
			}
			openDialog(context, dialogManager, veto.getDirtyHandlers(),
				() -> guard(context, dialogManager, step, onRefusedWithoutDialog), veto.getRollback());
		}
	}

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
			List<? extends StateHandler> dirtyHandlers, Runnable continuation, Runnable rollback) {
		Resources resources = Resources.getInstance();

		String title = resources.getString(I18NConstants.DIRTY_CONFIRM_TITLE);
		String message = resources.getString(I18NConstants.DIRTY_CONFIRM_MESSAGE);

		boolean canSave = dirtyHandlers.stream().noneMatch(StateHandler::hasErrors);

		// Body: message text + list of dirty handler descriptions.
		List<ReactControl> bodyChildren = new ArrayList<>();
		bodyChildren.add(new ReactTextControl(context, message));
		for (StateHandler handler : dirtyHandlers) {
			bodyChildren.add(new ReactTextControl(context, "\u2022 " + handler.getDescription()));
		}
		// The window body is flush, so the content brings its own inset.
		ReactInsetControl body = new ReactInsetControl(context, new ReactStackControl(context, bodyChildren));

		// Close handler for window X button — delegates to DialogManager (result handler does rollback).
		Runnable closeHandler = () -> dialogManager.closeTopDialog(DialogResult.cancelled());

		// Window chrome.
		ReactWindowControl window = new ReactWindowControl(context, title,
			DisplayDimension.px(550), closeHandler);
		window.setResizable(true);
		window.setChild(body);

		// Footer action buttons using MessageButtons for consistent icons.
		List<ReactControl> actions = new ArrayList<>();

		actions.add(MessageButtons.cancel(context, ctx -> {
			dialogManager.closeTopDialog(DialogResult.cancelled());
			return HandlerResult.DEFAULT_RESULT;
		}));

		actions.add(MessageButtons.discard(context, ctx -> {
			for (StateHandler handler : dirtyHandlers) {
				handler.executeDiscard();
			}
			dialogManager.closeTopDialog(DialogResult.ok(null));
			continuation.run();
			return HandlerResult.DEFAULT_RESULT;
		}));

		if (canSave) {
			actions.add(MessageButtons.save(context, ctx -> {
				for (StateHandler handler : dirtyHandlers) {
					handler.executeSave();
				}
				dialogManager.closeTopDialog(DialogResult.ok(null));
				continuation.run();
				return HandlerResult.DEFAULT_RESULT;
			}));
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
