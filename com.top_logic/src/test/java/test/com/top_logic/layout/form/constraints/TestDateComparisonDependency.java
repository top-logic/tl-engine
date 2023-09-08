/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.constraints;

import java.util.Calendar;

import junit.framework.Test;

import test.com.top_logic.layout.AbstractLayoutTest;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.layout.form.constraints.ComparisonDependency;
import com.top_logic.layout.form.constraints.DateComparisonDependency;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFactory;

/**
 * Tests {@link DateComparisonDependency}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestDateComparisonDependency extends AbstractLayoutTest {

	public void testGranularityMillisecond() {
		Calendar calendar = newCalendar();
		ComplexField datefield1 = FormFactory.newDateField("field1", calendar.getTime(), false);
		ComplexField datefield2 = FormFactory.newDateField("field2", calendar.getTime(), false);
		datefield1.addConstraint(new DateComparisonDependency(DateComparisonDependency.GRANULARITY_TYPE_MILLISECOND,
			ComparisonDependency.GREATER_OR_EQUALS_TYPE, datefield2));
		datefield1.check();
		assertFalse(datefield1.hasError());
		calendar.add(Calendar.MILLISECOND, -1);
		datefield1.setValue(calendar.getTime());
		datefield1.check();
		assertTrue(datefield1.hasError());
	}

	private Calendar newCalendar() {
		Calendar instance = CalendarUtil.createCalendar();
		DateUtil.adjustDateToYearEnd(instance);
		return instance;
	}

	public void testGranularityDay() {
		Calendar calendar = newCalendar();
		ComplexField datefield1 = FormFactory.newDateField("field1", calendar.getTime(), false);
		ComplexField datefield2 = FormFactory.newDateField("field2", calendar.getTime(), false);
		datefield1.addConstraint(new DateComparisonDependency(DateComparisonDependency.GRANULARITY_TYPE_DAY,
			ComparisonDependency.GREATER_OR_EQUALS_TYPE, datefield2));
		datefield1.check();
		assertFalse(datefield1.hasError());
		calendar.add(Calendar.HOUR, -20);
		datefield1.setValue(calendar.getTime());
		datefield1.check();
		assertFalse(datefield1.hasError());
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		datefield1.setValue(calendar.getTime());
		datefield1.check();
		assertTrue(datefield1.hasError());
	}

	public void testGranularityMonth() {
		Calendar calendar = newCalendar();
		ComplexField datefield1 = FormFactory.newDateField("field1", calendar.getTime(), false);
		ComplexField datefield2 = FormFactory.newDateField("field2", calendar.getTime(), false);
		datefield1.addConstraint(new DateComparisonDependency(DateComparisonDependency.GRANULARITY_TYPE_MONTH,
			ComparisonDependency.GREATER_OR_EQUALS_TYPE, datefield2));
		datefield1.check();
		assertFalse(datefield1.hasError());
		calendar.add(Calendar.DAY_OF_YEAR, -10);
		datefield1.setValue(calendar.getTime());
		datefield1.check();
		assertFalse(datefield1.hasError());
		calendar.add(Calendar.MONTH, -1);
		datefield1.setValue(calendar.getTime());
		datefield1.check();
		assertTrue(datefield1.hasError());
	}

	public void testGranularityYear() {
		Calendar calendar = newCalendar();
		ComplexField datefield1 = FormFactory.newDateField("field1", calendar.getTime(), false);
		ComplexField datefield2 = FormFactory.newDateField("field2", calendar.getTime(), false);
		datefield1.addConstraint(new DateComparisonDependency(DateComparisonDependency.GRANULARITY_TYPE_YEAR,
			ComparisonDependency.GREATER_OR_EQUALS_TYPE, datefield2));
		datefield1.check();
		assertFalse(datefield1.hasError());
		calendar.add(Calendar.MONTH, -5);
		datefield1.setValue(calendar.getTime());
		datefield1.check();
		assertFalse(datefield1.hasError());
		calendar.add(Calendar.YEAR, -1);
		datefield1.setValue(calendar.getTime());
		datefield1.check();
		assertTrue(datefield1.hasError());
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestDateComparisonDependency}.
	 */
	public static Test suite() {
		return suite(TestDateComparisonDependency.class);
	}

}

