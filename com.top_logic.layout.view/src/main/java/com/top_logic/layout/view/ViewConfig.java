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
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
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

	/** Configuration name for {@link #getLoginView()}. */
	String LOGIN_VIEW = "login-view";

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
	 * The view rendered for a session that belongs to no account, in place of whatever the URL
	 * names.
	 *
	 * <p>
	 * Every visitor reaches the application under an anonymous session. An application that has
	 * nothing to show a visitor names a login view here: a visitor then sees that view and nothing
	 * else, whether the URL names a route, the {@link #getDefaultView()} or one of the
	 * {@link #getEntryPoints()}. The requested URL is kept while it is displayed, so the page the
	 * visitor asked for is the one they reach once they are logged in.
	 * </p>
	 *
	 * <p>
	 * The exception is an entry point marked {@link EntryPoint#isAnonymous() anonymous}: a URL
	 * naming such a view shows it, because it is a page written for a visitor who has no account
	 * here.
	 * </p>
	 *
	 * <p>
	 * Unset for an application that shows itself to visitors, which then reaches its login through
	 * the display it renders for them.
	 * </p>
	 *
	 * <p>
	 * The file is resolved relative to {@code /WEB-INF/views/} in the webapp.
	 * </p>
	 */
	@Name(LOGIN_VIEW)
	@Nullable
	String getLoginView();

	/**
	 * A view that a URL may name as the contents of a browser tab.
	 */
	interface EntryPoint extends ConfigurationItem {

		/** Configuration name for {@link #getView()}. */
		String VIEW = "view";

		/** Configuration name for {@link #isAnonymous()}. */
		String ANONYMOUS = "anonymous";

		/**
		 * The view file, relative to {@code /WEB-INF/views/} in the webapp, e.g.
		 * {@code demo/pdf-demo.view.xml}.
		 */
		@Name(VIEW)
		@Mandatory
		String getView();

		/**
		 * Whether the view is shown to a session that belongs to no account as well.
		 *
		 * <p>
		 * An application with a {@link ViewConfig#getLoginView() login view} shows that view to
		 * every anonymous session in place of whatever the URL names; an entry point marked here is
		 * the exception - a URL naming it shows it, e.g. a page that explains something to a
		 * visitor before they can log in.
		 * </p>
		 *
		 * <p>
		 * An application without a login view shows a visitor what the URL names anyway, so the
		 * setting says nothing there.
		 * </p>
		 */
		@Name(ANONYMOUS)
		@BooleanDefault(false)
		boolean isAnonymous();
	}
}
