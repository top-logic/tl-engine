/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * Manages a stack of modal dialogs that are rendered as overlays on top of the application.
 *
 * <p>
 * This control is embedded in the {@link com.top_logic.layout.react.control.nav.ReactAppShellControl
 * app shell} and provides the {@link DialogManager} API for opening and closing dialogs
 * dynamically. Each dialog is a {@link ReactDialogControl} wrapping a content child.
 * </p>
 *
 * <p>
 * State:
 * </p>
 * <ul>
 * <li>{@link #DIALOGS} - list of dialog child descriptors (managed internally)</li>
 * </ul>
 */
public class ReactDialogManagerControl extends ReactControl implements DialogManager {

	private static final String REACT_MODULE = "TLDialogManager";

	/** Client state field holding the stack of open dialogs, bottom-most first. */
	public static final String DIALOGS = "dialogs";

	private final List<DialogEntry> _stack = new ArrayList<>();

	/**
	 * Creates a dialog manager control.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 */
	public ReactDialogManagerControl(ReactContext context) {
		super(context, null, REACT_MODULE);
		putState(DIALOGS, List.of());
		context.getSSEQueue().setDialogManager(this);
	}

	@Override
	public DialogHandle openDialog(boolean closeOnBackdrop, ReactControl child,
			DialogResultHandler<Void> handler) {
		ReactContext ctx = getReactContext();

		ReactDialogControl dialog = new ReactDialogControl(ctx, closeOnBackdrop,
			() -> closeTopDialog(DialogResult.cancelled()));
		dialog.setChild(child);
		dialog.open();

		DialogEntry entry = new DialogEntry(dialog, handler);
		_stack.add(entry);
		patchDialogsState();

		if (isAttached()) {
			// An opened dialog is displayed right away, so it must not stay detached: its content
			// contributes to the display (e.g. a form adding its commands to the dialog's button bar)
			// only while attached.
			dialog.attach();
		}

		return entry;
	}

	@Override
	public void closeTopDialog(DialogResult<Void> result) {
		if (_stack.isEmpty()) {
			return;
		}
		closeDialog(_stack.get(_stack.size() - 1), result);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The dialogs above are closed top-down, stopping at the first one that is not
	 * {@link DialogHandle#isClosable() closable}: that dialog and everything it hides stay open.
	 * </p>
	 */
	@Override
	public void closeDialogsAbove(DialogHandle dialog) {
		int index = _stack.indexOf(dialog);
		if (index < 0) {
			return;
		}
		while (_stack.size() > index + 1) {
			DialogEntry top = _stack.get(_stack.size() - 1);
			if (!top.isClosable()) {
				break;
			}
			_stack.remove(_stack.size() - 1);
			top.dialog().cleanupTree();
			top.handler().onResult(DialogResult.cancelled());
		}
		patchDialogsState();
	}

	/**
	 * Closes the given dialog together with all dialogs stacked on top of it.
	 *
	 * <p>
	 * The close is refused as a whole while the given dialog or any dialog above it is not
	 * {@link DialogHandle#isClosable() closable}: nothing is closed and no result handler is called.
	 * A dialog is only ever closed together with everything covering it, so a dialog that must stay
	 * open also keeps the ones below it open.
	 * </p>
	 *
	 * @param entry
	 *        The dialog to close.
	 * @param result
	 *        The result to pass to the dialog's handler.
	 */
	private void closeDialog(DialogEntry entry, DialogResult<Void> result) {
		int index = _stack.indexOf(entry);
		if (index < 0) {
			return;
		}
		if (!allClosableFrom(index)) {
			return;
		}

		// Cascade: close all dialogs above the target (top-down).
		while (_stack.size() > index + 1) {
			DialogEntry top = _stack.remove(_stack.size() - 1);
			top.dialog().cleanupTree();
			top.handler().onResult(DialogResult.cancelled());
		}

		// Close the target dialog.
		_stack.remove(index);
		entry.dialog().cleanupTree();
		patchDialogsState();
		entry.handler().onResult(result);
	}

	/**
	 * Whether the dialog at the given stack position and every dialog above it is
	 * {@link DialogHandle#isClosable() closable}.
	 */
	private boolean allClosableFrom(int index) {
		for (int n = index, size = _stack.size(); n < size; n++) {
			if (!_stack.get(n).isClosable()) {
				return false;
			}
		}
		return true;
	}

	private void patchDialogsState() {
		List<ReactControl> dialogs = _stack.stream()
			.map(DialogEntry::dialog)
			.collect(Collectors.toList());
		putState(DIALOGS, dialogs);
	}

	@Override
	protected void cleanupChildren() {
		super.cleanupChildren();
		_stack.clear();
	}

	/**
	 * One open dialog, serving as the {@link DialogHandle} its opener addresses it by.
	 */
	private final class DialogEntry implements DialogHandle {

		private final ReactDialogControl _dialog;

		private final DialogResultHandler<Void> _handler;

		DialogEntry(ReactDialogControl dialog, DialogResultHandler<Void> handler) {
			_dialog = dialog;
			_handler = handler;
		}

		ReactDialogControl dialog() {
			return _dialog;
		}

		DialogResultHandler<Void> handler() {
			return _handler;
		}

		@Override
		public void close(DialogResult<Void> result) {
			closeDialog(this, result);
		}

		@Override
		public void setClosable(boolean closable) {
			_dialog.setClosable(closable);
		}

		@Override
		public boolean isClosable() {
			return _dialog.isClosable();
		}
	}

}
