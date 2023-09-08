/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel.streaming;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.util.Zipper;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ZippedCSVWriter extends AbstractCellStreamWriter {

	private final Files tables;

	private Writer currentStream;

	public ZippedCSVWriter(int expectedTableCount) throws IOException {
		tables = new Files(expectedTableCount);
	}

	protected Writer getCurrentStream() {
		return currentStream;
	}

	@Override
	public void setFreezePane(int col, int row) {
		// does nothing in CSV export
	}

	@Override
	protected void internalWrite(Object cellvalue) throws IOException {
		String value = StringServices.nonNull((String) cellvalue);
		currentStream.write(value);
	}

	@Override
	protected void internalNewRow() throws IOException {
		currentStream.write("\r\n");
	}

	@Override
	protected File internalClose() throws IOException {
		tables.closeAllStreams();

		List<String> names = tables.tablenames;
		List<File> files = tables.files;

		try (InputStream zip = Zipper.zip(files.toArray(new File[] {}), names.toArray(new String[] {}))) {
			File result = File.createTempFile("ZippedCSVWriter", ".zip", Settings.getInstance().getTempDir());
			FileUtilities.copyToFile(zip, result);
			return result;
		}
	}

	@Override
	protected void internalNewTable(String tablename) throws IOException {
		beginNewFile(tablename);
	}

	private void beginNewFile(String tablename) throws IOException {
		currentStream = tables.newFile(tablename);
	}

	private class Files {
		final List<File> files;

		final List<String> tablenames;

		final List<Writer> streams;

		public Files(int expectedTableCount) {
			files = new ArrayList<>(expectedTableCount);
			tablenames = new ArrayList<>(expectedTableCount);
			streams = new ArrayList<>(expectedTableCount);
		}

		public Writer newFile(String tablename) throws IOException {
			File tmpFile = File.createTempFile(tablename, ".csv", Settings.getInstance().getTempDir());
			Writer tmpWriter =
				new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(tmpFile), LayoutConstants.ISO_8859_1));
			tablenames.add(tablename + ".csv");
			files.add(tmpFile);
			streams.add(tmpWriter);
			return tmpWriter;
		}

		public void closeAllStreams() throws IOException {
			IOException problem = null;
			for (Writer writer : streams) {
				try {
					writer.close();
				} catch (IOException ex) {
					problem = ex;
				}
			}
			if (problem != null) {
				throw problem;
			}
		}
	}
}
