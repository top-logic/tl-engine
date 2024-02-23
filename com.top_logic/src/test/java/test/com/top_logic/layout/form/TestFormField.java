/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form;

import java.text.NumberFormat;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.form.RawValueListener;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.constraints.ComparisonDependency;
import com.top_logic.layout.form.constraints.DigitsOnlyConstraint;
import com.top_logic.layout.form.constraints.LongPrimitiveRangeConstraint;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.ValueVetoListener;
import com.top_logic.layout.form.treetable.component.SilentVetoException;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests the class {@link com.top_logic.layout.form.FormField}
 * 
 * @author <a href="mailto:tma@top-logic.com">tma</a>
 */
public class TestFormField extends BasicTestCase {

	private static final ResPrefix ADDRESS = ResPrefix.forTest("adress");

	private static final ResPrefix DUMMY = ResPrefix.forTest("fc");

	private final class TestField extends AbstractFormField {

		public TestField(String name) {
			super(name, !MANDATORY, !IMMUTABLE, NORMALIZE, NO_CONSTRAINT);
		}

		@Override
		public Object visit(FormMemberVisitor v, Object arg) {
			return null;
		}

		@Override
		protected Object unparseValue(Object value) {
			return value;
		}

		@Override
		protected Object parseRawValue(Object rawValue) throws CheckException {
			// Accept no raw value
			throw new CheckException(rawValue + " can not be parsed.");
		}

		@Override
		protected Object narrowValue(Object value) throws IllegalArgumentException, ClassCastException {
			return value;
		}
	}

	private final class ValuePropertyEventCounter implements RawValueListener {
		private int eventCnt;
		
		public ValuePropertyEventCounter() {
			// Default constructor.
		}

		@Override
		public Bubble handleRawValueChanged(FormField field, Object oldValue, Object newValue) {
			eventCnt++;
			return Bubble.BUBBLE;
		}

		public int getEventCnt() {
			return eventCnt;
		}
	}

	/**
	 * Test that switching the mandatory property is reflected by the field
	 * checking logic.
	 */
	public void testMandatoryConstraint() {
		FormContext fc = new FormContext("fc", DUMMY);
		FormField s1 = FormFactory.newStringField("s1");
		fc.addMember(s1);
		
		s1.setValue(null);
		assertTrue(fc.checkAll());
		
		s1.setMandatory(true);
		assertFalse(fc.checkAll());
		
		s1.setMandatory(false);
		assertTrue(fc.checkAll());
	}
	
	/**
	 * Test {@link FormMember#setMode(FieldMode)}.
	 */
	public void testMode() {
		FormField field = FormFactory.newStringField("field");

		for (int n = 0; n < 2; n++) {
			for (FieldMode mode : FieldMode.values()) {
				if (mode == FieldMode.LOCALLY_IMMUTABLE) {
					// Special case.
					continue;
				}
				field.setMode(mode);
				assertEquals(mode, field.getMode());
			}
		}

		field.setMode(FieldMode.ACTIVE);
		assertTrue(field.isVisible());
		assertTrue(field.isActive());
		assertFalse(field.isDisabled());
		assertFalse(field.isImmutable());
		assertFalse(field.isBlocked());

		field.setMode(FieldMode.DISABLED);
		assertTrue(field.isVisible());
		assertTrue(field.isDisabled());
		assertFalse(field.isActive());
		assertFalse(field.isImmutable());
		assertFalse(field.isBlocked());

		field.setMode(FieldMode.IMMUTABLE);
		assertTrue(field.isVisible());
		assertTrue(field.isImmutable());
		assertFalse(field.isActive());
		assertFalse(field.isBlocked());

		field.setMode(FieldMode.BLOCKED);
		assertTrue(field.isVisible());
		assertTrue(field.isBlocked());
		assertFalse(field.isActive());

		field.setMode(FieldMode.INVISIBLE);
		assertFalse(field.isVisible());
		assertFalse(field.isActive());
	}

	/**
	 *  Tests, that assigning a {@FormField} to two {@link FormGroup} is not allowed.
	 */
	public void testDoubleAssignment(){
		FormGroup adress1 = new FormContext("adress1", ADDRESS);
		FormField street1 = FormFactory.newStringField("street");
		adress1.addMember(street1);
		
		FormGroup adress2 = new FormContext("adress2", ADDRESS);
		
		try{
			adress2.addMember(street1);
			fail("Expected to fail, as street1 has already been assigned to adress1");
		}catch (IllegalStateException exception) {
			//expected
		}
	}

