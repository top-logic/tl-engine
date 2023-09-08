/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form;

import java.text.Format;
import java.util.Calendar;
import java.util.Date;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.constraints.ComparisonDependency;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormFieldInternals;

/**
 * The class {@link TestFormFieldDependencies} tests the updating of
 * {@link FormField form fields} which depend of one another.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestFormFieldDependencies extends BasicTestCase {

	private ComplexField startField;
	private ComplexField endField;

	private Date beforeStart;
	private Date start;
	private Date betweenStartAndEnd;
	private Date afterEnd;
	private Date end;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		final Calendar calendar = CalendarUtil.createCalendar();
		calendar.set(2009, Calendar.FEBRUARY, 21);
		beforeStart = calendar.getTime();
		calendar.add(Calendar.DAY_OF_YEAR, 37);
		start = calendar.getTime();
		calendar.add(Calendar.DAY_OF_YEAR, 37);
		betweenStartAndEnd = calendar.getTime();
		calendar.add(Calendar.DAY_OF_YEAR, 37);
		end = calendar.getTime();
		calendar.add(Calendar.DAY_OF_YEAR, 37);
		afterEnd = calendar.getTime();
		calendar.add(Calendar.DAY_OF_YEAR, 37);

		startField = FormFactory.newDateField("start", new Date(), false);
		endField = FormFactory.newDateField("end", new Date(), false);
		ComparisonDependency.buildStartEndDependency(startField, endField);
		final FormContext formContext = new FormContext("fc", ResPrefix.forTest("fc"));
		formContext.addMember(startField);
		formContext.addMember(endField);

		/*
		 * can not set date during construction since in initial state no error
		 * of dependent fields will be reported.
		 */
		reset();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		startField = null;
		endField = null;

		beforeStart = null;
		start = null;
		betweenStartAndEnd = null;
		end = null;
		afterEnd = null;
	}

	public void testStartAfterEnd() {
		update(startField, afterEnd);
		assertTrue(startField.hasError());
		assertTrue("Ticket #2099: dependant field must also have an error.", endField.hasError());
	}

	public void testEndBeforeStart() {
		update(endField, beforeStart);
		assertTrue("Ticket #2099: dependant field must also have an error.", startField.hasError());
		assertTrue(endField.hasError());
	}

	public void testRepairByBrokenField() {
		update(startField, afterEnd);
		assertTrue(startField.hasError());
		assertTrue("Ticket #2099: dependant field must also have an error.", endField.hasError());

		update(startField, beforeStart);
		assertFalse(startField.hasError());
		assertFalse(endField.hasError());

		reset();

		update(endField, beforeStart);
		assertTrue("Ticket #2099: dependant field must also have an error.", startField.hasError());
		assertTrue(endField.hasError());

		update(endField, afterEnd);
		assertFalse(startField.hasError());
		assertFalse(endField.hasError());
	}

	public void testRepairByCorrectField() {
		update(startField, end);
		assertTrue(startField.hasError());
		assertTrue("Ticket #2099: dependant field must also have an error.", endField.hasError());

		update(endField, afterEnd);
		assertFalse("Ticket #2099: field must fulfill constraint after updateing the field it depends on", startField.hasError());
		assertFalse(endField.hasError());

		reset();

		update(endField, start);
		assertTrue("Ticket #2099: dependant field must also have an error.", startField.hasError());
		assertTrue(endField.hasError());

		update(startField, beforeStart);
		assertFalse(startField.hasError());
		assertFalse("Ticket #2099: field must fulfill constraint after updateing the field it depends on", endField.hasError());
	}

	public void testRepairByDeliver() throws CheckException, VetoException {
		update(startField, end);
		assertTrue(startField.hasError());
		assertTrue("Ticket #2099: dependant field must also have an error.", endField.hasError());

		FormFieldInternals.updateField(startField, startField.getFormat().format(beforeStart));
		assertFalse(startField.hasError());
		assertFalse(endField.hasError());

		reset();

		update(startField, end);
		assertTrue(startField.hasError());
		assertTrue("Ticket #2099: dependant field must also have an error.", endField.hasError());

		FormFieldInternals.updateField(endField, endField.getFormat().format(afterEnd));
		assertFalse("Ticket #2099: field must fulfill constraint after updateing the field it depends on", startField.hasError());
		assertFalse(endField.hasError());
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

	/**
	 * Sets the fields to the default values
	 */
	private void reset() {
		update(startField, start);
		update(endField, end);
		assertFalse(startField.hasError());
		assertFalse(endField.hasError());
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestFormFieldDependencies.class);
	}

}
