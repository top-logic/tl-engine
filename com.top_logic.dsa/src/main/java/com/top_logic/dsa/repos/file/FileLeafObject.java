/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.repos.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.dsa.repos.LeafObject;

/**
 * File based implementation of the leaf.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class FileLeafObject implements LeafObject {

	private final String _name;

	private final File _file;

	/**
	 * Creates a {@link LeafObject}.
	 * 
	 * @param aContainer
	 *        Container we live in.
	 * @param aName
	 *        Name of this leaf object.
	 */
	public FileLeafObject(File aContainer, String aName) {
		_name = aName;
		_file = new File(aContainer, aName);
	}

	/**
	 * Creates a {@link LeafObject}.
	 * 
	 * @param aFile
	 *        File represented by this leaf object.
	 */
	public FileLeafObject(File aFile) {
		_name = aFile.getName();
		_file = aFile;
	}

	@Override
	public void copy(LeafObject aDest) throws IOException {
		FileUtilities.copyFile(this.getFile(), ((FileLeafObject) aDest).getFile());
	}

	@Override
	public void copy(InputStream aStream) throws IOException {
		FileUtilities.copyToFile(aStream, this.getFile());
	}

	@Override
	public boolean delete() {
		return this.getFile().delete();
	}

	@Override
	public long lastModified() {
		return this.getFile().lastModified();
	}

	@Override
	public long length() {
		return this.getFile().length();
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public String getAbsolutePath() {
		return this.getFile().getAbsolutePath();
	}

	@Override
	public OutputStream getOutputStream() throws FileNotFoundException {
		return new FileOutputStream(this.getFile());
	}

	@Override
	public InputStream getInputStream() throws FileNotFoundException {
		return new FileInputStream(this.getFile());
	}

	@Override
	public Writer getWriter() throws IOException {
		return new FileWriter(this.getFile());
	}

	/**
	 * The represented file.
	 */
	protected File getFile() {
		return _file;
	}
}

