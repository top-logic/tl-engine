/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import com.top_logic.gui.ThemeVar;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.TemplateType;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.values.edit.editor.EditorUtils.ListEntryControl;

/**
 * Icon constants for this package.
 *
 * @see ThemeImage
 */
@SuppressWarnings("javadoc")
public class Icons extends IconsBase {

	public static ThemeImage TRANSLATE;

	public static ThemeImage DISPLAY_DERIVED_RESOURCES;

	public static ThemeImage HIDE_DERIVED_RESOURCES;

	/**
	 * Template for a field set box.
	 * 
	 * <p>
	 * A field set groups a form and can be potentially collapsed to temporarily safe space.
	 * </p>
	 */
	@TemplateType(ListEntryControl.class)
	public static ThemeVar<HTMLTemplateFragment> LIST_ENTRY_TEMPLATE;

}
