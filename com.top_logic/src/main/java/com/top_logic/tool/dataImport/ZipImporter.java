/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.dataImport;

import java.io.BufferedInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.top_logic.basic.io.binary.BinaryData;

/**
 * Algorithm processing ZIP file contents.
 * 
 * @see ImportZipDataCommand#createImporter(BinaryData)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ZipImporter {

	private BinaryData _data;

	/**
	 * Creates a {@link ZipImporter}.
	 */
	public ZipImporter(BinaryData data) {
		_data = data;
	}

	/**
	 * Starts the import.
	 */
	public void doImport() {
		try (InputStream stream = _data.getStream()) {
			uploadPostProcess(stream);
		} catch (IOException exception) {
			throw new IOError(exception);
		}
	}

	private void uploadPostProcess(InputStream stream) throws IOException {
		try (BufferedInputStream bufferedInputStream = new BufferedInputStream(stream)) {
			zipUploadPostProcess(bufferedInputStream);
		}
	}

	private void zipUploadPostProcess(BufferedInputStream bufferedInputStream) throws IOException {
		try (ZipInputStream zipStream = new ZipInputStream(bufferedInputStream)) {
			importZipEntries(zipStream);
		}
	}

	private void importZipEntries(ZipInputStream zipInputStream) throws IOException {
		ZipEntry zipEntry = zipInputStream.getNextEntry();

		while (zipEntry != null) {
			importZipEntry(zipInputStream, zipEntry);

			zipEntry = zipInputStream.getNextEntry();
		}
	}

	/**
	 * Imports the given {@link ZipEntry}.
	 */
	protected abstract void importZipEntry(ZipInputStream zipStream, ZipEntry zipEntry);

}
