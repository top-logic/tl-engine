/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import jakarta.servlet.ServletContext;

import com.top_logic.basic.io.FileUtilities;

/**
 * {@link FileManager} in a web application context.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler </a>
 */
public class WebappFileManager extends FileManagerDelegate {

    /** The context path the application lives in. */
	private final ServletContext context;

    /**
	 * Create a new instance of this class.
	 * 
	 * @param aContext
	 *        The context path to be used.
	 * @param delegate
	 *        {@link FileManager} to use for resolving {@link File} queries (not regular resource
	 *        lookups).
	 */
	public WebappFileManager(ServletContext aContext, FileManager delegate) {
		super(delegate);
        this.context = aContext;
    }

	@Override
	public Set<String> getResourcePaths(String path) {
		File direct = resolveDirect(path);
		if (direct != null) {
			if (!direct.exists()) {
				return Collections.emptySet();
			}

			Collection<File> files = FileUtilities.listFilesSafe(direct, (d, n) -> !n.startsWith("."));
			return FileUtilities.getPrefixedFilenames(files, path);
		}
		Set<String> result = context.getResourcePaths(normalize(path));
		return result == null ? Collections.emptySet() : result;
	}

	@Override
	public InputStream getStreamOrNull(String name) throws IOException, FileNotFoundException {
		File direct = resolveDirect(name);
		if (direct != null) {
			if (!direct.exists()) {
				return null;
			}
			return new FileInputStream(direct);
		}
		return context.getResourceAsStream(normalize(name));
	}

	@Override
	public URL getResourceUrl(String name) throws MalformedURLException {
		File direct = resolveDirect(name);
		if (direct != null) {
			if (!direct.exists()) {
				return null;
			}
			return direct.toURI().toURL();
		}
		return context.getResource(normalize(name));
	}

}