	/**
	 *  Tests, that assigning a {@FormField} twice is not allowed.
	 */
	public void testDoubleAssignment2(){
		FormGroup adress1 = new FormContext("adress1", ADDRESS);
		FormField street1 = FormFactory.newStringField("street");
		adress1.addMember(street1); // first assigment
		try {
			adress1.addMember(street1); // second assignment
			fail("Expected to fail, as assigning a FormField twice is not allowed");
		} catch (Throwable ex) {
			// expected (one of them, depending on wether the test runs with assertions enabled or not).
			assertInstanceof(ex, IllegalStateException.class, AssertionError.class);
		}
	}

	public void testDependency() throws VetoException {
        NumberFormat theNumberFormat = NumberFormat.getIntegerInstance();
		FormContext theContext = new FormContext("fc", DUMMY);
        ComplexField theStartField = FormFactory.newComplexField("start", theNumberFormat);
        ComplexField theEndField = FormFactory.newComplexField("end", theNumberFormat);
        theStartField.setFormat(theNumberFormat);
        theEndField.setFormat(theNumberFormat);
        theStartField.setParser(theNumberFormat);
        theEndField.setParser(theNumberFormat);
        theStartField.addConstraint(new LongPrimitiveRangeConstraint(1000L, 9999L, true));
        theEndField.addConstraint(new LongPrimitiveRangeConstraint(1000L, 9999L, true));
        theStartField.addConstraint(new ComparisonDependency(ComparisonDependency.LOWER_OR_EQUALS_TYPE, theEndField));
        theEndField.addConstraint(new ComparisonDependency(ComparisonDependency.GREATER_OR_EQUALS_TYPE, theStartField));
        theContext.addMember(theStartField);
        theContext.addMember(theEndField);
        theStartField.checkWithAllDependencies();
        theEndField.checkWithAllDependencies();
        
        // check garbage input
        theStartField.update("garbage");
        theContext.checkAll();
        HandlerResult theResult = new HandlerResult();
		AbstractApplyCommandHandler.fillHandlerResultWithErrors(theContext, theResult);
        assertEquals("Ticket #873: Dependency to field with input error must not be reported as error.", 1, theResult.getEncodedErrors().size());
        
        theStartField.reset();
        theEndField.reset();
        
        theStartField.update("2000");
        theEndField.update("trash");
        theContext.checkAll();
        theResult = new HandlerResult();
		AbstractApplyCommandHandler.fillHandlerResultWithErrors(theContext, theResult);
        assertEquals("Ticket #873: Dependency to field with input error must not be reported as error.", 1, theResult.getEncodedErrors().size());
	}
	
	/**
	 * Test that an update sent from the UI is not echoed back to the UI.
	 */
	public void testUpdateFromUI() throws VetoException {
		ComplexField f1 = FormFactory.newComplexField("f1", NumberFormat.getIntegerInstance(Locale.GERMAN));
		
		f1.addListener(FormField.VALUE_PROPERTY, new RawValueListener() {
			
			@Override
			public Bubble handleRawValueChanged(FormField field, Object oldValue, Object newValue) {
				fail("Value changes must not be echoed to the UI: '" + oldValue + "' -> '" + newValue + "'");
				return Bubble.BUBBLE;
			}
		});
		
		assertNull(f1.getValue());
		f1.update("42");
		assertEquals(42L, f1.getValue());
		f1.update("13");
		assertEquals(13L, f1.getValue());
		
		ComplexField f2 = FormFactory.newComplexField("f2", NumberFormat.getIntegerInstance(Locale.GERMAN));
		ValuePropertyEventCounter eventCounter = new ValuePropertyEventCounter();
		f2.addListener(FormField.VALUE_PROPERTY, eventCounter);

		// Test that normalization is echoed back to the UI.
		assertEquals(0, eventCounter.getEventCnt());
		f2.update("42");
		assertEquals("User update without normalization.", 0, eventCounter.getEventCnt());
		f2.update("42 ");
		assertEquals("User update with normalization.", 1, eventCounter.getEventCnt());
		assertEquals("42", f2.getRawValue());
	}

	/**
	 * Test that a programatic update is sent to the UI.
	 */
	public void testProgramaticUpdate() {
		ComplexField f1 = FormFactory.newComplexField("f1", NumberFormat.getIntegerInstance(Locale.GERMAN));
		
		ValuePropertyEventCounter eventCounter = new ValuePropertyEventCounter();
		f1.addListener(FormField.VALUE_PROPERTY, eventCounter);
		
		assertNull(f1.getValue());
		assertEquals(0, eventCounter.getEventCnt());
		f1.setValue(42);
		assertEquals(1, eventCounter.getEventCnt());
		assertEquals("42", f1.getRawValue());
		f1.setValue(13);
		assertEquals("13", f1.getRawValue());
		assertEquals(2, eventCounter.getEventCnt());
	}
	
