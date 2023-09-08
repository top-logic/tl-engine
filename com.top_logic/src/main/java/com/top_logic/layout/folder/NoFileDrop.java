/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.folder;

import java.util.List;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.common.folder.model.FolderNode;
import com.top_logic.layout.DisplayContext;

/**
 * Dummy {@link FileDropHandler} ignoring data.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoFileDrop implements FileDropHandler {

	/**
	 * Singleton {@link NoFileDrop} instance.
	 */
	public static final NoFileDrop INSTANCE = new NoFileDrop();

	private NoFileDrop() {
		// Singleton constructor.
	}

	@Override
	public void uploadFiles(DisplayContext context, FolderNode selectedFolder, List<BinaryData> files) {
		// Ignore.
	}

	/**
	 * Returns if upload is allowed.
	 * 
	 * <p>
	 * {@link NoFileDrop} ignores dropped files and uploads are never allowed.
	 * </p>
	 */
	@Override
	public boolean getuploadPossible() {
		return false;
	}

	@Override
	public long getMaxUploadSize() {
		return 0;
	}

}
