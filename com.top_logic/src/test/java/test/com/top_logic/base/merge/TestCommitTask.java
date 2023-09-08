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

import com.top_logic.base.merge.CommitTask;
import com.top_logic.basic.Logger;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * Testcase for the {@link com.top_logic.base.merge.CommitTask}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class TestCommitTask extends BasicTestCase {

    /**
     * Test perform() with a KnowledgeBase (which is a noop) 
     */
    public void testPerform() throws Exception {
        CommitTask ct = new CommitTask(KBSetup.getKnowledgeBase());
        ct.perform(null); 
    }

    /**
     * try to perform a commit() that will fail.
     */
    public void testCommitTaskWrapper() throws DataObjectException {
        KnowledgeBase theBase  = KBSetup.getKnowledgeBase();
		theBase.createKnowledgeObject(Person.OBJECT_NAME);
        CommitTask    ct       = new CommitTask(theBase);
        try {
            ct.perform(null);
			fail("Expected exception as commit() should fail.");
        } catch (Exception expected) { /* expected */ }
    }

    /**
     * Return the suite of test to execute.
     */
    public static Test suite () {
		return KBSetup.getSingleKBTest(TestCommitTask.class);
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
