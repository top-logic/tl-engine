/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.basic.core.workspace.Workspace;
import com.top_logic.basic.io.FileSystemCache;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;

/**
 * Helps to locate files in stacked projects or modules.
 * <p>
 * Imagine a base project (<i>TopLogic</i>) a Business-Framework (POS)
 * and some final customer Solution (FCS). Every project may override
 * files from one of the other. So files are searched in the order
 * FCS, POS, <i>TopLogic</i>. New files will always be created in FCS,
 * so basic projects are not polluted with garbage files.
 * </p>
 * <p>
 * This is only a base class. Subclasses must provide the actual implementation
 * for a Swing-/Web-/J2EE- or Whatever-Application.
 * </p>   
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class MultiFileManager extends FileManager {

	/**
	 * Creates a {@link MultiFileManager} for the given web application path in a development
	 * environment.
	 */
	public static FileManager createForModularApplication() {
		return createMultiFileManager(Workspace.applicationModules());
	}

	/**
	 * Creates a {@link MultiFileManager} using the given explicit resource path names.
	 */
	public static FileManager createMultiFileManager(Path... paths) {
		return createMultiFileManager(Arrays.asList(paths));
	}

	/**
	 * Creates a {@link MultiFileManager} using the given explicit resource path names.
	 */
	public static FileManager createMultiFileManager(String... paths) {
		return createMultiFileManager(paths(paths));
	}

	/**
	 * Creates a {@link MultiFileManager} using the given explicit resource paths.
	 */
	public static FileManager createMultiFileManager(List<Path> paths) {
		return new MultiFileManager(paths);
	}

	private List<Path> _paths;

	/**
	 * The directory of the running web application.
	 */
	private Path _topLevelWebapp;

    /**
	 * Construct a FileManager with some predefined directories.
	 * 
	 * Allows subclasses to create the files some other way.
	 * 
	 * @param paths
	 *        Directories to search files in.
	 */
	protected MultiFileManager(List<Path> paths) {
		if (paths.isEmpty()) {
			throw new IllegalArgumentException("No paths given.");
		}

		_paths = paths;

		Optional<File> topLevelWebapp = paths.stream()
			.filter(this::inDefaultFileSystem)
			.map(Path::toFile)
			.filter(this::mainFolder)
			.findFirst();
		_topLevelWebapp = (topLevelWebapp.isPresent() ? topLevelWebapp.get() : new File(".")).toPath();
    }

	/**
	 * The underlying {@link Path}s of the modular web application.
	 */
	protected final List<Path> paths() {
		return _paths;
	}

	@Deprecated
	@Override
	public List<File> getIDEPaths() {
		return filterDirectories(_paths);
	}

	@Override
	public List<Path> getPaths() {
		return _paths;
	}

	@Override
	public Set<String> getResourcePaths(String resourcePrefixName) {
		File direct = resolveDirect(resourcePrefixName);
		if (direct != null) {
			if (!direct.exists()) {
				return Collections.emptySet();
			}

			Collection<File> files = FileUtilities.listFilesSafe(direct, (d, n) -> !n.startsWith("."));
			return FileUtilities.getPrefixedFilenames(files, resourcePrefixName);
		}

		String normalizedPrefix = normalizeResourcePrefix(resourcePrefixName);

		Set<String> files = new HashSet<>();
		List<Path> paths = paths();
		String path = relative(resourcePrefixName);
		for (int n = paths.size() - 1; n >= 0; n--) {
			Path root = paths.get(n);
			Path base = root.resolve(path);
			if (Files.isDirectory(base)) {
				try (Stream<Path> contents = Files.list(base)) {
					contents
						.filter(p -> !p.getFileName().toString().startsWith("."))
						.forEach(p -> {
							String fileName = p.getFileName().toString();
							if (Files.isDirectory(p)) {
								if (fileName.charAt(fileName.length() - 1) != '/') {
									files.add(normalizedPrefix + fileName + "/");
								} else {
									files.add(normalizedPrefix + fileName);
								}
							} else {
								files.add(normalizedPrefix + fileName);
							}
						});
				} catch (IOException ex) {
					Logger.error("Cannot list: " + base, ex);
				}
			}
		}
		return files;
	}

	@Override
	public List<BinaryData> getDataOverlays(String path) throws IOException {
		return pathOverlays(path).stream().map(f -> BinaryDataFactory.createBinaryDataWithName(f, path))
			.collect(Collectors.toList());
	}

	@Deprecated
	@Override
	public List<File> getFiles(String resourceName) throws IOException {
		return filterDirectories(pathOverlays(resourceName));
	}

	private List<File> filterDirectories(List<Path> overlays) {
		return overlays.stream().filter(this::inDefaultFileSystem).map(Path::toFile).collect(Collectors.toList());
	}

	private List<Path> pathOverlays(String resourceName) throws IOException {
		File direct = resolveDirect(resourceName);
		if (direct != null) {
			if (direct.exists()) {
				return Collections.singletonList(direct.getCanonicalFile().toPath());
			} else {
				return Collections.emptyList();
			}
		} else {
			FileSystemCache cache = FileSystemCache.getCache();
			String path = relative(normalize(resourceName));
			if (cache.isCaching() && isRegularFile(path) && path.startsWith(ModuleLayoutConstants.WEB_INF_DIR)) {
				return cache.pathOverlays(path);
			} else {
				List<Path> files = new ArrayList<>();
				List<Path> paths = paths();
				for (Path root : paths) {
					Path file = root.resolve(path);
					if (Files.exists(file)) {
						files.add(file);
					}
				}
				return files;
			}
		}
	}

	private boolean isRegularFile(String path) {
		return path.lastIndexOf(".") > path.lastIndexOf("/");
	}

	@Override
	public URL getResourceUrl(String name) throws MalformedURLException {
		Path path = resolveFile(name, true);
		if (path == null) {
			return null;
		}
		return path.toUri().toURL();
	}

	@Override
	public BinaryData getDataOrNull(String name) {
		Path file = resolveFile(name, true);
		if (file == null) {
			return null;
		}
		return BinaryDataFactory.createBinaryDataWithName(file, name);
	}

	@Override
	public InputStream getStreamOrNull(String name) throws IOException {
		Path file = resolveFile(name, true);
		if (file != null) {
			return Files.newInputStream(file);
		}
		
		return getClasspathResourceAsStream(name);
	}

	@Override
	public void delete(String resourceName) throws IOException {
		Path file = resolveFile(resourceName, true);
		if (file != null) {
			if (Files.isDirectory(file)) {
				throw new IOException("Cannot delete directory resources: " + resourceName);
			}
			Files.delete(file);
		}
	}

	@Override
	public File getIDEFile(String resourceName) {
		Path path = resolveFile(resourceName, false);
		if (inDefaultFileSystem(path)) {
			return path.toFile();
		}

		return inTopLevelWebapp(resourceName).toFile();
	}

	private Path inTopLevelWebapp(String resourceName) {
		return _topLevelWebapp.resolve(relative(normalize(resourceName)));
	}

	private boolean inDefaultFileSystem(Path path) {
		return path.getFileSystem() == FileSystems.getDefault();
	}

	private Path resolveFile(String resourceName, boolean nullIfNotExists) {
		final File direct = resolveDirect(resourceName);
		if (direct != null) {
			if (nullIfNotExists && !direct.exists()) {
				return null;
			}
			return direct.toPath();
		}

		FileSystemCache cache = FileSystemCache.getCache();
		String path = relative(normalize(resourceName));
		if (cache.isCaching() && path.startsWith(ModuleLayoutConstants.WEB_INF_DIR)) {
			Path cachedResult = cache.resolveFile(path);
			if (cachedResult != null || nullIfNotExists) {
				return cachedResult;
			}

			// Not in cache, create top-level path.
			return inTopLevelWebapp(resourceName);
		}

		List<Path> paths = paths();
		int size = paths.size();
		for (int n = 0; n < size; n++) {
			Path file = paths.get(n).resolve(path);

			/**
			 * The Files.exists method has poor performance. Files that are located in the same
			 * filesystem should avoid using this API and use instead the resolved file itself.
			 * 
			 * The file object representing this path can not be resolved if the path points towards
			 * another filesystem (for instance into a jar container).
			 */
			if (inDefaultFileSystem(file)) {
				if (file.toFile().exists()) {
					return file;
				}
			} else {
				if (Files.exists(file)) {
					return file;
				}
			}
		}

		if (nullIfNotExists) {
			return null;
		}

		return inTopLevelWebapp(resourceName);
	}

	private static List<Path> paths(String[] pathNames) {
		Path[] paths = new Path[pathNames.length];
		for (int n = 0, cnt = pathNames.length; n < cnt; n++) {
			paths[n] = Paths.get(pathNames[n]).normalize();
		}
		return Arrays.asList(paths);
	}

	/**
	 * Whether the given folder is not a deploy folder.
	 */
	private boolean mainFolder(File dir) {
		try {
			File deployFolder = dir.getParentFile();
			if (deployFolder == null) {
				return true;
			}

			if (dir.getName().equals(ModuleLayoutConstants.WEBAPP_LOCAL_DIR_NAME)
				&& deployFolder.getName().equals(ModuleLayoutConstants.TEST_ASPECT)) {
				return false;
			}

			File deployRoot = deployFolder.getParentFile();
			if (deployRoot == null) {
				return true;
			}
			return !deployRoot.getCanonicalFile().getName().equals(ModuleLayoutConstants.DEPLOY_FOLDER_NAME);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

}
