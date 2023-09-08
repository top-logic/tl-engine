/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.producer;

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

	public static ResKey COLLECTION_SIZE_CALCULATOR;

	public static ResKey MATRIX_GRAPHICS_CHART_TOOLTIP = legacyKey("reporting.matrix.graphics.chart.tooltip");

	public static ResKey CLASSIFICATION_NOT_SET_LABEL;

	public static ResKey REPORTING_GRAPHICS_CHART_TOOLTIP;

	public static ResKey OVERVIEW_SUM;

	public static ResKey QUANTITY_AXIS_LABEL;

	static {
		initConstants(I18NConstants.class);
	}
}
