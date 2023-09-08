/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.ss.util.DateFormatConverter;

import com.top_logic.basic.Logger;
import com.top_logic.basic.format.DateFormatDefinition;
import com.top_logic.basic.format.FormatDefinition;
import com.top_logic.basic.format.FormatDefinition.Config;
import com.top_logic.basic.format.configured.FormatterService;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.util.TLContext;

/**
 * Instances of this class provide convenient access to Excel sheet using Apache
 * POI implementation.
 * 
 * @author <a href="mailto:wta@top-logic.com">wta</a>
 */
public class POIExcelContext extends ExcelContext {

	/**
	 * Id in the {@link FormatterService} under which the date format for excel exports is
	 * defined.
	 * 
	 * @see FormatterService#getFormatDefinition(String)
	 */
	public static final String EXCEL_DATE_ID = "excelDate";

	/**
	 * @see #sheet()
	 */
	private final Sheet _sheet;
	
	/**
	 * The {@link FormulaEvaluator} instance to be used for evaluating all cells of type
	 * {@link CellType#FORMULA}.
	 */
	private final FormulaEvaluator _evaluator;
	
	/**
     * Create a new {@link POIExcelContext} providing convenient access to the
     * given sheet.
     * 
     * @param sheet
     *            the {@link Sheet} to create the accessor for
     */
	protected POIExcelContext(final Sheet sheet) {
		_sheet = sheet;
        _evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
		_evaluator.setIgnoreMissingWorkbooks(true);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T value() {
	    final Cell cell = getCell(false);
	    
	    // no cell -> no value
	    if(cell == null) {
	        return null;
	    }
	    
		final CellType cellType;
		if (CellType.FORMULA == cell.getCellType()) {
			try {
				cellType = _evaluator.evaluateFormulaCell(cell);
			} catch (RuntimeException ex) {
				Logger.info(String.format("Failed to evaluate formula in %s:%s.",
					sheet().getSheetName(), new CellReference(cell).formatAsString()),
					ex, POIExcelContext.class);

				return null;
			}
	    } else {
			cellType = cell.getCellType();
	    }
	    
	    // only the basic cell types are expected at this point
	    switch(cellType) {
			case BOOLEAN:
				return (T) Boolean.valueOf(cell.getBooleanCellValue());
			case NUMERIC:
				return (T) (DateUtil.isCellDateFormatted(cell) ? cell.getDateCellValue() : cell.getNumericCellValue());
			case STRING:
				return (T) cell.getStringCellValue();
			default:
				return null;
	    }
	}

	@Override
	public <T> POIExcelContext value(final T value) {
	    // Boolean implementation
	    if(value instanceof Boolean) {
	        getCell(true).setCellValue((Boolean) value);
	    }
	    
	    // Number implementation always uses double
	    else if(value instanceof Number) {
	        getCell(true).setCellValue(((Number)value).doubleValue());
	    }
	    
	    // Date implementation formats the cell with a date format if necessary
	    else if(value instanceof Date) {
	        final Cell cell = getCell(true);
	
	        // apply date format to the cell if necessary
			if(!isDateFormatted(cell)) {
				final Workbook workbook = cell.getSheet().getWorkbook();
				final short formatIndex = workbook.createDataFormat().getFormat(getDateFormatString());
	            
				CellUtil.setCellStyleProperty(cell, CellUtil.DATA_FORMAT, formatIndex);
	        }
	        
	        // apply the cell value
	        cell.setCellValue((Date) value);
	    }
	    
	    // Calendar implementation uses Date implementation for consistency
	    else if(value instanceof Calendar) {
	        value(((Calendar) value).getTime());
	    }
	    
	    // String implementation is always unformatted
	    else if(value instanceof String) {
	        getCell(true).setCellValue((String) value);
	    }
	    
	    // Null implementation avoids creating blank cells
	    else if(value == null) {
	        final Cell cell = getCell(false);
	        if(cell != null) {
	            cell.setCellValue((String) null);
	        }
	    }
	    
	    // Default implementation uses MetaLabelProvider
	    else {
	        value(MetaLabelProvider.INSTANCE.getLabel(value));
	    }
	    
	    return this;
	}

	private String getDateFormatString() {
		FormatDefinition<?> dateFormat = FormatterService.getInstance().getFormatDefinition(EXCEL_DATE_ID);
		String pattern = dateFormat.getPattern();
		Locale currentLocale = TLContext.getLocale();
		if (pattern != null) {
			return DateFormatConverter.convert(currentLocale, pattern);
		}
		Config<?> config = dateFormat.getConfig();
		int dateFormatStyle;
		if (config instanceof DateFormatDefinition.Config) {
			dateFormatStyle = ((DateFormatDefinition.Config) config).getStyle().intValue();
		} else {
			dateFormatStyle = DateFormat.SHORT;
		}

		return DateFormatConverter.getJavaDatePattern(dateFormatStyle, currentLocale);
	}

    @Override
	public int getLastCellNum() {
		final Row row = this.getRow(false);

		return (row != null) ? row.getLastCellNum() : -1;
    }

    @Override
    public boolean hasMoreRows() {
    	return this.row() <= this.sheet().getLastRowNum();
    }

    @Override
	public boolean isInvalid(int aColumn) {
    	Row theRow = getRow(false);

    	if (theRow != null) {
    		Cell theCell = theRow.getCell(aColumn);

    		if (theCell != null) {
    			CellStyle theStyle = theCell.getCellStyle();
    			Font      theFont  = theCell.getSheet().getWorkbook().getFontAt(theStyle.getFontIndex());

    			return theFont.getStrikeout();
    		}
    	}

        return false;
    }

    @Override
	public String getURL(int aColumn) {
        Cell      theCell = ((POIExcelContext) this.column(aColumn)).getCell(false);
        Hyperlink theURL  = theCell.getHyperlink();

        return (theURL != null) ? theURL.getAddress() : null;
    }

    @Override
	public void close() {
    	// No need to close here explicit.
    }

	/**
	 * the {@link Sheet} being accessed
	 */
	public Sheet sheet() {
	    return _sheet;
	}

	/**
	 * Returns the row object the accessor is currently positioned at.
	 * 
	 * @param create
	 *            {@code true} to create a new object if no object exists for
	 *            the row at the current position
	 * @return the {@link Row} object the accessor is currently positioned at or
	 *         {@code null} if the row at the current position does not exist
	 *         and a creation is not desired according to the boolean argument
	 */
	public Row getRow(final boolean create) {
	    final Row row = _sheet.getRow(row());
	    
	    if(row == null && create) {
	        return _sheet.createRow(row());
	    } else {
	        return row;
	    }
	}

	/**
	 * Returns the cell object the accessor is currently positioned at.
	 * 
	 * @param create
	 *            {@code true} to create a new object if no object exists for
	 *            the row and column at the current position
	 * @return the {@link Cell} object the accessor is currently positioned at
	 *         or {@code null} if the row or column at the current position does
	 *         not exist and no creation is desired according to the boolean
	 *         argument.
	 */
	public Cell getCell(final boolean create) {
	    final Row row = getRow(create);
	    
	    if(row != null) {
	        final Cell cell = row.getCell(column());
	        
	        if(cell == null && create) {
	            return row.createCell(column());
	        } else {
	            return cell;
	        }
	    }
	    
	    return null;
	}

	/**
	 * Checks if the given {@link Cell} is date formatted.
	 * 
	 * @param cell
	 *        the {@link Cell} to check the format for
	 * @return {@code true} if the given cell is formatted using a date format as defined by Excel
	 */
	private boolean isDateFormatted(final Cell cell) {
		final CellStyle style = cell.getCellStyle();

		if (style != null) {
			return DateUtil.isADateFormat(style.getDataFormat(), style.getDataFormatString());
		} else {
			return false;
		}
	}

}