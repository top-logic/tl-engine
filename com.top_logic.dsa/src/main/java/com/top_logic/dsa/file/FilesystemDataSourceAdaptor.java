/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.file;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.dsa.evt.DataChangeEvent;
import com.top_logic.dsa.ex.NotSupportedException;

/**
 * Implements the interface DataSourceAdaptor for the file system.
 *
 * It assumes that the java.io.File Object will always accept 
 * Filenames with '/' as Seperator (as it does on Windows and Unixes).
 *
 * @author  Karsten Buch / Klaus Halfmann
 */
public class FilesystemDataSourceAdaptor extends AbstractFilesystemDataSourceAdaptor {

	/**
	 * Configuration of a {@link FilesystemDataSourceAdaptor}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractFilesystemDataSourceAdaptor.Config<FilesystemDataSourceAdaptor> {

		/** Name of the configuration {@link #getHome()}. */
		String HOME = "home";

		/** Name of the configuration {@link #getWebRoot()}. */
		String WEB_ROOT = "webRoot";

		/** Name of the configuration {@link #getURLPrefix()}. */
		String URL_PREFIX = "URLPrefix";

		/** Name of the configuration {@link #getRedirectPrefix()}. */
		String REDIRECT_PREFIX = "RedirectPrefix";

		/**
		 * Home directory.
		 */
		@Name(HOME)
		String getHome();

		/**
		 * Setter for {@link #getHome()}.
		 * 
		 * @param value
		 *        New value of {@link #getHome()}.
		 */
		void setHome(String value);

		/**
		 * The URL Prefix
		 */
		@Name(URL_PREFIX)
		String getURLPrefix();

		/**
		 * Setter for {@link #getURLPrefix()}.
		 * 
		 * @param value
		 *        New value of {@link #getURLPrefix()}.
		 */
		void setURLPrefix(String value);

		/**
		 * The Redirecte URL Prefix
		 */
		@Name(REDIRECT_PREFIX)
		String getRedirectPrefix();

		/**
		 * Setter for {@link #getRedirectPrefix()}.
		 * 
		 * @param value
		 *        New value of {@link #getRedirectPrefix()}.
		 */
		void setRedirectPrefix(String value);

		/**
		 * A copy of the Home in the webserver
		 */
		@Name(WEB_ROOT)
		String getWebRoot();

