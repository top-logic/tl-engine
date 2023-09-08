/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.path;

import static com.top_logic.basic.col.factory.CollectionFactory.*;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * {@link FileVisitor} that collects a {@link List} of all visited {@link Path}s.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class PathCollector extends SimpleFileVisitor<Path> {

	private final List<Path> _paths = list();

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		_paths.add(dir);
		return super.preVisitDirectory(dir, attrs);
	}

	@Override
	public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
		_paths.add(path);
		return super.visitFile(path, attrs);
	}

	/**
	 * All visited {@link Path}s.
	 * 
	 * @return A new, mutable and resizable {@link List}.
	 */
	public List<Path> getPaths() {
		return list(_paths);
	}

}
