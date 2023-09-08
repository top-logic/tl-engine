/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.report.range;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.reporting.ReportingSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.Accessor;
import com.top_logic.reporting.report.model.ItemVO;
import com.top_logic.reporting.report.model.aggregation.AggregationFunction;
import com.top_logic.reporting.report.model.aggregation.AggregationFunctionConfiguration;
import com.top_logic.reporting.report.model.aggregation.AggregationFunctionFactory;
import com.top_logic.reporting.report.model.aggregation.AverageFunction;
import com.top_logic.reporting.report.model.aggregation.CountFunction;
import com.top_logic.reporting.report.model.aggregation.MaxFunction;
import com.top_logic.reporting.report.model.aggregation.MinFunction;
import com.top_logic.reporting.report.model.aggregation.SumFunction;


/**
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class TestRangeFunction extends BasicTestCase {
    
    public static final String  ATTRIBUTE        = "value";

    private boolean ignoreNullValues = true;
    
    public void testCountFunction() {
        AggregationFunction theFunction = createFunction(CountFunction.class, ATTRIBUTE);
        
        theFunction.setAttributeAccessor(getAccessor());
        
        assertEquals(0, theFunction.getValueObjectFor(Collections.EMPTY_LIST).getValue().intValue());
        
        // do some counts
        Collection theColl = getNumberVOs();
        ItemVO theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertEquals(theColl.size(), theItemVO.getValue().intValue());
        
        theColl = getDateVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertEquals(theColl.size(), theItemVO.getValue().intValue());
        
        theColl = getStringVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertEquals(theColl.size(), theItemVO.getValue().intValue());

        
        // do some counts
        theFunction.setIgnoreNullValues(ignoreNullValues);
        assertEquals(0.0, theFunction.getValueObjectFor(Collections.EMPTY_LIST).getValue().doubleValue(), EPSILON);
        
        theColl = getNumberVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertEquals(theColl.size()-1, theItemVO.getValue().intValue());
        
        theColl = getDateVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertEquals(theColl.size()-1, theItemVO.getValue().intValue());
        
        theColl = getStringVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertEquals(theColl.size()-1, theItemVO.getValue().intValue());
    }

    public void testSumFunction() {
        AggregationFunction theFunction = createFunction(SumFunction.class, ATTRIBUTE);
        theFunction.setAttributeAccessor(getAccessor());
        assertEquals(0, theFunction.getValueObjectFor(Collections.EMPTY_LIST).getValue().intValue());
        
        // do some counts
        Collection theColl = getNumberVOs();
        ItemVO theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertEquals(54.0, theItemVO.getValue().doubleValue(), 0);        
        
        theColl = getDateVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertTrue(Double.isNaN(theItemVO.getValue().doubleValue()));
        
        theColl = getStringVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertTrue(Double.isNaN(theItemVO.getValue().doubleValue()));

        
        // do some counts
        theFunction.setIgnoreNullValues(ignoreNullValues);
        assertEquals(0.0, theFunction.getValueObjectFor(Collections.EMPTY_LIST).getValue().doubleValue(), EPSILON);
        
        theColl = getNumberVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertEquals(54.0, theItemVO.getValue().doubleValue(), 0);
        
        theColl = getDateVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertTrue(Double.isNaN(theItemVO.getValue().doubleValue()));
        
        theColl = getStringVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertTrue(Double.isNaN(theItemVO.getValue().doubleValue()));
    }
    
    public void testAverageFunction() {
        AggregationFunction theFunction = createFunction(AverageFunction.class, ATTRIBUTE);
        theFunction.setAttributeAccessor(getAccessor());
        assertEquals(0, theFunction.getValueObjectFor(Collections.EMPTY_LIST).getValue().intValue());
        
        // do some counts
        Collection theColl = getNumberVOs();
        ItemVO theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertEquals(54.0/theColl.size(), theItemVO.getValue().doubleValue(), 0);        

        theColl = getDateVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertTrue(Double.isNaN(theItemVO.getValue().doubleValue()));
        
        theColl = getStringVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertTrue(Double.isNaN(theItemVO.getValue().doubleValue()));

        
        // do some counts
        theFunction.setIgnoreNullValues(ignoreNullValues);
        assertEquals(0.0, theFunction.getValueObjectFor(Collections.EMPTY_LIST).getValue().doubleValue(), EPSILON);
        
        theColl = getNumberVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertEquals(54.0/(theColl.size()-1), theItemVO.getValue().doubleValue(), 0);
        
        theColl = getDateVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertTrue(Double.isNaN(theItemVO.getValue().doubleValue()));
        
        theColl = getStringVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertTrue(Double.isNaN(theItemVO.getValue().doubleValue()));
    }
    
    public void testMinFunction() {
        AggregationFunction theFunction = createFunction(MinFunction.class, ATTRIBUTE);
        theFunction.setAttributeAccessor(getAccessor());
        assertEquals(0.0, theFunction.getValueObjectFor(Collections.EMPTY_LIST).getValue().doubleValue(), EPSILON);
        
        // do some counts
        Collection theColl = getNumberVOs();
        ItemVO theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertEquals(0.0, theItemVO.getValue().doubleValue(), 0);        

        theColl = getDateVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertEquals(0.0, theItemVO.getValue().doubleValue(), 0);
        
        theColl = getStringVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertEquals(0.0, theItemVO.getValue().doubleValue(), 0);

        
        // do some counts
        theFunction.setIgnoreNullValues(ignoreNullValues);
        assertEquals(0.0, theFunction.getValueObjectFor(Collections.EMPTY_LIST).getValue().doubleValue(), EPSILON);
        
        theColl = getNumberVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertEquals(0.0, theItemVO.getValue().doubleValue(), 0);
        
        theColl = getDateVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertEquals(0.0, theItemVO.getValue().doubleValue(), 0);
        
        theColl = getStringVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertEquals(0.0, theItemVO.getValue().doubleValue(), 0);
    }
    
    public void testMaxFunction() {
        AggregationFunction theFunction = createFunction(MaxFunction.class, ATTRIBUTE);
        theFunction.setAttributeAccessor(getAccessor());
        assertEquals(0.0, theFunction.getValueObjectFor(Collections.EMPTY_LIST).getValue().doubleValue(), 0);
        
        // do some counts
        Collection theColl = getNumberVOs();
        ItemVO theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertEquals(9.0, theItemVO.getValue().doubleValue(), EPSILON);        
        
        theColl = getDateVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertEquals(0.0, theItemVO.getValue().doubleValue(), 0);
        
        theColl = getStringVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertEquals(0.0, theItemVO.getValue().doubleValue(), 0);
        
        
        // do some counts
        theFunction.setIgnoreNullValues(ignoreNullValues);
        assertEquals(0.0, theFunction.getValueObjectFor(Collections.EMPTY_LIST).getValue().doubleValue(), EPSILON);
        
        theColl = getNumberVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertEquals(9.0, theItemVO.getValue().doubleValue(), 0);
        
        theColl = getDateVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertEquals(0.0, theItemVO.getValue().doubleValue(), 0);
        
        theColl = getStringVOs();
        theItemVO = theFunction.getValueObjectFor(theColl);
        assertNotNull(theItemVO);
        assertEquals(0.0, theItemVO.getValue().doubleValue(), 0);
    }
    
    private Accessor getAccessor() {
        return new ValueObjectAccessor();
    }
    
    // size 12
    // null 1
    // same 2
    // sum  54
    private Collection getNumberVOs() {
        Set theColl = new HashSet();
        
        for (int i=0; i<5; i++) {
			theColl.add(new ValueObject(Integer.valueOf(i)));
        }

        for (int i=5; i<10; i++) {
			theColl.add(new ValueObject(Double.valueOf(i)));
        }
        
		theColl.add(new ValueObject(Integer.valueOf(9)));
        theColl.add(new ValueObject(null));
        
        return theColl;
    }
    
    private Collection getStringVOs() {
        Set theColl = new HashSet();
        
        for (int i=0; i<10; i++) {
            theColl.add(new ValueObject(String.valueOf(i)));
        }
        theColl.add(new ValueObject(String.valueOf(9)));
        theColl.add(new ValueObject(null));
        
        return theColl;
    }
    
    private Collection getDateVOs() {
        Set theColl = new HashSet();
        
        for (int i=0; i<10; i++) {
            theColl.add(new ValueObject(new Date(1907, i, 1)));
        }
        
        theColl.add(new ValueObject(new Date(1907, 9, 1)));
        theColl.add(new ValueObject(null));
        
        return theColl;
    }
    
    private class ValueObject {
        Object value;
        
        public ValueObject(Object aValue) {
            this.value = aValue;
        }
        
        public Object getValue() {
            return this.value;
        }
    }
    
    public static class ValueObjectAccessor implements Accessor {
        
        @Override
		public Object getValue(Object object, String property) {
            ValueObject theObject = (ValueObject) object;
            if (ATTRIBUTE.equals(property)) {
                return theObject.getValue();
            }
            return null;
        }
        
        @Override
		public void setValue(Object object, String property, Object value) {
        }
    }
    /** 
     * This method returns the test in a suite.
     * 
     * @return Returns the test in a suite.
     */
    public static Test suite () {
        return ReportingSetup.createReportingSetup(TestRangeFunction.class);
    }
    
    protected AggregationFunction createFunction(Class aFunctionClass, String anAttributePath) {
        
        try {
            AggregationFunctionConfiguration theConf = AggregationFunctionFactory.getInstance().createAggregationFunctionConfiguration(aFunctionClass);
            theConf.setAttributePath(anAttributePath);
            
            return AggregationFunctionFactory.getInstance().getAggregationFunction(theConf);
        } catch (ConfigurationException c) {
            throw new RuntimeException(c);
        }
        
    }
    
    
    
    /**
     * This method can be used for direct testing.
     * 
     * @param args A string array of argument.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }

}
