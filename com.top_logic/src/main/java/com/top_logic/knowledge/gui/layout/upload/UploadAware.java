/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.upload;

import java.util.List;

import com.top_logic.basic.io.binary.BinaryData;

/**
 * Declares the interface for the upload-handling part of an
 * application to access the receiving part of the application.
 * 
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public interface UploadAware extends MultiUploadAware {

    /**
	 * Processes the uploaded file.
	 * 
	 * @param file
	 *        The uploaded data.
	 */
	public void receiveFile(BinaryData file) throws Exception;

	@Override
	default void receiveFiles(List<BinaryData> files) throws Exception {
		for (BinaryData file : files) {
			receiveFile(file);
		}
	}
}
