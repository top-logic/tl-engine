/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import com.top_logic.gui.ThemeVar;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.basic.DefaultValue;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.TemplateType;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Icon constants for this package.
 *
 * @see ThemeImage
 */
public class Icons extends IconsBase {

	/**
	 * Horizontal gap between columns in a form.
	 * 
	 * <p>
	 * This value must not be modified, because the canonical value is taken from
	 * {@link com.top_logic.layout.structure.Icons#FORM_COLUMN_GAP}.
	 * </p>
	 */
	public static ThemeVar<String> RF_HORIZONTAL_GAP_COLUMNS;

	/**
	 * Whether the label is rendered above the content.
	 */
	@DefaultValue("false")
	public static ThemeVar<Boolean> LABEL_ABOVE;

	/**
	 * Whether a colon is set after a label.
	 */
	@DefaultValue("true")
	public static ThemeVar<Boolean> COLON;

	/**
	 * Template defining the layout of a single input element in a form.
	 * 
	 * <p>
	 * The template must arrange the input element's label, input and error display.
	 * </p>
	 */
	@TemplateType(DescriptionCellControl.class)
	public static ThemeVar<HTMLTemplateFragment> FORM_CELL_TEMPLATE;

	/**
	 * Template for a field set box.
	 * 
	 * <p>
	 * A field set groups a form and can be potentially collapsed to temporarily safe space.
	 * </p>
	 */
	@TemplateType(GroupCellControl.class)
	public static ThemeVar<HTMLTemplateFragment> FORM_GROUP_TEMPLATE;
}
