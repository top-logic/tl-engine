/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.gui.ThemeVar;
import com.top_logic.layout.basic.DefaultValue;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Icon constants for this package.
 *
 * @see ThemeImage
 */
@SuppressWarnings("javadoc")
public class Icons extends IconsBase {

	/**
	 * Overlay image for edit actions.
	 */
	public static ThemeImage EDIT48;

	public static ThemeImage FILTER_FORM;

	/**
	 * Image of a collapsed group.
	 */
	public static ThemeImage GROUP_COLLAPSED;

	/**
	 * Image of a expanded group.
	 */
	public static ThemeImage GROUP_EXPANDED;

	/**
	 * Overlay image for create actions.
	 */
	public static ThemeImage PLUS48;

	public static ThemeImage RELOAD_SMALL;

	public static ThemeImage RELOAD_SMALL_DISABLED;

	public static ThemeImage SYSTEMSTATE_GREEN;

	public static ThemeImage INCREMENT_BUTTON_IMAGE;

	public static ThemeImage DECREMENT_BUTTON_IMAGE;

	public static ThemeImage INCREMENT_BUTTON_DISABLED_IMAGE;

	public static ThemeImage DECREMENT_BUTTON_DISABLED_IMAGE;

	public static ThemeImage DELETE_BUTTON;

	public static ThemeImage DELETE_BUTTON_DISABLED;

	@DefaultValue("css:bi bi-arrow-down")
	public static ThemeImage DOWN;

	public static ThemeImage OPEN_CHOOSER;

	public static ThemeImage OPEN_CHOOSER_DISABLED;

	public static ThemeImage UP;

	@ClassDefault(StaticPageRenderer.class)
	public static ThemeVar<PageRenderer> PAGE_RENDERER;

}
