/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.common.format;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.top_logic.base.time.BusinessYearConfiguration;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;

/**
 * @author     <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public abstract class ReportingDateFormat extends DateFormat {

    protected final boolean useBusinessYear;

	public ReportingDateFormat() {
		this(TimeZones.systemTimeZone(), ThreadContext.getLocale());
	}

	public ReportingDateFormat(TimeZone timeZone, Locale locale) {
        this.useBusinessYear = false;

		setCalendar(CalendarUtil.createCalendar(timeZone, locale));

        // the following is never used, but it seems that DateFormat requires
        // it to be non-null.  It isn't well covered in the spec, refer to 
        // bug parade 5061189 for more info.
		setNumberFormat(NumberFormat.getInstance(locale));
    }
    
    @Override
    public final StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        this.calendar.setTime(date);
		toAppendTo.append(Resources.getInstance().getString(getKey(calendar, date)));
        return toAppendTo;
    }

	/**
	 * The key to internationalize, as format for the date to format.
	 * 
	 * @param cal
	 *        {@link Calendar} with the date to format, having the correct timezone and locale
	 * @param date
	 *        The {@link Date} to format.
	 */
	protected abstract ResKey getKey(Calendar cal, Date date);

    @Override
    public final Date parse(String source, ParsePosition pos) {
        return null;
    }

    public static DateFormat getHalfYearFormat() {
		return new ReportingDateFormat() {
			@Override
			protected ResKey getKey(Calendar cal, Date date) {
				int year = cal.get(Calendar.YEAR);
				int month = cal.get(Calendar.MONTH);
				int half = month / 6;
				return I18NConstants.HALF_YEAR_DATE_FORMAT__DATE_YEAR_HALF.fill(date, year, REGULAR_HALFS[half]);
			}
		};
	}

	public static DateFormat getHalfYearFormatBusinessYear() {
		return new ReportingDateFormat() {
		    @Override
			protected ResKey getKey(Calendar cal, Date date) {
				int half = BusinessYearConfiguration.getQuarter(date, cal) / 2;
				int year = BusinessYearConfiguration.getYear(date, cal);
				return I18NConstants.HALF_YEAR_DATE_FORMAT_BUSINESS_YEAR__DATE_YEAR_HALF.fill(date, year,
					REGULAR_HALFS[half]);
		    }
		};
	}

	public static DateFormat getHalfYearFormatTooltip() {
		return new ReportingDateFormat() {
		    @Override
			protected ResKey getKey(Calendar cal, Date date) {
		        int year  = cal.get(Calendar.YEAR);
		        int month = cal.get(Calendar.MONTH);
		        int half = month / 6;
				return I18NConstants.HALF_YEAR_DATE_FORMAT_TOOLTIP__DATE_YEAR_HALF.fill(date, year,
					REGULAR_HALFS[half]);
		    }
		};
	}

	public static DateFormat getHalfYearFormatBusinessYearTooltip() {
		return new ReportingDateFormat() {
		    @Override
			protected ResKey getKey(Calendar cal, Date date) {
				int half = BusinessYearConfiguration.getQuarter(date, cal) / 2;
				int year = BusinessYearConfiguration.getYear(date, cal);
				return I18NConstants.HALF_YEAR_DATE_FORMAT_BUSINESS_YEAR_TOOLTIP__DATE_YEAR_HALF.fill(date, year,
					REGULAR_HALFS[half]);
		    }
		};
	}

	public static DateFormat getQuarterFormat() {
		return new ReportingDateFormat() {
		    @Override
			protected ResKey getKey(Calendar cal, Date date) {
		        int year  = cal.get(Calendar.YEAR);
		        int month = cal.get(Calendar.MONTH);
		        int quarter = month / 3;
				return I18NConstants.QUARTER_DATE_FORMAT__DATE_YEAR_QUARTER.fill(date, year, REGULAR_QUARTERS[quarter]);
		    }
		};
	}

	public static DateFormat getQuarterFormatBusinessYear() {
		return new ReportingDateFormat() {
		    @Override
			protected ResKey getKey(Calendar cal, Date date) {
				int quarter = BusinessYearConfiguration.getQuarter(date, cal);
				int year = BusinessYearConfiguration.getYear(date, cal);
				return I18NConstants.QUARTER_DATE_FORMAT_BUSINESS_YEAR__DATE_YEAR_QUARTER.fill(date, year,
					REGULAR_QUARTERS[quarter]);
		    }
		};
	}

	public static DateFormat getQuarterFormatTooltip() {
		return new ReportingDateFormat() {
		    @Override
			protected ResKey getKey(Calendar cal, Date date) {
		        int year  = cal.get(Calendar.YEAR);
		        int month = cal.get(Calendar.MONTH);
		        int quarter = month / 3;
				return I18NConstants.QUARTER_DATE_FORMAT_TOOLTIP__DATE_YEAR_QUARTER.fill(date, year,
					REGULAR_QUARTERS[quarter]);
		    }
		};
	}

	public static DateFormat getQuarterFormatBusinessYearTooltip() {
		return new ReportingDateFormat() {
		    @Override
			protected ResKey getKey(Calendar cal, Date date) {
				int quarter = BusinessYearConfiguration.getQuarter(date, cal);
				int year = BusinessYearConfiguration.getYear(date, cal);
				return I18NConstants.QUARTER_DATE_FORMAT_BUSINESS_YEAR_TOOLTIP__DATE_YEAR_QUARTER.fill(date, year,
					REGULAR_QUARTERS[quarter]);
		    }
		};
	}

	public static DateFormat getYearFormatBusinessYearTooltip() {
		return new ReportingDateFormat() {
		    @Override
		    protected ResKey getKey(Calendar cal, Date date) {
				int year = BusinessYearConfiguration.getYear(date, cal);
				return I18NConstants.YEAR_DATE_FORMAT_BUSINESS_YEAR_TOOLTIP__DATE_YEAR.fill(date, year);
		    }
		};
	}

	public static DateFormat getYearFormatTooltip() {
		return new ReportingDateFormat() {
		    @Override
		    protected ResKey getKey(Calendar cal, Date date) {
		        int year = cal.get(Calendar.YEAR);
				return I18NConstants.YEAR_DATE_FORMAT_TOOLTIP__DATE_YEAR.fill(date, year);
		    }
		};
	}

	public static DateFormat getYearFormatBusinessYear() {
		return new ReportingDateFormat() {
		    @Override
		    protected ResKey getKey(Calendar cal, Date date) {
				int year = BusinessYearConfiguration.getYear(date, cal);
				return I18NConstants.YEAR_DATE_FORMAT_BUSINESS_YEAR__DATE_YEAR.fill(date, year);
		    }
		};
	}

	public static DateFormat getYearFormat() {
		return new ReportingDateFormat() {
		    @Override
		    protected ResKey getKey(Calendar cal, Date date) {
		        int year = cal.get(Calendar.YEAR);
				return I18NConstants.YEAR_DATE_FORMAT__DATE_YEAR.fill(date, year);
		    }
		};
	}

	private static final String[] REGULAR_HALFS = new String[] {"1", "2"};
    private static final String[] REGULAR_QUARTERS = new String[] {"1", "2", "3", "4"};
}
