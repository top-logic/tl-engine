/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;

/**
 * Application-level configuration for the view system.
 *
 * <p>
 * Registered via {@link ApplicationConfig} and accessible from any module. Applications override
 * properties in their {@code .conf.config.xml} files.
 * </p>
 */
public interface ViewConfig extends ConfigurationItem {

	/** Configuration name for {@link #getDefaultView()}. */
	String DEFAULT_VIEW = "default-view";

	/**
	 * The name of the default view file loaded when the {@link ViewServlet} is accessed without a
	 * path (e.g. {@code /view/}).
	 *
	 * <p>
	 * The file is resolved relative to {@code /WEB-INF/views/} in the webapp.
	 * </p>
	 */
	@Name(DEFAULT_VIEW)
	@StringDefault("app.view.xml")
	String getDefaultView();
}
