/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel.handler;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

import com.top_logic.base.office.PoiTypeHandler;
import com.top_logic.base.office.excel.POITypeSupporter;
import com.top_logic.basic.time.CalendarUtil;

/**
 * Set calendar values to excel cells via POI.
 * 
 * <p>
 * <strong style="color: red;">Note: </strong>Excel doesn't support milliseconds so these will be removed in here. 
 * If a date was adjusted to day end with the methods in dateUtil, 
 * excel rounds the date to next day (e.g. 01.01.2009 23:59:59:999 -> 02.01.2009 00:00:00).
 * </p>
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class CalendarPOIType implements PoiTypeHandler {

	private DateFormat dateFormat = CalendarUtil.newSimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

	@Override
	public Class<?> getHandlerClass() {
		return Calendar.class;
	}

	@Override
	public int setValue(Cell aCell, Workbook aWorkbook, Object aValue, POITypeSupporter aSupport) {
		CellStyle theStyle    = aSupport.getDateStyle();
		Calendar  theCalendar = (Calendar) aValue;

		theCalendar.set(Calendar.MILLISECOND, 0);

    	aCell.setCellValue(theCalendar);
		aCell.setCellStyle(theStyle);

		return this.dateFormat.format(theCalendar.getTime()).length();
	}
}

