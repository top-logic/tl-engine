/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormHandler;

/**
 * Internationalization constants for this package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey BUTTON_ACTIVATE;

	public static ResKey ERROR_BUTTON_INACTIVE;

	public static ResPrefix FILTER_SIDEBAR = legacyPrefix("filterSidebar.");

	public static ResKey FILTER_SIDEBAR_SELECT = legacyKey("tl.table.sidebar.select");

	/**
	 * Shown message when spontaneously no apply closure is available.
	 * 
	 * <p>
	 * It is possible that between showing the dirty dialog and executing the apply action the apply
	 * closure of an {@link FormHandler} disappears, e.g. if the token for the object timed out. In
	 * that case this messge is shown.
	 * </p>
	 * 
	 **/
	public static ResKey SPONTANEOUS_NO_APPLY_CLOSURE;

	/**
	 * "Add bookmark" text.
	 * 
	 * @see BookmarkRenderer
	 */
	public static ResKey1 BOOKMARK_TOOLTIP__LABEL;

	/**
	 * "Add bookmark" text for null models.
	 * 
	 * @see BookmarkRenderer
	 */
	public static ResKey BOOKMARK_TOOLTIP__LABEL_EMPTY;

	public static ResKey CANNOT_EXPAND_ALREADY_ALL_EXPANDED;

	public static ResKey CANNOT_COLLAPSE_ALREADY_ALL_COLLAPSED;

	public static ResKey SIDEBAR_COLLAPSE_ALL = legacyKey("tl.table.sidebar.collapseAll");

	public static ResKey ERROR_COMMAND_NOT_EXECUTABLE = legacyKey("tl.command.notExecutableError");

	public static ResKey SIDEBAR_REFRESH_DISABLED = legacyKey("tl.table.sidebar.refresh.disabled");

	public static ResKey SIDEBAR_EXPAND_ALL = legacyKey("tl.table.sidebar.expandAll");

	public static ResKey SIDEBAR_FILTERS = legacyKey("tl.table.sidebar.filterChooser");

	public static ResKey ERROR_INVALID_INPUT = legacyKey("tl.executable.disabled.formErrors");

	public static ResKey ERROR_APPLY_NOT_POSSIBLE = legacyKey("tl.executable.disabled.noTabApply");

	public static ResKey SIDEBAR_REFRESH = legacyKey("tl.table.sidebar.refresh");

	public static ResKey KEY_CODE_HANDLER;

	public static ResKey RENDERING_ERROR;

	public static ResKey1 RENDERING_ERROR_TOOLTIP;

	public static ResKey1 INVALID_THEME_IMAGE_FORMAT;

	public static ResKey CONTEXT_MENU_OPENER;

	public static ResPrefix DIRTY_HANDLING;

	static {
		initConstants(I18NConstants.class);
	}
}
