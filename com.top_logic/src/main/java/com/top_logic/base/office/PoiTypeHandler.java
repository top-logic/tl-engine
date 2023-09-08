/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

import com.top_logic.base.office.excel.POITypeSupporter;

/**
 * A type handler for setting special values in cells.
 * 
 * @author    <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
 */
public interface PoiTypeHandler {

    /** 
     * Returns the class this handler is used for.
     * 
     * @return the class this handler is used for.
     */
    Class<?> getHandlerClass();
    
    /** 
     * This method is called to set the value in the cell.
     * 
     * @param    aCell        The cell to set the value in
     * @param    aWorkbook    The workbook is needed for styling the cells.
     * @param    aValue       The value to set
     * @param    aSupport     The supporter calling this method.
     * @return   the width of the cell.
     */
    int setValue(Cell aCell, Workbook aWorkbook, Object aValue, POITypeSupporter aSupport);
    
}
