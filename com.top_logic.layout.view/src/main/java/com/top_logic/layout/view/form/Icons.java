/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.basic.DefaultValue;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Icon constants for form command buttons.
 *
 * @see ThemeImage
 */
@SuppressWarnings("javadoc")
public class Icons extends IconsBase {

	@DefaultValue("css:fas fa-edit")
	public static ThemeImage FORM_EDIT;

	@DefaultValue("css:fas fa-save")
	public static ThemeImage FORM_SAVE;

	@DefaultValue("css:fas fa-times")
	public static ThemeImage FORM_CANCEL;

	@DefaultValue("css:fas fa-info-circle")
	public static ThemeImage COMPOSITION_TABLE_DETAIL;

	@DefaultValue("css:fas fa-trash")
	public static ThemeImage COMPOSITION_TABLE_DELETE;

	@DefaultValue("css:fas fa-plus")
	public static ThemeImage COMPOSITION_TABLE_ADD;

}
