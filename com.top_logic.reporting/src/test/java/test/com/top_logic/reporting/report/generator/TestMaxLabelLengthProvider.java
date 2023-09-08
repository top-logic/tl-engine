/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.report.generator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.NamedConstant;
import com.top_logic.reporting.report.generator.MaxLabelLengthProvider;

/**
 * The TestMaxLabelLengthProvider. 
 * 
 * @author  <a href=mailto:tdi@top-logic.com>Thomas Dickhut</a>
 */
public class TestMaxLabelLengthProvider extends TestCase{
  
    // Tests
    
    /** This methods tests the creation of the {@link MaxLabelLengthProvider}. */
    public void testCreateMaxLabelLengthProvider() {
        // Test max length to low
        try {
            new MaxLabelLengthProvider(9);
            fail("The label length must be greater than 10.");
        }
        catch (IllegalArgumentException iae) {
            // All is ok
        }
        
        // Test the extreme value
        new MaxLabelLengthProvider(10);
    }
    
    /** This method tests the easy case no label is duplicated or to long. */
    public void testLabelLength() {
        MaxLabelLengthProvider labelProvider = new MaxLabelLengthProvider(10);
        
        Object object1 = new NamedConstant("object1");
        String label1  = "A";
        
        Object object2 = new NamedConstant("object2");
        String label2  = "MIDDLE";
        
        Object object3 = new NamedConstant("object3");
        String label3  = "1234567890";
        
        // Add labels
        labelProvider.addLabel(object1, label1);
        labelProvider.addLabel(object2, label2);
        labelProvider.addLabel(object3, label3);
        
        assertEquals(labelProvider.getLabel(object1), label1);
        assertEquals(labelProvider.getLabel(object2), label2);
        assertEquals(labelProvider.getLabel(object3), label3);
    }
    
    /** This method tests a label which is bigger than the max length and duplicated. */
    public void testLabelIsToLongAndDuplicated() {
        MaxLabelLengthProvider labelProvider = new MaxLabelLengthProvider(10);
        
        Object object1 = new NamedConstant("object1");
        String label1  = "ABCDEFGHIJK";
        
        Object object2 = new NamedConstant("object2");
        String label2  = "ABCDEFGHIJK";
        
        Object object3 = new NamedConstant("object3");
        String label3  = "ABCDEFGHIJK";
        
        // Add labels
        labelProvider.addLabel(object1, label1);
        labelProvider.addLabel(object2, label2);
        labelProvider.addLabel(object3, label3);
        
        String storedLabel1 = labelProvider.getLabel(object1);
        assertEquals(storedLabel1.length(), 10);
        
        String storedLabel2 = labelProvider.getLabel(object2);
        assertEquals(storedLabel2.length(), 10);
        assertTrue(storedLabel2.endsWith(MaxLabelLengthProvider.SEP + 2));
        
        String storedLabel3 = labelProvider.getLabel(object3);
        assertEquals(storedLabel3.length(), 10);
        assertTrue(storedLabel3.endsWith(MaxLabelLengthProvider.SEP + 3));
    }
    
    /** This method tests more than 9 duplicates. */
    public void testLabelMoreThan10DulicatedLabels() {
        MaxLabelLengthProvider labelProvider = new MaxLabelLengthProvider(10);
        
        Object object1 = new NamedConstant("object1");
        String label1  = "ABCDEFGHIJK";
        
        Object object2 = new NamedConstant("object2");
        String label2  = "ABCDEFGHIJK";
        
        Object object3 = new NamedConstant("object3");
        String label3  = "ABCDEFGHIJK";
        
        Object object4 = new NamedConstant("object4");
        String label4  = "ABCDEFGHIJK";
        
        Object object5 = new NamedConstant("object5");
        String label5  = "ABCDEFGHIJK";

        Object object6 = new NamedConstant("object6");
        String label6  = "ABCDEFGHIJK";
        
        Object object7 = new NamedConstant("object7");
        String label7  = "ABCDEFGHIJK";
        
        Object object8 = new NamedConstant("object8");
        String label8  = "ABCDEFGHIJK";
        
        Object object9 = new NamedConstant("object9");
        String label9  = "ABCDEFGHIJK";
        
        Object object0 = new NamedConstant("object0");
        String label0  = "ABCDEFGHIJK";
        
        // Add labels
        labelProvider.addLabel(object1, label1);
        labelProvider.addLabel(object2, label2);
        labelProvider.addLabel(object3, label3);
        labelProvider.addLabel(object4, label4);
        labelProvider.addLabel(object5, label5);
        labelProvider.addLabel(object6, label6);
        labelProvider.addLabel(object7, label7);
        labelProvider.addLabel(object8, label8);
        labelProvider.addLabel(object9, label9);
        labelProvider.addLabel(object0, label0);
        
        String storedLabel1 = labelProvider.getLabel(object1);
        assertEquals(storedLabel1.length(), 10);
        
        String storedLabel2 = labelProvider.getLabel(object2);
        assertEquals(storedLabel2.length(), 10);
        assertTrue(storedLabel2.endsWith(MaxLabelLengthProvider.SEP + 2));
        
        String storedLabel3 = labelProvider.getLabel(object3);
        assertEquals(storedLabel3.length(), 10);
        assertTrue(storedLabel3.endsWith(MaxLabelLengthProvider.SEP + 3));
        
        String storedLabel4 = labelProvider.getLabel(object4);
        assertEquals(storedLabel4.length(), 10);
        assertTrue(storedLabel4.endsWith(MaxLabelLengthProvider.SEP + 4));
        
        String storedLabel5 = labelProvider.getLabel(object5);
        assertEquals(storedLabel5.length(), 10);
        assertTrue(storedLabel5.endsWith(MaxLabelLengthProvider.SEP + 5));
        
        String storedLabel6 = labelProvider.getLabel(object6);
        assertEquals(storedLabel6.length(), 10);
        assertTrue(storedLabel6.endsWith(MaxLabelLengthProvider.SEP + 6));
        
        String storedLabel7 = labelProvider.getLabel(object7);
        assertEquals(storedLabel7.length(), 10);
        assertTrue(storedLabel7.endsWith(MaxLabelLengthProvider.SEP + 7));
        
        String storedLabel8 = labelProvider.getLabel(object8);
        assertEquals(storedLabel8.length(), 10);
        assertTrue(storedLabel8.endsWith(MaxLabelLengthProvider.SEP + 8));
        
        String storedLabel9 = labelProvider.getLabel(object9);
        assertEquals(storedLabel9.length(), 10);
        assertTrue(storedLabel9.endsWith(MaxLabelLengthProvider.SEP + 9));
        
        String storedLabel0 = labelProvider.getLabel(object0);
        assertEquals(storedLabel0.length(), 10);
        assertTrue(storedLabel0.endsWith(MaxLabelLengthProvider.SEP + 10));
    }
    
    /**
     * This method tests labels which are bigger than the max length and they
     * are dublicated.
     */
    public void testLabelIsToLong() {
        MaxLabelLengthProvider labelProvider = new MaxLabelLengthProvider(10);
        
        Object object1 = new NamedConstant("object1");
        String label1  = "12345678901";
        
        labelProvider.addLabel(object1, label1);
        
        assertEquals(labelProvider.getLabel(object1).length(), 10);
    }

    /** 
     * This method returns the test in a suite.
     * 
     * @return Returns the test in a suite.
     */
    public static Test suite () {
        return new TestSuite(TestMaxLabelLengthProvider.class);
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
