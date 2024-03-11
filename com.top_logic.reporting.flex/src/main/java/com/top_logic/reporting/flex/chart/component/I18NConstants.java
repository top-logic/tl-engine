/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix INTERACTIVE_CHART_BUILDER = legacyPrefix("InteractiveChartBuilder");

	public static ResPrefix CHART = legacyPrefix("reporting.chart.");

	public static ResKey ERROR_COMMIT_FAILED;

	public static ResKey1 ERROR_NO_TL_CLASS__TYPE_NAME;

	public static ResPrefix CHART_FILE_PREFIX;

	public static ResKey OPEN_CHART_DETAILS;

	static {
		initConstants(I18NConstants.class);
	}
}
