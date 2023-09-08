/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.xml.source;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link ResourceTransaction} that updates a {@link File}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class FilesystemTransaction extends AbstractResourceTransaction {
	/**
	 * The original file created or updated.
	 */
	private final File _orig;

	private File _tmp;

	private FileOutputStream _out;

	/**
	 * Creates a {@link FilesystemTransaction}.
	 * 
	 * @param orig
	 *        The {@link File} to create or update.
	 */
	public FilesystemTransaction(File orig) {
		_orig = orig;
	}

	@Override
	protected OutputStream internalOpen() throws IOException {
		if (isOpen()) {
			throw new IllegalStateException("Transaction already open.");
		}

		File dir = _orig.getParentFile();
		if (dir == null) {
			dir = new File(".");
		} else {
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}

		_tmp = File.createTempFile(_orig.getName(), "", dir);
		_out = new FileOutputStream(_tmp);
		return _out;
	}

	@Override
	protected void internalCommit() throws IOException {
		if (!isOpen()) {
			return;
		}

		flush();

		File backup;
		if (_orig.exists()) {
			backup = File.createTempFile(_orig.getName(), "", _orig.getParentFile());
			backup.delete();
			boolean backupCreated = _orig.renameTo(backup);
			if (!backupCreated) {
				deleteTmp();
				throw new IOException("Cannot create backup file: " + backup.getPath());
			}
		} else {
			backup = null;
		}

		boolean updateInstalled = _tmp.renameTo(_orig);
		if (updateInstalled) {
			_tmp = null;
			if (backup != null) {
				backup.delete();
			}
		} else {
			// Revert.
			if (backup != null) {
				backup.renameTo(_orig);
			}
			deleteTmp();
			throw new IOException("Cannot update original file from: " + _tmp);
		}
	}

	@Override
	protected void internalClose() throws IOException {
		if (isOpen()) {
			if (_out != null) {
				flush();
			}

			deleteTmp();
		}
	}

	private void deleteTmp() {
		_tmp.delete();
		_tmp = null;
	}

	private void flush() throws IOException {
		_out.close();
		_out = null;
	}

	private boolean isOpen() {
		return _tmp != null;
	}
}