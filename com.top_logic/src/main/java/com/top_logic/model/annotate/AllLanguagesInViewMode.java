/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;

/**
 * This annotation determines whether the values for all languages or only for the user's language
 * are displayed in view mode.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("display-all-languages")
@InApp
@TargetType(value = TLTypeKind.STRING, name = { "tl.model.i18n:I18NHtml", "tl.model.i18n:I18NString" })
@Label("Display all languages in view mode")
public interface AllLanguagesInViewMode extends TLAttributeAnnotation, BooleanAnnotation {

	/**
	 * If this value is set to <code>true</code>, the values for all languages are displayed in the
	 * view mode. If it is set to <code>false</code>, in view mode only the value for the user's
	 * language is displayed.
	 */
	@Override
	boolean getValue();

}
