/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;

/**
 * Access to the view files a static scan of the view configuration follows.
 *
 * @see ViewMounts#scan(String, ViewResolver)
 */
@FunctionalInterface
public interface ViewResolver {

	/**
	 * Resolves the view file referenced from a view configuration.
	 *
	 * @param viewRef
	 *        Path of the view file relative to {@link ViewLoader#VIEW_BASE_PATH}, as written in the
	 *        configuration.
	 * @return The root element of the referenced view.
	 * @throws ConfigurationException
	 *         If the file does not exist or cannot be parsed.
	 */
	ViewElement getView(String viewRef) throws ConfigurationException;
}
