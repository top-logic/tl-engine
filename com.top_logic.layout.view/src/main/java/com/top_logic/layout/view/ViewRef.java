/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.Objects;

/**
 * A reference to a view file, as a configuration property names it.
 *
 * <p>
 * A view reference holds the path of a {@code .view.xml} file relative to
 * {@link ViewLoader#VIEW_BASE_PATH}, exactly as it is written in the configuration. A configuration
 * property whose value names a view file has this type (parsed by {@link ViewRefFormat}), so that
 * tools traversing a configuration (e.g. the view designer) recognize every view the configuration
 * refers to by the type of the property alone.
 * </p>
 *
 * <p>
 * Example: the attribute {@code view} of
 * </p>
 *
 * <pre>
 * &lt;view-ref view="customers/detail.view.xml"/&gt;
 * </pre>
 *
 * <p>
 * is a view reference with the {@link #getPath() path} {@code customers/detail.view.xml}, which
 * names the file {@link #fullPath()} {@code /WEB-INF/views/customers/detail.view.xml}.
 * </p>
 *
 * @see ViewRefFormat
 */
public final class ViewRef {

	private final String _path;

	/**
	 * Creates a {@link ViewRef}.
	 *
	 * @param path
	 *        See {@link #getPath()}.
	 */
	public ViewRef(String path) {
		_path = Objects.requireNonNull(path, "path");
	}

	/**
	 * The path of the referenced view file relative to {@link ViewLoader#VIEW_BASE_PATH}, as written
	 * in the configuration (e.g. {@code customers/detail.view.xml}).
	 */
	public String getPath() {
		return _path;
	}

	/**
	 * The path to load the referenced view file with (e.g.
	 * {@code /WEB-INF/views/customers/detail.view.xml}).
	 *
	 * @see ViewLoader#fullPath(String)
	 */
	public String fullPath() {
		return ViewLoader.fullPath(_path);
	}

	@Override
	public String toString() {
		return _path;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ViewRef)) {
			return false;
		}
		return _path.equals(((ViewRef) obj)._path);
	}

	@Override
	public int hashCode() {
		return _path.hashCode();
	}
}
