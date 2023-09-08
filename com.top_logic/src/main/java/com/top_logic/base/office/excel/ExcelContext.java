/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import java.io.Closeable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.top_logic.base.office.POIUtil;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.CSVReader;

/**
 * Base class provide convenient access to excel sheets.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class ExcelContext implements Closeable {

	/** @see #row() */
	private int _row;

	/** @see #column() */
	private int _column;

	/** Mapping from column name to correct column number. */
	private Map<String, Integer> columnMap;

	/**
     * Create a new {@link ExcelContext} providing access to an excel file.
     */
	protected ExcelContext() {
	}

	/**
	 * the {@link Object} value from the {@link Cell} the accessor is currently positioned at. The
	 * returned value may be {@code null} if
	 * <ul>
	 * <li>the {@link Cell} object not exist</li>
	 * <li>the {@link Cell} object is {@link CellType#BLANK}</li>
	 * <li>the {@link Cell} object does not contain any type of supported data</li>
	 * </ul>
	 */
	public abstract <T> T value();

	/**
	 * Sets the given value to the {@link Cell} the accessor is positioned at.
	 * 
	 * <p>
	 * <b>Note: </b> If the {@link Cell} object at the current position does not
	 * exist and the given value is not {@code null} a new cell object is
	 * created.
	 * </p>
	 * 
	 * @param value
	 *            the {@code <T>} object to be set as value for the cell at the
	 *            current position or {@code null} to reset the value
	 * @return {@link ExcelContext this} instance for call chaining
	 */
	public abstract <T> ExcelContext value(final T value);

	/**
	 * Return the number of the last cell in the current excel row (see POI definition of
	 * {@link Row#getLastCellNum()}).
	 * 
	 * @return the number of the last cell in the current excel row, -1 when row is empty.
	 */
    public abstract int getLastCellNum();

    /** 
     * Check, if there are more lines to read in the excel file.
     * 
     * @return    <code>true</code>, when there are lines left in the excel file.
     */
    public abstract boolean hasMoreRows();

	/**
	 * the (zero based) index of the row the accessor is positioned at
	 */
	public int row() {
	    return _row;
	}

	/**
	 * Positions the accessor at the given row.
	 * 
	 * <p>
	 * <b>Note: </b>Calling this method with a negative index will result in the
	 * accessor being positioned at the very first row in the sheet (zero).
	 * </p>
	 * 
	 * @param index
	 *            the (zero based) index of the row to position the accessor at
	 * @return {@link ExcelContext this} instance for call chaining
	 */
	public ExcelContext row(final int index) {
	    if(index < 0) {
	        _row = 0;
	    } else {
	        _row = index;
	    }
	    
	    return this;
	}

	/**
	 * Move the accessor one row up.
	 * 
	 * <p>
	 * <b>Note: </b>Has no effect if the accessor is already positioned at the
	 * first row (zero).
	 * </p>
	 * 
	 * @return {@link ExcelContext this} instance for call chaining
	 */
	public ExcelContext up() {
	    return row(row() - 1);
	}

	/**
	 * Move the accessor one row down.
	 * 
	 * @return {@link ExcelContext this} instance for call chaining
	 */
	public ExcelContext down() {
	    return row(row() + 1);
	}

	/**
	 * the (zero based) index of the column the accessor is positioned
	 *         at
	 */
	public int column() {
	    return _column;
	}

	/**
	 * Positions the accessor at the given column.
	 * 
	 * <p>
	 * <b>Note: </b>Calling this method with a negative index will result in the accessor being
	 * positioned at the very first column in the sheet (zero).
	 * </p>
	 * 
	 * @param index
	 *        the (zero based) index of the column to position the accessor at
	 * @return {@link ExcelContext this} instance for call chaining
	 */
	public ExcelContext column(final int index) {
	    if(index < 0) {
	        _column = 0;
	    } else {
	        _column = index;
	    }
	
	    return this;
	}

	/**
	 * Move the accessor one column to the left.
	 * 
	 * <p>
	 * <b>Note: </b>Has no effect if the accessor is already positioned at the
	 * first column (zero).
	 * </p>
	 * 
	 * @return {@link ExcelContext this} instance for call chaining
	 */
	public ExcelContext left() {
	    return column(column() - 1);
	}

	/**
	 * Move the accessor one column to the right.
	 * 
	 * @return {@link ExcelContext this} instance for call chaining
	 */
	public ExcelContext right() {
	    return column(column() + 1);
	}

    /** 
     * Check, if an excel cell is marked as invalid (strike through).
     * 
     * @param    aColumn    The requested column.
     * @return   <code>true</code> when the cell format is strike through.
     */
    public boolean isInvalid(int aColumn) {
    	return false;
    }

    /** 
     * Set the cell focus to the given {@link #column()} in the current {@link #row()}.
     * 
     * @param    aKey    The name of the requested column, must not be <code>null</code>.
     * @return   Reference to this instance.
     */
    public ExcelContext column(final String aKey) {
        return column(this.getColumnMap().get(aKey));
    }

    /** 
     * Return the value of the given column name.
     * 
     * @param    aColumn    The name of the requested column, must not be <code>null</code>.
     * @return   The requested string value, may be <code>null</code>.
     */
    public String getString(String aColumn) {
        Integer theColumn = this.getColumnMap().get(aColumn);
        return theColumn == null ? null : this.getString(theColumn);
    }

    /** 
     * Return the value of the given column number.
     * 
     * @param    aColumn    The number of the requested column.
     * @return   The requested string value, may be <code>null</code>.
     */
    public String getString(int aColumn) {
        Object theValue = this.column(aColumn).value();

        return (theValue == null) ? null : String.valueOf(theValue);
    }
    
    /** 
     * Return the value of the given column name.
     * 
     * @param    aColumn    The name of the requested column, must not be <code>null</code>.
     * @return   The requested boolean value, may be <code>null</code>.
     */
    public Boolean getBoolean(String aColumn) {
        Integer theColumn = this.getColumnMap().get(aColumn);
        return theColumn == null ? null : this.getBoolean(theColumn);
    }

    /** 
     * Return the value of the given column number.
     * 
     * @param    aColumn    The number of the requested column.
     * @return   The requested boolean value, may be <code>null</code>.
     */
    public Boolean getBoolean(int aColumn) {
        Object theValue = this.column(aColumn).value();

        if (theValue instanceof Boolean) {
            return (Boolean) theValue;
        }
        else if (theValue instanceof String) {
            return this.getBooleanTrues().contains(((String) theValue).toLowerCase());
        }
        else {
            return null;
        }
    }

	/** 
     * Return the value of the given column name.
     * 
     * @param    aColumn    The name of the requested column, must not be <code>null</code>.
     * @return   The requested double value, may be <code>null</code>.
     */
    public Double getDouble(String aColumn) {
    	Integer theColumn = this.getColumnMap().get(aColumn);
    	return theColumn == null ? null : this.getDouble(theColumn);
    }
    
    /** 
     * Return the value of the given column number.
     * 
     * @param    aColumn    The number of the requested column.
     * @return   The requested double value, may be <code>null</code>.
     */
    public Double getDouble(int aColumn) {
    	Object theValue = this.column(aColumn).value();
    	
    	if (theValue instanceof Number) {
    		return ((Number) theValue).doubleValue();
    	}
    	else if (theValue instanceof String) {
    		return Double.parseDouble((String) theValue);
    	}
    	else {
    		return null;
    	}
    }
    
    /** 
     * Return the value of the given column name.
     * 
     * @param    aColumn    The name of the requested column, must not be <code>null</code>.
     * @return   The requested integer value, may be <code>null</code>.
     */
    public Integer getInt(String aColumn) {
        Integer theColumn = this.getColumnMap().get(aColumn);
        return theColumn == null ? null : this.getInt(theColumn);
    }

    /** 
     * Return the value of the given column number.
     * 
     * @param    aColumn    The number of the requested column.
     * @return   The requested integer value, may be <code>null</code>.
     */
    public Integer getInt(int aColumn) {
        Object theValue = this.column(aColumn).value();
        
        if (theValue instanceof Number) {
            return ((Number) theValue).intValue();
        }
        else if (theValue instanceof String) {
            return Integer.parseInt((String) theValue);
        }
        else {
            return null;
        }
    }
    
    /** 
     * Return the value of the given column name.
     * 
     * @param    aColumn    The name of the requested column, must not be <code>null</code>.
     * @return   The requested long value, may be <code>null</code>.
     */
    public Long getLong(String aColumn) {
        Integer theColumn = this.getColumnMap().get(aColumn);
        return theColumn == null ? null : this.getLong(theColumn);
    }

    /** 
     * Return the value of the given column number.
     * 
     * @param    aColumn    The number of the requested column.
     * @return   The requested long value, may be <code>null</code>.
     */
    public Long getLong(int aColumn) {
        Object theValue = this.column(aColumn).value();
        
        if (theValue instanceof Number) {
            return ((Number) theValue).longValue();
        }
        else if (theValue instanceof String) {
            return Long.parseLong((String) theValue);
        }
        else {
            return null;
        }
    }

    /** 
     * Return the value of the given column number.
     * 
     * @param    aColumn    The number of the requested column.
     * @return   The URL as string, may be <code>null</code>.
     */
    public String getURL(String aColumn) {
        Integer theColumn = this.getColumnMap().get(aColumn);
        return theColumn == null ? null : this.getURL(theColumn);
    }
    
    /** 
     * Return the URL from an excel cell.
     * 
     * @param    aColumn   The requested column.
     * @return   The URL as string, may be <code>null</code>.
     */
    public String getURL(int aColumn) {
    	return null;
    }

    /** 
     * Cell value is invalid, when value is strike out.
     * 
     * @param    aColumn    The requested column.
     * @return   <code>true</code> when value is strike out in excel.
     */
    public boolean isInvalid(String aColumn) {
        return isInvalid(this.getColumnMap().get(aColumn));
    }

	/** 
     * Return the column name for the given column number.
     * 
     * @param    aCol    The column number to get the name for.
     * @return   The requested column name, may be <code>null</code>.
     */
    public String getColumnNr(int aCol) {
        for (Entry<String, Integer> theEntry : this.getColumnMap().entrySet()) {
            if (aCol == theEntry.getValue()) {
                return theEntry.getKey();
            }
        }

        return null;
    }

    /** 
     * Check, if there is a column with the given name.
     * 
     * @param    aColumn    The name to be looked up.
     * @return   <code>true</code> when the column name is known.
     */
    public boolean hasColumn(String aColumn) {
        return this.getColumnMap().containsKey(aColumn);
    }

    /** 
     * Return the mapping from column names to numbers.
     * 
     * @return    The requested mapping.
     */
    public Map<String, Integer> getColumnMap() {
    	return this.columnMap;
    }

    /** 
     * Identify the headers in the current sheet.
     * 
     * Due to the fact, that the sheets have different headers, we need to
     * make this on ever sheet imported.
     */
    public void prepareHeaderRows() {
        this.prepareHeaderRows(Short.MAX_VALUE);
    }

    /** 
     * Identify the headers in the current sheet.
     * 
     * Due to the fact, that the sheets have different headers, we need to
     * make this on ever sheet imported.
     * 
     * @param    aMaxColumn    The maximum number of columns to be inspected.
     */
    public void prepareHeaderRows(int aMaxColumn) {
        int theMax = Math.min(aMaxColumn, this.getLastCellNum());

        this.columnMap = new HashMap<>();

        for (int thePos = 0; thePos < theMax; thePos++) {
            String theString = this.getString(thePos);

            if (!StringServices.isEmpty(theString)) {
                this.columnMap.put(theString.trim().replace('\n', ' '), thePos);
            }
        }
    }
    
    /**
     * to be called if there are no headers in the sheet and the excel names of the columns are used 
     * to identify the columns, i.e. A, B, .., Z, AA, AB,..
     */
    public void prepareColNamesAsHeaders() {
        this.prepareColNamesAsHeaders(Short.MAX_VALUE);
    }

    /** 
     * Prepare the header names and build up the {@link #columnMap}.
     * 
     * @param aMaxColumn    The maximum number of columns to be inspected.
     */
    protected void prepareColNamesAsHeaders(int aMaxColumn) {
        int theMax = Math.min(aMaxColumn, this.getLastCellNum());

        this.columnMap = new HashMap<>();

        for (int thePos = 0; thePos < theMax; thePos++) {
            String theString = POIUtil.toColumnName(thePos);

            if (!StringServices.isEmpty(theString)) {
                this.columnMap.put(theString.trim().replace('\n', ' '), thePos);
            }
        }
    } 

    /** 
	 * Return the string representation of a boolean value "true".
	 * 
	 * @return    The possible options.
	 */
	protected Collection<String> getBooleanTrues() {
		return CollectionUtil.createList("ja", "x", "true", "yes");
	}

    /** 
     * Return the matching excel context for the given source object.
     * 
     * @param    aSource    The source to provide data.
     * @return   The requested context, never <code>null</code>.
     * @throws   IllegalArgumentException  When source is no {@link Sheet} or {@link CSVReader}.
     */
    public static ExcelContext getInstance(Object aSource) {
    	if (aSource instanceof Sheet) {
    		return new POIExcelContext((Sheet) aSource);
    	}
    	else if (aSource instanceof CSVReader) {
    		return new CSVExcelContext((CSVReader) aSource);
    	}
    	else {
    		throw new IllegalArgumentException("Cannot create an excel context from " + aSource);
    	}
    }
}