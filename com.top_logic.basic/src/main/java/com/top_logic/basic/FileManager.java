/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import jakarta.servlet.ServletContext;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.AbstractBinaryData;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.mime.MimeTypesModule;

/**
 * Access to web application resources in a TopLogic application.
 * 
 * <p>
 * The {@link FileManager} encapsulates the {@link ServletContext} and additionally allows to create
 * new resources when the application is run in an IDE.
 * </p>
 */
public abstract class FileManager {

	/**
	 * Constant used as prefix for a file pathname to indicate that the pathname shall be resolved
	 * directly, not relative to one of the internal pathes.
	 * 
	 * @see #markDirect(String)
	 */
	private static final String DIRECT_FILE_RESOLVING_PREFIX = "file://";

	/**
	 * String length of {@link #DIRECT_FILE_RESOLVING_PREFIX}.
	 */
	private static final int DIRECT_FILE_RESOLVING_PREFIX_LENGTH = DIRECT_FILE_RESOLVING_PREFIX.length();

	/**
	 * @deprecated Use {@link #createData(String, BinaryContent)}.
	 */
	@Deprecated
    public static final boolean IGNORE_EXISTANCE = true;

    /** Singleton pattern */
    private static volatile FileManager singleton;

	/**
	 * Creates a {@link FileManager}.
	 */
	protected FileManager() {
		super();
	}

	/**
	 * The list of all paths checked out in the current IDE, this {@link FileManager} looks up
	 * resources from.
	 */
	@FrameworkInternal
	public abstract List<File> getIDEPaths();

	/**
	 * The list of paths, this {@link FileManager} looks up resources from.
	 */
	@FrameworkInternal
	public abstract List<Path> getPaths();

	/**
	 * Checks whether a resource with the given name exists.
	 */
	public boolean exists(String name) {
		return getDataOrNull(name) != null;
	}

	/**
	 * Constructs an {@link URL} pointing to the web application resource with the given name.
	 *
	 * @param name
	 *        The name of the resource.
	 * @return An {@link URL} pointing to the resource with the given name, or <code>null</code>, if
	 *         no such resource exists.
	 * 
	 * @see ServletContext#getResource(String)
	 */
	public URL getResourceUrl(String name) throws MalformedURLException {
		File result = getIDEFileOrNull(name);
		if (result == null) {
			return null;
		}
		return result.toURI().toURL();
	}

	/**
	 * Returns an input stream for the requested file.
	 * 
	 * @since 5.7.3
	 * 
	 * @param fileName
	 *            the (relative) path to the requested file.
	 *            
	 * @return an {@link InputStream} for the requested file. never <code>null</code>.
	 * 
	 * @throws IOException
	 *             If the requested file could not be found.
	 *             
	 * @see ServletContext#getResourceAsStream(String)
	 */
	public final InputStream getStream(String fileName) throws IOException {
		InputStream stream = getStreamOrNull(fileName);
		if (stream != null) {
			return stream;
		}
		throw failNotExists(fileName);
	}

	/**
	 * Return an input stream for the requested path.
	 * 
	 * This implementation falls back to {@link #getDataOrNull(String)}, but others
	 * may use other context to return streams.
	 * 
	 * @since 5.7.3
	 * 
	 * @param name
	 *            The name of the file (including its path).
	 * @return An InputStream for the requested file, or null
	 * 
	 * @see #getStream(String)
	 */
	public InputStream getStreamOrNull(String name) throws IOException {
		URL resource = getResourceUrl(name);
		if (resource != null) {
			return resource.openStream();
		}
		return getClasspathResourceAsStream(name);
	}

	/**
	 * Look up resource from <code>/META-INF/resources</code>.
	 */
	protected final InputStream getClasspathResourceAsStream(String name) {
		// Simulate a JAR resources.
		return getClass().getResourceAsStream("/META-INF/resources" + normalize(name));
	}

	/**
	 * Normalizes the given resource name representing a web application resource.
	 */
	protected static String normalize(String path) {
		if (!path.isEmpty() && path.charAt(0) != '/') {
			Logger.error("Web application resources must start with a '/' character: " + path, FileManager.class);
			path = "/" + path;
		}
		if (path.indexOf('\\') > 0) {
			Logger.error("Web application resources must not contain a back slash character: " + path,
				FileManager.class);
			path = path.replaceAll("\\\\", "/");
		}
		if (path.indexOf("//") > 0) {
			Logger.error("Web application resource paths must be normalized: " + path, FileManager.class);
			path = path.replaceAll("//+", "/");
		}
		return path;
	}

