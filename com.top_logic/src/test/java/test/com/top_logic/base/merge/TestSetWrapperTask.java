/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.merge;

import junit.framework.Test;
import junit.textui.TestRunner;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.merge.RootMergeNode;
import com.top_logic.base.merge.SetWrapperTask;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.Address;

/**
 * Testcase for the {@link com.top_logic.base.merge.CommitTask}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class TestSetWrapperTask extends BasicTestCase {

    /**
     * Test perform() without destination (which is a noop) 
     */
    public void testNullDest() throws Exception {
        RootMergeNode aNode      = new RootMergeNode();
        aNode.addMessage(new SetWrapperTask("street1", "The Street",!SetWrapperTask.APPROVEABLE));
        aNode.perform();
    }

    /**
     * Try creating a SetWrapperTask with some variants
     */
    public void testSetAttributes() throws Exception {
        KnowledgeBase theBase    = KBSetup.getKnowledgeBase();
        Address       theAddress = Address.createAddress(theBase);
        RootMergeNode aNode      = new RootMergeNode(null, theAddress);
        aNode.addMessage(new SetWrapperTask("street1", "The Street"   ,!SetWrapperTask.APPROVEABLE));
        aNode.addMessage(new SetWrapperTask("street2", "Not approved" , SetWrapperTask.APPROVEABLE));

        // TODO KHA: Attribute does not exist. What does this "test" intend? Testing that an error is ignored???
        // aNode.addMessage(new SetWrapperTask("streetX", "Not there"    , !SetWrapperTask.APPROVEABLE));
        
        aNode.perform();
        assertEquals("The Street", theAddress.getStreet1());
        assertNull  (              theAddress.getStreet2());
		theAddress.tDelete();
		theBase.commit();
    }

    /**
     * Return the suite of test to execute.
     */
    public static Test suite () {
		return KBSetup.getSingleKBTest(TestSetWrapperTask.class);
    }
    
    /**
     * Main method to start this test case.
     *
     * @param    args    Will be ignored.
     */
    public static void main (String[] args) {
        
        KBSetup.setCreateTables(false); // do not really need a KBase here
        
        Logger.configureStdout();
        TestRunner.run (suite ());
    }
}
