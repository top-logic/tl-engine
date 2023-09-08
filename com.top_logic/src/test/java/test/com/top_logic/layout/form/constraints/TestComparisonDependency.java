/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.constraints;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.form.constraints.ComparisonDependency;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.IntField;

/**
 * Tests the class {@link com.top_logic.layout.form.constraints.ComparisonDependency}
 * 
 * @author     <a href="mailto:tma@top-logic.com">tma</a>
 */
public class TestComparisonDependency extends TestCase {

	private FormContext tempContext;
	private IntField minTempField;
	private IntField maxTempField;
	private ComparisonDependency comparisonDependancy;
	
	/**
	 * The main method
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(TestComparisonDependency.class);
	}

	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		tempContext = new FormContext("temp", ResPrefix.forTest("i18n"));
		
		minTempField = FormFactory.newIntField("min");
		maxTempField = FormFactory.newIntField("max");
		
		tempContext.addMember(minTempField);
		tempContext.addMember(maxTempField);
		
	}
	
	/**
	 * Simple utility method.
	 * 
	 * @param type the type 
	 */
	private void setupDependency(int type){
		comparisonDependancy = new ComparisonDependency(type,maxTempField);
		minTempField.addConstraint(comparisonDependancy);
	}
	
	/**
	 * Assert that the minTempField set - to the value of x - is valid, if the dependend values of the
	 * maxTempField are set to y.
	 * 
	 * @param x the lower value
	 * @param y the max value
	 * @param valid considered valid ?
	 */
	private void assertValid(int x,int y,boolean valid){
		minTempField.setValue(Integer.valueOf(x));
		maxTempField.setValue(Integer.valueOf(y));
		
		minTempField.check();
		
		assertEquals(valid,minTempField.isValid());
	}
	
	/**
	 * Test method for {@link com.top_logic.layout.form.constraints.ComparisonDependency#check(Object)}
	 */
	public final void testCheckLower() {
		
		setupDependency(ComparisonDependency.LOWER_TYPE);
		
		assertValid(1,2,true);
		assertValid(2,1,false);
		assertValid(2,2,false);
	}
	
	/**
	 * Test method for {@link com.top_logic.layout.form.constraints.ComparisonDependency#check(Object)}
	 */
	public final void testCheckLowerOrEquals() {
				
		setupDependency(ComparisonDependency.LOWER_OR_EQUALS_TYPE);
		
		assertValid(1,2,true);
		assertValid(2,2,true);
		assertValid(2,1,false);
		
	}
	
	/**
	 * Test method for {@link com.top_logic.layout.form.constraints.ComparisonDependency#check(Object)}
	 */
	public final void testCheckNotEquals() {
		
		setupDependency(ComparisonDependency.NOT_EQUALS_TYPE);
		
		assertValid(2,2,false);
		assertValid(2,1,true);
		assertValid(1,2,true);
		
	}
	
	/**
	 * Test method for {@link com.top_logic.layout.form.constraints.ComparisonDependency#check(Object)}
	 */
	public final void testCheckGreater() {
		
		setupDependency(ComparisonDependency.GREATER_TYPE);
		
		assertValid(2,1,true);
		assertValid(2,2,false);
		assertValid(1,2,false);
		
	}
	
	/**
	 * Test method for {@link com.top_logic.layout.form.constraints.ComparisonDependency#check(Object)}
	 */
	public final void testCheckGreaterOrEquals() {
		
		setupDependency(ComparisonDependency.GREATER_OR_EQUALS_TYPE);
		
		assertValid(2,1,true);
		assertValid(2,2,true);
		assertValid(1,2,false);
	}
	
	/**
	 * Test method for {@link com.top_logic.layout.form.constraints.ComparisonDependency#check(Object)}
	 */
	public void testCheckIllegalValue() throws VetoException{
		setupDependency(ComparisonDependency.LOWER_OR_EQUALS_TYPE);
		
		maxTempField.update("An illegal value");
		maxTempField.check();
		assertTrue(maxTempField.hasError());
		
		/**
		 * The min field is not valid as it's dependency (the max field) has errors.
		 * The min field itself does not have errors.
		 * {@link com.top_logic.layout.form.FormField#check()}
		 */
		minTempField.update("1");
		minTempField.check();
		assertFalse(minTempField.isValid());
		assertFalse(minTempField.hasError());
	}

	/**
	 * Test method for {@link com.top_logic.layout.form.constraints.ComparisonDependency#check(Object)}
	 */
	public void testCheckEmptyValue() throws VetoException{
		setupDependency(ComparisonDependency.LOWER_OR_EQUALS_TYPE);
		
		maxTempField.update("");
		maxTempField.check();
		assertTrue(maxTempField.isValid());
		assertTrue(maxTempField.hasValue()); // the empty value is also a value
		assertNull(maxTempField.getValue());
		assertFalse(maxTempField.hasError());
		
		minTempField.update("1");
		minTempField.check();
		assertTrue(minTempField.hasValue());
		assertEquals(Integer.valueOf(1), minTempField.getValue());
		assertTrue(minTempField.isValid()); // as the int constraint and the dependency is valid
	}
	
	
	/**
	 * Returns the test suite
	 * 
	 * @return the test suite
	 */
    public static Test suite () {
        TestSuite theSuite  = new TestSuite(TestComparisonDependency.class);
        return TLTestSetup.createTLTestSetup(theSuite);
    }

	/**
	 * Test method for {@link com.top_logic.layout.form.constraints.ComparisonDependency#reportDependencies()}
	 */
	public final void testReportDependencies() {

	}

}
