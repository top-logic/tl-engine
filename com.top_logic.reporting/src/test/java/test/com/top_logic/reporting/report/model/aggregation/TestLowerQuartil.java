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
import com.top_logic.reporting.report.model.aggregation.LowerQuartil;
import com.top_logic.reporting.report.model.aggregation.SupportsType;

/**
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class TestLowerQuartil extends AbstractAggregationTest {

	public TestLowerQuartil(String aTest) {
		super(aTest);
    }

	// main functions
	public void testCalculateResult() {
		int[] odd = new int[] {13, 13, 13, 13, 14, 14, 16, 18, 21};    // size 9
		int[] even = new int[] {8, 9, 10, 10, 10, 11, 11, 11, 12, 13}; // size 10
		LowerQuartil mf = getFunction("value");

		ArrayList someObjects = new ArrayList();
		for(int i = 0; i < odd.length; i++) {
			TestValueObject theObj = new TestValueObject("value", Integer.valueOf(odd[i]));
			someObjects.add(theObj);
		}
		ItemVO theVO = mf.getValueObjectFor(someObjects);
		Number theValue = theVO.getValue();
		assertTrue(theValue.doubleValue() == 13.0);
		someObjects.clear();
		for(int i = 0; i < even.length; i++) {
			TestValueObject theObj = new TestValueObject("value", Integer.valueOf(even[i]));
			someObjects.add(theObj);
		}
		theVO = mf.getValueObjectFor(someObjects);
		theValue = theVO.getValue();
		assertTrue(theValue.doubleValue() == 10.0);
	}

	public void testSupportsType() {
		LowerQuartil mf = getFunction("value");
		
		SupportsType types = mf.getClass().getAnnotation(SupportsType.class);
        int[] supportedTypes = types.value();
        Arrays.sort(supportedTypes);
        
        assertTrue(Arrays.binarySearch(supportedTypes, LegacyTypeCodes.TYPE_FLOAT) > -1);
        assertTrue(Arrays.binarySearch(supportedTypes, LegacyTypeCodes.TYPE_LONG) > -1);
        assertFalse(Arrays.binarySearch(supportedTypes, LegacyTypeCodes.TYPE_BOOLEAN) > -1);
	}

	protected LowerQuartil getFunction(String anAttribute) {
	    return (LowerQuartil) super.createFunction(LowerQuartil.class, anAttribute);
	}
	
    public static Test suite() {
        return TLTestSetup.createTLTestSetup(TestLowerQuartil.class);
    }

    public static void main(String[] args) {
        TestRunner theRunner = new TestRunner();
        theRunner.doRun(suite());
    }
}
