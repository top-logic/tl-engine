/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Objects;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.ViewRef;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link DesignSource} of a {@code .view.xml} file of the application.
 *
 * <p>
 * The tree is built on the configuration the {@link ViewLoader#getOrLoadConfig(String) view loader
 * caches} for the file. A save writes the file in the IDE workspace and normalizes it; the view
 * loader notices the changed file by its timestamp. {@link #discard() Discarding} drops the cached
 * configuration, which the tree has modified in place.
 * </p>
 */
public final class FileDesignSource implements DesignSource {

	private final String _path;

	/**
	 * Creates a {@link FileDesignSource}.
	 *
	 * @param path
	 *        See {@link #getPath()}.
	 */
	public FileDesignSource(String path) {
		_path = Objects.requireNonNull(path, "path");
	}

	/**
	 * Creates the {@link FileDesignSource} of the file the given reference names.
	 */
	public static FileDesignSource of(ViewRef ref) {
		return new FileDesignSource(ref.fullPath());
	}

	/**
	 * The full path of the view file (e.g. {@code /WEB-INF/views/app.view.xml}).
	 */
	public String getPath() {
		return _path;
	}

	@Override
	public String getId() {
		return _path;
	}

	/**
	 * The path of the file relative to {@link ViewLoader#VIEW_BASE_PATH}, as a {@link ViewRef}
	 * names it.
	 */
	@Override
	public String getLabel() {
		return ViewLoader.viewRef(_path);
	}

	@Override
	public ViewElement.Config load() throws ConfigurationException {
		return ViewLoader.getOrLoadConfig(_path);
	}

	@Override
	public void save(String content) {
		File file = FileManager.getInstance().getIDEFileOrNull(_path);
		if (file == null) {
			throw new TopLogicException(I18NConstants.ERROR_SAVE_VIEW_NO_IDE_FILE__PATH.fill(_path));
		}

		try (Writer out = new OutputStreamWriter(new FileOutputStream(file), StringServices.CHARSET_UTF_8)) {
			out.write(content);
		} catch (IOException ex) {
			throw new TopLogicException(I18NConstants.ERROR_SAVE_VIEW_FAILED__PATH.fill(_path), ex);
		}
		try {
			XMLPrettyPrinter.normalizeFile(file);
		} catch (Exception ex) {
			Logger.warn("Failed to normalize XML: " + _path, ex, FileDesignSource.class);
		}
	}

	@Override
	public void discard() {
		ViewLoader.discardCachedConfig(_path);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof FileDesignSource other && _path.equals(other._path);
	}

	@Override
	public int hashCode() {
		return _path.hashCode();
	}

	/**
	 * The {@link #getLabel() label}, as a breadcrumb shows the source.
	 */
	@Override
	public String toString() {
		return getLabel();
	}
}
