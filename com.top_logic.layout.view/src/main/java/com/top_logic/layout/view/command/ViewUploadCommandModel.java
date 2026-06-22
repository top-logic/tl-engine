/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.UploadCommandModel;
import com.top_logic.layout.view.channel.ViewChannel;

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
				() -> processFile(context, files, index + 1));
		} catch (RuntimeException ex) {
			Logger.error("Upload failed for file '" + file.getName() + "'.", ex, ViewUploadCommandModel.class);
			processFile(context, files, index + 1);
		}
	}
}
