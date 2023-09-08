/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.col.ListBuilder;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.FormFieldHelper;
import com.top_logic.util.TLContext;

/**
 * Testcase for {@link FormFieldHelper}.
 *
 * @author <a href=mailto:CBR@top-logic.com>CBR</a>
 */
public class TestFormFieldHelper extends BasicTestCase {

    public void testGetProperValue() {
        Object theValue = null;
        assertSame(null, FormFieldHelper.getProperValue(theValue));
		theValue = Integer.valueOf(5);
        assertSame(theValue, FormFieldHelper.getProperValue(theValue));
        theValue = Arrays.asList(new String[] {"A", "B", "C"});
        assertEquals("A", FormFieldHelper.getProperValue(theValue));
        StringField theField = FormFactory.newStringField("Field");
		assertEquals(theField.getValue(), FormFieldHelper.getProperValue(theField));
        theValue = Arrays.asList(new StringField[] {theField, null});
		assertEquals(theField.getValue(), FormFieldHelper.getProperValue(theValue));
        theField.setValue("Test");
        assertEquals("Test", FormFieldHelper.getProperValue(theField));
        assertEquals("Test", FormFieldHelper.getProperValue(theValue));
        BooleanField theField2 = FormFactory.newBooleanField("Field2");
        theField2.setAsBoolean(true);
        assertTrue(((Boolean)FormFieldHelper.getProperValue(theField2)).booleanValue());
        theValue = Arrays.asList(new FormField[] {theField2, theField});
        assertTrue(((Boolean)FormFieldHelper.getProperValue(theValue)).booleanValue());
        theField2.setAsBoolean(false);
        assertFalse(((Boolean)FormFieldHelper.getProperValue(theField2)).booleanValue());
        assertFalse(((Boolean)FormFieldHelper.getProperValue(theValue)).booleanValue());

        List theOptionList = Arrays.asList(new String[] {"A", "B", "C"});
        SelectField theSingleField = FormFactory.newSelectField("SingleSelectField", theOptionList, false, false);
        SelectField theMultiField = FormFactory.newSelectField("MultiSelectField", theOptionList, true, false);
        theSingleField.setAsSingleSelection("C");
        theMultiField.setAsSelection(Arrays.asList(new String[] {"A", "B"}));
        assertTrue(CollectionUtil.containsSame(Arrays.asList(new String[] {"C"}), (List)FormFieldHelper.getProperValue(theSingleField)));
        assertTrue(CollectionUtil.containsSame(Arrays.asList(new String[] {"A", "B"}), (List)FormFieldHelper.getProperValue(theMultiField)));
    }

    public void testGetSingleProperValue() {
        Object theValue = null;
        assertSame(null, FormFieldHelper.getSingleProperValue(theValue));
		theValue = Integer.valueOf(5);
        assertSame(theValue, FormFieldHelper.getSingleProperValue(theValue));
        theValue = Arrays.asList(new String[] {"A", "B", "C"});
        assertEquals("A", FormFieldHelper.getSingleProperValue(theValue));
        StringField theField = FormFactory.newStringField("Field");
		assertEquals(theField.getValue(), FormFieldHelper.getSingleProperValue(theField));
        theValue = Arrays.asList(new StringField[] {theField, null});
		assertEquals(theField.getValue(), FormFieldHelper.getSingleProperValue(theValue));
        theField.setValue("Test");
        assertEquals("Test", FormFieldHelper.getSingleProperValue(theField));
        assertEquals("Test", FormFieldHelper.getSingleProperValue(theValue));
        BooleanField theField2 = FormFactory.newBooleanField("Field2");
        theField2.setAsBoolean(true);
        assertTrue(((Boolean)FormFieldHelper.getSingleProperValue(theField2)).booleanValue());
        theValue = Arrays.asList(new FormField[] {theField2, theField});
        assertTrue(((Boolean)FormFieldHelper.getSingleProperValue(theValue)).booleanValue());
        theField2.setAsBoolean(false);
        assertFalse(((Boolean)FormFieldHelper.getSingleProperValue(theField2)).booleanValue());
        assertFalse(((Boolean)FormFieldHelper.getSingleProperValue(theValue)).booleanValue());

        List theOptionList = Arrays.asList(new String[] {"A", "B", "C"});
        SelectField theSingleField = FormFactory.newSelectField("SingleSelectField", theOptionList, false, false);
        SelectField theMultiField = FormFactory.newSelectField("MultiSelectField", theOptionList, true, false);
        theSingleField.setAsSingleSelection("C");
        theMultiField.setAsSelection(Arrays.asList(new String[] {"A", "B"}));
        assertEquals("C", FormFieldHelper.getSingleProperValue(theSingleField));
        assertEquals("A", FormFieldHelper.getSingleProperValue(theMultiField));
    }

