/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.Set;

/**
 * Listener notified when {@code .view.xml} files have been modified and the corresponding control
 * subtrees should be rebuilt.
 *
 * @see ViewContext#addViewReloadListener(ViewReloadListener)
 * @see ViewContext#fireViewChanged(Set)
 */
public interface ViewReloadListener {

	/**
	 * Called when one or more view files have been written to disk.
	 *
	 * @param changedPaths
	 *        The set of changed view file paths (e.g. {@code "/WEB-INF/views/sidebar.view.xml"}).
	 *        Implementations should check whether their own view path is contained.
	 */
	void viewChanged(Set<String> changedPaths);
}
