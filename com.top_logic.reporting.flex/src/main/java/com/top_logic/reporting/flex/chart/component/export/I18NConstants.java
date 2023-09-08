/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component.export;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey DATE_LABEL = legacyKey("reporting.chart.export.date.label");

	public static ResKey TITLE_LABEL = legacyKey("reporting.chart.export.title.label");

	public static ResKey USER_LABEL = legacyKey("reporting.chart.export.user.label");

	public static ResKey INDEX_COLUMN = legacyKey("SlideReplacer.table.rowIndex");
	
	public static ResKey DOWNLOAD_FILE_NAME;

	static {
		initConstants(I18NConstants.class);
	}
}
