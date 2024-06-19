/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.MultiFileManager;
import com.top_logic.basic.col.GCQueue;
import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.basic.util.StopWatch;

/**
 * {@link FileSystemCache} that keeps track of multiple overlay paths.
 */
public class IDEFileSystemCache extends FileSystemCache {

	private WatchService _watcher;

	private FileManager _fm;

	private final Map<String, List<Path>> _fileCache = new HashMap<>();

	private List<Path> _paths;

	private final GCQueue<PathUpdate> _updates = new GCQueue<>();

	private final Iterator<PathUpdate> _cacheUpdate = _updates.iterator();

	/**
	 * Creates a {@link IDEFileSystemCache}.
	 */
	public IDEFileSystemCache() {
		super();
	}

	@Override
	public boolean isCaching() {
		return true;
	}

	@Override
	public List<Path> pathOverlays(String resourceName) {
		validateCache();
		return new ArrayList<>(_fileCache.getOrDefault(resourceName, Collections.emptyList()));
	}

	@Override
	public Path resolveFile(String resourceName) {
		validateCache();
		return CollectionUtil.getFirst(_fileCache.get(resourceName));
	}

	@Override
	public Iterator<PathUpdate> getUpdates() {
		return _updates.iterator();
	}

	@Override
	public synchronized void fetchUpdates() {
		if (_watcher == null) {
			return;
		}

		WatchKey key = poll();
		if (key == null) {
			return;
		}

		_updates.add(createUpdate(key));
	}

	private PathUpdate createUpdate(WatchKey key) {
		Set<Path> creations = new LinkedHashSet<>(), changes = new LinkedHashSet<>(), deletions = new LinkedHashSet<>();

		while (key != null) {
			processEvents(key, creations, changes, deletions);
			key.reset();

			key = poll();
		}

		return new PathUpdate(creations, changes, deletions);
	}

	private void processEvents(WatchKey key, Set<Path> creations, Set<Path> changes, Set<Path> deletions) {
		Map<Path, Kind<?>[]> eventKindsByPath = new HashMap<>();

		for (WatchEvent<?> event : key.pollEvents()) {
			addEventKind(eventKindsByPath, ((Path) key.watchable()).resolve((Path) event.context()), event.kind());
		}

		for (Entry<Path, Kind<?>[]> entry : eventKindsByPath.entrySet()) {
			Kind<?> eventKind = reduceEventKinds(entry.getValue()[0], entry.getValue()[1]);

			addPathByEventKind(creations, changes, deletions, entry.getKey(), eventKind);
		}
	}

	private void addEventKind(Map<Path, Kind<?>[]> eventsByPath, Path key, Kind<?> eventKind) {
		Kind<?>[] events = eventsByPath.get(key);

		if (events == null) {
			eventsByPath.put(key, new Kind<?>[] { eventKind, eventKind });
		} else {
			events[1] = eventKind;
		}
	}

	private Kind<?> reduceEventKinds(Kind<?> firstEventKind, Kind<?> lastEventKind) {
		if (firstEventKind == ENTRY_CREATE) {
			return lastEventKind == ENTRY_DELETE ? null : ENTRY_CREATE;
		} else if (firstEventKind == ENTRY_MODIFY) {
			return lastEventKind == ENTRY_DELETE ? ENTRY_DELETE : ENTRY_MODIFY;
		} else if (firstEventKind == ENTRY_DELETE) {
			return lastEventKind == ENTRY_DELETE ? ENTRY_DELETE : ENTRY_MODIFY;
		} else {
			return lastEventKind == OVERFLOW ? null : lastEventKind;
		}
	}

	private void addPathByEventKind(Set<Path> creations, Set<Path> changes, Set<Path> deletions, Path path,
			Kind<?> kind) {
		if (kind != null) {
			if (kind == ENTRY_CREATE) {
				if (Files.isDirectory(path)) {
					walkAndRegisterDirectories(path);
				}

				creations.add(path);
			} else if (kind == ENTRY_MODIFY) {
				changes.add(path);
			} else if (kind == ENTRY_DELETE) {
				deletions.add(path);
			}
		}
	}

	private WatchKey poll() {
		for (WatchKey key = _watcher.poll(); key != null; key = _watcher.poll()) {
			if (key.isValid()) {
				return key;
			}
		}

		return null;
	}

	@Override
	protected void startUp() {
		super.startUp();

		try {
			_watcher = FileSystems.getDefault().newWatchService();

			_fm = FileManager.getInstance();
			List<Path> idePaths = _fm.getIDEPaths().stream().map(f -> f.toPath()).collect(Collectors.toList());
			for (Path path : idePaths) {
				if (Files.exists(path)) {
					walkAndRegisterDirectories(path.normalize());
				}
			}

			_paths = _fm.getPaths();
			StopWatch watch = StopWatch.createStartedWatch();
			for (int n = 0; n < _paths.size(); n++) {
				Path root = _paths.get(n);
				Path dir = root.resolve(ModuleLayoutConstants.WEB_INF_DIR);
				if (Files.exists(dir) && Files.isDirectory(dir)) {
					scan(new StringBuilder(ModuleLayoutConstants.WEB_INF_DIR), dir);
				}
			}
			watch.stop();
			Logger.info("Indexing WEB-INF files took " + watch.toString() + ".", MultiFileManager.class);
		} catch (IOException exception) {
			Logger.warn("Unable to create watch service for the filesystem. ", exception, this);
		}
	}

	@Override
	protected void shutDown() {
		try {
			if (_watcher != null) {
				_watcher.close();
				_watcher = null;
			}
		} catch (IOException exception) {
			Logger.error("Unable to close the watch service that is watching for file system changes.", exception,
				this);
		}
	
		super.shutDown();
	}

