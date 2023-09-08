/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.folder;

import java.util.List;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.common.folder.model.FolderNode;
import com.top_logic.common.webfolder.ui.WebFolderComponent;
import com.top_logic.common.webfolder.ui.WebFolderUIFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.FolderField;

/**
 * Uploads dropped files or folders.
 * 
 * @author <a href="mailto:Diana.Pankratz@top-logic.com">Diana Pankratz</a>
 */
public interface FileDropHandler {

	/**
	 * Uploads all given files.
	 * 
	 * @param context
	 *        The {@link DisplayContext}
	 * @param selectedFolder
	 *        The Folder (e.g. in a {@link FolderField}) where the files should be uploaded.
	 * @param files
	 *        The {@link BinaryData} of the files.
	 * @see WebFolderComponent#getRenderingControl()
	 * @see WebFolderUIFactory#createControl(FolderField field)
	 */
	void uploadFiles(DisplayContext context, FolderNode selectedFolder, List<BinaryData> files);

	/**
	 * If upload of files/folders with drag and drop is possible.
	 */
	boolean getuploadPossible();

	/**
	 * Maximum size of uploaded data.
	 */
	long getMaxUploadSize();

}
