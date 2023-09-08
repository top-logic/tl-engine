/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.nio.file.Path;
import java.util.Collection;

/**
 * General update created by {@link FileSystemCache} when files in the file system are created,
 * deleted or changed.
 * 
 * @see FileSystemCache
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class PathUpdate {

	private final Collection<Path> _creations;

	private final Collection<Path> _changes;

	private final Collection<Path> _deletions;

	/**
	 * Creates a new filesystem update.
	 */
	public PathUpdate(Collection<Path> creations, Collection<Path> changes, Collection<Path> deletions) {
		_creations = creations;
		_changes = changes;
		_deletions = deletions;
	}

	/**
	 * Collection of paths to new created files.
	 */
	public Collection<Path> getCreations() {
		return _creations;
	}

	/**
	 * Collection of paths to changed files.
	 */
	public Collection<Path> getChanges() {
		return _changes;
	}

	/**
	 * Collection of paths to deleted files.
	 */
	public Collection<Path> getDeletions() {
		return _deletions;
	}

}