		/**
		 * Setter for {@link #getWebRoot()}.
		 * 
		 * @param value
		 *        New value of {@link #getWebRoot()}.
		 */
		void setWebRoot(String value);
	}

    /** 
     * Optional webapp path weher files shoud be copied to. 
     * 
     * To be used in a cluster where multiple webRoot exist.
     * 
     * @see #copyToWeb(String)
     */
    protected File                        webRoot;

    /** Optional Prefix to generate URLs, if null we assume we are not part of a wep-app. */
	String urlPrefix = StringServices.EMPTY_STRING;

    /** Optional Prefix to generate redirect URLs */
	String redirectPrefix = StringServices.EMPTY_STRING;
    
	private File _root;

    /**
     * Create a FilesystemDataSourceAdaptor with the given root directory
     *
     * @param  rPath   the root directory for the FilesystemDataSourceAdaptor,
     *                 a String to be interpreted by the {@link FileManager}.
     */
    public FilesystemDataSourceAdaptor(String rPath) throws DatabaseAccessException {
		super();
		_root = checkFolderPath(rPath);
    }

    /** 
     * Check that aRootPath is a valid reference to the file System.
     */
    protected static File checkFolderPath(String aRootPath) throws DatabaseAccessException {
        File theRoot = new File(aRootPath);
        if (!theRoot.exists()) {
			if (!theRoot.mkdirs()) {
				throw new DatabaseAccessException("Unable to create folder: " + aRootPath);
			}
        }
        return theRoot;
    }

	/**
	 * Creates a new {@link FilesystemDataSourceAdaptor} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link FilesystemDataSourceAdaptor}.
	 */
	public FilesystemDataSourceAdaptor(InstantiationContext context, Config config) {
		super(context, config);

		this._root = checkFolderPath(config.getHome());
		this.urlPrefix = config.getURLPrefix();
		this.redirectPrefix = config.getRedirectPrefix();

		// extract redirectPrefix from URLPrefix if possible
		if (!urlPrefix.isEmpty() && redirectPrefix.isEmpty()) {
			int i = urlPrefix.indexOf('/', 1);
			if (i > 0) { // keep leading /
				redirectPrefix = urlPrefix.substring(i);
			}
		}
		String webRootS = config.getWebRoot();
		if (!webRootS.isEmpty()) {
			webRoot = checkFolderPath(webRootS);
		}

		Logger.info("<init> rootPath=" + _root
			+ " root=" + _root
			+ " urlPrefix = " + urlPrefix
			+ " redirectPrefix = " + redirectPrefix
			+ (webRoot != null ? webRoot.getAbsolutePath() : ""), this);
	}

	/**
	 * Get a URL string for redirecting to an entry.
	 *
	 * @param path
	 *        the name of the element
	 * @return a URL relative to WebContainer
	 *
	 * @throws NotSupportedException
	 *         in case no urlPrefix was configured
	 */
	@Override
	public String getURL(String path)
			throws NotSupportedException {
		if (urlPrefix.isEmpty()) {
			throw new NotSupportedException("No URL Prefix configured");
		}
		if (webRoot != null)
			copyToWeb(path);

		return urlPrefix + path;
	}

	/**
	 * Get a URL string for forwarding to an entry.
	 *
	 * @param path
	 *        the name of the element
	 * @return a URL relative to the WebApplication
	 *
	 * @throws NotSupportedException
	 *         in case no urlPrefix was configured
	 */
	@Override
	public String getForwardURL(String path)
			throws NotSupportedException {
		if (redirectPrefix.isEmpty()) {
			throw new NotSupportedException("No Redirect Prefix given");
		}
		if (webRoot != null)
			copyToWeb(path);

		return redirectPrefix + path;
	}

	/**
	 * Delete an element from a database. If the element is a container, it is deleted only if it is
	 * empty.
	 *
	 * @param path
	 *        the name of the element
	 */
	@Override
	public void delete(String path, boolean force)
			throws DatabaseAccessException {

		// Logger.debug ("delete(" + aName + ")" , this);

		File theFile = this.getFile(path);
		int theMode = theFile.isDirectory() ? DataChangeEvent.CONTAINER_DELETED
			: DataChangeEvent.ENTRY_DELETED;

		DataChangeEvent theEvent = new DataChangeEvent(this, theMode,
			this.getSourceName(path));

		this.fireCheckAllow(theEvent);
		boolean success = theFile.delete();
		if (!success) {
			throw new DatabaseAccessException(this.getClass().getName() +
				".delete: Could not delete: " + path);
		}
		if (webRoot != null) {
			theFile = new File(webRoot, path);
			if (!theFile.delete())
				Logger.error("Failed to delete web file " + theFile.getAbsolutePath(), this);
		}
		this.fireDataChanged(theEvent);
	}

	/**
	 * Delete the container and all sub-elements (works for an entry as well). Can delete non-empty
	 * containers. For entries it is equivalent to delete(String).
	 *
	 * @param path
	 *        the name of the element
	 */
	@Override
	public void deleteRecursively(String path)
			throws DatabaseAccessException {

		// Logger.debug ("deleteRecursively(" + name + ")" , this);
		File file = this.getFile(path);

		if (!file.exists()) {
			return;
		}

		int mode = file.isDirectory() ? DataChangeEvent.CONTAINER_DELETEDREC : DataChangeEvent.ENTRY_DELETED;

		DataChangeEvent theEvent = new DataChangeEvent(this, mode,
			this.getSourceName(path));
		this.fireCheckAllow(theEvent);
		if (!FileUtilities.deleteR(file)) {
			throw new DatabaseAccessException("deleteRecursively(" + path + ") failed.");
		}
		if (webRoot != null) {
			file = new File(webRoot, path);
			if (!FileUtilities.deleteR(file))
				Logger.error("Failed to deleteRecursively web file " + file.getAbsolutePath(), this);
		}
		this.fireDataChanged(theEvent);
	}

    /**
     * Delivers the description of the instance of this class.
     *
     * @return    The description for debugging.
     */
    @Override
	public String toString () {
		return this.getClass().getName() + " [rootPath: " + this._root + ']';
    }

    /**
     * Get the path (fully qualified name) of the element (entry/container)
     *
     * @param       name 	the name of the element
     * @return  	the path of the entry/container (not fully qualified)
     */
    public String getPath (String name)
        	throws DatabaseAccessException {
        return name;
	}

    /** 
     * Copy the given file from root to webRoot when needed.
     * 
     * TODO KHA/TMA check multithreaded ..
     * 
     * Must only be called when webRoot != null. 
     */
    protected void copyToWeb(String path) {
        File rootFile = this.getFile(path);
        File webFile  = new File(webRoot, path);

        try {
            if (rootFile.isFile() && rootFile.exists() && !webFile.exists()) {
                webFile.getParentFile().mkdirs();
                // TODO KHA/TMA check timestamps to avoid copy ?
                FileUtilities.copyFile(rootFile, webFile);
            }
        } catch (IOException iox) {
            Logger.error(
                "Failed to copy from " + rootFile.getAbsolutePath() 
                              + " to " + webFile.getAbsolutePath(), iox, this);
        }
    }

	@Override
	protected File getFile(String path) {
    	try {
			return new File(this.getRoot(), path);
        }
        catch (Exception ex) {
            Logger.error("failed to getFile()", ex, this);

            return (null);
        }
    }

	@Override
	protected List<File> filesForPath(String path) {
		return Collections.singletonList(getFile(path));
	}

	/** The root of the file system. */
	protected File getRoot() {
		return this._root;
    }

}
