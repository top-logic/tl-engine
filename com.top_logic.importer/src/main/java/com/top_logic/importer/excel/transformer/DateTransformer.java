/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.transformer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.importer.excel.AbstractExcelFileImportParser;
import com.top_logic.importer.logger.ImportLogger;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class DateTransformer implements Transformer<Date> {
    
	private DateFormat DATE_TS_FORMAT = CalendarUtil.newSimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");

	private DateFormat DATE_KW_SHORT_FORMAT = CalendarUtil.newSimpleDateFormat("yyyy-ww");

	private DateFormat DATE_KW_FORMAT = CalendarUtil.newSimpleDateFormat("'KW:' ww-yyyy");
	
    private final DateFormat DATE_FORMAT_STD = CalendarUtil.newSimpleDateFormat("dd.MM.yy");

	public interface Config extends Transformer.Config {
	    String getFormat();
	    Integer getDayOfWeek();
	    String getEmptyString();
	}

	private SimpleDateFormat format;

    private String emptyString;

    private Integer dayOfWeek;

	private boolean mandatory;

	public DateTransformer(boolean isMandatory, String aPattern, Integer aDayOfWeek, String anEmptyString) {
        if (!StringServices.isEmpty(aPattern)) {
			this.format = CalendarUtil.newSimpleDateFormat(aPattern);
            this.dayOfWeek = aDayOfWeek;
        }

        this.emptyString = anEmptyString;
        this.mandatory   = isMandatory;
	}

	public DateTransformer(InstantiationContext context, Config config) {
	    this(config.isMandatory(), config.getFormat(), config.getDayOfWeek(), config.getEmptyString());
	}

	@Override
	public Date transform(ExcelContext aContext, String columnName, AbstractExcelFileImportParser<?> handler, ImportLogger logger) {
		Date theDate = getDate(aContext, columnName, logger);
		
		if (mandatory && theDate == null) {
    		throw new TransformException(I18NConstants.VALUE_EMPTY, aContext.row() + 1, columnName, aContext.getString(columnName));
		}
		
		return theDate;
	}
	
	public Date getDate(ExcelContext aContext, String columnName, ImportLogger logger) {
	    return this.getDateAsCWOrDate(aContext, columnName, logger);
	}

	protected Date getDateAsCWOrDate(ExcelContext aContext, String columnName, ImportLogger logger) {
        if (!aContext.hasColumn(columnName)) {
            return null;
        }
        Object theValue = aContext.column(columnName).value();

        if (theValue instanceof Date) {
            return this.toDayOfWeek((Date) theValue);
        }
        else if (theValue instanceof String) {
            String theString = (String) theValue;

            if (!StringTransformer.isEmpty(theString, Collections.singletonList(this.emptyString))) {
                try {
                    if (this.format != null) {
                        return this.toDayOfWeek(this.format.parse(theString));
                    }
                    else { 
                        return this.toDayOfWeek(this.parseDate(theString));
                    }
                }
                catch (ParseException ex) {
                    return this.toDayOfWeek(this.getDate(theValue, aContext, columnName, logger));
                }
            }

            return null;
        }
        else if (theValue == null) {
            return null;
        }
        else { 
            return this.toDayOfWeek(this.getDate(theValue, aContext, columnName, logger));
        }
	}

    protected Date toDayOfWeek(Date aDate) {
        if (this.dayOfWeek != null) {
			Calendar theCal = CalendarUtil.createCalendar(aDate);

            theCal.set(Calendar.DAY_OF_WEEK, this.dayOfWeek);

            return theCal.getTime();
        }
        else {
            return aDate;
        }
    }
	
    protected Date getDate(Object theValue, ExcelContext aContext, String columnName, ImportLogger logger) {
        if (theValue instanceof String) {
            try {
                return DATE_FORMAT_STD.parse((String) theValue);
            }
            catch (ParseException ex1) {
                try {
                    return this.parseDate((String) theValue);
                }
                catch (ParseException ex) {
            		throw new TransformException(I18NConstants.DATE_PARSE_ERROR, aContext.row() + 1, columnName, theValue);
                }
            }
        }
        else {
        	throw new TransformException(I18NConstants.DATE_PARSE_ERROR, aContext.row() + 1, columnName, theValue);
        }
    }

	protected Date parseDate(String aString) throws ParseException {
        try {
            Date     theDate = DATE_TS_FORMAT.parse(aString);
			Calendar theCal = CalendarUtil.createCalendar(theDate);

            // Milliseconds must be cut of, equals will not work correct with database values!
            theCal.set(Calendar.MILLISECOND, 0);
            return theCal.getTime();
        }
        catch (ParseException ex) {
            try {
                return DATE_KW_FORMAT.parse(aString);
            }
            catch (ParseException ex1) {
                return DATE_KW_SHORT_FORMAT.parse(aString);
            }
        }
	}
}
