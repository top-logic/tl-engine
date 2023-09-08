/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.common.format;

import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Resources of this package.
 * 
 * @author    <a href=mailto:fsc@top-logic.com>fsc</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey3 QUARTER_DATE_FORMAT_BUSINESS_YEAR_TOOLTIP__DATE_YEAR_QUARTER;
    public static ResKey3 QUARTER_DATE_FORMAT_TOOLTIP__DATE_YEAR_QUARTER;

    public static ResKey3 QUARTER_DATE_FORMAT_BUSINESS_YEAR__DATE_YEAR_QUARTER;
    public static ResKey3 QUARTER_DATE_FORMAT__DATE_YEAR_QUARTER;
	
    public static ResKey3 HALF_YEAR_DATE_FORMAT_BUSINESS_YEAR_TOOLTIP__DATE_YEAR_HALF;
    public static ResKey3 HALF_YEAR_DATE_FORMAT_TOOLTIP__DATE_YEAR_HALF;

    public static ResKey3 HALF_YEAR_DATE_FORMAT_BUSINESS_YEAR__DATE_YEAR_HALF;
    public static ResKey3 HALF_YEAR_DATE_FORMAT__DATE_YEAR_HALF;
	
    public static ResKey2 YEAR_DATE_FORMAT_BUSINESS_YEAR_TOOLTIP__DATE_YEAR;
    public static ResKey2 YEAR_DATE_FORMAT_TOOLTIP__DATE_YEAR;

    public static ResKey2 YEAR_DATE_FORMAT_BUSINESS_YEAR__DATE_YEAR;
    public static ResKey2 YEAR_DATE_FORMAT__DATE_YEAR;
	
	static {
		initConstants(I18NConstants.class);
	}
}
