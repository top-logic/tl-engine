/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * {@link FileSystemCache} that does no caching at all.
 */
public final class NoFileSystemCache extends FileSystemCache {

	/**
	 * Singleton {@link NoFileSystemCache} instance.
	 */
	public static final NoFileSystemCache INSTANCE = new NoFileSystemCache();

	private NoFileSystemCache() {
		// Singleton constructor.
	}

	@Override
	public boolean isCaching() {
		return false;
	}

	@Override
	public List<Path> pathOverlays(String resourceName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Path resolveFile(String resourceName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<PathUpdate> getUpdates() {
		return Collections.emptyIterator();
	}

	@Override
	public void fetchUpdates() {
		// Ignore.
	}
}