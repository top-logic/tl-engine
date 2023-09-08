/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.dataImport;

import com.top_logic.basic.io.binary.BinaryData;

/**
 * Abstract command to import an uploaded ZIP file.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public abstract class ImportZipDataCommand extends UploadDataCommand {

	/**
	 * Creates a {@link ImportZipDataCommand} to import the ZIP file from the given
	 * {@link UploadDataDialog}.
	 */
	public ImportZipDataCommand(UploadDataDialog uploadDataDialog) {
		super(uploadDataDialog);
	}

	@Override
	protected void uploadPostProcess(BinaryData data) {
		createImporter(data).doImport();
	}

	/**
	 * Creates the importer to process ZIP data.
	 */
	protected abstract ZipImporter createImporter(BinaryData data);

}
