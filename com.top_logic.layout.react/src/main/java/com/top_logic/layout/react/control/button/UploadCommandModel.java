/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

import java.util.List;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * A {@link CommandModel} for a button that triggers a client-side file picker and processes the
 * selected file(s) directly, instead of dispatching a server command on click.
 *
 * <p>
 * Rendered by a {@link ReactUploadButtonControl}: clicking the button opens the native file chooser;
 * on selection the files are uploaded and handed to {@link #upload(ReactContext, List)}, which
 * runs the configured behavior once per file.
 * </p>
 */
public interface UploadCommandModel extends CommandModel {

	/**
	 * The {@code accept} filter passed to the native file input (e.g. {@code "image/*,.pdf"}), or
	 * {@code null}/{@code "*"} for no restriction.
	 */
	String getAccept();

	/**
	 * Whether the file chooser allows selecting multiple files.
	 */
	boolean isMultiple();

	/**
	 * Processes the uploaded files, if the command is executable.
	 *
	 * <p>
	 * The upload counterpart of {@link #executeCommand(ReactContext)}: the command's
	 * {@link #getExecutableState() state} decides, and a command that is not executable processes
	 * nothing and returns the {@link HandlerResult#notExecutable(ExecutableState) refusal}
	 * carrying that state.
	 * </p>
	 *
	 * @param context
	 *        The view context the upload button was created in.
	 * @param files
	 *        The uploaded files (never empty).
	 * @return The result of processing the files, or the refusal of a command that is not
	 *         executable.
	 */
	default HandlerResult upload(ReactContext context, List<BinaryData> files) {
		ExecutableState state = getExecutableState();
		if (!state.isExecutable()) {
			return HandlerResult.notExecutable(state);
		}
		return uploadFiles(context, files);
	}

	/**
	 * Processes the uploaded files, in client-selection order.
	 *
	 * <p>
	 * Called by {@link #upload(ReactContext, List)} after the command has been found executable; an
	 * upload button hands its files to {@link #upload(ReactContext, List)}, never to this method.
	 * </p>
	 *
	 * @param context
	 *        The view context the upload button was created in (provides channels and the dialog
	 *        manager for any confirmation step).
	 * @param files
	 *        The uploaded files (never empty).
	 * @return The result of processing the files.
	 */
	HandlerResult uploadFiles(ReactContext context, List<BinaryData> files);
}
