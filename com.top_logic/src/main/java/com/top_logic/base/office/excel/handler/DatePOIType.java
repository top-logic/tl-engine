/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel.handler;

import java.util.Calendar;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.LocaleUtil;

import com.top_logic.base.office.excel.POITypeSupporter;

/**
 * Set date values to excel cells via POI.
 * 
 * <p>
 * <strong style="color: red;">Note: </strong>Excel doesn't support milliseconds so these will be removed in here. 
 * If a date was adjusted to day end with the methods in dateUtil, 
 * excel rounds the date to next day (e.g. 01.01.2009 23:59:59:999 -> 02.01.2009 00:00:00).
 * </p>
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class DatePOIType extends CalendarPOIType {

	@Override
	public Class<?> getHandlerClass() {
		return Date.class;
	}

	@Override
	public int setValue(Cell aCell, Workbook aWorkbook, Object aValue, POITypeSupporter aSupport) {
		Calendar theCal = LocaleUtil.getLocaleCalendar();
		theCal.setTime((Date) aValue);

    	return super.setValue(aCell, aWorkbook, theCal, aSupport);
	}
}

