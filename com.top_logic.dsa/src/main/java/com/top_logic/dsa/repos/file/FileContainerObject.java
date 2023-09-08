/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.repos.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.dsa.repos.ContainerObject;
import com.top_logic.dsa.repos.LeafObject;
import com.top_logic.dsa.repos.RepositoryException;

/**
 * File based implementation of the container.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class FileContainerObject implements ContainerObject {

	private final File _file;

	/**
	 * Creates a {@link FileContainerObject}.
	 * 
	 * @param aParent
	 *        Parent file directory for the new container.
	 * @param aName
	 *        Name of the new container.
	 */
	public FileContainerObject(FileContainerObject aParent, String aName) {
		this(new File(aParent.getFile(), aName));
	}

	/**
	 * Creates a {@link FileContainerObject}.
	 * 
	 * @param aName
	 *        Name of the new container.
	 */
	public FileContainerObject(String aName) {
		this(new File(aName));
	}

	/**
	 * Creates a {@link FileContainerObject}.
	 * 
	 * @param aFile
	 *        Directory to be represented.
	 */
	public FileContainerObject(File aFile) {
		_file = aFile;
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("name", _file.getName())
			.add("path", _file.getPath())
			.add("dir", _file.getAbsolutePath())
			.build();
	}

	@Override
	public String getName() {
		return _file.getName();
	}

	@Override
	public String getPath() {
		return _file.getPath();
	}

	@Override
	public ContainerObject getParentContainer() {
		File theFile = this.getFile();
		return (theFile != null) ? new FileContainerObject(theFile.getParentFile()) : null;
	}

	@Override
	public long lastModified() {
		return this.getFile().lastModified();
	}

	@Override
	public String getParent() {
		return this.getFile().getParent();
	}

	@Override
	public String getAbsolutePath() {
		return this.getFile().getAbsolutePath();
	}

	@Override
	public ContainerObject createTempFile(String aPrefix, String aSuffix) throws IOException {
		return new FileContainerObject(File.createTempFile(aPrefix, aSuffix, this.getFile()));
	}

	@Override
	public LeafObject createLeafObject(String versionfile) {
		return new FileLeafObject(this.getFile(), versionfile);
	}

	@Override
	public List<LeafObject> listChildren() throws RepositoryException {
		List<LeafObject> theChildren = new ArrayList<>();

		try {
			for (File theChild : FileUtilities.listFiles(this.getFile())) {
				theChildren.add(new FileLeafObject(theChild));
			}
		}
		catch (IOException ex) {
			throw new RepositoryException(ex);
		}

		return theChildren;
	}

	@Override
	public List<String> list() {
		return CollectionUtil.toList(this.getFile().list());
	}

	@Override
	public boolean delete() {
		return this.getFile().delete();
	}

	@Override
	public boolean mkdir() {
		return this.getFile().mkdir();
	}

	@Override
	public boolean mkdirs() {
		return this.getFile().mkdirs();
	}

	@Override
	public boolean exists() {
		return this.getFile().exists();
	}

	@Override
	public boolean isDirectory() {
		return this.getFile().isDirectory();
	}

	@Override
	public boolean renameTo(ContainerObject aNew) {
		return this.getFile().renameTo(((FileContainerObject) aNew).getFile());
	}

	@Override
	public ContainerObject createContainer(String aName) {
		return new FileContainerObject(this, aName);
	}

	/**
	 * The represented file.
	 */
	public File getFile() {
		return _file;
	}
}