    public void testGetStringValue() {
        assertEquals(null, FormFieldHelper.getStringValue(null));
        assertEquals("blub", FormFieldHelper.getStringValue("blub"));
        assertEquals("true", FormFieldHelper.getStringValue(Boolean.TRUE));
        assertEquals("false", FormFieldHelper.getStringValue(Boolean.FALSE));
		assertEquals("5", FormFieldHelper.getStringValue(Integer.valueOf(5)));
		assertEquals("2.0", FormFieldHelper.getStringValue(Double.valueOf(2)));

        Object theValue = new NamedConstant("test value");
        assertEquals(theValue.toString(), FormFieldHelper.getStringValue(theValue));
        theValue = Arrays.asList(new String[] {"A", "B", "C"});
        assertEquals("A", FormFieldHelper.getStringValue(theValue));
        StringField theField = FormFactory.newStringField("Field");
		assertEquals(theField.getValue(), FormFieldHelper.getStringValue(theField));
        theValue = Arrays.asList(new StringField[] {theField, null});
		assertEquals(theField.getValue(), FormFieldHelper.getStringValue(theValue));
        theField.setValue("Test");
        assertEquals("Test", FormFieldHelper.getStringValue(theField));
        assertEquals("Test", FormFieldHelper.getStringValue(theValue));
        BooleanField theField2 = FormFactory.newBooleanField("Field2");
        theField2.setAsBoolean(true);
        assertEquals("true", FormFieldHelper.getStringValue(theField2));
        theValue = Arrays.asList(new FormField[] {theField2, theField});
        assertEquals("true", FormFieldHelper.getStringValue(theValue));
        theField2.setAsBoolean(false);
        assertEquals("false", FormFieldHelper.getStringValue(theField2));
        assertEquals("false", FormFieldHelper.getStringValue(theValue));
        SelectField theField3 = FormFactory.newSelectField("Field3", new ListBuilder().add("A").add("B").add("C").toList());
        theField3.setAsSingleSelection("B");
        assertEquals("B", FormFieldHelper.getStringValue(theField3));
    }

    public void testGetBooleanValue() {
        Boolean TRUE = Boolean.TRUE; Boolean FALSE = Boolean.FALSE;
        assertEquals(FALSE, FormFieldHelper.getBooleanValue(null));
        assertEquals(TRUE, FormFieldHelper.getBooleanValue(Boolean.TRUE));
        assertEquals(FALSE, FormFieldHelper.getBooleanValue(Boolean.FALSE));
        assertEquals(TRUE, FormFieldHelper.getBooleanValue("true"));
        assertEquals(TRUE, FormFieldHelper.getBooleanValue("TRUE"));
        assertEquals(TRUE, FormFieldHelper.getBooleanValue("TruE"));
        assertEquals(FALSE, FormFieldHelper.getBooleanValue("false"));
        assertEquals(FALSE, FormFieldHelper.getBooleanValue("blub"));
		assertEquals(FALSE, FormFieldHelper.getBooleanValue(Integer.valueOf(5)));

        assertEquals(false, FormFieldHelper.getbooleanValue(null));
        assertEquals(true, FormFieldHelper.getbooleanValue(Boolean.TRUE));
        assertEquals(false, FormFieldHelper.getbooleanValue(Boolean.FALSE));
        assertEquals(true, FormFieldHelper.getbooleanValue("true"));
        assertEquals(true, FormFieldHelper.getbooleanValue("TRUE"));
        assertEquals(true, FormFieldHelper.getbooleanValue("TruE"));
        assertEquals(false, FormFieldHelper.getbooleanValue("false"));
        assertEquals(false, FormFieldHelper.getbooleanValue("blub"));
		assertEquals(false, FormFieldHelper.getbooleanValue(Integer.valueOf(5)));

        BooleanField theField = FormFactory.newBooleanField("Field");
        theField.setAsBoolean(true);
        assertEquals(TRUE, FormFieldHelper.getBooleanValue(theField));
        assertEquals(true, FormFieldHelper.getbooleanValue(theField));
        theField.setAsBoolean(false);
        assertEquals(FALSE, FormFieldHelper.getBooleanValue(theField));
        assertEquals(false, FormFieldHelper.getbooleanValue(theField));
        theField.setValue(null);
        assertEquals(FALSE, FormFieldHelper.getBooleanValue(theField));
        assertEquals(false, FormFieldHelper.getbooleanValue(theField));
        Object theValue = Arrays.asList(new FormField[] {theField, null});
        assertEquals(FALSE, FormFieldHelper.getBooleanValue(theValue));
        assertEquals(false, FormFieldHelper.getbooleanValue(theValue));
        theField.setAsBoolean(true);
        assertEquals(TRUE, FormFieldHelper.getBooleanValue(theValue));
        assertEquals(true, FormFieldHelper.getbooleanValue(theValue));
    }

