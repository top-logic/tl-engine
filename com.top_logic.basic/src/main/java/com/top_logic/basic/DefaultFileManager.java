/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;

/**
 * Default {@link FileManager} implementation based on a single root path.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultFileManager extends FileManager {

	private File _root;

	/**
	 * Creates a {@link DefaultFileManager} for the current directory.
	 */
	public DefaultFileManager() {
		this(new File("./").getAbsoluteFile());
	}

	/**
	 * Creates a {@link DefaultFileManager}.
	 * 
	 * @param rootPath
	 *        The root path of the virtual file system.
	 */
	public DefaultFileManager(String rootPath) {
		this(new File(rootPath).getAbsoluteFile());
	}

	/**
	 * Creates a {@link DefaultFileManager}.
	 * 
	 * @param root
	 *        The root path of the virtual file system.
	 */
	public DefaultFileManager(File root) {
		assert root.exists() : "Root path does not exist: " + root;

		_root = root;
	}

	@Override
	public List<File> getIDEPaths() {
		return Collections.singletonList(_root);
	}

	@Override
	public List<Path> getPaths() {
		return Collections.singletonList(_root.toPath());
	}

	@Override
	public List<BinaryData> getDataOverlays(String name) throws IOException {
		BinaryData result = getDataOrNull(name);
		if (result == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(result);
	}

	@Override
	public Set<String> getResourcePaths(String path) {
		Collection<File> files = FileUtilities.listFilesSafe(toFile(path), (d, n) -> !n.startsWith("."));

		return FileUtilities.getPrefixedFilenames(files, path);
	}

	@Override
	@Deprecated
	public List<File> getFiles(String name) throws IOException {
		File file = toFile(name);
		if (file.exists()) {
			return Collections.singletonList(file);
		}
		return Collections.emptyList();
	}

	@Override
	public BinaryData getDataOrNull(String aName) {
		File file = toFile(aName);
		if (!file.exists()) {
			return null;
		}
		return BinaryDataFactory.createBinaryDataWithName(file, aName);
	}

	@Override
	public void delete(String resourceName) throws IOException {
		File file = toFile(resourceName);
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("Cannot delete directory resources: " + resourceName);
			}
			boolean result = file.delete();
			if (!result) {
				throw new IOException("Failed to delete resource: " + resourceName);
			}
		}
	}

	@Override
	public File getIDEFile(String aName) {
		return toFile(aName);
	}

	private File toFile(String resourceName) {
		File direct = resolveDirect(resourceName);
		if (direct != null) {
			return direct;
		}
		return new File(_root, toPath(resourceName));
	}

	/**
	 * Creates a relative path name for the given absolute resource name.
	 */
	protected String toPath(String resourceName) {
		return relative(resourceName);
	}
}
