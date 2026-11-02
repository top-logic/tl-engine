/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import com.top_logic.layout.basic.DefaultValue;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Icon constants of the view designer: the icons of the {@link DesignNodeKind}s.
 *
 * @see ThemeImage
 */
@SuppressWarnings("javadoc")
public class Icons extends IconsBase {

	@DefaultValue("css:bi bi-file-earmark-code")
	public static ThemeImage DESIGN_NODE_VIEW_FILE;

	@DefaultValue("css:bi bi-box")
	public static ThemeImage DESIGN_NODE_ELEMENT;

	@DefaultValue("css:bi bi-lightning")
	public static ThemeImage DESIGN_NODE_COMMAND;

	@DefaultValue("css:bi bi-sliders")
	public static ThemeImage DESIGN_NODE_PART;

	@DefaultValue("css:bi bi-diagram-2")
	public static ThemeImage DESIGN_NODE_GROUP;

	@DefaultValue("css:bi bi-box-arrow-up-right")
	public static ThemeImage DESIGN_NODE_LINK;

	@DefaultValue("css:bi bi-exclamation-triangle")
	public static ThemeImage DESIGN_NODE_ERROR;

}
