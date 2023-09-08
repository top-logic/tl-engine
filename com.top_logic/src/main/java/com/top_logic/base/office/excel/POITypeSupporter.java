/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

import com.top_logic.base.office.PoiTypeHandler;
import com.top_logic.base.office.excel.POIExcelTemplate.POITemplateEntry;
import com.top_logic.base.office.excel.handler.AbstractImagePOIType;
import com.top_logic.base.office.excel.handler.CalendarPOIType;

/**
 * Methods to be used by the {@link PoiTypeHandler} instances.
 * 
 * <ul>
 *   <li>{@link #getDateStyle()}: For {@link CalendarPOIType}</li>
 *   <li>{@link #getTemplate(Cell)}: For {@link AbstractImagePOIType}</li>
 *   <li>{@link #getDrawingManager()}: For {@link AbstractImagePOIType}</li>
 * </ul>
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface POITypeSupporter {

	/** 
	 * Return the cell style to be used for date cells.
	 * 
	 * @return    The requested style, never <code>null</code>.
	 */
	public CellStyle getDateStyle();

	/** 
	 * Return an entry from the excel template for the given cell.
	 * 
	 * <p>This method will be used by the {@link AbstractImagePOIType} to find out
	 * the correct sizing of the image in the excel sheet. When the method returns
	 * <code>null</code>, the image will be resized to match into the cell.</p>
	 * 
	 * @param    aCell    The cell to get the entry from, must not be <code>null</code>.
	 * @return   The requested entry, may be <code>null</code>.
	 */
	public POITemplateEntry getTemplate(Cell aCell);

	/** 
	 * Return the manager responsible for embedding images into an excel file.
	 * 
	 * @return    The requested drawing manager.
	 */
	public POIDrawingManager getDrawingManager();
}

