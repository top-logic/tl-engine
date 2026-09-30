/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

import com.top_logic.base.office.excel.POITypeSupporter;
import com.top_logic.base.office.excel.handler.POITypeProvider;

/**
 * An algorithm setting a Java value to an Excel cell.
 * 
 * <p>
 * Instances of this class are registered to the configuration of {@link POITypeProvider}.
 * </p>
 * 
 * @see com.top_logic.base.office.excel.handler.POITypeProvider.Config#getTypeHandlers()
 */
public interface PoiTypeHandler {

    /**
	 * The value type, this handler is used for.
	 */
    Class<?> getHandlerClass();
    
    /**
	 * Sets the given value to the given cell.
	 * 
	 * @param cell
	 *        The cell to set the value in
	 * @param workbook
	 *        The workbook is needed for styling the cells.
	 * @param value
	 *        The value to set
	 * @param support
	 *        The supporter calling this method.
	 * @return The width of the cell resulting cell in characters.
	 */
	int setValue(Cell cell, Workbook workbook, Object value, POITypeSupporter support);
    
}
