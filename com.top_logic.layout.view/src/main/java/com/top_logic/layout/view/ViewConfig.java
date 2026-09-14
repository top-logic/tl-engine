/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.List;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
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

	/** Configuration name for {@link #getEntryPoints()}. */
	String ENTRY_POINTS = "entry-points";

	/**
	 * The name of the default view file loaded when the {@link ViewServlet} is accessed without a
	 * path (e.g. {@code /view/}).
	 *
	 * <p>
	 * The file is resolved relative to {@code /WEB-INF/views/} in the webapp.
	 * </p>
	 *
	 * <p>
	 * The default view is an entry point of the application, so it needs no entry in
	 * {@link #getEntryPoints()}.
	 * </p>
	 */
	@Name(DEFAULT_VIEW)
	@StringDefault("app.view.xml")
	String getDefaultView();

	/**
	 * The views that a URL may name as the contents of a browser tab, besides the
	 * {@link #getDefaultView()}.
	 *
	 * <p>
	 * A view file is only part of the application's surface where it is listed here: a URL naming
	 * any other view is answered with "not found". Most view files are fragments of a display - a
	 * dialog, a menu, a page of a tab - which the enclosing view supplies with the channels they
	 * read, and which alone in a browser tab show nothing or fail. An entry point is a view that
	 * stands on its own.
	 * </p>
	 *
	 * <p>
	 * Keyed by {@link EntryPoint#getView()} so that the registrations of several modules merge into
	 * one list.
	 * </p>
	 */
	@Name(ENTRY_POINTS)
	@EntryTag("entry-point")
	@Key(EntryPoint.VIEW)
	List<EntryPoint> getEntryPoints();

	/**
	 * A view that a URL may name as the contents of a browser tab.
	 */
	interface EntryPoint extends ConfigurationItem {

		/** Configuration name for {@link #getView()}. */
		String VIEW = "view";

		/**
		 * The view file, relative to {@code /WEB-INF/views/} in the webapp, e.g.
		 * {@code demo/pdf-demo.view.xml}.
		 */
		@Name(VIEW)
		@Mandatory
		String getView();
	}
}
