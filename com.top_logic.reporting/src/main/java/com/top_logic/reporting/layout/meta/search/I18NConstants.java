/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.meta.search;

import com.top_logic.basic.util.ResKey;
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

	public static ResKey DELETE_REPORT = legacyKey("reporting.report.delete");

	public static ResKey ERROR_DELETE_FAILED =
		com.top_logic.element.workItem.layout.commandHandler.I18NConstants.ERROR_COMMIT_FAILED;

	public static ResKey ERROR_NO_REPORT = legacyKey("tl.executable.disabled.noReport");

	public static ResKey CHART_DETAILS_TAB = legacyKey("reporting.chart.chart.details.tabber");

	static {
		initConstants(I18NConstants.class);
	}
}
