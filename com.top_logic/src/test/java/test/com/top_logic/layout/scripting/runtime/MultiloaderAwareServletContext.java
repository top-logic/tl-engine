/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting.runtime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import jakarta.activation.FileTypeMap;
import jakarta.servlet.ServletContext;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.MultiFileManager;

/**
 * A {@link ServletContext} that resolves paths via the {@link MultiFileManager multiloader} /
 * {@link FileManager}. Everything else is dispatched to the given {@link ServletContext}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
final class MultiloaderAwareServletContext extends ProxyServletContext {

	private final FileTypeMap _mimeTypes;

	public MultiloaderAwareServletContext(FileTypeMap mimeTypes, ServletContext impl) {
		super(impl);
		_mimeTypes = mimeTypes;
	}

	@Override
	public ServletContext getContext(String uripath) {
		ServletContext servletContext = super.getContext(uripath);
		if (servletContext == null) {
			return null;
		}
		return new MultiloaderAwareServletContext(_mimeTypes, servletContext);
	}

	@Override
	public String getRealPath(String path) {
		File file = FileManager.getInstance().getIDEFileOrNull(path);
		if (file == null) {
			return null;
		}
		return file.getAbsolutePath();
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		return FileManager.getInstance().getResourceUrl(path);
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		try {
			return FileManager.getInstance().getStreamOrNull(path);
		} catch (IOException ex) {
			return null;
		}
	}

	@Override
	public Set<String> getResourcePaths(String path) {
		return FileManager.getInstance().getResourcePaths(path);
	}

	@Override
	public String getMimeType(String file) {
		return _mimeTypes.getContentType(file);
	}

}