	public void testVetoException() {
		SelectField field =
			FormFactory.newSelectField("f", list("a", "b"), !FormFactory.MULTIPLE, list("b"), !FormFactory.IMMUTABLE);
		field.addValueVetoListener(new ValueVetoListener() {
			
			@Override
			public void checkVeto(FormField vetoField, Object newValue) throws VetoException {
				throw new SilentVetoException();
			}
		});
		
		try {
			field.update(list(field.getOptionID("a")));
			fail("VetoException expected!");
		} catch (VetoException ex) {
			assertEquals("Value of field had changed, whereas veto was given", list("b"), field.getValue());
			assertTrue("#2322: field has to be valid.", field.isValid());
		}
	}
	
	public void testIllegalInputSwitchVisibility() throws VetoException {
		AbstractFormField field = new TestField("name");
		
		field.update("new value:");
		
		assertFalse(field.hasValue());
		try {
			field.setVisible(false);
			assertFalse(field.isVisible());
			field.setVisible(true);
		} catch (IllegalStateException ex) {
			fail("Ticket #3552: Changing visibility of field with illegal input failed.", ex);
		}
	}
	
	public void testClearConstraints() {
		AbstractFormField field = new TestField("name");

		String value = "NotOnlyDigits";
		Constraint constraint = DigitsOnlyConstraint.INSTANCE;
		try {
			constraint.check(value);
			fail("Test needs that constraint does not accepts value.");
		} catch (CheckException ex) {
			// expected.
		}

		field.setValue(value);

		field.addConstraint(constraint);
		field.check();
		assertFalse("Constraint violated.", field.checkConstraints());
		assertTrue("Check adds error, because constraint is violated.", field.hasError());

		field.removeConstraint(constraint);
		field.check();
		assertTrue("Constraint was removed.", field.checkConstraints());
		assertFalse("Check removes error, because constraint was removed.", field.hasError());

		field.addConstraint(constraint);
		field.check();
		assertFalse("Constraint violated.", field.checkConstraints());
		assertTrue("Check adds error, because constraint is violated.", field.hasError());

		field.removeConstraint(constraint);
		field.check();
		assertTrue("Constraint was removed.", field.checkConstraints());
		assertFalse("Check removes error, because constraint was removed.", field.hasError());

		field.addConstraint(constraint);
		field.check();
		assertFalse("Constraint violated.", field.checkConstraints());
		assertTrue("Check adds error, because constraint is violated.", field.hasError());

		field.clearConstraints();
		field.check();
		assertTrue("All constraints were removed.", field.checkConstraints());
		assertFalse("Check removes error, because there is not constraint to be violated.", field.hasError());
	}

	public void testClearWarningConstraints() {
		AbstractFormField field = new TestField("name");

		String value = "NotOnlyDigits";
		DigitsOnlyConstraint constraint = DigitsOnlyConstraint.INSTANCE;
		try {
			constraint.check(value);
			fail("Test needs that constraint does not accepts value.");
		} catch (CheckException ex) {
			// expected.
		}

		field.setValue(value);

		field.addWarningConstraint(constraint);
		field.check();
		assertTrue("Warning constraints do not prevent checking constraints.", field.checkConstraints());
		assertFalse("Checking does not produse errors for warning constraints.", field.hasError());
		assertTrue("Check adds warning, because warning constraint is voiolated.", field.hasWarnings());

		field.removeWarningConstraint(constraint);
		field.check();
		assertTrue("Warning constraints do not prevent checking constraints.", field.checkConstraints());
		assertFalse("Checking does not produse errors for warning constraints.", field.hasError());
		assertFalse("Check removes warning, because warning constraint was removed.", field.hasWarnings());

		field.addWarningConstraint(constraint);
		field.check();
		assertTrue("Warning constraints do not prevent checking constraints.", field.checkConstraints());
		assertFalse("Checking does not produse errors for warning constraints.", field.hasError());
		assertTrue("Check adds warning, because warning constraint is voiolated.", field.hasWarnings());

		field.clearConstraints();
		field.check();
		assertTrue("Warning constraints do not prevent checking constraints.", field.checkConstraints());
		assertFalse("Checking does not produse errors for warning constraints.", field.hasError());
		assertFalse("Check removes warning, because all constraint were removed.", field.hasWarnings());
	}

	public void testCorrectRawValue() {
		AbstractFormField testField = new TestField("name");

		testField.setValue("foo");
		assertEquals("foo", testField.getValue());
		assertEquals("foo", testField.getRawValue());

		testField.addValueListener(new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				assertEquals(testField, field);
				assertEquals(newValue, testField.getValue());
				assertEquals(newValue, testField.getRawValue());
			}
		});

		testField.setValue(156);
	}

    public static Test suite () {
        TestSuite theSuite  = new TestSuite(TestFormField.class);
        return TLTestSetup.createTLTestSetup(theSuite);
    }
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
