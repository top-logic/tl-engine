/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.folder.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.Named;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.common.folder.FolderDefinition;

/**
 * A lazy {@link FolderDefinition} that creates its content from the given directory {@link File}.
 * <p>
 * Directories are represented as {@link FileFolderDefinition}s and non-directory files are
 * represented as {@link FileDocument}.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class FileFolderDefinition implements FolderDefinition {

	private final File _file;

	/**
	 * Creates a {@link FileFolderDefinition} for the given directory {@link File}.
	 * 
	 * @param file
	 *        Is not allowed to be null. Has to be a {@link File#isDirectory() directory}.
	 */
	public FileFolderDefinition(File file) {
		if (!file.isDirectory()) {
			throw new IllegalArgumentException("This is no directory: " + FileUtilities.getSafeDetailedPath(file));
		}
		_file = file;
	}

	/**
	 * The underlying {@link File}.
	 */
	public File getFile() {
		return _file;
	}

	@Override
	public String getName() {
		return _file.getName();
	}

	@Override
	public Collection<Named> getContents() {
		File[] rawFiles;
		try {
			rawFiles = FileUtilities.listFiles(_file);
		} catch (IOException ex) {
			Logger.error("Directory listing failed: " + ex.getMessage(), ex);
			return Collections.emptyList();
		}
		List<Named> result = new ArrayList<>();
		for (File file : rawFiles) {
			result.add(wrap(file));
		}
		return result;
	}

	private Named wrap(File file) {
		if (file.isDirectory()) {
			return new FileFolderDefinition(file);
		}
		return new FileDocument(file);
	}

	@Override
	public boolean isLinkedContent(Named content) {
		return false;
	}

	@Override
	public String toString() {
		return new NameBuilder(this).addUnnamed(_file.getName()).build();
	}

}