	/**
	 * Normalizes the given resource prefix name representing a web application resource directory.
	 */
	protected static String normalizeResourcePrefix(String resourcePrefixName) {
		resourcePrefixName = normalize(resourcePrefixName);
		if (!resourcePrefixName.isEmpty() && resourcePrefixName.charAt(resourcePrefixName.length() - 1) != '/') {
			Logger.error("Directory resource names must end with a '/' character: " + resourcePrefixName,
				FileManager.class);
			resourcePrefixName = resourcePrefixName + '/';
		}
		return resourcePrefixName;
	}

	/**
	 * Converts a resource name to a relative path that can be resolved relative to some base
	 * directory.
	 */
	protected static String relative(String name) {
		if (!name.isEmpty() && name.charAt(0) == '/') {
			return name.substring(1);
		}
		return name;
	}

	/**
	 * A set of resource names of resources with the given common prefix.
	 * 
	 * @param path
	 *        The directory path name to list.
	 * @return A listing of sub-paths below the given path, see {@link #isDirectory(String)}.
	 * 
	 * @see ServletContext#getResourcePaths(String)
	 */
	public abstract Set<String> getResourcePaths(String path);

	/**
	 * Whether the given resource represents a directory that can be listed with
	 * {@link #getResourcePaths(String)}.
	 * 
	 * @param resourceName
	 *        A resource name returned by {@link #getResourcePaths(String)}.
	 */
	public boolean isDirectory(String resourceName) {
		return !resourceName.isEmpty() && resourceName.charAt(resourceName.length() - 1) == '/';
	}

	/**
	 * The resource with the given path.
	 * 
	 * <p>
	 * In case name starts with {@value #DIRECT_FILE_RESOLVING_PREFIX} it will be resolved directly
	 * using whatever name is given.
	 * </p>
	 * 
	 * @param name
	 *        The name of the resource (including its path relative to the web application root).
	 * @return A handle for the requested resource, never <code>null</code>. If the requested
	 *         resource does not exist, accessing the stream will fail.
	 */
	public final BinaryData getData(String name) {
		return new FileManagerBinaryContent(name);
	}

	/**
	 * The resource with the given path.
	 * 
	 * <p>
	 * In case name starts with {@value #DIRECT_FILE_RESOLVING_PREFIX} it will be resolved directly
	 * using whatever name is given.
	 * </p>
	 * 
	 * @param name
	 *        The name of the resource (including its path relative to the web application root).
	 * @return A handle for the requested resource, nor <code>null</code>, if the resource does not
	 *         exist.
	 */
	public abstract BinaryData getDataOrNull(String name);

	/**
	 * Creates a new or overrides an existing resource in the current workspace.
	 *
	 * @param aName
	 *        The name of the resource (including its path relative to the web application root).
	 * @param content
	 *        The content to store.
	 * @throws IOException
	 *         If reading the contents or writing the destination fails.
	 */
	public void createData(String aName, BinaryContent content) throws IOException {
		FileUtilities.copyToFile(content, getIDEFile(aName));
	}

	/**
	 * Deletes the resource with the given name.
	 *
	 * @param resourceName
	 *        The web application resource name of the resource to delete. The name is expected to
	 *        start with a '/' character.
	 * @throws IOException
	 *         If deletion fails.
	 */
	public abstract void delete(String resourceName) throws IOException;

	/**
	 * Resolves a path name to all overridden versions of a resource in all stacked web application
	 * fragments.
	 * 
	 * <p>
	 * The returned resources have the same order as {@link #getIDEPaths()}.
	 * </p>
	 * 
	 * @param name
	 *        The name of the resource.
	 */
	public abstract List<BinaryData> getDataOverlays(String name) throws IOException;

	/**
	 * A {@link File} reference to the resource with the given path name if it exists in the
	 * expanded part of the modular web application.
	 * 
	 * <p>
	 * In case name starts with {@value #DIRECT_FILE_RESOLVING_PREFIX} it will be resolved directly
	 * using whatever name is given.
	 * </p>
	 * 
	 * @param resourceName
	 *        The name of the file (including its path relative to the web application root)
	 *        starting with a '/' character.
	 * @return The requested file reference, or <code>null</code> if no such file exists or it is
	 *         not expanded.
	 */
	@FrameworkInternal
	public final File getIDEFileOrNull(String resourceName) {
		File result = getIDEFile(resourceName);
		if (!result.exists()) {
			return null;
		}
		return result;
	}

