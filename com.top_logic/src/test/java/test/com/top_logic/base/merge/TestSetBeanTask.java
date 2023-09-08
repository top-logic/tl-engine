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
import com.top_logic.base.merge.SetBeanTask;
import com.top_logic.basic.Logger;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.Address;

/**
 * Testcase for the {@link com.top_logic.base.merge.CommitTask}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class TestSetBeanTask extends BasicTestCase {

    /**
     * Test perform() without destination (which is a noop) 
     */
    public void testNullDest() throws Exception {
        RootMergeNode aNode      = new RootMergeNode();
        aNode.addMessage(new SetBeanTask("whoCares", "the Fussboden",!SetBeanTask.APPROVEABLE));
        aNode.perform();
    }

    /**
     * Try creating a SetBeanTask with some variants
     */
    public void testSetAttributes() throws Exception {
        KnowledgeBase theBase    = KBSetup.getKnowledgeBase();
        Address       theAddress = Address.createAddress(theBase);
        RootMergeNode aNode      = new RootMergeNode(null, theAddress);
        aNode.addMessage(new SetBeanTask("street1"     , "The Street"      ,!SetBeanTask.APPROVEABLE));
        aNode.addMessage(new SetBeanTask("street2"     , "Not approved"    , SetBeanTask.APPROVEABLE));
        aNode.perform();
        assertEquals("The Street", theAddress.getStreet1());
        assertNull  (              theAddress.getStreet2());
		theAddress.tDelete();
		theBase.commit();
    }

    /**
     * Try creating a SetWrapperTask where setter is not avileable
     */
    public void testNoSetter() throws Exception {
        KnowledgeBase theBase    = KBSetup.getKnowledgeBase();
        Address       theAddress = Address.createAddress(theBase);
        RootMergeNode aNode      = new RootMergeNode(null, theAddress);
        aNode.addMessage(new SetBeanTask("noSuchSetter", "this does matter" ,!SetBeanTask.APPROVEABLE));
        try {
            aNode.perform();
            fail("Expected some Exception here");
        } catch (NoSuchAttributeException expected ) { /* expected */ }
		theAddress.tDelete();
		theBase.commit();
    }

    /**
     * Return the suite of test to execute.
     */
    public static Test suite () {
		return KBSetup.getSingleKBTest(TestSetBeanTask.class);
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
