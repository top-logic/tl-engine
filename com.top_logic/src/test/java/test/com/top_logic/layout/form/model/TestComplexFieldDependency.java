/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.model;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.Test;

import com.top_logic.layout.VetoException;
import com.top_logic.layout.form.constraints.ComparisonDependency;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFactory;

/**
 * Test case for a {@link ComplexField} with associated dependencies.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestComplexFieldDependency extends AbstractComplexFieldTest {
	private static final String GARBAGE = "garbage";
	private static final Date TO_EARLY;
	private static final Date IN_TIME;
	private static final Date TO_LATE;

	private static final Date APPROXIMATELY_TO_EARLY;
	private static final Date APPROXIMATELY_IN_TIME;
	private static final Date APPROXIMATELY_TO_LATE;

	private static final String TO_EARLY_AS_STRING;
	private static final String IN_TIME_AS_STRING;
	private static final String TO_LATE_AS_STRING;
	
	static {
		GregorianCalendar CALENDAR = new GregorianCalendar();
		CALENDAR.setTime(NOW);
		CALENDAR.add(Calendar.HOUR, 1);
		IN_TIME = CALENDAR.getTime();

		CALENDAR.setTime(NOW);
		CALENDAR.add(Calendar.HOUR, -3);
		TO_EARLY = CALENDAR.getTime();

		CALENDAR.setTime(LATER);
		CALENDAR.add(Calendar.HOUR, 3);
		TO_LATE = CALENDAR.getTime();
		
		TO_EARLY_AS_STRING = DATE_TIME_FORMAT.format(TO_EARLY);
		IN_TIME_AS_STRING = DATE_TIME_FORMAT.format(IN_TIME);
		TO_LATE_AS_STRING = DATE_TIME_FORMAT.format(TO_LATE);
		
		try {
			APPROXIMATELY_TO_EARLY = DATE_TIME_FORMAT.parse(TO_EARLY_AS_STRING);
			APPROXIMATELY_IN_TIME = DATE_TIME_FORMAT.parse(IN_TIME_AS_STRING);
			APPROXIMATELY_TO_LATE = DATE_TIME_FORMAT.parse(TO_LATE_AS_STRING);
		} catch (ParseException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	protected ComplexField lowerBound;
	protected ComplexField upperBound;
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		
		// Create bound fields with empty input.
		lowerBound = FormFactory.newComplexField("lower", DATE_TIME_FORMAT, false);
		lowerBound.setDefaultValue(NOW);
		
		upperBound = FormFactory.newComplexField("upper", DATE_TIME_FORMAT, false);
		upperBound.setDefaultValue(LATER);
		
		// Add mutual dependencies.
		field.addConstraint(new ComparisonDependency(ComparisonDependency.LOWER_OR_EQUALS_TYPE, upperBound));
		field.addConstraint(new ComparisonDependency(ComparisonDependency.GREATER_OR_EQUALS_TYPE, lowerBound));
		
		upperBound.addConstraint(new ComparisonDependency(ComparisonDependency.GREATER_OR_EQUALS_TYPE, field));
		lowerBound.addConstraint(new ComparisonDependency(ComparisonDependency.LOWER_OR_EQUALS_TYPE, field));
	}

	public void testInitialization() {
		// The initial value is ok. It was set before adding the constraints.
		assertFalse(field.hasError());
		assertTrue(field.hasValue());
		
		assertEquals(NOW, field.getValue());
	}

	public void testSetValue() {
		assertEquals(NOW, field.getValue());
		
		resetBounds();
		
		field.setValue(TO_EARLY);
		
		// Programatically set values bring the field to its initial state,
		// where no errors are shown. This allows to pre-initialize a GUI with
		// not totally consistent values and show hints only as reaction to user
		// input.
		assertFalse(field.hasError());
		assertTrue(field.hasValue());
		assertEquals(TO_EARLY, field.getValue());
		
		field.setValue(TO_LATE);
		assertFalse(field.hasError());
		assertTrue(field.hasValue());
		assertEquals(TO_LATE, field.getValue());
	}

	private void resetBounds() {
		// Initialize the lower and upper bounds to their default values.
		lowerBound.reset();
		upperBound.reset();
		
		assertEquals(NOW, lowerBound.getValue());
		assertEquals(LATER, upperBound.getValue());
	}
	
	public void testSuccessfulUpdate() throws VetoException {
		assertEquals(NOW, field.getValue());

		resetBounds();

		field.update(IN_TIME_AS_STRING);

		assertFalse(field.hasError());
		
		field.check();

		assertFalse(field.hasError());
		assertTrue(field.hasValue());
		
		assertEquals(APPROXIMATELY_IN_TIME, field.getValue());
	}

	public void testConstraintError() throws VetoException {
		assertEquals(NOW, field.getValue());

		resetBounds();
		
		field.update(TO_LATE_AS_STRING);
		field.check();

		// The field has a value (because parsing of the user input succeeded),
		// but it also has an error, because the new value violates a
		// constraint.
		assertTrue(field.hasError());
		assertTrue(field.hasValue());
		
		assertEquals(APPROXIMATELY_TO_LATE, field.getValue());
		
		// Since the check was performed only locally, The dependencies are
		// still valid.
		assertFalse(upperBound.hasError());
		assertTrue(upperBound.hasValue());
		assertFalse(lowerBound.hasError());
		assertTrue(lowerBound.hasValue());
	}

	public void testDeferedCheck() throws VetoException {
		assertEquals(NOW, field.getValue());
		
		inputGarbageToBounds();

		field.update(IN_TIME_AS_STRING);

		field.check();
		
		assertFalse(field.hasError());
		assertTrue(field.hasValue());
		
		// All dependencies could not be checked. Therefore, the field is not
		// yet valid, but also has no errors.
		assertFalse(field.isValid());
		
		inputValueToBound(upperBound, LATER_AS_STRING);

		// The field is still not valid, because it has still a defered dependency to its lower bound.
		assertFalse(field.isValid());
		
		inputValueToBound(lowerBound, NOW_AS_STRING);
		
		// The field is still not valid, because it has still a defered dependency to its lower bound.
		assertTrue(field.isValid());
	}

	private void inputValueToBound(ComplexField bound, String value) throws VetoException {
		bound.update(value);
		bound.checkWithAllDependencies();
		
		assertFalse(bound.hasError());
		assertTrue(bound.hasValue());
		
		// All dependencies on the bound could be checked.
		assertTrue(bound.isValid());
	}

	private void inputGarbageToBounds() throws VetoException {
		lowerBound.update(GARBAGE);
		lowerBound.check();

		assertTrue(lowerBound.hasError());
		assertFalse(lowerBound.hasValue());
		
		upperBound.update(GARBAGE);
		upperBound.check();

		assertTrue(upperBound.hasError());
		assertFalse(upperBound.hasValue());
	}

	public void testFailingBoundCheck() throws VetoException {
		assertEquals(NOW, field.getValue());
		
		// Simulate a user input in the bound fields, because dependencies are
		// only checked if the user has already touched their values.
		lowerBound.update(NOW_AS_STRING);
		upperBound.update(LATER_AS_STRING);
		
		field.update(IN_TIME_AS_STRING);

		assertFalse(upperBound.hasError());
		upperBound.checkWithAllDependencies();

		assertFalse(lowerBound.hasError());
		lowerBound.checkWithAllDependencies();

		assertFalse(field.hasError());
		field.checkWithAllDependencies();

		assertTrue(upperBound.isValid());
		assertTrue(lowerBound.isValid());
		assertTrue(field.isValid());

		// Enter a value that is to late.
		field.update(TO_LATE_AS_STRING);
		field.checkWithAllDependencies();

		assertTrue(upperBound.hasError());
		assertTrue(lowerBound.isValid());
		assertTrue(field.hasError());

		assertTrue(field.hasValue());
		assertEquals(field.getValue(), APPROXIMATELY_TO_LATE);
		
		// Enter a value that is to early.
		field.update(TO_EARLY_AS_STRING);
		field.checkWithAllDependencies();

		assertTrue(upperBound.isValid());
		assertTrue(lowerBound.hasError());
		assertTrue(field.hasError());

		assertTrue(field.hasValue());
		assertEquals(field.getValue(), APPROXIMATELY_TO_EARLY);

		// Enter a value that is in time.
		field.update(IN_TIME_AS_STRING);
		field.checkWithAllDependencies();

		assertTrue(upperBound.isValid());
		assertTrue(lowerBound.isValid());
		assertTrue(field.isValid());
		
		assertEquals(lowerBound.getValue(), APPROXIMATELY_NOW);
		assertEquals(field.getValue(), APPROXIMATELY_IN_TIME);
		assertEquals(upperBound.getValue(), APPROXIMATELY_LATER);
	}

    public static Test suite () {
		return suite(TestComplexFieldDependency.class);
    }
	
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
