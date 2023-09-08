/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.infoservice;

import com.top_logic.gui.ThemeVar;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.TemplateType;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Icon constants for this package.
 *
 * @see ThemeImage
 */
@SuppressWarnings("javadoc")
public class Icons extends IconsBase {

	public static ThemeImage CLOSE_INFO_SERVICE_ITEM;

	public static ThemeImage INFOSERVICE_ERROR;

	public static ThemeImage INFOSERVICE_WARNING;

	public static ThemeImage INFOSERVICE_INFO;

	public static ThemeImage INFO_SERVICE_DETAIL_EXPANDER;

	/**
	 * Template for displaying a message container in the application. These messages can be either
	 * errors, warnings or information.
	 */
	@TemplateType(DefaultInfoServiceItem.class)
	public static ThemeVar<HTMLTemplateFragment> INFO_SERVICE_ITEM_TEMPLATE;

	/**
	 * Template for displaying the inner message of the INFO_SERVICE_ITEM_TEMPLATE.
	 */
	@TemplateType(InfoServiceItemMessageFragment.class)
	public static ThemeVar<HTMLTemplateFragment> INFO_SERVICE_MESSAGE_TEMPLATE;

}
