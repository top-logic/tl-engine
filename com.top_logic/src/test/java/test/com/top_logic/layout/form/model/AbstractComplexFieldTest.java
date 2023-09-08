/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import test.com.top_logic.layout.AbstractLayoutTest;

import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFactory;


/**
 * Setup for testing {@link ComplexField}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractComplexFieldTest extends AbstractLayoutTest {

	protected static final Date NOW;
	protected static final Date LATER;
	protected static final Date APPROXIMATELY_NOW;
	protected static final SimpleDateFormat DATE_TIME_FORMAT;

	protected static final SimpleDateFormat DATE_FORMAT;
	protected static final String NOW_AS_STRING;
	protected static final String LATER_AS_STRING;
	protected static final Date APPROXIMATELY_LATER;
	
	static {
		try {
			GregorianCalendar CALENDAR = new GregorianCalendar();
			CALENDAR.set(2006, Calendar.JUNE, 11, 16, 58, 13);
			
			NOW = CALENDAR.getTime();
			CALENDAR.add(Calendar.HOUR, 3);
			LATER = CALENDAR.getTime();

			DATE_TIME_FORMAT = CalendarUtil.newSimpleDateFormat("dd.MM.yy HH:mm");
			DATE_FORMAT = CalendarUtil.newSimpleDateFormat("dd.MM.yy");
			NOW_AS_STRING = DATE_TIME_FORMAT.format(NOW);
			LATER_AS_STRING = DATE_TIME_FORMAT.format(LATER);
			
			APPROXIMATELY_NOW = DATE_TIME_FORMAT.parse(NOW_AS_STRING);
			APPROXIMATELY_LATER = DATE_TIME_FORMAT.parse(LATER_AS_STRING);
		} catch (ParseException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	protected ComplexField field;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		
		field = FormFactory.newComplexField("name", DATE_TIME_FORMAT, NOW, false);
		field.setDefaultValue(LATER);
	}
	
	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
	    super.tearDown();
	    field = null; // Leave for GC
	}
}
