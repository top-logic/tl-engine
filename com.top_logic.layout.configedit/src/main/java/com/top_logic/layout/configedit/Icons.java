/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import com.top_logic.layout.basic.DefaultValue;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Icons of the configuration form.
 *
 * <p>
 * Deliberately the same three glyphs a model-generated form uses for the same three commands, so a
 * configuration form in a panel's toolbar is not recognizable as something else.
 * </p>
 */
@SuppressWarnings("javadoc")
public class Icons extends IconsBase {

	@DefaultValue("css:fas fa-edit")
	public static ThemeImage CONFIG_FORM_EDIT;

	@DefaultValue("css:fas fa-save")
	public static ThemeImage CONFIG_FORM_APPLY;

	@DefaultValue("css:fas fa-times")
	public static ThemeImage CONFIG_FORM_CANCEL;

}
