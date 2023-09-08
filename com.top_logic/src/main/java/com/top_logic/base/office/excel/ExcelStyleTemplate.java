/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.base.office.excel.ExcelValue.CellPosition;
import com.top_logic.basic.Logger;

/**
 * The ExcelStyleTemplate is an abstract super class of excel templates, which holds styles in form
 * of cell positions.
 * 
 * <p>
 * Usage:
 * </p>
 * 
 * <ul>
 * <li>Create an excel file and format respectively one cell for each style you want to use, but
 * leave the A1 cell clean.</li>
 * 
 * <li>Extend this class and define "static style constants" as fields, one for each created style
 * in your excel file, like the {@link #CLEAN} constant in this class. Some fields may be have
 * <code>null</code> as value to keep default style of your excel file.</li>
 * 
 * <li>When creating ExcelValues in your getExportData method, create them with the desired style
 * constant of your subclass of this template class. When finished, call the
 * {@link #cleanFormatCells(List)} method to clean all format block cells.</li>
 * </ul>
 * 
 * <p>
 * In Addition to that, this class can be used to cache and reuse cell styles because Excel can't
 * use unlimited individual styles.
 * </p>
 * 
 * <p>
 * Usage:
 * </p>
 * <ul>
 * <li>Create a new excel value without a reference style.</li>
 * <li>Set custom styles by calling the according methods in the {@link ExcelValue} class.</li>
 * <li>Call the {@link #finalizeStyle(ExcelValue)} method in this class with your excel value if the
 * style is complete. If that was the first value with this individual style, the style will be
 * cached, otherwise the excel value will get a reference style of a cached style.</li>
 * </ul>
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public abstract class ExcelStyleTemplate {

    public final CellPosition CLEAN = getCleanCellPosition();

    protected Map cellStyleCache = new HashMap();

    public void cleanFormatCells(List aResultList) {
        Field[] theFields = getClass().getFields();
        for (int i = 0; i < theFields.length; i++) {
            Field theField = theFields[i];
            try {
                if (Modifier.isStatic(theField.getModifiers()) && theField.getType().equals(CellPosition.class)) {
                    CellPosition theCell = (CellPosition)theField.get(null);
                    if (theCell != null && theCell != CLEAN) {
                        ExcelValue excelValue = new ExcelValue(theCell.getRow(), theCell.getColumn(), null);
						excelValue.setCellStyleFrom(CLEAN);
						aResultList.add(excelValue);
                    }
                }
            }
            catch (Exception e) {
                Logger.error("Failed to clean up template styles.", e, this);
            }
        }
    }

    /**
     * Replaces the individual cell style with a reference cell style of cached styles of
     * excel values passes to this method before. Call this after the style settings of the
     * excel value is finished and can be cached.
     *
     * @param aValue
     *        the excel value whose style to mark as finished.
     */
    public void finalizeStyle(ExcelValue aValue) {
        if (aValue == null || aValue.hasReferenceStyle() || !aValue.hasIndividualStyle()) return;
        Object theKey = aValue.getStyleKey();
        CellPosition thePos = (CellPosition)cellStyleCache.get(theKey);
        if (thePos == null) {
            cellStyleCache.put(theKey, new CellPosition(aValue.getRow(), aValue.getCol()));
        }
        else {
            aValue.clearStyle();
            aValue.setCellStyleFrom(thePos);
        }
    }

    /**
     * Resets the style cache.
     */
    public void resetCache() {
        cellStyleCache.clear();
    }

    /**
     * Gets a clean cell to cleanUp format cells in template.
     *
     * @return the position of a clean cell
     */
    public CellPosition getCleanCellPosition() {
        return new CellPosition("A1");
    }

    /**
     * Gets the relative path to the excel template file, which this class describes.
     *
     * @return relative path to the excel template file
     */
    public abstract String getTemplateFileName();

}
