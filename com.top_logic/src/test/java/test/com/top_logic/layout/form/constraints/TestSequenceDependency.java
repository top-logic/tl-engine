/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.constraints;

import java.text.Format;
import java.util.Calendar;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.constraints.SequenceDependency;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;

/**
 * Tests the class {@link SequenceDependency}.
 * 
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class TestSequenceDependency extends BasicTestCase {
	
	private ComplexField startField;
	private ComplexField endField;
	private ComplexField middleField;

	/**
	 * Dates will be set during setup.
	 */
	/** 21.02.2009 */
	private Date date1;
	/** 21.03.2009 */
	private Date date2;
	/** 21.04.2009 */
	private Date date3;
	/** 21.05.2009 */
	private Date date4;
	/** 21.06.2009 */
	private Date date5;

    /**
	 * Returns the test suite
	 */
    public static Test suite () {
        TestSuite theSuite  = new TestSuite(TestSequenceDependency.class);
        return TLTestSetup.createTLTestSetup(theSuite);
    }
    
	public void testCorrectStartMiddleEnd() {
		update(startField,  date1);
		update(middleField, date3);
		update(endField,    date5);
		
		assertFalse(startField.hasError());
		assertFalse(middleField.hasError());
		assertFalse(endField.hasError());
	}

	public void testCorrectEndMiddleStart() {
		update(endField,    date5);
		update(middleField, date3);
		update(startField,  date1);
		
		assertFalse(startField.hasError());
		assertFalse(middleField.hasError());
		assertFalse(endField.hasError());
	}
	
    /**
     * The main method
     */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		final Calendar calendar = CalendarUtil.createCalendar();
		calendar.set(2009, Calendar.FEBRUARY, 21);
		date1 = calendar.getTime();
		calendar.set(2009, Calendar.MARCH, 21);
		date2 = calendar.getTime();
		calendar.set(2009, Calendar.APRIL, 21);
		date3 = calendar.getTime();
		calendar.set(2009, Calendar.MAY, 21);
		date4 = calendar.getTime();
		calendar.set(2009, Calendar.JUNE, 21);
		date5 = calendar.getTime();
		
		startField = FormFactory.newDateField("start", new Date(), false);
		Thread.sleep(1000);
		middleField = FormFactory.newDateField("middle", new Date(), false);
		Thread.sleep(1000);
		endField = FormFactory.newDateField("end", new Date(), false);
		
		final FormContext formContext = new FormContext("fc", ResPrefix.forTest("fc"));
		formContext.addMember(startField);
		formContext.addMember(middleField);
		formContext.addMember(endField);
		
		SequenceDependency sequenceDep = new SequenceDependency(new FormField[] {startField, middleField, endField} );
		sequenceDep.attach();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		startField = null;
		middleField = null;
		endField = null;

		date1 = null;
		date2 = null;
		date3 = null;
		date5 = null;
		date4 = null;
	}
	
	/**
	 * Simulates a client side update of the given field to the given date
	 */
	private void update(ComplexField field, Date date) {
		final Format startFormat = field.getFormat();
		try {
			field.update(startFormat.format(date));
		} catch (VetoException ex) {
			fail("No veto handler has been registered?", ex);
		}
	}
}
