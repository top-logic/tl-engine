/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.format.configured;

import static com.top_logic.basic.StringServices.*;
import static com.top_logic.basic.util.RegExpUtil.*;

import java.text.DateFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.format.FormatConfig;
import com.top_logic.basic.format.FormatDefinition;
import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.basic.format.configured.FormatterService;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.ModuleUtil.ModuleContext;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.basic.util.ComputationEx;

/**
 * Test the {@link Formatter}.
 *
 * @author Ulrich Frank
 */
@SuppressWarnings("javadoc")
public class TestFormatter extends BasicTestCase {

	private TimeZone _timeZone;

	/** Default CTor, create Test-case by calling given function name. */
	public TestFormatter(String name) {
	    super (name);
	}
    
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_timeZone = getTimeZoneBerlin();
	}

	/** Test the German (default) format */
	public void testGermanFormatNumbers() {
		Formatter fm = getHTMLFormatterGerman();
        
        assertNotNull(fm.toString());
        
		assertEquals(Long.valueOf(1234), fm.parseNumber("1.234"));
		assertEquals(Double.valueOf(1234.56), fm.parseNumber("1.234,56"));
        assertNull  (fm.parseNumber(null));
        assertNull  (fm.parseNumber(""));
        assertNull  (fm.parseNumber("Das iss avver Übel"));

		assertEquals("1.234", fm.formatNumber(Integer.valueOf(1234)));
		assertEquals("1.234,56", fm.formatNumber(Double.valueOf(1234.56)));
        
		assertEquals(Double.valueOf(1234), fm.parseDouble("1.234"));
		assertEquals(Double.valueOf(1234.56), fm.parseDouble("1.234,56"));
        assertNull  (                         fm.parseDouble(null));

        assertEquals(1234.0                 , fm.parseDoubleValue("1.234")    , EPSILON);
        assertEquals(1234.56                , fm.parseDoubleValue("1.234,56") , EPSILON);
        assertNull  (                         fm.parseDouble(""));

        assertEquals("1.234,00"             , fm.formatDouble(1234.0));
        assertEquals("1.234,56"             , fm.formatDouble(1234.56));

		assertEquals(Integer.valueOf(1234), fm.parseInteger("1.234"));
		assertEquals(Integer.valueOf(1234), fm.parseInteger("1.234,56"));
        assertNull  (                         fm.parseInteger(""));

        assertEquals(1234                   , fm.parseIntValue("1.234"));
        assertEquals(1234                   , fm.parseIntValue("1.234,56"));

        assertEquals("1.234"                , fm.formatInt(1234));
        assertEquals("123.456"              , fm.formatInt(123456));

		assertEquals("99%", normalizePercent(fm.formatPercent(0.99)));
		assertEquals("1%", normalizePercent(fm.formatPercent(0.011)));
        
	}

	private String normalizePercent(String percentValue) {
		return percentValue.replace("\u00A0", "");
	}

	public void testGermanFormatDates() {
		Locale locale = Locale.GERMAN;
		TimeZone timeZoneBerlin = getTimeZoneBerlin();
		Formatter fm = FormatterService.getFormatter(timeZoneBerlin, locale);
		Calendar calendarBerlin = CalendarUtil.createCalendar(timeZoneBerlin, locale);
		Calendar calendarSystem = CalendarUtil.createCalendar(TimeZones.systemTimeZone(), locale);

		Date d = DateUtil.createDate(calendarSystem, 1967, Calendar.FEBRUARY, 6, 0, 0, 0);
        assertEquals("06.02.1967"           , fm.formatDate(d));
        assertEquals(d                      , fm.parseDate("06.02.1967"));
        
        assertEquals("06.02.67"             , fm.formatShortDate(d));
        assertEquals(d                      , fm.parseShortDate("06.02.67"));
        assertEquals(d                      , fm.parseShortDate("06.02.1967"));
        
		Date newMillenium = DateUtil.createDate(calendarSystem, 2007, Calendar.AUGUST, 5, 0, 0, 0);
        assertEquals("05.08.07"             , fm.formatShortDate(newMillenium));
        assertEquals(newMillenium           , fm.parseShortDate("5.8.07"));
        assertEquals(newMillenium           , fm.parseShortDate("5.8.07"));
        
		Date future = DateUtil.createDate(calendarSystem, 2067, Calendar.AUGUST, 5, 0, 0, 0);
        // Fails:
        // assertEquals(future                 , fm.parseShortDate("5.8.67"));
        // assertEquals(future                 , fm.parseShortDate("5.8.067"));
        assertEquals(future                 , fm.parseShortDate("5.8.2067"));

		Date t = DateUtil.createDate(calendarBerlin, 1970, Calendar.JANUARY, 1, 11, 17, 36);
        assertEquals("11:17:36"             , fm.formatTime(t));
        assertEquals(t                      , fm.parseTime("11:17:36"));

		Date st = DateUtil.createDate(calendarBerlin, 1970, Calendar.JANUARY, 1, 11, 17, 0);
        assertEquals("11:17"                , fm.formatShortTime(st));
        assertEquals(st                     , fm.parseShortTime("11:17"));

		Date dt = DateUtil.createDate(calendarBerlin, 2002, Calendar.JUNE, 2, 11, 17, 36);
		assertEquals("02.06.2002, 11:17:36", fm.formatDateTime(dt));
		assertEquals(dt, fm.parseDateTime("02.06.2002, 11:17:36"));

		Date sdt = DateUtil.createDate(calendarBerlin, 2002, Calendar.JUNE, 2, 11, 17, 00);
		assertEquals("02.06.02, 11:17", fm.formatShortDateTime(sdt));
		assertEquals(sdt, fm.parseShortDateTime("02.06.02, 11:17"));

		assertEquals("02.06.2002, 11:17", fm.formatMediumDateTime(sdt));
		assertEquals(sdt, fm.parseMixedDateTime("02.06.2002, 11:17"));
	}
    
	public void testInvalidDate() {
		Formatter fm = getHTMLFormatterGerman();
        
		try {
			fm.getDateFormat().parse("0.0.0");
			fail("Expected parse exception for invalid date.");
		} catch (ParseException ex) {
			// Expected.
		}
    }

	public void testInvalidDate2() {
		Formatter fm = getHTMLFormatterGerman();
    	
		try {
			fm.getDateFormat().parse("32.5.2010");
			fail("Expected parse exception for invalid date.");
		} catch (ParseException ex) {
			// Expected.
		}
    }
    
    public void testValidDate() throws Exception {
		Formatter fm = getHTMLFormatterGerman();
    	
    	Date date = fm.getDateFormat().parse("31.5.2010");
    	
    	String output = fm.formatDate(date);
    	assertEquals("31.05.2010", output);
    }
    
    /** Test the US Locale based formats */
	public void testUSFormatNumbers() {
		Formatter fm = getHTMLFormatterUS();
		assertEquals(Long.valueOf(1234), fm.parseNumber("1,234"));
		assertEquals(Double.valueOf(1234.56), fm.parseNumber("1,234.56"));
        assertNull  (fm.parseNumber(null));
        assertNull  (fm.parseNumber(""));
        assertNull  (fm.parseNumber("Das iss avver Übel"));
        
        assertNotNull(fm.toString());
        
		assertEquals("1,234", fm.formatNumber(Integer.valueOf(1234)));
		assertEquals("1,234.56", fm.formatNumber(Double.valueOf(1234.56)));
    
		assertEquals(Double.valueOf(1234), fm.parseDouble("1,234"));
		assertEquals(Double.valueOf(1234.56), fm.parseDouble("1,234.56"));

        assertEquals(1234.0                 , fm.parseDoubleValue("1,234")    , 0.00001);
        assertEquals(1234.56                , fm.parseDoubleValue("1,234.56") , 0.00001);

        assertEquals("1,234.00"             , fm.formatDouble(1234.0));
        assertEquals("1,234.56"             , fm.formatDouble(1234.56));

		assertEquals(Integer.valueOf(1234), fm.parseInteger("1,234"));
		assertEquals(Integer.valueOf(1234), fm.parseInteger("1,234.56"));

        assertEquals(1234                   , fm.parseIntValue("1,234"));
        assertEquals(1234                   , fm.parseIntValue("1,234.56"));

        assertEquals("1,234"                , fm.formatInt(1234));
        assertEquals("123,456"              , fm.formatInt(123456));
	}

	public void testUSFormatDates() {
		Locale locale = Locale.US;
		TimeZone timeZoneLA = getTimeZoneLosAngeles();
		Formatter fm = FormatterService.getFormatter(timeZoneLA, locale);
		Calendar calendarLA = CalendarUtil.createCalendar(timeZoneLA, locale);
		Calendar calendarSystem = CalendarUtil.createCalendar(TimeZones.systemTimeZone(), locale);

		Date d = DateUtil.createDate(calendarSystem, 1967, Calendar.FEBRUARY, 6, 0, 0, 0);
        assertEquals("Feb 6, 1967"          , fm.formatDate(d));
        assertEquals(d                      , fm.parseDate("Feb 6, 1967"));

        assertEquals("2/6/67"               , fm.formatShortDate(d));
        assertEquals(d                      , fm.parseShortDate("2/6/67"));

		Date t = DateUtil.createDate(calendarLA, 1970, Calendar.JANUARY, 1, 11, 17, 36);
		// Replace all kinds of whitespaces with a blank.
		// Different Java versions use different whitespaces as separator.
		String formattedTime = fm.formatTime(t);
		boolean useNarrowNoBreakSpace = formattedTime.indexOf(NARROW_NO_BREAK_SPACE) != -1;
		char separator = useNarrowNoBreakSpace ? NARROW_NO_BREAK_SPACE : ' ';
		assertEquals("11:17:36 AM", normalizeWhitespace(formattedTime));
		assertEquals(t, fm.parseTime("11:17:36" + separator + "AM"));

		Date st = DateUtil.createDate(calendarLA, 1970, Calendar.JANUARY, 1, 11, 17, 0);
		assertEquals("11:17 AM", normalizeWhitespace(fm.formatShortTime(st)));
		assertEquals(st, fm.parseShortTime("11:17" + separator + "AM"));

		Date dt = DateUtil.createDate(calendarLA, 2002, Calendar.JUNE, 2, 11, 17, 36);
		assertEquals("Jun 2, 2002, 11:17:36 AM", normalizeWhitespace(fm.formatDateTime(dt)));
		assertEquals(dt, fm.parseDateTime("Jun 2, 2002, 11:17:36" + separator + "AM"));


		Date sdt = DateUtil.createDate(calendarLA, 2002, Calendar.JUNE, 2, 11, 17, 00);
		assertEquals("6/2/02, 11:17 AM", normalizeWhitespace(fm.formatShortDateTime(sdt)));
		assertEquals(sdt, fm.parseShortDateTime("6/2/02, 11:17" + separator + "AM"));

		assertEquals("Jun 2, 2002, 11:17 AM", normalizeWhitespace(fm.formatMediumDateTime(sdt)));
		assertEquals(sdt, fm.parseMixedDateTime("Jun 2, 2002, 11:17" + separator + "AM"));
	}

	public void testFormatParseDecimal() throws ParseException {
		FormatDefinition<?> def = getFormatDefinition("testDecimal");
		assertNotNull(def);
		Format format = def.newFormat(globalConfig(), _timeZone, Locale.GERMANY);
		double value = 1123.45456D;
		assertEquals("1.123,45", format.format(value));
		assertEquals(1123.45D, format.parseObject("1123,45456"));
	}

	public void testNormalize() throws ParseException {
		FormatDefinition<?> decimalDef = getFormatDefinition("testRawDecimal");
		assertNotNull(decimalDef);
		FormatDefinition<?> normalizeDef = getFormatDefinition("testDecimal");
		assertNotNull(normalizeDef);

		assertEquals(1123123.45D,
			decimalDef.newFormat(globalConfig(), _timeZone, Locale.GERMANY).parseObject("1.123.123,45"));
		assertEquals(1123123.456D,
			decimalDef.newFormat(globalConfig(), _timeZone, Locale.GERMANY).parseObject("1.123.123,456"));
		assertEquals(1123123.45D,
			decimalDef.newFormat(globalConfig(), _timeZone, Locale.UK).parseObject("1,123,123.45"));
		assertEquals(1123123.456D,
			decimalDef.newFormat(globalConfig(), _timeZone, Locale.UK).parseObject("1,123,123.456"));

		assertEquals(1123123.45D,
			normalizeDef.newFormat(globalConfig(), _timeZone, Locale.GERMANY).parseObject("1.123.123,45"));
		assertEquals(1123123.46D,
			normalizeDef.newFormat(globalConfig(), _timeZone, Locale.GERMANY).parseObject("1.123.123,456"));
		assertEquals(1123123.45D,
			normalizeDef.newFormat(globalConfig(), _timeZone, Locale.UK).parseObject("1,123,123.45"));
		assertEquals(1123123.46D,
			normalizeDef.newFormat(globalConfig(), _timeZone, Locale.UK).parseObject("1,123,123.456"));

		try {
			Object result = normalizeDef.newFormat(globalConfig(), _timeZone, Locale.UK).parseObject("foobar");
			fail("Error expected, but got: " + result);
		} catch (ParseException ex) {
			// Expected.
		}
	}

	public void testDecimal() {
		FormatDefinition<?> def = getFormatDefinition("testDecimal");
		assertNotNull(def);
		double value = 1123123.45D;
		assertEquals("1.123.123,45", def.newFormat(globalConfig(), _timeZone, Locale.GERMANY).format(value));
		assertEquals("1,123,123.45", def.newFormat(globalConfig(), _timeZone, Locale.UK).format(value));
	}

	public void testResultType() throws ParseException {
		FormatDefinition<?> defaultDef = getFormatDefinition("testDecimal");
		assertNotNull(defaultDef);
		FormatDefinition<?> doubleDef = getFormatDefinition("testDouble");
		assertNotNull(doubleDef);
		FormatDefinition<?> longDef = getFormatDefinition("testLong");
		assertNotNull(longDef);

		assertEquals(1234L, defaultDef.newFormat(globalConfig(), _timeZone, Locale.GERMANY).parseObject("1.234"));
		assertEquals(1234.5D, defaultDef.newFormat(globalConfig(), _timeZone, Locale.GERMANY).parseObject("1.234,5"));

		assertEquals(1234D, doubleDef.newFormat(globalConfig(), _timeZone, Locale.GERMANY).parseObject("1.234"));
		assertEquals(1234.5D, doubleDef.newFormat(globalConfig(), _timeZone, Locale.GERMANY).parseObject("1.234,5"));

		assertEquals(1234L, longDef.newFormat(globalConfig(), _timeZone, Locale.GERMANY).parseObject("1.234"));
		try {
			Object result = longDef.newFormat(globalConfig(), _timeZone, Locale.GERMANY).parseObject("1.234,5");
			fail("Expected failure, but got: " + result);
		} catch (ParseException ex) {
			// Expected.
		}
	}

	public void testFormatLong() {
		FormatDefinition<?> def = getFormatDefinition("testLong");
		assertNotNull(def);

		assertEquals("1.123.123", def.newFormat(globalConfig(), _timeZone, Locale.GERMANY).format(1123123));
		assertEquals("1.123.123", def.newFormat(globalConfig(), _timeZone, Locale.GERMANY).format(1123123.4));
		assertEquals("1.123.124", def.newFormat(globalConfig(), _timeZone, Locale.GERMANY).format(1123123.5));

		assertEquals("1,123,123", def.newFormat(globalConfig(), _timeZone, Locale.UK).format(1123123));
		assertEquals("1,123,123", def.newFormat(globalConfig(), _timeZone, Locale.UK).format(1123123.4));
		assertEquals("1,123,124", def.newFormat(globalConfig(), _timeZone, Locale.UK).format(1123123.5));
	}

	public void testParseInvalidNativeLong() {
		FormatDefinition<?> def = getFormatDefinition("long");
		assertNotNull(def);

		try {
			Object result =
				def.newFormat(globalConfig(), _timeZone, Locale.GERMANY).parseObject("999999999999999999999999999999");
			fail("Expected parse error, but got: " + result);
		} catch (ParseException ex) {
			// Expected.
		}
	}

	public void testFormatDouble() {
		FormatDefinition<?> def = getFormatDefinition("testDouble");
		assertNotNull(def);

		assertEquals("1.123.123,00", def.newFormat(globalConfig(), _timeZone, Locale.GERMANY).format(1123123));
		assertEquals("1.123.123,40", def.newFormat(globalConfig(), _timeZone, Locale.GERMANY).format(1123123.40));
		assertEquals("1.123.123,45", def.newFormat(globalConfig(), _timeZone, Locale.GERMANY).format(1123123.445));

		assertEquals("1,123,123.00", def.newFormat(globalConfig(), _timeZone, Locale.UK).format(1123123));
		assertEquals("1,123,123.40", def.newFormat(globalConfig(), _timeZone, Locale.UK).format(1123123.4));
		assertEquals("1,123,123.45", def.newFormat(globalConfig(), _timeZone, Locale.UK).format(1123123.445));
	}

	public void testNoTimeZoneButLocale() throws ParseException, IllegalArgumentException, ModuleException {
		try (ModuleContext moduleContext = ModuleUtil.beginContext()) {
			ModuleUtil.INSTANCE.startModule(TimeZones.class);
			executeInSystemTimeZone(TimeZones.UTC, new ComputationEx<Void, ParseException>() {

				@Override
				public Void run() throws ParseException {
					FormatDefinition<?> def = getFormatDefinition("testNoTimeZoneButLocale");
					assertNotNull(def);
					SimpleDateFormat format = CalendarUtil.newSimpleDateFormat("dd.MM.yyyy HH:mm");
					format.setTimeZone(TimeZones.UTC);
					Date value = format.parse("02.01.1999 17:12");

					// 02.01.1999 is in the 53. week of 1998 in Germany
					assertEquals("User time zone is ignored in format, but not the locale.", "1998 53 02.01.1999 17:12",
						def.newFormat(globalConfig(), getTimeZone(12), Locale.GERMANY).format(value));
					assertEquals("User time zone is ignored in format, but not the locale.", value,
						def.newFormat(globalConfig(), getTimeZone(12), Locale.GERMANY)
						.parseObject("1998 53 02.01.1999 17:12"));
					// 02.01.1999 is in the 1. week of 1999 in US
					assertEquals("User time zone is ignored in format, but not the locale.", "1999 01 02.01.1999 17:12",
						def.newFormat(globalConfig(), getTimeZone(-12), Locale.US).format(value));
					assertEquals("User time zone is ignored in format, but not the locale.", value,
						def.newFormat(globalConfig(), getTimeZone(-12), Locale.US)
							.parseObject("1999 01 02.01.1999 17:12"));
					return null;
				}
			});
		}
	}

	public void testDate() throws ParseException {
		FormatDefinition<?> def = getFormatDefinition("testDate");
		assertNotNull(def);
		SimpleDateFormat format = CalendarUtil.newSimpleDateFormat("dd.MM.yyyy HH'h'");
		format.setTimeZone(TimeZones.systemTimeZone());
		Date value = format.parse("02.01.1998 17h");

		assertEquals("User time zone is ignored in format.", "02.01.1998 17h",
			def.newFormat(globalConfig(), getTimeZone(12), Locale.GERMANY).format(value));
		assertEquals("User time zone is ignored in format.", value,
			def.newFormat(globalConfig(), getTimeZone(12), Locale.GERMANY).parseObject("02.01.1998 17h"));
		assertEquals("User time zone is ignored in format.", "02.01.1998 17h",
			def.newFormat(globalConfig(), getTimeZone(-12), Locale.US).format(value));
		assertEquals("User time zone is ignored in format.", value,
			def.newFormat(globalConfig(), getTimeZone(-12), Locale.US).parseObject("02.01.1998 17h"));
	}

	public void testDateUserTime() throws ParseException {
		FormatDefinition<?> def = getFormatDefinition("testDateUserTime");
		assertNotNull(def);
		SimpleDateFormat format = CalendarUtil.newSimpleDateFormat("dd.MM.yyyy HH'h'");
		format.setTimeZone(TimeZones.UTC);
		Date value = format.parse("02.01.1998 17h");

		assertEquals("User time zone is used in format.", "03.01.1998 05h",
			def.newFormat(globalConfig(), getTimeZone(12), Locale.GERMANY).format(value));
		assertEquals("User time zone is used in format.", value,
			def.newFormat(globalConfig(), getTimeZone(12), Locale.GERMANY).parseObject("03.01.1998 05h"));
		assertEquals("User time zone is used in format.", "02.01.1998 05h",
			def.newFormat(globalConfig(), getTimeZone(-12), Locale.US).format(value));
		assertEquals("User time zone is used in format.", value,
			def.newFormat(globalConfig(), getTimeZone(-12), Locale.US).parseObject("02.01.1998 05h"));
	}

	public void testDateTime() throws ParseException {
		FormatDefinition<?> def = getFormatDefinition("testDateTime");
		assertNotNull(def);
		SimpleDateFormat format = CalendarUtil.newSimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		format.setTimeZone(TimeZones.UTC);
		Date value = format.parse("15.07.2014 17:42:56");

		assertEquals("16.07.2014, 05:42:56",
			def.newFormat(globalConfig(), getTimeZone(12), Locale.GERMANY).format(value));
		assertEquals(value,
			def.newFormat(globalConfig(), getTimeZone(12), Locale.GERMANY).parseObject("16.07.2014, 05:42:56"));
		assertEquals("15.07.2014, 05:42:56",
			def.newFormat(globalConfig(), getTimeZone(-12), Locale.GERMANY).format(value));
		assertEquals(value,
			def.newFormat(globalConfig(), getTimeZone(-12), Locale.GERMANY).parseObject("15.07.2014, 05:42:56"));
	}

	public void testCurrency() {
		FormatDefinition<?> def = getFormatDefinition("testCurrency");
		assertNotNull(def);
		double value = 1123123.45D;
		assertEquals("1.123.123,45 \u20AC",
			normalizeUnit(def.newFormat(globalConfig(), _timeZone, Locale.GERMANY).format(value)));
		assertEquals("$1,123,123.45", def.newFormat(globalConfig(), _timeZone, Locale.US).format(value));
	}

	private String normalizeUnit(String format) {
		return format.replace('\u00A0', ' ');
	}

	public void testPercent() {
		FormatDefinition<?> def = getFormatDefinition("testPercent");
		assertNotNull(def);
		double value = 10.45D;
		assertEquals("1.045%",
			normalizePercent(def.newFormat(globalConfig(), _timeZone, Locale.GERMANY).format(value)));
		assertEquals("1,045%", normalizePercent(def.newFormat(globalConfig(), _timeZone, Locale.UK).format(value)));
	}

	public void testChoice() {
		FormatDefinition<?> def = getFormatDefinition("testGeneric");
		assertNotNull(def);
		Format format = def.newFormat(globalConfig(), _timeZone, Locale.GERMANY);
		assertEquals("is more than 2.", format.format(2.1D));
		assertEquals("is 1+", format.format(1.9D));
	}

	public void testModifyDefaultNumberFormatCopy() {
		int expectedDigits = 4;
		Formatter htmlFormatter = getHTMLFormatterGerman();
		NumberFormat numberFormatCopy = (NumberFormat) htmlFormatter.getNumberFormat().clone();
		numberFormatCopy.setMinimumFractionDigits(expectedDigits);

		assertEquals("Copy of default format must be modifiable!", expectedDigits,
			numberFormatCopy.getMinimumFractionDigits());
	}

	public void testModifyDefaultDateFormatCopy() {
		boolean expected = true;
		Formatter htmlFormatter = getHTMLFormatterGerman();
		DateFormat dateFormatCopy = (DateFormat) htmlFormatter.getDateFormat().clone();
		dateFormatCopy.setLenient(expected);

		assertEquals("Copy of default format must be modifiable!", expected,
			dateFormatCopy.isLenient());
	}

	public void testFailModifyDefaultNumberFormat() {
		Formatter htmlFormatter = getHTMLFormatterGerman();
		try {
			htmlFormatter.getNumberFormat().setMinimumFractionDigits(4);
			fail("Must not allow modification of default format!");
		} catch (UnsupportedOperationException ex) {
			assertEquals("Cannot modify default format!", ex.getMessage());
		}
	}

	public void testFailModifyDefaultDateFormat() {
		Formatter htmlFormatter = getHTMLFormatterGerman();
		try {
			htmlFormatter.getDateFormat().setLenient(true);
			fail("Must not allow modification of default format!");
		} catch (UnsupportedOperationException ex) {
			assertEquals("Cannot modify default format!", ex.getMessage());
		}
	}

	FormatDefinition<?> getFormatDefinition(String id) {
		return FormatterService.getInstance().getFormatDefinition(id);
	}

	FormatConfig globalConfig() {
		return TypedConfiguration.newConfigItem(FormatConfig.class);
	}

    /**
     * Provoke problems by using the Functions in multiple Threads.
     */
    public void testGermanMultiThreaded() throws Exception {
        parallelTest(3,new ExecutionFactory() {
            @Override
			public Execution createExecution(int aId) {
				return () -> {
					testGermanFormatNumbers();
					testGermanFormatDates();
				};
            }
        });
    }

    /**
     * Provoke problems by using the Functions in multiple Threads.
     */
    public void testUSMultiThreaded() throws Exception {
        parallelTest(3,new ExecutionFactory() {
            @Override
			public Execution createExecution(int aId) {
				return () -> {
					testUSFormatNumbers();
					testUSFormatDates();
				};
            }
        });
    }

	protected Formatter getHTMLFormatterGerman() {
		return FormatterService.getFormatter(Locale.GERMAN);
	}

	protected Formatter getHTMLFormatterUS() {
		return FormatterService.getFormatter(Locale.US);
	}

	/**
	 * the suite of Tests to execute 
	 */
	static public Test suite () {
		return BasicTestSetup.createBasicTestSetup(
			ServiceTestSetup.createSetup(TestFormatter.class, FormatterService.Module.INSTANCE));
	}

}
