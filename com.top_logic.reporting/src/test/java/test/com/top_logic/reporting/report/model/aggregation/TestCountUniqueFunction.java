/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.report.model.aggregation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.textui.TestRunner;
import test.com.top_logic.TLTestSetup;

import com.top_logic.reporting.report.model.ItemVO;
import com.top_logic.reporting.report.model.aggregation.CountUniqueFunction;
import com.top_logic.reporting.report.model.aggregation.SupportsType;

/**
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class TestCountUniqueFunction extends AbstractAggregationTest {

	public TestCountUniqueFunction(String aTest) {
		super(aTest);
    }
	
	// main functions
	// TODO create a list with duplicated objects
	public void testCalculateResult() {
		TestValueObject i1 = new TestValueObject("value", Integer.valueOf(8));
		TestValueObject i2 = new TestValueObject("value", Integer.valueOf(9));
		TestValueObject i3 = new TestValueObject("value", Integer.valueOf(10));
		TestValueObject i4 = new TestValueObject("value", Integer.valueOf(11));
		TestValueObject i5 = new TestValueObject("value", Integer.valueOf(12));
		TestValueObject i6 = new TestValueObject("value", Integer.valueOf(13));
		TestValueObject i7 = new TestValueObject("value", Integer.valueOf(14));
		TestValueObject i8 = new TestValueObject("value", Integer.valueOf(16));
		TestValueObject i9 = new TestValueObject("value", Integer.valueOf(18));
		TestValueObject i0 = new TestValueObject("value", Integer.valueOf(21));
		//{13, 13, 13, 13, 14, 14, 16, 18, 21};
		List odd = new ArrayList(9);
		odd.add(i6);
		odd.add(i6);
		odd.add(i6);
		odd.add(i6);
		odd.add(i7);
		odd.add(i7);
		odd.add(i8);
		odd.add(i9);
		odd.add(i0);
		//{8, 9, 10, 10, 10, 11, 11, 11, 12, 13};
		List even = new ArrayList(10);
		even.add(i1);
		even.add(i2);
		even.add(i3);
		even.add(i3);
		even.add(i3);
		even.add(i4);
		even.add(i4);
		even.add(i4);
		even.add(i5);
		even.add(i6);
		CountUniqueFunction mf =  getFunction("value");

		ItemVO theVO = mf.getValueObjectFor(odd);
		Number theValue = theVO.getValue();
		assertTrue(theValue.doubleValue() == 5.0);

		theVO = mf.getValueObjectFor(even);
		theValue = theVO.getValue();
		assertTrue(theValue.doubleValue() == 6.0);
	}

	public void testSupportsType() {
		CountUniqueFunction mf = getFunction("value");
        SupportsType types = mf.getClass().getAnnotation(SupportsType.class);
        int[] supportedTypes = types.value();
        Arrays.sort(supportedTypes);
        assertEquals(0, supportedTypes.length);
	}

	protected CountUniqueFunction getFunction(String anAttribute) {
	    return (CountUniqueFunction) super.createFunction(CountUniqueFunction.class, anAttribute);
	}
	
    public static Test suite() {
        return TLTestSetup.createTLTestSetup(TestCountUniqueFunction.class);
    }

    public static void main(String[] args) {
        TestRunner theRunner = new TestRunner();
        theRunner.doRun(suite());
    }
}
