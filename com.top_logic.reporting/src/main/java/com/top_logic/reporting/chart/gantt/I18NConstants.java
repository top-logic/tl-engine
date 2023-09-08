/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	static {
		initConstants(I18NConstants.class);
	}

	/** Error message key for width out of range automatic. */
	public static ResKey ERROR_WIDTH_OUT_OF_RANGE_AUTOMATIC;

	/** Error message key for width out of range manual. */
	public static ResKey ERROR_WIDTH_OUT_OF_RANGE_MANUAL;

	/** Error message key for height out of range manual. */
	public static ResKey ERROR_HEIGHT_OUT_OF_RANGE;

	/** Error message key for not enough memory. */
	public static ResKey ERROR_MEMORY_OUT_OF_RANGE;

	/** Error message key for page out of range. */
	public static ResKey ERROR_PAGE_OUT_OF_RANGE;

	/** Error message key for page out of range. */
	public static ResKey ERROR_CREATION_FAILED;

	/** Error message key for no data. */
	public static ResKey ERROR_NO_DATA;

	/** Error message key for empty chart. */
	public static ResKey ERROR_EMPTY_CHART;

	public static ResPrefix GENERATOR;

	public static ResKey PDF_EXPORT_NAME;

	public static ResKey PDF_EXPORT_FAILED;

	public static ResKey PDF_EXPORT_COMMAND;

}
