/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.layout.react.control.button.UploadCommandModel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link ViewCommandModel} for an {@link UploadCommand}: runs the command's action chain once per
 * uploaded file, sequentially.
 *
 * <p>
 * Files are processed one at a time - the next file starts only after the current file's chain has
 * fully settled (resumed past its last action <em>or</em> aborted). This serializes any per-file
 * {@code <confirm>} dialog, and makes each file independent: aborting one file's confirmation skips
 * only that file.
 * </p>
 *
 * <p>
 * A file whose chain fails is reported to the user through the window's snackbar - naming the file
 * and the reason the chain gave - and the remaining files are processed regardless, see
 * {@link CommandErrors}.
 * </p>
 */
public class ViewUploadCommandModel extends ViewCommandModel implements UploadCommandModel {

	private final UploadCommand _uploadCommand;

	/**
	 * Creates a new {@link ViewUploadCommandModel}.
	 */
	public ViewUploadCommandModel(UploadCommand command, UploadCommand.Config config, ViewChannel inputChannel,
			ViewExecutabilityRule rule) {
		super(command, config, inputChannel, rule);
		_uploadCommand = command;
	}

	@Override
	public String getAccept() {
		return _uploadCommand.getAccept();
	}

	@Override
	public boolean isMultiple() {
		return _uploadCommand.isMultiple();
	}

	@Override
	public void uploadFiles(ReactContext context, List<BinaryData> files) {
		processFile(context, files, 0);
	}

	private void processFile(ReactContext context, List<BinaryData> files, int index) {
		if (index >= files.size()) {
			return;
		}
		BinaryData file = files.get(index);
		try {
			// onComplete advances to the next file once this file's chain settles (success or
			// abort), so a suspended <confirm> dialog resolves before the next file is processed.
			ViewActionChain.run(context, _uploadCommand.getActions(), file,
				result -> processFile(context, files, index + 1));
		} catch (RuntimeException ex) {
			HandlerResult result =
				CommandErrors.failure(ex, "Upload of '" + file.getName() + "'", ViewUploadCommandModel.class);

			// The summary names the file that could not be uploaded, the reason the chain gave is
			// listed below it as detail.
			ResKey reason = result.getErrorTitle();
			result.setErrorTitle(I18NConstants.ERROR_UPLOAD_FAILED__FILE.fill(file.getName()));
			result.setErrorMessage(reason);

			CommandErrors.show(context.getErrorSink(), result);

			processFile(context, files, index + 1);
		}
	}
}
