/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.io.binary.BinaryData;

/**
 * {@link FileManager} that delegates all functionality to some other FileManager.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FileManagerDelegate extends FileManager {

	private final FileManager _delegate;

	/**
	 * Creates a {@link FileManagerDelegate}.
	 */
	public FileManagerDelegate(FileManager delegate) {
		_delegate = delegate;
	}

	@Override
	public List<File> getIDEPaths() {
		return _delegate.getIDEPaths();
	}

	@Override
	public List<Path> getPaths() {
		return _delegate.getPaths();
	}

	@Override
	public List<BinaryData> getDataOverlays(String name) throws IOException {
		return _delegate.getDataOverlays(name);
	}

	@Override
	public Set<String> getResourcePaths(String path) {
		return _delegate.getResourcePaths(path);
	}

	@Override
	@Deprecated
	public List<File> getFiles(String name) throws IOException {
		return _delegate.getFiles(name);
	}

	@Override
	public BinaryData getDataOrNull(String aName) {
		return _delegate.getDataOrNull(aName);
	}

	@Override
	public void delete(String resourceName) throws IOException {
		_delegate.delete(resourceName);
	}

	@Override
	public File getIDEFile(String aName) {
		return _delegate.getIDEFile(aName);
	}
}
