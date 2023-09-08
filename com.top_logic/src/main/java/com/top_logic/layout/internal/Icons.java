/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.internal;

import com.top_logic.gui.ThemeVar;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.TemplateType;
import com.top_logic.layout.internal.WindowHandler.LoadingModel;

/**
 * Icon constants for this package.
 */
public class Icons extends IconsBase {

	/**
	 * Page rendered while the application is loading.
	 */
	@TemplateType(LoadingModel.class)
	public static ThemeVar<HTMLTemplateFragment> LOADING_SCREEN;

}
