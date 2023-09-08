/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.chart.chartjs.tile;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey ERROR_NO_CHART_COMPONENT_FOUND;

	public static ResKey1 ERROR_NOT_CHART_COMPONENT__COMPONENT;

	public static ResKey1 ERROR_NO_CHART_COMPONENT_DESCENDENT__COMPONENT;

	static {
		initConstants(I18NConstants.class);
	}
}