	/**
	 * Register the given directory, and all its sub-directories, with the {@link WatchService}.
	 */
	private void walkAndRegisterDirectories(final Path start) {
		try {
			Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					try {
						dir.register(_watcher, ENTRY_MODIFY, ENTRY_DELETE, ENTRY_CREATE);
					} catch (IOException exception) {
						Logger.error("register of watchkey for " + dir + " failed.", exception, this);
					}

					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException exception) {
			Logger.error("Unable to register watchers for " + start, exception, this);
		}
	}

	private void scan(StringBuilder resourceName, Path path) {
		try {
			if (Files.isDirectory(path)) {
				try (Stream<Path> listing = Files.list(path)) {
					listing.filter(c -> !c.getFileName().toString().startsWith("."))
					.forEach(content -> {
						int lengthBefore = resourceName.length();
						try {
							String fileName = content.getFileName().toString();
							if (!fileName.isEmpty()) {
								resourceName.append('/');
								if (fileName.charAt(fileName.length() - 1) == '/') {
									// ZIP paths have file names ending with '/'.
									resourceName.append(fileName, 0, fileName.length() - 1);
								} else {
									resourceName.append(fileName);
								}
							}
							scan(resourceName, content);
						} finally {
							resourceName.setLength(lengthBefore);
						}
					});
				}
			} else {
				_fileCache.computeIfAbsent(resourceName.toString(), k -> new ArrayList<>()).add(path);
			}
		} catch (IOException exception) {
			Logger.error("Path " + path + " could not be cached", exception, this);
		}
	}

	private synchronized void validateCache() {
		fetchUpdates();

		while (_cacheUpdate.hasNext()) {
			PathUpdate update = _cacheUpdate.next();

			addFilesToCache(update.getCreations());
			removeFromCache(update.getDeletions());
		}
	}

	private void addFilesToCache(Collection<Path> creations) {
		for (Path creation : creations) {
			int indexOfRootFile = getIndexOfRootPath(creation);

			if (indexOfRootFile != -1) {
				try {
					Files.walk(creation).filter(Files::isRegularFile).forEach(path -> {
						addFileToCache(path, indexOfRootFile);
					});
				} catch (IOException exception) {
					Logger.error("Path " + creation + " can not be accessed.", exception, this);
				}
			}
		}
	}

	private void addFileToCache(Path path, int indexOfRootFile) {
		String pathFromRoot = FileUtilities.getCombinedPath(getPathFromRoot(path, indexOfRootFile));

		if (pathFromRoot.startsWith(ModuleLayoutConstants.WEB_INF_DIR)) {
			addFileToCache(path, indexOfRootFile, pathFromRoot);
		}
	}

	private void addFileToCache(Path path, int indexOfRootFile, String pathFromRoot) {
		List<Path> cachedFiles = _fileCache.computeIfAbsent(pathFromRoot, k -> new ArrayList<>());
		Path canonicalFile = path.normalize();

		if (cachedFiles.contains(canonicalFile)) {
			return;
		}

		if (cachedFiles.isEmpty()) {
			cachedFiles.add(canonicalFile);
		} else {
			List<Path> paths = _paths;
			for (int j = indexOfRootFile + 1; j < paths.size(); j++) {
				Path file = paths.get(j).resolve(pathFromRoot);

				if (Files.exists(file)) {
					int indexOf = cachedFiles.indexOf(file.normalize());
					cachedFiles.add(indexOf, canonicalFile);
					return;
				}
			}

			cachedFiles.add(canonicalFile);
		}
	}

	private void removeFromCache(Collection<Path> deletions) {
		for (Path deletion : deletions) {
			removeFromCache(deletion);
		}
	}

	private void removeFromCache(Path deletion) {
		String normalizedPath = FileUtilities.getCombinedPath(getPathFromRoot(deletion));

		if (normalizedPath.startsWith(ModuleLayoutConstants.WEB_INF_DIR)) {
			List<Path> files = _fileCache.get(normalizedPath);
			if (files != null) {
				files.remove(deletion.normalize());
			}

			/* As the path is deleted, it can not be inspected to check whether it is a
			 * directory. */
			/* It can not be ensured that the removed path represents a regular file. Therefore it
			 * may be a directory a potential child may have been removed. */
			// Even if the directory was not cached a child may have been cached.
			removeChildResources(normalizedPath, deletion);
		}
	}

	/** Note: This code is totally inefficient, but deletion of files is very rare. */
	private void removeChildResources(String resource, Path deletedPath) {
		for (Entry<String, List<Path>> cacheEntry : _fileCache.entrySet()) {
			String cacheKey = cacheEntry.getKey();
			if (cacheKey.startsWith(resource) && cacheKey.length() > resource.length()) {
				List<Path> cacheValue = cacheEntry.getValue();
				for (int i = cacheValue.size() - 1; i >= 0; i--) {
					if (cacheValue.get(i).startsWith(deletedPath)) {
						cacheValue.remove(i);
					}
				}
			}
		}
	}

	private Path getPathFromRoot(Path path) {
		return getPathFromRoot(path, getIndexOfRootPath(path));
	}

	private Path getPathFromRoot(Path path, int indexOfRootPath) {
		return _paths.get(indexOfRootPath).relativize(path);
	}

	private int getIndexOfRootPath(Path path) {
		for (int i = 0; i < _paths.size(); i++) {
			if (path.startsWith(_paths.get(i))) {
				return i;
			}
		}

		return -1;
	}

}
