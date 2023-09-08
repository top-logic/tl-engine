/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.boundsec.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.util.CommandGroupComparator;

/**
 * Testcase for the {@link CommandGroupComparator}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class TestCommandGroupComparator extends TestCase {

    /**
     * Main and single tsaces here.
     */
    public void testCommandGroupComparator() {
        
        CommandGroupComparator cgc = CommandGroupComparator.INSTANCE;
        
        assertEquals(0, cgc.compare(null , null));
        assertEquals(0, cgc.compare(SimpleBoundCommandGroup.READ ,
                                    SimpleBoundCommandGroup.READ));
        assertEquals(0, cgc.compare(SimpleBoundCommandGroup.WRITE ,
                                    SimpleBoundCommandGroup.WRITE));
        assertEquals(0, cgc.compare(SimpleBoundCommandGroup.DELETE ,
                                    SimpleBoundCommandGroup.DELETE));
        assertEquals(0, cgc.compare( SimpleBoundCommandGroup.WRITE , null)); // Strange

        assertTrue(cgc.compare(null ,
                               SimpleBoundCommandGroup.WRITE) < 0);
        assertTrue(cgc.compare(SimpleBoundCommandGroup.READ ,
                               SimpleBoundCommandGroup.WRITE) > 0);
        assertTrue(cgc.compare(SimpleBoundCommandGroup.READ ,
                               SimpleBoundCommandGroup.DELETE) > 0);
        assertTrue(cgc.compare(SimpleBoundCommandGroup.WRITE ,
                               SimpleBoundCommandGroup.DELETE) > 0);

    }

    /**
     * the suite of tests to execute.
     */
     public static Test suite () {
         TestSuite theSuite = new TestSuite (TestCommandGroupComparator.class);
         
         return theSuite;
     }

     /** main function for direct testing.
      *
      * @param args  you may supply an objectId as an argument
      */  
     public static void main (String[] args) {
         junit.textui.TestRunner.run (suite ());
     }


}
