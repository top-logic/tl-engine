/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * A watch service that watches registered file system paths for changes and events.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public abstract class FileSystemCache extends ManagedClass {

	/**
	 * Whether this instance does caching at all.
	 */
	public abstract boolean isCaching();

	/**
	 * All real paths for a given resource name.
	 */
	public abstract List<Path> pathOverlays(String resourceName);

	/**
	 * The (top-level) path for a given resource name.
	 */
	public abstract Path resolveFile(String resourceName);

	/**
	 * A queue of {@link PathUpdate}s.
	 * 
	 * <p>
	 * Note: Before fetching more elements, {@link #fetchUpdates()} must be called.
	 * </p>
	 */
	public abstract Iterator<PathUpdate> getUpdates();

	/**
	 * Fetches file system updates an puts them to the update queues returned be
	 * {@link #getUpdates()}.
	 */
	public abstract void fetchUpdates();

	/**
	 * The {@link FileSystemCache} service.
	 */
	// Note: This method must not be named getInstace(). Otherwise, the configuration things the
	// service class was a singleton and directly calls getInstance() instead of instantiating the
	// configured class.
	public static FileSystemCache getCache() {
		if (!Module.INSTANCE.isActive()) {
			return NoFileSystemCache.INSTANCE;
		}
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Service reference for {@link FileSystemCache}.
	 */
	public static class Module extends TypedRuntimeModule<FileSystemCache> {

		/**
		 * Singleton {@link FileSystemCache.Module} instance.
		 */
		public static final FileSystemCache.Module INSTANCE = new FileSystemCache.Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<FileSystemCache> getImplementation() {
			return FileSystemCache.class;
		}

		@Override
		protected Class<? extends FileSystemCache> getDefaultImplementationClass() {
			return NoFileSystemCache.class;
		}

	}

}
