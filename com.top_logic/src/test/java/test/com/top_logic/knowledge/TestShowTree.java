/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge;

import java.util.Iterator;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.Logger;
import com.top_logic.dob.DataObject;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;


/**
 * A PsuedoTest that shows the Graph of all Knowledgeobjects in a a KB.
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestShowTree extends BasicTestCase {

    /** 
     * Constructor for a special test.
     *
     * @param name fucntion nam of the test to execute.
     */
    public TestShowTree (String name) {
        super (name);
    }


    /** Helper function to show a KA for an KO */
    protected void showDA(String prefix, DataObject aDo) {
        System.out.print(prefix);
        System.out.print(aDo.getIdentifier());
        System.out.print(" (");
        System.out.print(aDo.tTable().getName());
        System.out.println(')');
    }
    
	/** Show Graph Starting with KnowledgeObjects  */
	public void testShowKOGraph() throws Exception {
        KnowledgeBase kb = KBSetup.getKnowledgeBase();
        Iterator it = kb.getAllKnowledgeObjects().iterator();
        while (it.hasNext()) {
            System.out.println();
            KnowledgeObject ko = (KnowledgeObject) it.next();
            showDA("", ko);
            Iterator ass = ko.getIncomingAssociations();
            while (ass.hasNext()) {
                KnowledgeAssociation ka  = (KnowledgeAssociation) ass.next();
                KnowledgeObject      src = ka.getSourceObject();
                showDA("  * ", src);
                showDA("  -> * ", ka);
            }
            ass = ko.getOutgoingAssociations();
            while (ass.hasNext()) {
                KnowledgeAssociation ka  = (KnowledgeAssociation) ass.next();
                KnowledgeObject      dst = ka.getDestinationObject();
                showDA("  * -> ", ka);
                showDA("    -> "  , dst);
            }
        }
	}

    /** Show Graph Starting with KnowledgeAssociation */
    public void testShowKAGraph() throws Exception {
        KnowledgeBase kb = KBSetup.getKnowledgeBase();
        Iterator it = kb.getAllKnowledgeAssociations().iterator();
        while (it.hasNext()) {
            System.out.println();
            KnowledgeAssociation ka = (KnowledgeAssociation) it.next();
            KnowledgeObject src = ka.getSourceObject();
            KnowledgeObject dst = ka.getDestinationObject();
            showDA("",   src);
            showDA("  ->", ka);
            showDA("    ->", dst);
        }
    }

    public static Test suite () {
        return KBSetup.getKBTest(TestShowTree.class);
    }

    /** Main function for direct testing.
     */
    public static void main (String[] args) {
        
        // KBSetup.CREATE_TABLES = false;
        
        // Suppress a lot of Info- and Debug-Messages
        Logger.configureStdout("ERROR");
        
        junit.textui.TestRunner.run (suite ());
    }

    

}
