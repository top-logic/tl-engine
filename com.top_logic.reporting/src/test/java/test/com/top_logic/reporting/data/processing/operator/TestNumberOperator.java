/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.data.processing.operator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.Logger;
import com.top_logic.reporting.data.base.value.Value;
import com.top_logic.reporting.data.base.value.common.NullValue;
import com.top_logic.reporting.data.base.value.common.NumberValue;
import com.top_logic.reporting.data.base.value.common.NumericValue;
import com.top_logic.reporting.data.processing.operator.AbstractNumericValueOperator;
import com.top_logic.reporting.data.processing.operator.common.NumericValueDiffOperator;
import com.top_logic.reporting.data.processing.operator.common.NumericValueMaxOperator;
import com.top_logic.reporting.data.processing.operator.common.NumericValueMinOperator;
import com.top_logic.reporting.data.processing.operator.common.NumericValueSumOperator;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class TestNumberOperator extends TestCase {

    private static final Class[] TYPES  = {NumericValue.class};

    private static final Value[] VALUES_EMPTY = new Value[0];

    private static final Value[] VALUES = {new NumberValue(10),
                                           new NumberValue(20),
                                           new NumberValue(30),
                                           new NumberValue(40),
                                           new NumberValue(50)};

    private static final Value[] VALUES_NULL = {new NumberValue(10),
                                           new NumberValue(20),
                                           null,
                                           new NumberValue(40),
                                           new NumberValue(50)};

    private AbstractNumericValueOperator sumOperator;

    private AbstractNumericValueOperator diffOperator;

    private AbstractNumericValueOperator minOperator;

    private AbstractNumericValueOperator maxOperator;

    /**
     * Constructor for some tests on simple NumberOperators.
     */
    public TestNumberOperator(String name) {
        super(name);
    }

    public void testProcess() {
        this._testProcess(this.sumOperator,  null, NullValue.INSTANCE);
        this._testProcess(this.diffOperator, null, NullValue.INSTANCE);
        this._testProcess(this.minOperator,  null, NullValue.INSTANCE);
        this._testProcess(this.maxOperator,  null, NullValue.INSTANCE);

        this._testProcess(this.sumOperator,  VALUES_EMPTY, NullValue.INSTANCE );
        this._testProcess(this.diffOperator, VALUES_EMPTY, NullValue.INSTANCE );
        this._testProcess(this.minOperator,  VALUES_EMPTY, NullValue.INSTANCE );
        this._testProcess(this.maxOperator,  VALUES_EMPTY, NullValue.INSTANCE );

        this._testProcess(this.sumOperator,  VALUES, new NumberValue(150.0d));
        this._testProcess(this.diffOperator, VALUES, new NumberValue(-130.0d));
        this._testProcess(this.minOperator,  VALUES, new NumberValue(10.0d));
        this._testProcess(this.maxOperator,  VALUES, new NumberValue(50.0d));

        this._testProcess(this.sumOperator,  VALUES_NULL, new NumberValue(120.0d));
        this._testProcess(this.diffOperator, VALUES_NULL, new NumberValue(-100.0d));
        this._testProcess(this.minOperator,  VALUES_NULL, new NumberValue(10.0d));
        this._testProcess(this.maxOperator,  VALUES_NULL, new NumberValue(50.0d));
    }

    public void testGetSupportedTypes() {
        this._testGetSupportedTypes(this.sumOperator, TYPES);
        this._testGetSupportedTypes(this.diffOperator, TYPES);
        this._testGetSupportedTypes(this.minOperator, TYPES);
        this._testGetSupportedTypes(this.maxOperator, TYPES);
    }

    public void testGetNeutralValue() {
        this._testGetNeutralValue(this.sumOperator);
        this._testGetNeutralValue(this.diffOperator);
        this._testGetNeutralValue(this.minOperator);
        this._testGetNeutralValue(this.maxOperator);
    }

    public void testGetDisplayName() {
        this._testGetDisplayName(this.sumOperator);
        this._testGetDisplayName(this.diffOperator);
        this._testGetDisplayName(this.minOperator);
        this._testGetDisplayName(this.maxOperator);
    }

    @Override
	protected void setUp() {
        this.sumOperator  = new NumericValueSumOperator("SUM");
        this.diffOperator = new NumericValueDiffOperator("DIFF");
        this.minOperator  = new NumericValueMinOperator("MIN");
        this.maxOperator  = new NumericValueMaxOperator("MAX");
    }

    @Override
	protected void tearDown() {
        this.sumOperator  = null;
        this.diffOperator = null;
        this.minOperator  = null;
        this.maxOperator  = null;
    }

    protected void _testProcess(AbstractNumericValueOperator anOperator,
                                Value[] someValues,
                                Value givenResult) {
        Value  theResult = anOperator.process(someValues);
		boolean same = givenResult.valueEquals(theResult);

        assertTrue(same);
    }

    protected void _testGetSupportedTypes(AbstractNumericValueOperator anOperator,
                                          Class[] someTypes) {
        assertEquals("Failed to get correct number of type for '" + 
                          anOperator.getName() + "'!", 
                          someTypes.length, 
                          anOperator.getSupportedTypes().length);
        assertEquals("Failed to get correct type for '" + 
                          anOperator.getName() + "'!", 
                          someTypes[0], 
                          anOperator.getSupportedTypes()[0]);

        
    }

    protected void _testGetNeutralValue(AbstractNumericValueOperator anOperator) {
        assertNotNull("Neutral value is empty for '" + 
                           anOperator.getName() + "'!", 
                           anOperator.getNeutralValue());
    }

    protected void _testGetDisplayName(AbstractNumericValueOperator anOperator) {
        assertNotNull("Display name is null for '" + 
                           anOperator.getName() + "'!", 
                           anOperator.getDisplayName());
        assertTrue("Display name is empty for '" + 
                           anOperator.getName() + "'!", 
                           anOperator.getDisplayName().length() > 0);
    }

    public static Test suite () {
        return new TestSuite(TestNumberOperator.class);
    }
    
    /**
     * Main method to start this test case.
     *
     * @param    args    Will be ignored.
     */
    public static void main (String[] args) {
        // SHOW_TIME        = false;     // for debugging
    
        Logger.configureStdout();
        
        junit.textui.TestRunner.run (suite ());
    }

}