	/**
	 * Returns a file using the {@link #getIDEPaths()} setting.
	 * 
	 * <p>
	 * If no existing file is found, a file in the top-level resource path is returned.
	 * </p>
	 * 
	 * In case aName starts with {@value #DIRECT_FILE_RESOLVING_PREFIX} it will be resolved directly
	 * using whatever name is given, the <code>ignoreExistence</code> will not have any effect.
	 * 
	 * @param aName
	 *        The name of the requested file.
	 * @return The requested file reference. If no resource with the given path name exists, a file
	 *         with the corresponding name is returned in the the top-level web application.
	 * 
	 * @see #getIDEFileOrNull(String)
	 */
	@FrameworkInternal
	public abstract File getIDEFile(String aName);

	/**
	 * A {@link File} reference to the resource with the given path name if it exists in the
	 * expanded part of the modular web application.
	 * 
	 * <p>
	 * In case name starts with {@value #DIRECT_FILE_RESOLVING_PREFIX} it will be resolved directly
	 * using whatever name is given.
	 * </p>
	 * 
	 * @param aName
	 *        The name of the file (including its path relative to the web application root).
	 * @return The requested file reference, or <code>null</code> if no such file exists or it is
	 *         not expanded.
	 * 
	 * @Use {@link #getDataOrNull(String)}, {@link #getStreamOrNull(String)},
	 *      {@link #getResourceUrl(String)}.
	 */
	@Deprecated
	public final File getFile(String aName) {
		return getIDEFileOrNull(aName);
	}

	/**
	 * Same as {@link #getFile(String)} but throws an exception, if the referenced resource does not
	 * exist.
	 * 
	 * @Use {@link #getData(String)}, {@link #getStream(String)}, {@link #getResourceUrl(String)}.
	 */
	@Deprecated
	public final File getFileNotNull(String aName) throws FileNotFoundException {
		File result = getIDEFileOrNull(aName);
		if (result == null) {
			throw failNotExists(aName);
		}
		return result;
	}

    /**
	 * Returns a file using the {@link #getIDEPaths()} setting.
	 * 
	 * In case aName starts with {@value #DIRECT_FILE_RESOLVING_PREFIX} it will be resolved directly
	 * using whatever name is given, the <code>ignoreExistence</code> will not have any effect.
	 * 
	 * If <code>ignoreExistence</code> is set the potential file will be found in the very first
	 * path known to the FileManager.
	 * 
	 * @param aName
	 *        The name of the requested file.
	 * @param ignoreExistence
	 *        Returns the file found in the very first path known to the FileManager, even if it
	 *        does not exist
	 * @return The requested file or <code>null</code>, if no file could be found.
	 * 
	 * @deprecated Use {@link #getData(String)} or {@link #getDataOrNull(String)} depending on the
	 *             boolean parameter.
	 */
	@Deprecated
	public final File getFile(String aName, boolean ignoreExistence) {
		if (ignoreExistence) {
			return getIDEFile(aName);
		} else {
			return getIDEFileOrNull(aName);
		}
	}
    
	/**
	 * Returns a file using the {@link #getIDEPaths()} setting.
	 * 
	 * In case aName starts with {@value #DIRECT_FILE_RESOLVING_PREFIX} it will be resolved directly
	 * using whatever name is given, the <code>ignoreExistence</code> will not have any effect.
	 * 
	 * If <code>ignoreExistence</code> is set the potential file will be found in the very first
	 * path known to the FileManager.
	 * 
	 * @param aName
	 *        The name of the requested file.
	 * @param ignoreExistence
	 *        Returns the file found in the very first path known to the FileManager, even if it
	 *        does not exist
	 * @return The requested file or <code>null</code>, if no file could be found.
	 * 
	 * @deprecated Use {@link #getData(String)} or {@link #getDataOrNull(String)} or
	 *             {@link #createData(String, BinaryContent)} depending on the boolean parameters.
	 */
	@Deprecated
	public final File getFile(String aName, boolean ignoreExistence, boolean onlyTopPath) {
		if (onlyTopPath) {
			throw new UnsupportedOperationException();
		} else {
			return getFile(aName, ignoreExistence);
		}
	}

	/**
	 * File references to resources returned in {@link #getDataOverlays(String)} that exist as
	 * expanded resources in the workspace.
	 *
	 * @param name
	 *        The name of the resource.
	 * 
	 * @deprecated Use {@link #getDataOverlays(String)} to get a consistent behavior independent of
	 *             the current workspace layout.
	 */
	@Deprecated
	public abstract List<File> getFiles(String name) throws IOException;

