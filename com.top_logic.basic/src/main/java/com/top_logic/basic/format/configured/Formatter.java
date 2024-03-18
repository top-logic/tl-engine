/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.format.configured;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.format.FormatConfig;
import com.top_logic.basic.format.FormatDefinition;
import com.top_logic.basic.time.CalendarUtil;

/**
 * Thread-safe access to common formats to be used consistently throughout an application.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Formatter {

	/**
	 * Default format ID for {@link #formatNumber(Number)}.
	 * 
	 * @see #getFormatNonNull(String)
	 */
	public static final String NUMBER_STYLE = "number";

	/**
	 * Default format ID for {@link #formatDouble(double)}.
	 * 
	 * @see #getFormatNonNull(String)
	 */
	public static final String DOUBLE_STYLE = "double";

	/**
	 * Default format ID for {@link #formatLong(long)}.
	 * 
	 * @see #getFormatNonNull(String)
	 */
	public static final String LONG_STYLE = "long";

	/**
	 * Default format ID for {@link #formatPercent(double)}.
	 * 
	 * @see #getFormatNonNull(String)
	 */
	public static final String PERCENT_STYLE = "percent";

	/**
	 * Default format ID for {@link #formatDate(Date)}.
	 * 
	 * @see #getFormatNonNull(String)
	 */
	public static final String DATE_STYLE = "date";

	/**
	 * Default format ID for {@link #formatTime(Date)}.
	 * 
	 * @see #getFormatNonNull(String)
	 */
	public static final String TIME_STYLE = "time";

	/**
	 * Default format ID for {@link #formatDateTime(Date)}.
	 * 
	 * @see #getFormatNonNull(String)
	 */
	public static final String DATE_TIME_STYLE = "date-time";

	/**
	 * Default format ID for {@link #formatShortDate(Date)}.
	 * 
	 * @see #getFormatNonNull(String)
	 */
	public static final String SHORT_DATE_STYLE = "short-date";

	/**
	 * Default format ID for {@link #formatShortTime(Date)}.
	 * 
	 * @see #getFormatNonNull(String)
	 */
	public static final String SHORT_TIME_STYLE = "short-time";

	/**
	 * Default format ID for {@link #formatShortDateTime(Date)}.
	 * 
	 * @see #getFormatNonNull(String)
	 */
	public static final String SHORT_DATE_TIME_STYLE = "short-date-time";

	/**
	 * Default format ID for {@link #formatMediumDateTime(Date)}.
	 * 
	 * @see #getFormatNonNull(String)
	 */
	public static final String MEDIUM_DATE_TIME_STYLE = "medium-date-time";

    /** Standard Formatter for number */
	private final NumberFormat _sharedNumberFormat, _wrappedNumberFormat;

	/**
	 * @see #getDoubleFormat()
	 */
	private final NumberFormat _sharedDoubleFormat, _wrappedDoubleFormat;

	/**
	 * @see #getLongFormat()
	 */
	private final NumberFormat _sharedLongFormat, _wrappedLongFormat;

    /** Formatter for percent values. */
	private final NumberFormat _sharedPercentFormat, _wrappedPercentFormat;

    /** Standard Formatter for Dates (without time) */
	private final DateFormat _sharedDateFormat, _wrappedDateFormat;
    
    /** Standard Formatter for Time */
	private final DateFormat _sharedTimeFormat, _wrappedTimeFormat;

    /** Standard Formatter for Date and Time. */
	private final DateFormat _sharedDateTimeFormat, _wrappedDateTimeFormat;

    /** Standard Formatter for short Dates (without time) */
	private final DateFormat _sharedShortDateFormat, _wrappedShortDateFormat;
    
    /** Standard Formatter for short Time */
	private final DateFormat _sharedShortTimeFormat, _wrappedShortTimeFormat;

    /** Standard Formatter for short Date and Time. */
	private final DateFormat _sharedShortDateTimeFormat, _wrappedShortDateTimeFormat;

    /** Standard Formatter for mixed Date and Time. */
	private final DateFormat _sharedMediumDateTimeFormat, _wrappedMediumDateTimeFormat;
    

    /** The locale this instance represents. */
	private final Locale _locale;

	private final TimeZone _timeZone;

	private final FormatConfig _formatConfig;

	private final Map<String, Format> _wrappedFormats;

	private final Map<String, Format> _sharedFormats;

    /**
	 * Initialized a concrete {@link Formatter} with its {@link Locale}.
	 * 
	 * Use {@link com.top_logic.basic.format.configured.FormatterService#getFormatter()} instead.
	 * 
	 * @param timeZone
	 *        The {@link TimeZone} of the {@link DateFormat}s encapsulated by this formatter.
	 * @param locale
	 *        The locale of the formats encapsulated by this formatter.
	 * @param formats
	 *        All defined {@link FormatDefinition}s.
	 */
	protected Formatter(FormatConfig config, TimeZone timeZone, Locale locale,
			Map<String, FormatDefinition<?>> formats) {
		_timeZone = timeZone;
		_locale = locale;
		_formatConfig = config;

		_sharedFormats = createSharedFormats(formats);
		_wrappedFormats = createWrapperFormats(formats);

		_sharedNumberFormat = sharedNumberFormatNonNull(NUMBER_STYLE);
		_wrappedNumberFormat = getNumberFormatNonNull(NUMBER_STYLE);

		_sharedDoubleFormat = sharedNumberFormatNonNull(DOUBLE_STYLE);
		_wrappedDoubleFormat = getNumberFormatNonNull(DOUBLE_STYLE);

		_sharedLongFormat = sharedNumberFormatNonNull(LONG_STYLE);
		_wrappedLongFormat = getNumberFormatNonNull(LONG_STYLE);

		_sharedPercentFormat = sharedNumberFormatNonNull(PERCENT_STYLE);
		_wrappedPercentFormat = getNumberFormatNonNull(PERCENT_STYLE);
        
		_sharedDateFormat = sharedDateFormatNonNull(DATE_STYLE);
		_wrappedDateFormat = getDateFormatNonNull(DATE_STYLE);

		_sharedTimeFormat = sharedDateFormatNonNull(TIME_STYLE);
		_wrappedTimeFormat = getDateFormatNonNull(TIME_STYLE);

		_sharedDateTimeFormat = sharedDateFormatNonNull(DATE_TIME_STYLE);
		_wrappedDateTimeFormat = getDateFormatNonNull(DATE_TIME_STYLE);

		_sharedShortDateFormat = sharedDateFormatNonNull(SHORT_DATE_STYLE);
		_wrappedShortDateFormat = getDateFormatNonNull(SHORT_DATE_STYLE);

		_sharedShortTimeFormat = sharedDateFormatNonNull(SHORT_TIME_STYLE);
		_wrappedShortTimeFormat = getDateFormatNonNull(SHORT_TIME_STYLE);

		_sharedShortDateTimeFormat = sharedDateFormatNonNull(SHORT_DATE_TIME_STYLE);
		_wrappedShortDateTimeFormat = getDateFormatNonNull(SHORT_DATE_TIME_STYLE);

		_sharedMediumDateTimeFormat = sharedDateFormatNonNull(MEDIUM_DATE_TIME_STYLE);
		_wrappedMediumDateTimeFormat = getDateFormatNonNull(MEDIUM_DATE_TIME_STYLE);
    }

	private Map<String, Format> createSharedFormats(Map<String, FormatDefinition<?>> formats) {
		HashMap<String, Format> result = MapUtil.newMap(formats.size());
		for (Entry<String, FormatDefinition<?>> definition : formats.entrySet()) {
			Format format = definition.getValue().newFormat(_formatConfig, _timeZone, _locale);
			result.put(definition.getKey(), format);
		}
		return result;
	}

	private Map<String, Format> createWrapperFormats(Map<String, FormatDefinition<?>> formats) {
		final FormatterService service = FormatterService.getInstance();
		HashMap<String, Format> result = MapUtil.newMap(formats.size());
		for (Entry<String, FormatDefinition<?>> definition : formats.entrySet()) {
			final String id = definition.getKey();

			Format format = sharedFormatNonNull(id);

			Format wrapperFormat;
			if (format instanceof DateFormat) {
				NumberFormat numberFormat = (NumberFormat) ((DateFormat) format).getNumberFormat().clone();
				Calendar calendar = CalendarUtil.clone(((DateFormat) format).getCalendar());
				DateFormatProxy dateFormatWrapper =
					new DateFormatProxy(service, id, _timeZone, _locale, numberFormat, calendar);
				wrapperFormat = dateFormatWrapper;
			}
			else if (format instanceof NumberFormat) {
				wrapperFormat = new NumberFormatProxy(service, id, _timeZone, _locale);
			}
			else {
				wrapperFormat = new FormatProxy(service, id, _timeZone, _locale);
			}
			result.put(id, wrapperFormat);
		}
		return result;
	}

	/**
	 * The configured format with the given ID as {@link NumberFormat}.
	 * 
	 * @throws ConfigurationError
	 *         If no format is defined for the given ID, or it is not a {@link NumberFormat}.
	 * 
	 * @see #getFormatNonNull(String)
	 */
	public NumberFormat getNumberFormatNonNull(String id) {
		return asNumberFormat(id, getFormatNonNull(id));
	}

	private NumberFormat asNumberFormat(String id, Format result) {
		if (!(result instanceof NumberFormat)) {
			return new NumberFormat() {
				@Override
				public Number parse(String source, ParsePosition parsePosition) {
					Object value = result.parseObject(source, parsePosition);
					if (value != null && !(value instanceof Number)) {
						throw new RuntimeException("Format with ID '" + id
							+ "' does not provide a number, but is used as number format, value: " + value);
					}
					return (Number) value;
				}

				@Override
				public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
					return result.format(number, toAppendTo, pos);
				}

				@Override
				public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
					return result.format(number, toAppendTo, pos);
				}
			};
		}
		return (NumberFormat) result;
	}

	/**
	 * The configured format with the given ID as {@link DateFormat}.
	 * 
	 * @throws ConfigurationError
	 *         If no format is defined for the given ID, or it is not a {@link DateFormat}.
	 * 
	 * @see #getFormatNonNull(String)
	 */
	public DateFormat getDateFormatNonNull(String id) {
		return asDateFormat(id, getFormatNonNull(id));
	}

	private DateFormat asDateFormat(String id, Format result) {
		if (!(result instanceof DateFormat)) {
			throw new ConfigurationError("Format with ID '" + id + "' is no date format: " + result);
		}
		return (DateFormat) result;
	}

	/**
	 * The configured format with the given ID.
	 * 
	 * @param id
	 *        The format ID as defined in the configuration, see
	 *        {@link FormatterService.Config#getFormats()}. Predefined formats are e.g.
	 *        {@link #NUMBER_STYLE}.
	 * 
	 * @throws ConfigurationError
	 *         If no format is defined for the given ID.
	 * 
	 * @see #getNumberFormatNonNull(String)
	 * @see #getDateFormatNonNull(String)
	 */
	public final Format getFormatNonNull(String id) {
		Format result = getFormat(id);
		if (result == null) {
			throw new ConfigurationError("The format with id '" + id + "' is not defined.");
		}
		return result;
	}

	/**
	 * Return a configured format known under given ID.
	 * 
	 * @param id
	 *        The format ID as defined in the configuration, see
	 *        {@link FormatterService.Config#getFormats()}. Predefined formats are e.g.
	 *        {@link #NUMBER_STYLE}.
	 * 
	 * @return The configured format or <code>null</code> if no format for the given ID exists.
	 * 
	 * @see #getFormatNonNull(String)
	 */
	public final Format getFormat(String id) {
		// Note: Handing out the format must create a new instance to prevent the usage of the
		// returned format from different threads: The same HTMLFormatter is used throughout the
		// calling thread. This thread may create multiple components with formats returned from
		// this method. But rendering theses components may happen in different threads
		// concurrently.
		Format format = _wrappedFormats.get(id);
		if (format == null) {
			return null;
		}
		return format;
	}

	final NumberFormat sharedNumberFormatNonNull(String id) {
		return asNumberFormat(id, sharedFormatNonNull(id));
	}

	final DateFormat sharedDateFormatNonNull(String id) {
		return asDateFormat(id, sharedFormatNonNull(id));
	}

	final Format sharedFormatNonNull(String id) {
		Format result = _sharedFormats.get(id);
		if (result == null) {
			throw new ConfigurationError("The format with id '" + id + "' is not defined.");
		}
		return result;
	}

    @Override
    public String toString() {
		return this.getClass().getSimpleName() + " for " + _locale + " and " + _timeZone;
    }
    
	/**
	 * Parses the given value using the format with the given ID.
	 * 
	 * @param formatId
	 *        The ID of the configured format to use.
	 * @param value
	 *        The string to parse.
	 * @return The parsed value, or <code>null</code>, if the value was <code>null</code> or empty,
	 *         or a {@link ParseException} occurred.
	 */
	public Object parse(String formatId, String value) {
		if (value != null && value.length() > 0) {
			try {
				return sharedFormatNonNull(formatId).parseObject(value);
			} catch (ParseException ignored) {
				// Ignore.
			}
		}
		return null;
	}

	/**
	 * Format the given value by using the format with the given ID.
	 * 
	 * @param formatId
	 *        The ID of the configured format to use.
	 * @param value
	 *        The value to format.
	 * @return The formatted value.
	 */
	public String format(String formatId, Object value) {
		if (value == null) {
			return "";
		}
		return sharedFormatNonNull(formatId).format(value);
	}

    /** 
     * Creates an Number object from the string aNumber.
     *  
     * @return If aNumber can not be parse the result will be null. 
     *
     */
    public Number parseNumber(String aNumber) {
        Number theParsedNumber = null;

        if (aNumber != null && aNumber.length() > 0) {
            try {
                theParsedNumber = _sharedNumberFormat.parse(aNumber);
            }
            catch (ParseException ignored)  { /* ignored */ }
        }

        return theParsedNumber;
    }

    /** Format a given number by using the internal numberformat.
     * 
     * null values will be converted to the empty Strings
     */
    public String formatNumber(Number aNum) {
        if (aNum != null) {
            return _sharedNumberFormat.format(aNum);
        }
        else {
            return "";
        }
    }

    /** Creates an Double object from the string aNumber. 
     *
     * @return If aNumber is not a number the result will be null 
     *
     */
    public Double parseDouble(String aNumber) {

        Number n = parseNumber(aNumber);
        if (n != null) {
            if (n instanceof Double) {
                return(Double) n;
            }
            else    
				return Double.valueOf(n.doubleValue());
        }
        return null;
    }
    
	/**
	 * Creates an <code>long</code> value from the given string.
	 * 
	 * @return If aNumber is not a number <code>0</code> will be returned
	 */
	public long parseLongValue(String text) {
		Number number = this.parseNumber(text);

		return (number != null) ? number.longValue() : 0L;
	}

	/**
	 * Format a given number by using the internal {@link #LONG_STYLE} format.
	 * 
	 * @param aValue
	 *        The value to be formatted.
	 * @return The string representing the given value.
	 */
	public String formatLong(long aValue) {
		return _sharedLongFormat.format(aValue);
	}

    /** Creates an double value from the string aNumber. 
     *
     * @return If aNumber is not a number 0.0 will be returned
     *
     */
    public double parseDoubleValue(String aNumber) {
        Number theNumber = this.parseNumber(aNumber);
        
        return (theNumber != null) ? theNumber.doubleValue() : 0.0;
    }

    /** 
     * Format a given number by using the internal numberformat.
     * 
     * @param    aValue    The value to be formatted.
     * @return   The string representing the given value.
     */
    public String formatDouble(double aValue) {
		return _sharedDoubleFormat.format(aValue);
    }

    /**
     * Format a given number as a percentage.
     * 
     * @param    aValue    The value to be formatted.
     * @return   The string representing the given percentage.
     */
    public String formatPercent(double aValue) {
		return _sharedPercentFormat.format(aValue);
    }

    /** Creates an Integer object from the string aNumber.
     *
     * In case the number actually was something else, all
     * fractional digts will silently be discarded. 
     *
     * @return If aNumber is not a number the result will be null 
     */
    public Integer parseInteger(String aNumber) {
        Number n = parseNumber(aNumber);
        if (n != null) {
            if (n instanceof Integer) {
                return (Integer) n;
            }
            else    
				return Integer.valueOf(n.intValue());
        }
        return null;
    }

    /** Creates an int value from the string aNumber. 
     *
     * In case the number actually was something else, all
     * fractional digts will silently be discarded. 
     *
     * @return If aNumber is not a number 0 will be returned
     *
     */
    public int parseIntValue(String aNumber) {
        Number n = parseNumber(aNumber);
        if (n != null) {
            return n.intValue();
        }
        return 0;
    }
    
    /** Format a given number by using the internal numberformat.
     */
    public String formatInt(int n) {
        return _sharedNumberFormat.format(n);
    }

    /** Creates a date object from the string aDate with given style. 
     *
     *  @return If aDate is in a wrong format it returns null
     */
    public Date parseDate(String aDate) {
        
        if (aDate != null && aDate.length() > 0) {
          try {
				return _sharedDateFormat.parse(aDate);
          }
          catch (ParseException ignored)  { /* ignored */ }
        }

        return null;
    }

    /** Format a given Date by using the internal dateformat.
     * 
     * null values will be converted to the empty Strings
     */
    public String formatDate(Date aDate) {
        if (aDate != null) {
			return _sharedDateFormat.format(aDate);
        }
        return "";    
    }
    
    /** Creates a date object from the string aDate with given style.
     *
     *  @return If aDate is in a wrong format it returns null
     */
    public Date parseTime(String aDate) {
        
        if (aDate != null && aDate.length() > 0) {
            try {
				return _sharedTimeFormat.parse(aDate);
            }
            catch (ParseException ignored)  { /* ignored */ }
        }
    
        return null;
    }

    /** Format a given Time by using the internal timeformat.
     * 
     * null values will be converted to the empty Strings
     */
    public String formatTime(Date aDate) {
        if (aDate != null) {
			return _sharedTimeFormat.format(aDate);
        }
        else
            return "";    
    }

    /** Creates a date object from the string aDate with given style. 
     *
     *  @return If aDate is in a wrong format it returns null
     *
     */
    public Date parseDateTime(String aDate) {
        
        if (aDate != null && aDate.length() > 0) {
            try {
				return _sharedDateTimeFormat.parse(aDate);
            }
            catch (ParseException ignored)  { /* ignored */ }
        }

        return null;
    }

    /** Format a given Date/Time by using the internal datetimeformat.
     * 
     * null values will be converted to the empty Strings
     */
    public String formatDateTime(Date aDate) {
        if (aDate != null) {
			return _sharedDateTimeFormat.format(aDate);
        }
        else
            return "";    
    }

    /** Creates a date object from the string aDate with short style. 
     *
     *  @return If aDate is in a wrong format it returns null
     */
    public Date parseShortDate(String aDate) {
        
        if (aDate != null && aDate.length() > 0) {
            try {
				return _sharedShortDateFormat.parse(aDate);
            }
            catch (ParseException ignored)  { /* ignored */ }
        }

        return null;
    }

    /** Format a given Date by using the internal short dateformat.
     * 
     * null values will be converted to the empty Strings
     */
    public String formatShortDate(Date aDate) {
        if (aDate != null) {
			return _sharedShortDateFormat.format(aDate);
        }
        return "";    
    }

    /** Creates a date object from the string aDate with short style.
     *
     *  @return If aDate is in a wrong format it returns null
     */
    public Date parseShortTime(String aDate) {
        
        if (aDate != null && aDate.length() > 0) {
            try {
				return _sharedShortTimeFormat.parse(aDate);
            }
            catch (ParseException ignored)  { /* ignored */ }
        }
    
        return null;
    }

    /** Format a given Time by using the short timeformat.
     * 
     * null values will be converted to the empty Strings
     */
    public String formatShortTime(Date aDate) {
        if (aDate != null) {
			return _sharedShortTimeFormat.format(aDate);
        }
        return "";    
    }

    /** Creates a date object from the string aDate with short style. 
     *
     *  @return If aDate is in a wrong format it returns null
     *
     */
    public Date parseShortDateTime(String aDate) {
        
        if (aDate != null && aDate.length() > 0) {
            try {
				return _sharedShortDateTimeFormat.parse(aDate);
            }
            catch (ParseException ignored)  { /* ignored */ }
        }

        return null;
    }

    /** Format a given Date/Time by using the short datetimeformat.
     * 
     * null values will be converted to the empty Strings
     */
    public String formatShortDateTime(Date aDate) {
        if (aDate != null) {
			return _sharedShortDateTimeFormat.format(aDate);
        }
        return "";    
    }

    /** Creates a date object from the string aDate with mixed style (medium date, short time). 
     *
     *  @return If aDate is in a wrong format it returns null
     *
     */
    public Date parseMixedDateTime(String aDate) {
        
        if (aDate != null && aDate.length() > 0) {
            try {
				return _sharedMediumDateTimeFormat.parse(aDate);
            }
            catch (ParseException ignored)  { /* ignored */ }
        }

        return null;
    }

    /** Format a given Date/Time by using the mixed datetimeformat (medium date, short time).
     * 
     * null values will be converted to the empty Strings
     */
    public String formatMediumDateTime(Date aDate) {
        if (aDate != null) {
			return _sharedMediumDateTimeFormat.format(aDate);
        }
        return "";    
    }

    /**
     * Acessor for dateFormat.
     */
    public DateFormat getDateFormat() {
		return _wrappedDateFormat;
    }

    /**
	 * Format defined by {@link NumberFormat#getNumberInstance(Locale)}.
	 * 
	 * <p>
	 * Note: The class of the objects returned by this format are polymorphic (at least either
	 * {@link Long} or {@link Double}). Use {@link #getDoubleFormat()} to be sure to get
	 * {@link Double} instances.
	 * </p>
	 * 
	 * @see #getDoubleFormat()
	 * @see #getLongFormat()
	 */
    public NumberFormat getNumberFormat() {
		return _wrappedNumberFormat;
    }
    
    /**
	 * Format defined by {@link NumberFormat#getIntegerInstance(Locale)}.
	 * 
	 * <p>
	 * Note: The resulting format parses in a normalizing way (returning always <code>long</code>
	 * instances).
	 * </p>
	 * 
	 * @see #getDoubleFormat()
	 * @see #getNumberFormat()
	 */
	public NumberFormat getLongFormat() {
		return _wrappedLongFormat;
	}

	/**
	 * Acessor for shortDateFormat
	 */
    public DateFormat getShortDateFormat() {
		return _wrappedShortDateFormat;
    }

	/**
	 * Medium size date-time format.
	 */
	public DateFormat getMediumDateTimeFormat() {
		return _wrappedMediumDateTimeFormat;
	}

    /**
     * Acessor for shortDateTimeFormat
     */
    public DateFormat getShortDateTimeFormat() {
		return _wrappedShortDateTimeFormat;
    }

    /**
     * Acessor for shortTimeFormat
     */
    public DateFormat getShortTimeFormat() {
		return _wrappedShortTimeFormat;
    }

    /**
     * Acessor for timeFormat
     */
    public DateFormat getTimeFormat() {
		return _wrappedTimeFormat;
    }

    /**
     * Acessor for dateTimeFormat
     */
    public DateFormat getDateTimeFormat() {
		return _wrappedDateTimeFormat;
    }
    
	/**
	 * Normalizing {@link Format} using two decimal digits and guaranteeing that a {@link Double}
	 * value is returned as result no matter how many fractional digits were entered.
	 * 
	 * @see #getLongFormat()
	 * @see #getNumberFormat()
	 */
	public NumberFormat getDoubleFormat() {
		return _wrappedDoubleFormat;
	}

	/**
	 * Format for percentage values.
	 */
	public NumberFormat getPercentFormat() {
		return _wrappedPercentFormat;
	}

	private static final class FormatProxy extends Format {
		private final FormatterService _service;

		private final String _id;

		private final TimeZone _timeZone;

		private final Locale _locale;

		public FormatProxy(FormatterService service, String id, TimeZone timeZone, Locale locale) {
			_service = service;
			_id = id;
			_timeZone = timeZone;
			_locale = locale;
		}

		@Override
		public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
			return threadFormat().format(obj, toAppendTo, pos);
		}

		@Override
		public Object parseObject(String source, ParsePosition pos) {
			return threadFormat().parseObject(source, pos);
		}

		@Override
		public Object clone() {
			return threadFormat().clone();
		}

		private Format threadFormat() {
			Formatter threadInstance = _service.getThreadInstance(_timeZone, _locale);
			return threadInstance.sharedFormatNonNull(_id);
		}
	}

	private static final class NumberFormatProxy extends NumberFormat {

		private final TimeZone _timeZone;

		private final Locale _locale;

		private final String _id;

		private final FormatterService _service;

		public NumberFormatProxy(FormatterService service, String id, TimeZone timeZone, Locale locale) {
			_service = service;
			_id = id;
			_timeZone = timeZone;
			_locale = locale;
		}

		@Override
		public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
			return threadFormat().format(number, toAppendTo, pos);
		}

		@Override
		public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
			return threadFormat().format(number, toAppendTo, pos);
		}

		@Override
		public Number parse(String source, ParsePosition parsePosition) {
			return threadFormat().parse(source, parsePosition);
		}

		@Override
		public Object clone() {
			return threadFormat().clone();
		}

		@Override
		public void setCurrency(Currency currency) {
			throw new UnsupportedOperationException("Cannot modify default format!");
		}

		@Override
		public void setGroupingUsed(boolean newValue) {
			throw new UnsupportedOperationException("Cannot modify default format!");
		}

		@Override
		public void setMaximumFractionDigits(int newValue) {
			throw new UnsupportedOperationException("Cannot modify default format!");
		}

		@Override
		public void setMaximumIntegerDigits(int newValue) {
			throw new UnsupportedOperationException("Cannot modify default format!");
		}

		@Override
		public void setMinimumFractionDigits(int newValue) {
			throw new UnsupportedOperationException("Cannot modify default format!");
		}

		@Override
		public void setMinimumIntegerDigits(int newValue) {
			throw new UnsupportedOperationException("Cannot modify default format!");
		}

		@Override
		public void setParseIntegerOnly(boolean value) {
			throw new UnsupportedOperationException("Cannot modify default format!");
		}

		@Override
		public void setRoundingMode(RoundingMode roundingMode) {
			throw new UnsupportedOperationException("Cannot modify default format!");
		}

		private NumberFormat threadFormat() {
			Formatter threadInstance = _service.getThreadInstance(_timeZone, _locale);
			return threadInstance.sharedNumberFormatNonNull(_id);
		}
	}

	private static final class DateFormatProxy extends DateFormat {
		private final FormatterService _service;

		private final String _id;

		private final TimeZone _timeZone;

		private final Locale _locale;

		public DateFormatProxy(FormatterService service, String id, TimeZone timeZone, Locale locale,
				NumberFormat numberFormat, Calendar calendar) {
			super();
			// Make sure that the underlying DateFormat is fully initialized. Otherwise toString()
			// would fail with a NPE.
			this.numberFormat = numberFormat;
			this.calendar = calendar;
			
			_service = service;
			_id = id;
			_locale = locale;
			_timeZone = timeZone;
		}

		@Override
		public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
			return threadFormat().format(date, toAppendTo, fieldPosition);
		}

		@Override
		public Date parse(String source, ParsePosition pos) {
			return threadFormat().parse(source, pos);
		}

		@Override
		public Object clone() {
			return threadFormat().clone();
		}

		@Override
		public void setTimeZone(TimeZone zone) {
			throw new UnsupportedOperationException("Cannot modify default format!");
		}

		@Override
		public void setCalendar(Calendar newCalendar) {
			throw new UnsupportedOperationException("Cannot modify default format!");
		}

		@Override
		public void setLenient(boolean lenient) {
			throw new UnsupportedOperationException("Cannot modify default format!");
		}

		@Override
		public void setNumberFormat(NumberFormat newNumberFormat) {
			throw new UnsupportedOperationException("Cannot modify default format!");
		}

		private DateFormat threadFormat() {
			Formatter threadInstance = _service.getThreadInstance(_timeZone, _locale);
			return threadInstance.sharedDateFormatNonNull(_id);
		}
	}

}
