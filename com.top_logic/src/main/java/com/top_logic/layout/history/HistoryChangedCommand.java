/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.history;

import java.util.Iterator;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The {@link HistoryChangedCommand} is triggered on the client if the 'browser
 * back' or the 'browser forward' button was pushed.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class HistoryChangedCommand extends ControlCommand {

	static final String COMMAND_ID = "changed";
	// end of Constant are used in history.js

	static final HistoryChangedCommand INSTANCE = new HistoryChangedCommand();

	/**
	 * parameter whose value is set at the client.
	 * 
	 * the value is the {@link IdentifiedEntry#getID() id} of some
	 * {@link IdentifiedEntry} written in
	 * {@link HistoryControl#historyContentHandler} under the key
	 * 'services.ajax.currentState'.
	 */
	private static final String CURRENT_STATE = "requestedState";

	private HistoryChangedCommand() {
		super(COMMAND_ID);
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		final String entryId = (String) arguments.get(CURRENT_STATE);
		requestEntry(commandContext, (HistoryControl) control, entryId);

		return HandlerResult.DEFAULT_RESULT;
	}

	void requestEntry(DisplayContext context, HistoryControl control, String requiredEntryId) {
		final HistoryQueue historyQueue = control.historyQueue;

		if (HistoryControl.SAFETY_FRAME.equals(requiredEntryId)) {
			WarnLogoutDialog.openDialog(context, control);
			/* must revert the client side browser back action directly, as during the dialog is
			 * open, the browser and client are inconsistent */
			revertChanges(context, control, historyQueue.getFirst());
			return;
		}

		// search for the index of the entry with the requested id
		int index = historyQueue.size();
		final Iterator<IdentifiedEntry> it = historyQueue.iterator(true);
		while (it.hasNext()) {
			if (it.next().getID().equals(requiredEntryId)) {
				break;
			} else {
				index--;
			}
		}

		if (index == 0) {
			/*
			 * No entry with the given ID. Maybe the history on the browser is
			 * larger then the history stack on the server, so no replay is
			 * possible.
			 */
			WarnLogoutDialog.openDialog(context, control);
			/* must revert the client side browser back action directly, as during the dialog is
			 * open, the browser and client are inconsistent */
			revertChanges(context, control, historyQueue.getFirst());
			return;
		}

		// browser back was pushed
		if (index < historyQueue.getIndex()) {
			do {
				IdentifiedEntry elementToUndo = historyQueue.current();
				if (elementToUndo.endsStack()) {
					NotUndoableOperationDialog.openDialog(context, control);
					/* must revert the client side browser back action directly, as during the
					 * dialog is open, the browser and client are inconsistent */
					revertChanges(context, control, elementToUndo);
					return;
				} else {
					try {
						elementToUndo.undo(context);
					} catch (HistoryException ex) {
						revertChanges(context, control, elementToUndo);
						return;
					}
					historyQueue.decreaseCurrentIndex();
				}
			} while (index < historyQueue.getIndex());
		} else

		// browser forward was pushed
		if (historyQueue.getIndex() < index) {
			do {
				historyQueue.increaseCurrentIndex();
				IdentifiedEntry elementToRedo = historyQueue.current();
				try {
					elementToRedo.redo(context);
				} catch (HistoryException ex) {
					revertChanges(context, control, elementToRedo);
					return;
				}
			} while (historyQueue.getIndex() < index);
		}
	}

	/**
	 * Reverts the control to the given entry
	 * 
	 * @param context
	 *        context in which history changed
	 * @param control
	 *        current history control
	 * @param entry
	 *        entry to revert to
	 */
	void revertChanges(DisplayContext context, HistoryControl control, IdentifiedEntry entry) {
		control.updateToHistory(entry);
		if (context.getUserAgent().is_firefox()) {
			/* Seems to be an absurdity of FF when using browser back button. It does not only
			 * switch back the history frame but also the frame currently visible to a different
			 * content. In BrowserWindowControl it is ensured that each browser back action displays
			 * a warn dialog. This code ensures that the client is consistent after showing dialog. */
			context.getWindowScope().getTopLevelFrameScope().addClientAction(MainLayout.createFullReload());
		}
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.HISTORY_CHANGED;
	}

}