    /**
	 * Throws an exception announcing that a non-existing resource was requested.
	 */
	protected final FileNotFoundException failNotExists(String aName) throws FileNotFoundException {
		throw new FileNotFoundException("Could not resolve '" + aName + "' to an existing resource. ");
	}
	
	@Override
	public String toString() {
		return this.getClass().getName() + " with path: "
			+ StringServices.join(getIDEPaths(), File.pathSeparator);
	}

    /**
     * Define the given manager as default manager for files or images.
     * 
     * @param    newFileManager    The manager to be used for providing files and images.
     * @return The replaced {@link FileManager} instance.
     */
    public static FileManager setInstance(FileManager newFileManager){
    	Logger.info("Installing file manager: " + newFileManager, FileManager.class);
    	FileManager oldFileManager = singleton;
        singleton = newFileManager;
        return oldFileManager;
    }

    /**
     * Return the instance of the FileManager.
     * 
     * If there is no instance defined, this method will create a default
     * variant, which will use the filesystem for getting files and images.
     * 
     * @return    The only instance.
     */
    public static FileManager getInstance() {
		FileManager result = getInstanceOrNull();
		if (result == null) {
			result = new DefaultFileManager();
			setInstance(result);
        }
		return result;
    }

	/**
	 * The currently installed {@link FileManager} or <code>null</code>, if
	 * {@link #setInstance(FileManager)} was not yet called.
	 */
	public static FileManager getInstanceOrNull() {
		return singleton;
	}
    
	/**
	 * Returns a {@link BinaryContent} whose stream is given by the resource with the given name.
	 * 
	 * @deprecated Use {@link #getData(String)}
	 */
	@Deprecated
	public final BinaryContent getBinaryContent(final String resource) {
		return getData(resource);
    }

	/**
	 * Ensures that the file with the given pathName relative to the top most path exists.
	 * 
	 * In case name starts with {@value #DIRECT_FILE_RESOLVING_PREFIX} it will be resolved directly
	 * using whatever name is given.
	 * 
	 * @param relPathName
	 *        the name of the desired file
	 * @return the desired file
	 * 
	 * @throws IOException
	 *         iff the file does not yet exists and creation failed
	 * 
	 * @deprecated Use {@link #createData(String, BinaryContent)}
	 */
	@Deprecated
	public final File ensureExistence(String relPathName) throws IOException {
		File file = getIDEFile(relPathName);
		if (!file.exists()) {
			File parentFile = file.getParentFile();
			parentFile.mkdirs();
			file.createNewFile();
		}
		return file;
	}

	/**
	 * Resolves an absolute path or a legacy {@value #DIRECT_FILE_RESOLVING_PREFIX} pseudo URL,
	 * <code>null</code> otherwise.
	 */
	protected final File resolveDirect(String aName) {
		// Legacy handling.
		if (aName.startsWith(DIRECT_FILE_RESOLVING_PREFIX)) {
			return new File(aName.substring(DIRECT_FILE_RESOLVING_PREFIX_LENGTH));
		}

		return null;
	}

	/**
	 * Marks the given path name as direct.
	 * 
	 * <p>
	 * A direct path is not resolved relative to the modular web application root, but relative to
	 * the file systems current working directory.
	 * </p>
	 */
	public static String markDirect(String aName) {
		return DIRECT_FILE_RESOLVING_PREFIX + aName;
	}

	/**
	 * Implementation of {@link BinaryContent} which uses the {@link FileManager} to locate a
	 * resource.
	 */
	protected class FileManagerBinaryContent extends AbstractBinaryData {

		private final String _resource;

		private String _lazyContentType = null;

		/**
		 * Creates a {@link FileManagerBinaryContent}.
		 *
		 * @param resource
		 *        Name of a web application resource.
		 */
		protected FileManagerBinaryContent(String resource) {
			if (resource == null) {
				throw new NullPointerException("The resource must not be null.");
			}
			_resource = resource;
		}

		@Override
		public String getName() {
			return _resource;
		}

		@Override
		public String getContentType() {
			if (_lazyContentType == null) {
				_lazyContentType = guessContentType(getName());
			}
			return _lazyContentType;
		}

		private String guessContentType(String filename) {
			return MimeTypesModule.getInstance().getMimeType(filename);
		}

		@Override
		public long getSize() {
			return -1;
		}

		@Override
		public InputStream getStream() throws IOException {
			return FileManager.this.getStream(_resource);
		}

		@Override
		public String toString() {
			return getName();
		}

	}

}
