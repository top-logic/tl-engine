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
 * <li>{@code dialogs} - list of dialog child descriptors (managed internally)</li>
 * </ul>
 */
public class ReactDialogManagerControl extends ReactControl implements DialogManager {

	private static final String REACT_MODULE = "TLDialogManager";

	private static final String DIALOGS = "dialogs";

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

		return result -> closeDialog(entry, result);
	}

	@Override
	public void closeTopDialog(DialogResult<Void> result) {
		if (_stack.isEmpty()) {
			return;
		}
		closeDialog(_stack.get(_stack.size() - 1), result);
	}

	private void closeDialog(DialogEntry entry, DialogResult<Void> result) {
		int index = _stack.indexOf(entry);
		if (index < 0) {
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

	private void patchDialogsState() {
		List<ReactControl> dialogs = _stack.stream()
			.map(DialogEntry::dialog)
			.collect(Collectors.toList());
		putState(DIALOGS, dialogs);
	}

	@Override
	protected void cleanupChildren() {
		for (DialogEntry entry : _stack) {
			entry.dialog().cleanupTree();
		}
		_stack.clear();
	}

	private record DialogEntry(ReactDialogControl dialog, DialogResultHandler<Void> handler) {
		// Pure data record.
	}

}
