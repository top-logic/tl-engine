/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.flexreporting.producer.tooltips;

import com.top_logic.basic.i18n.CustomKey;
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

	@CustomKey("reporting.chart.tooltip.amount")
	public static ResKey AMOUNT_TOOLTIP;

	@CustomKey("reporting.chart.tooltip.attribute")
	public static ResKey ATTRIBUTE_TOOLTIP;

	@CustomKey("CountFunction")
	public static ResKey COUNT_FUNCTION;

	@CustomKey("reporting.chart.tooltip.function")
	public static ResKey FUNCTION_TOOLTIP;

	@CustomKey("reporting.chart.tooltip.range")
	public static ResKey RANGE_TOOLTIP;

	static {
		initConstants(I18NConstants.class);
	}
}
