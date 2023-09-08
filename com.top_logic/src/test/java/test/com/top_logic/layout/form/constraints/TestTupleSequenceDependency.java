/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.constraints;

import java.text.DecimalFormat;
import java.text.Format;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.constraints.TupleSequenceDependency;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;

/**
 * Tests the class {@link TupleSequenceDependency}.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class TestTupleSequenceDependency extends BasicTestCase {
	private ComplexField field1;
	private ComplexField field2;
	private ComplexField field3;
	private ComplexField field4;
	private ComplexField field5;
	private ComplexField field6;
	
	/**
	 * Returns the test suite
	 */
    public static Test suite () {
        TestSuite theSuite  = new TestSuite(TestTupleSequenceDependency.class);
        return TLTestSetup.createTLTestSetup(theSuite);
    }
    
    /** 
     * All fields are in correct order 
     */
    public void testCorrectSequence() {
		update(field1, 1);
		update(field2, 2);
		update(field3, 3);
		update(field4, 4);
		update(field5, 5);
		update(field6, 6);
		
		assertFalse(field1.hasError());
		assertFalse(field2.hasError());
		assertFalse(field3.hasError());
		assertFalse(field4.hasError());
		assertFalse(field5.hasError());
		assertFalse(field6.hasError());
	}
    
    /**
     * 1st tuple is not valid, so the fields 1 and 2 should show an error.
     */
    public void testFirstTupleFail() {
    	update(field1, 3);
		update(field2, 2);
		update(field3, 3);
		update(field4, 4);
		update(field5, 5);
		update(field6, 6);
		
		assertTrue(field1.hasError());
		assertTrue(field2.hasError());
		assertFalse(field3.hasError());
		assertFalse(field4.hasError());
		assertFalse(field5.hasError());
		assertFalse(field6.hasError());
    }
    
    /**
     * 2nd tuple is not valid, so the fields 3 and 4 should show an error.
     */
    public void testSecondTupleFail() {
    	update(field1, 2);
		update(field2, 2);
		update(field3, 5);
		update(field4, 4);
		update(field5, 5);
		update(field6, 6);
		
		assertFalse(field1.hasError());
		assertFalse(field2.hasError());
		assertTrue(field3.hasError());
		assertTrue(field4.hasError());
		assertFalse(field5.hasError());
		assertFalse(field6.hasError());
    }
    
    /**
     * 3rd tuple is not valid, so the fields 5 and 6 should show an error.
     */
    public void testThirdTupleFail() {
    	update(field1, 2);
		update(field2, 2);
		update(field3, 3);
		update(field4, 3);
		update(field5, 7);
		update(field6, 6);
		
		assertFalse(field1.hasError());
		assertFalse(field2.hasError());
		assertFalse(field3.hasError());
		assertFalse(field4.hasError());
		assertTrue(field5.hasError());
		assertTrue(field6.hasError());
    }
    
    /**
     * 3rd tuple is not valid, so the fields 5 and 6 should show an error.
     * Then the error is fixed by changing field 6 value.
     */
    public void testThirdTupleFailRecover3rd1() {
    	update(field1, 2);
		update(field2, 2);
		update(field3, 3);
		update(field4, 3);
		update(field5, 7);
		update(field6, 6);
		
		assertFalse(field1.hasError());
		assertFalse(field2.hasError());
		assertFalse(field3.hasError());
		assertFalse(field4.hasError());
		assertTrue(field5.hasError());
		assertTrue(field6.hasError());
		
		update(field6, 7);
		
		assertFalse(field1.hasError());
		assertFalse(field2.hasError());
		assertFalse(field3.hasError());
		assertFalse(field4.hasError());
		assertFalse(field5.hasError());
		assertFalse(field6.hasError());
    }
    
    /**
     * 3rd tuple is not valid, so the fields 5 and 6 should show an error.
     * Then the error is fixed by changing field 5 value.
     */
    public void testThirdTupleFailRecover3rd2() {
    	update(field1, 2);
		update(field2, 2);
		update(field3, 3);
		update(field4, 3);
		update(field5, 7);
		update(field6, 6);
		
		assertFalse(field1.hasError());
		assertFalse(field2.hasError());
		assertFalse(field3.hasError());
		assertFalse(field4.hasError());
		assertTrue(field5.hasError());
		assertTrue(field6.hasError());
		
		update(field5, 5);
		
		assertFalse(field1.hasError());
		assertFalse(field2.hasError());
		assertFalse(field3.hasError());
		assertFalse(field4.hasError());
		assertFalse(field5.hasError());
		assertFalse(field6.hasError());
    }
    
	/**
	 * 3rd tuple is not valid, so the fields 5 and 6 should show an error.
	 * Then the error is fixed by changing field 3 value so that field 3 and 4
	 * are not equal anymore.
	 */
    public void testThirdTupleFailRecover2nd1() {
    	update(field1, 2);
		update(field2, 2);
		update(field3, 3);
		update(field4, 3);
		update(field5, 7);
		update(field6, 6);
		
		assertFalse(field1.hasError());
		assertFalse(field2.hasError());
		assertFalse(field3.hasError());
		assertFalse(field4.hasError());
		assertTrue(field5.hasError());
		assertTrue(field6.hasError());
		
		update(field3, 2);
		
		assertFalse(field1.hasError());
		assertFalse(field2.hasError());
		assertFalse(field3.hasError());
		assertFalse(field4.hasError());
		assertFalse(field5.hasError());
		assertFalse(field6.hasError());
    }
    
    /**
	 * 3rd tuple is not valid, so the fields 5 and 6 should show an error.
	 * Then the error is fixed by changing field 4 value so that field 3 and 4
	 * are not equal anymore.
	 */
    public void testThirdTupleFailRecover2nd2() {
    	update(field1, 2);
		update(field2, 2);
		update(field3, 3);
		update(field4, 3);
		update(field5, 7);
		update(field6, 6);
		
		assertFalse(field1.hasError());
		assertFalse(field2.hasError());
		assertFalse(field3.hasError());
		assertFalse(field4.hasError());
		assertTrue(field5.hasError());
		assertTrue(field6.hasError());
		
		update(field4, 4);
		
		assertFalse(field1.hasError());
		assertFalse(field2.hasError());
		assertFalse(field3.hasError());
		assertFalse(field4.hasError());
		assertFalse(field5.hasError());
		assertFalse(field6.hasError());
    }
    
    /**
	 * 3rd tuple is not valid, so the fields 5 and 6 should show an error.
	 * Then the error is "fixed" by changing field 3 value so that field 3 and 4
	 * are not valid anymore. So the error should show up at field 3 and 4.
	 */
    public void testThirdTupleFailRecover2nd3() {
    	update(field1, 2);
		update(field2, 2);
		update(field3, 3);
		update(field4, 3);
		update(field5, 7);
		update(field6, 6);
		
		assertFalse(field1.hasError());
		assertFalse(field2.hasError());
		assertFalse(field3.hasError());
		assertFalse(field4.hasError());
		assertTrue(field5.hasError());
		assertTrue(field6.hasError());
		
		update(field3, 4);
		
		assertFalse(field1.hasError());
		assertFalse(field2.hasError());
		assertTrue(field3.hasError());
		assertTrue(field4.hasError());
		assertFalse(field5.hasError());
		assertFalse(field6.hasError());
    }
    
    /**
	 * 3rd tuple is not valid, so the fields 5 and 6 should show an error.
	 * Then the error is fixed by changing field 2 value so that field 1 and 2
	 * are not equal anymore.
	 */
    public void testThirdTupleFailRecover1st1() {
    	update(field1, 2);
		update(field2, 2);
		update(field3, 3);
		update(field4, 3);
		update(field5, 7);
		update(field6, 6);
		
		assertFalse(field1.hasError());
		assertFalse(field2.hasError());
		assertFalse(field3.hasError());
		assertFalse(field4.hasError());
		assertTrue(field5.hasError());
		assertTrue(field6.hasError());
		
		update(field2, 3);
		
		assertFalse(field1.hasError());
		assertFalse(field2.hasError());
		assertFalse(field3.hasError());
		assertFalse(field4.hasError());
		assertFalse(field5.hasError());
		assertFalse(field6.hasError());
    }
    
    /**
	 * 3rd tuple is not valid, so the fields 5 and 6 should show an error.
	 * Then the error is fixed by changing field 1 value so that field 1 and 2
	 * are not equal anymore.
	 */
    public void testThirdTupleFailRecover1st2() {
    	update(field1, 2);
		update(field2, 2);
		update(field3, 3);
		update(field4, 3);
		update(field5, 7);
		update(field6, 6);
		
		assertFalse(field1.hasError());
		assertFalse(field2.hasError());
		assertFalse(field3.hasError());
		assertFalse(field4.hasError());
		assertTrue(field5.hasError());
		assertTrue(field6.hasError());
		
		update(field1, 1);
		
		assertFalse(field1.hasError());
		assertFalse(field2.hasError());
		assertFalse(field3.hasError());
		assertFalse(field4.hasError());
		assertFalse(field5.hasError());
		assertFalse(field6.hasError());
    }
    
	/**
	 * 3rd tuple is not valid, so the fields 5 and 6 should show an error.
	 * Then the error is fixed by changing field 1 value to <code>null</code> so
	 * that the values for the remaining fields is irrelevant.
	 */
    public void testThirdTupleFailRecover1st3() {
    	update(field1, 2);
		update(field2, 2);
		update(field3, 3);
		update(field4, 3);
		update(field5, 7);
		update(field6, 6);
		
		assertFalse(field1.hasError());
		assertFalse(field2.hasError());
		assertFalse(field3.hasError());
		assertFalse(field4.hasError());
		assertTrue(field5.hasError());
		assertTrue(field6.hasError());
		
		update(field1, null);
		
		assertFalse(field1.hasError());
		assertFalse(field2.hasError());
		assertFalse(field3.hasError());
		assertFalse(field4.hasError());
		assertFalse(field5.hasError());
		assertFalse(field6.hasError());
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
		
		field1 = FormFactory.newComplexField("field1", DecimalFormat.getIntegerInstance(), null, false);
		field2 = FormFactory.newComplexField("field2", DecimalFormat.getIntegerInstance(), null, false);
		field3 = FormFactory.newComplexField("field3", DecimalFormat.getIntegerInstance(), null, false);
		field4 = FormFactory.newComplexField("field4", DecimalFormat.getIntegerInstance(), null, false);
		field5 = FormFactory.newComplexField("field5", DecimalFormat.getIntegerInstance(), null, false);
		field6 = FormFactory.newComplexField("field6", DecimalFormat.getIntegerInstance(), null, false);
		
		final FormContext formContext = new FormContext("fc", ResPrefix.forTest("fc"));
		formContext.addMember(field1);
		formContext.addMember(field2);
		formContext.addMember(field3);
		formContext.addMember(field4);
		formContext.addMember(field5);
		formContext.addMember(field6);
		
		TupleSequenceDependency theDep = new TupleSequenceDependency(new FormField[] {field1, field2, field3, field4, field5, field6} );
		theDep.attach();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		field1 = null;
		field2 = null;
		field3 = null;
		field4 = null;
		field5 = null;
		field6 = null;
	}
	
	/**
	 * Simulates a client side update of the given field to the given date
	 */
	private void update(ComplexField aField, Integer anInt) {
		final Format startFormat = aField.getFormat();
		try {
			if (anInt != null) {
				aField.update(startFormat.format(anInt));
			}
			else {
				aField.update(null);
			}
		} catch (VetoException ex) {
			fail("No veto handler has been registered?", ex);
		}
	}
}
