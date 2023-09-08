/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.report.model.aggregation;

import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.Test;
import junit.textui.TestRunner;
import test.com.top_logic.TLTestSetup;

import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.reporting.report.model.ItemVO;
import com.top_logic.reporting.report.model.aggregation.AggregationFunctionConfiguration;
import com.top_logic.reporting.report.model.aggregation.SumFunction;
import com.top_logic.reporting.report.model.aggregation.SupportsType;

/**
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class TestSumFunction extends AbstractAggregationTest {

	public TestSumFunction(String aTest) {
		super(aTest);
    }
	
	public void testMedianFunctionString() {
		try {
			/*SumFunction mf = */ getFunction(null);
			fail(AggregationFunctionConfiguration.class.getName() + " with null as attributePath must not be allowed");
		}
		catch(RuntimeException npe) {
			// do nothing, this is expected
		}
		
		SumFunction mf = getFunction("value");
		assertTrue("value".equals(mf.getAttributePath()));
		assertFalse(mf.ignoreNullValues());

		
	}

	// main functions
	public void testCalculateResult() {
		int[] odd  = new int[] {13, 13, 13, 13, 14, 14, 16, 18, 21};
		int[] even = new int[] {8, 9, 10, 10, 10, 11, 11, 11, 12, 13};
		
		SumFunction mf = getFunction("value");

		ArrayList<TestValueObject> someObjects = new ArrayList<>();
		for(int i = 0; i < odd.length; i++) {
			TestValueObject theObj = new TestValueObject("value", Integer.valueOf(odd[i]));
			someObjects.add(theObj);
		}
		
		ItemVO theVO = mf.getValueObjectFor(someObjects);
		
		Number theValue = theVO.getValue();
		
		assertEquals(135.0, theValue.doubleValue(), 0);
		
		someObjects.clear();
		for(int i = 0; i < even.length; i++) {
			TestValueObject theObj = new TestValueObject("value", Integer.valueOf(even[i]));
			someObjects.add(theObj);
		}
		theVO = mf.getValueObjectFor(someObjects);
		theValue = theVO.getValue();
		
		assertEquals(105.0, theValue.doubleValue(), 0);
	}

	public void testSupportsType() {
		SumFunction mf = getFunction("value");
        SupportsType types = mf.getClass().getAnnotation(SupportsType.class);

        int[] supportedTypes = types.value();
        Arrays.sort(supportedTypes);
        
        assertTrue(Arrays.binarySearch(supportedTypes, LegacyTypeCodes.TYPE_FLOAT) > -1);
        assertTrue(Arrays.binarySearch(supportedTypes, LegacyTypeCodes.TYPE_LONG) > -1);
        assertFalse(Arrays.binarySearch(supportedTypes, LegacyTypeCodes.TYPE_BOOLEAN) > -1);
	}
	
    protected SumFunction getFunction(String anAttribute) {
        return (SumFunction) super.createFunction(SumFunction.class, anAttribute);
    }

    public static Test suite() {
        return TLTestSetup.createTLTestSetup(TestSumFunction.class);
    }

    public static void main(String[] args) {
        TestRunner theRunner = new TestRunner();
        theRunner.doRun(suite());
    }
}