    public void testGetIntegerValue() {
        assertEquals(null, FormFieldHelper.getIntegerValue(null));
		assertEquals(Integer.valueOf(5), FormFieldHelper.getIntegerValue(Double.valueOf(5.0)));
		assertEquals(Integer.valueOf(5), FormFieldHelper.getIntegerValue(Integer.valueOf(5)));
		assertEquals(Integer.valueOf(5), FormFieldHelper.getIntegerValue("5"));
		assertEquals(Integer.valueOf(6), FormFieldHelper.getIntegerValue("6"));
		assertEquals(Integer.valueOf(0), FormFieldHelper.getIntegerValue("0"));
        assertEquals(null, FormFieldHelper.getIntegerValue("blub"));
        assertEquals(null, FormFieldHelper.getIntegerValue(""));

        assertTrue(0 == FormFieldHelper.getintValue(null));
		assertTrue(5 == FormFieldHelper.getintValue(Double.valueOf(5.0)));
		assertTrue(5 == FormFieldHelper.getintValue(Integer.valueOf(5)));
        assertTrue(5 == FormFieldHelper.getintValue("5"));
        assertTrue(6 == FormFieldHelper.getintValue("6"));
        assertTrue(0 == FormFieldHelper.getintValue("0"));
        assertTrue(0 == FormFieldHelper.getintValue("blub"));
        assertTrue(0 == FormFieldHelper.getintValue(""));

        Object theValue = new NamedConstant("test value");
        assertEquals(null, FormFieldHelper.getIntegerValue(theValue));
		theValue = Arrays.asList(new Number[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) });
		assertEquals(Integer.valueOf(1), FormFieldHelper.getIntegerValue(theValue));
        ComplexField theField = FormFactory.newComplexField("Field", HTMLFormatter.getInstance().getNumberFormat());
        assertEquals(null, FormFieldHelper.getIntegerValue(theField));
        theValue = Arrays.asList(new FormField[] {theField, null});
        assertEquals(null, FormFieldHelper.getIntegerValue(theValue));
		theField.setValue(Integer.valueOf(7));
		assertEquals(Integer.valueOf(7), FormFieldHelper.getIntegerValue(theField));
		assertEquals(Integer.valueOf(7), FormFieldHelper.getIntegerValue(theValue));
        StringField theField2 = FormFactory.newStringField("Field2");
        theField2.setValue("8");
		assertEquals(Integer.valueOf(8), FormFieldHelper.getIntegerValue(theField2));
        theValue = Arrays.asList(new FormField[] {theField2, theField});
		assertEquals(Integer.valueOf(8), FormFieldHelper.getIntegerValue(theValue));
        theField2.setValue("6");
		assertEquals(Integer.valueOf(6), FormFieldHelper.getIntegerValue(theField2));
		assertEquals(Integer.valueOf(6), FormFieldHelper.getIntegerValue(theValue));
        theField2.setValue("blub");
        assertEquals(null, FormFieldHelper.getIntegerValue(theField2));
        assertEquals(null, FormFieldHelper.getIntegerValue(theValue));
    }

    public void testGetLongValue() {
        assertEquals(null, FormFieldHelper.getLongValue(null));
		assertEquals(Long.valueOf(5), FormFieldHelper.getLongValue(Double.valueOf(5.0)));
		assertEquals(Long.valueOf(5), FormFieldHelper.getLongValue(Long.valueOf(5)));
		assertEquals(Long.valueOf(5), FormFieldHelper.getLongValue("5"));
		assertEquals(Long.valueOf(6), FormFieldHelper.getLongValue("6"));
		assertEquals(Long.valueOf(0), FormFieldHelper.getLongValue("0"));
        assertEquals(null, FormFieldHelper.getLongValue("blub"));
        assertEquals(null, FormFieldHelper.getLongValue(""));

        assertTrue(0 == FormFieldHelper.getlongValue(null));
		assertTrue(5 == FormFieldHelper.getlongValue(Double.valueOf(5.0)));
		assertTrue(5 == FormFieldHelper.getlongValue(Long.valueOf(5)));
        assertTrue(5 == FormFieldHelper.getlongValue("5"));
        assertTrue(6 == FormFieldHelper.getlongValue("6"));
        assertTrue(0 == FormFieldHelper.getlongValue("0"));
        assertTrue(0 == FormFieldHelper.getlongValue("blub"));
        assertTrue(0 == FormFieldHelper.getlongValue(""));

        Object theValue = new NamedConstant("test value");
        assertEquals(null, FormFieldHelper.getLongValue(theValue));
		theValue = Arrays.asList(new Number[] { Integer.valueOf(1), Long.valueOf(2), Long.valueOf(3) });
		assertEquals(Long.valueOf(1), FormFieldHelper.getLongValue(theValue));
        ComplexField theField = FormFactory.newComplexField("Field", HTMLFormatter.getInstance().getNumberFormat());
        assertEquals(null, FormFieldHelper.getLongValue(theField));
        theValue = Arrays.asList(new FormField[] {theField, null});
        assertEquals(null, FormFieldHelper.getLongValue(theValue));
		theField.setValue(Long.valueOf(7));
		assertEquals(Long.valueOf(7), FormFieldHelper.getLongValue(theField));
		assertEquals(Long.valueOf(7), FormFieldHelper.getLongValue(theValue));
        StringField theField2 = FormFactory.newStringField("Field2");
        theField2.setValue("8");
		assertEquals(Long.valueOf(8), FormFieldHelper.getLongValue(theField2));
        theValue = Arrays.asList(new FormField[] {theField2, theField});
		assertEquals(Long.valueOf(8), FormFieldHelper.getLongValue(theValue));
        theField2.setValue("6");
		assertEquals(Long.valueOf(6), FormFieldHelper.getLongValue(theField2));
		assertEquals(Long.valueOf(6), FormFieldHelper.getLongValue(theValue));
        theField2.setValue("blub");
        assertEquals(null, FormFieldHelper.getLongValue(theField2));
        assertEquals(null, FormFieldHelper.getLongValue(theValue));
    }

    public void testGetDoubleValue() {
        assertEquals(null, FormFieldHelper.getDoubleValue(null));
        assertEquals(Double.valueOf(5.0), FormFieldHelper.getDoubleValue(Double.valueOf(5.0)));
        assertEquals(Double.valueOf(5.0), FormFieldHelper.getDoubleValue(Integer.valueOf(5)));
        assertEquals(Double.valueOf(5.0), FormFieldHelper.getDoubleValue("5.0"));
        assertEquals(Double.valueOf(6.0), FormFieldHelper.getDoubleValue("6"));
        assertEquals(Double.valueOf(0.0), FormFieldHelper.getDoubleValue("0"));
        assertEquals(null, FormFieldHelper.getDoubleValue("blub"));
        assertEquals(null, FormFieldHelper.getDoubleValue(""));

        assertTrue(0.0 == FormFieldHelper.getdoubleValue(null)); // $codepro.audit.disable floatComparison
        assertTrue(5.0 == FormFieldHelper.getdoubleValue(Double.valueOf(5.0))); // $codepro.audit.disable floatComparison
        assertTrue(5.0 == FormFieldHelper.getdoubleValue(Integer.valueOf(5))); // $codepro.audit.disable floatComparison
        assertTrue(5.0 == FormFieldHelper.getdoubleValue("5.0")); // $codepro.audit.disable floatComparison
        assertTrue(5.0 == FormFieldHelper.getdoubleValue("5")); // $codepro.audit.disable floatComparison
        assertTrue(0.0 == FormFieldHelper.getdoubleValue("0")); // $codepro.audit.disable floatComparison
        assertTrue(0.0 == FormFieldHelper.getdoubleValue("blub")); // $codepro.audit.disable floatComparison
        assertTrue(0.0 == FormFieldHelper.getdoubleValue("")); // $codepro.audit.disable floatComparison

        Object theValue = new NamedConstant("test value");
        assertEquals(null, FormFieldHelper.getDoubleValue(theValue));
		theValue = Arrays.asList(new Number[] { Double.valueOf(1.0), Double.valueOf(2.0), Double.valueOf(3.0) });
		assertEquals(Double.valueOf(1.0), FormFieldHelper.getDoubleValue(theValue));
        ComplexField theField = FormFactory.newComplexField("Field", HTMLFormatter.getInstance().getNumberFormat());
        assertEquals(null, FormFieldHelper.getDoubleValue(theField));
        theValue = Arrays.asList(new FormField[] {theField, null});
        assertEquals(null, FormFieldHelper.getDoubleValue(theValue));
		theField.setValue(Double.valueOf(7.0));
		assertEquals(Double.valueOf(7.0), FormFieldHelper.getDoubleValue(theField));
		assertEquals(Double.valueOf(7.0), FormFieldHelper.getDoubleValue(theValue));
        StringField theField2 = FormFactory.newStringField("Field2");
        theField2.setValue("8.0");
		assertEquals(Double.valueOf(8.0), FormFieldHelper.getDoubleValue(theField2));
        theValue = Arrays.asList(new FormField[] {theField2, theField});
		assertEquals(Double.valueOf(8.0), FormFieldHelper.getDoubleValue(theValue));
        theField2.setValue("6");
		assertEquals(Double.valueOf(6.0), FormFieldHelper.getDoubleValue(theField2));
		assertEquals(Double.valueOf(6.0), FormFieldHelper.getDoubleValue(theValue));
        theField2.setValue("blub");
        assertEquals(null, FormFieldHelper.getDoubleValue(theField2));
        assertEquals(null, FormFieldHelper.getDoubleValue(theValue));
    }

	public void testGetDateValue() throws Throwable {
		Calendar theCalendar = CalendarUtil.createCalendar();

        Date theDate = theCalendar.getTime();
        assertEquals(null, FormFieldHelper.getDateValue(null));
        assertEquals(theDate, FormFieldHelper.getDateValue(theDate));
        assertEquals(theDate, FormFieldHelper.getDateValue(theCalendar));

		final Date theDay = DateUtil.createDate(theCalendar, 2004, Calendar.MAY, 16);
        assertEquals(theDay, FormFieldHelper.getDateValue(theDay));
        assertEquals(null, FormFieldHelper.getDateValue("blub"));
        assertEquals(null, FormFieldHelper.getDateValue(""));

        Object theValue = new NamedConstant("test value");
        assertEquals(null, FormFieldHelper.getDateValue(theValue));
        theValue = Arrays.asList(new Date[] {theDate, theDay});
        assertEquals(theDate, FormFieldHelper.getDateValue(theValue));
        ComplexField theField = FormFactory.newDateField("Field", null, false);
        assertEquals(null, FormFieldHelper.getDateValue(theField));
        theValue = Arrays.asList(new FormField[] {theField, null});
        assertEquals(null, FormFieldHelper.getDateValue(theValue));
        theField.setValue(theDate);
        assertEquals(theDate, FormFieldHelper.getDateValue(theField));
        assertEquals(theDate, FormFieldHelper.getDateValue(theValue));

        // Test date parsing / formatting with different locales
        String theLocaleDate = HTMLFormatter.getInstance().getDateFormat().format(theDay);
        assertEquals(theDay, FormFieldHelper.getDateValue(theLocaleDate));

		final AtomicReference<Throwable> problem = new AtomicReference<>();
		Thread t = new Thread() {
			@Override
			public void run() {
				assertNull("Test to format without context.", TLContext.getContext());
				try {
					BasicTestCase.executeInDefaultLocale(Locale.GERMANY, () -> {
						assertEquals(theDay, FormFieldHelper.getDateValue("16.05.2004"));
						return null;
					});
					BasicTestCase.executeInDefaultLocale(Locale.UK, () -> {
						assertEquals(theDay, FormFieldHelper.getDateValue("16 May 2004"));
						return null;
					});
					BasicTestCase.executeInDefaultLocale(Locale.US, () -> {
						assertEquals(theDay, FormFieldHelper.getDateValue("May 16, 2004"));
						return null;
					});
				} catch (Throwable problem1) {
					problem.set(problem1);
				}
			}
		};
		t.start();
		t.join();
		if (problem.get() != null) {
			throw problem.get();
		}

        TLContext theContext = TLContext.getContext();
		Locale theDefault = theContext.getCurrentLocale();
		try {
			theContext.setCurrentLocale(Locale.GERMANY);
			assertEquals(theDay, FormFieldHelper.getDateValue("16.05.2004"));
			theContext.setCurrentLocale(Locale.UK);
			assertEquals(theDay, FormFieldHelper.getDateValue("16 May 2004"));
			theContext.setCurrentLocale(Locale.US);
			assertEquals(theDay, FormFieldHelper.getDateValue("May 16, 2004"));
		} finally {
			theContext.setCurrentLocale(theDefault);
        }
    }



    /**
     * Return the suite of Tests to perform.
     *
     * @return the test for this class
     */
    public static Test suite() {
        return TLTestSetup.createTLTestSetup(new TestSuite(TestFormFieldHelper.class));
    }

    /**
     * Main function for direct execution.
     *
     * @param args
     *            command line parameters are ignored.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

}
