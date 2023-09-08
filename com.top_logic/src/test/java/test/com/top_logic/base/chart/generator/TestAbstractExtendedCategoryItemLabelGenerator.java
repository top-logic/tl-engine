/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.chart.generator;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jfree.data.category.DefaultCategoryDataset;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.chart.dataset.ExtendedCategoryDataset;
import com.top_logic.base.chart.generator.item.ExtendedCategoryItemLabelGenerator;

/**
 * Tests the {@link ExtendedCategoryItemLabelGenerator}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class TestAbstractExtendedCategoryItemLabelGenerator extends BasicTestCase {

    /**
     * Test the {@link ExtendedCategoryItemLabelGenerator}.
     */
    public void testItemLabelGenerator() {
        String theCategoryA = "Category A";
        String theCategoryB = "Category B";
        String theSeries1   = "Series 1";
        String theObj1      = "Obj1";
        String theObj2      = "Obj2";
        
        ExtendedCategoryDataset theDataset = new ExtendedCategoryDataset();
        theDataset.addValue(5, theSeries1, theCategoryA, theObj1);
        theDataset.addValue(5, theSeries1, theCategoryB, theObj2);
        
        DummyItemLabelGenerator theGenerator = new DummyItemLabelGenerator();
        assertEquals(theCategoryA, theGenerator.generateColumnLabel(theDataset, 0));
        assertEquals(theCategoryB, theGenerator.generateColumnLabel(theDataset, 1));
        assertEquals(theSeries1,   theGenerator.generateRowLabel(theDataset, 0));
        assertEquals(theObj1,      theGenerator.generateLabel(theDataset, 0, 0));
        assertEquals(theObj2,      theGenerator.generateLabel(theDataset, 0, 1));
        
        DefaultCategoryDataset theNormalDataset = new DefaultCategoryDataset();
        theNormalDataset.addValue(5, theSeries1, theCategoryA);
        theNormalDataset.addValue(5, theSeries1, theCategoryB);
        assertEquals("", theGenerator.generateLabel(theNormalDataset, 0, 0));
        assertEquals("", theGenerator.generateLabel(theNormalDataset, 0, 1));
    }
    
    /** 
     * This method returns the test suite.
     */
    public static Test suite() {
        return TLTestSetup.createTLTestSetup(new TestSuite(TestAbstractExtendedCategoryItemLabelGenerator.class));
    }
    
    /**
     * The main function can be used for direct testing.
     */
    public static void main(String[] args) {
        // Logger.configureStdout();
        
        junit.textui.TestRunner.run(suite());
    }

    /**
     * The DummyItemLabelGenerator calls only the toString method of the given string.
     */
    /*package protected*/ class DummyItemLabelGenerator extends ExtendedCategoryItemLabelGenerator {

        @Override
		protected String getItemLabelFor(Object anObject) {
            return anObject.toString();
        }
        
    }
    
}

