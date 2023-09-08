/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service;

import junit.framework.Test;

import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseClusterTest;

import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeVisitor;
import com.top_logic.knowledge.service.Transaction;

/**
 * Testcase for {@link com.top_logic.knowledge.service.KnowledgeVisitor}.
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestKnowledgeVisitor extends AbstractDBKnowledgeBaseClusterTest {

    /** Test {@link com.top_logic.knowledge.service.KnowledgeVisitor#searchByAttribute}
      */
    public void testSearchByAttribute() throws Exception
    {
        KnowledgeBase kb     = KBSetup.getKnowledgeBase();
        KnowledgeObject treeKOs[] = new KnowledgeObject[10];
        KnowledgeObject found;
        try {
			Transaction tx = kb.beginTransaction();
			treeKOs[0] = newB("0");
			treeKOs[1] = newB("1");
			treeKOs[2] = newB("2");
			treeKOs[3] = newB("3");
			treeKOs[4] = newC("4");
			treeKOs[5] = newC("5");
			treeKOs[6] = newC("6");
			treeKOs[7] = newD("7");
			treeKOs[8] = newD("8");
			treeKOs[9] = newD("9");
            
            //      0           Buzild a Tree like this
            //   1      2
            // 3   4  5   6
            // 7   8      9
            
			newAB(treeKOs[0], treeKOs[1]);
			newAB(treeKOs[1], treeKOs[3]);
			newAB(treeKOs[3], treeKOs[7]);
			newAB(treeKOs[1], treeKOs[4]);
			newAB(treeKOs[4], treeKOs[8]);
			newAB(treeKOs[0], treeKOs[2]);
			newAB(treeKOs[2], treeKOs[5]);
			newAB(treeKOs[2], treeKOs[6]);
			newAB(treeKOs[6], treeKOs[9]);
            
            KnowledgeObject root = treeKOs[0];

			String int42 = "42";
             
			treeKOs[0].setAttributeValue(A1_NAME, "searchByAttribute");
			treeKOs[5].setAttributeValue(A1_NAME, "searchByAttribute2");
			treeKOs[9].setAttributeValue(A2_NAME, int42);
            
            found = KnowledgeVisitor.searchByAttribute(
				root, A1_NAME, "searchByAttribute");
			assertSame(treeKOs[0], found);
 
            found = KnowledgeVisitor.searchByAttribute(
				root, A1_NAME, "searchByAttribute2");
			assertSame(treeKOs[5], found);
            
            found = KnowledgeVisitor.searchByAttribute(
				root, A2_NAME, int42);
			assertSame(treeKOs[9], found);

            found = KnowledgeVisitor.searchByAttribute(
				root, A2_NAME, "This will not be found");
			assertNull(found);

            found = KnowledgeVisitor.searchByAttribute(
				root, C1_NAME, "This will not be found");
			assertNull(found);
    
			found = KnowledgeVisitor.searchByAttribute(
				root, "flexAttribute", null);
			assertSame("Root has null as value for flex attribute", root, found);

            found = KnowledgeVisitor.searchByAttribute(
				root, "flexAttribute", "non null value.");
			assertNull("No object has any flex atribute set", found);

			tx.commit();

			// refetch to be able to resolve object
			refetchNode2();
			// now the same on node 2
			KnowledgeObject rootNode2 = (KnowledgeObject) node2Item(root);
            
            found = KnowledgeVisitor.searchByAttribute(
				rootNode2, A1_NAME, "searchByAttribute");
			assertSame(rootNode2, found);
 
            found = KnowledgeVisitor.searchByAttribute(
				rootNode2, A1_NAME, "searchByAttribute2");
			assertEquals(node2Item(treeKOs[5]), found);
            
            found = KnowledgeVisitor.searchByAttribute(
				rootNode2, A2_NAME, int42);
			assertEquals(node2Item(treeKOs[9]), found);

            found = KnowledgeVisitor.searchByAttribute(
				rootNode2, A2_NAME, "This will not be found");
			assertNull(found);

            found = KnowledgeVisitor.searchByAttribute(
				rootNode2, C1_NAME, "This will not be found");
			assertNull(found);
    
			found = KnowledgeVisitor.searchByAttribute(
				rootNode2, "flexAttribute", null);
			assertSame("Root has null as value for flex attribute", rootNode2, found);

            found = KnowledgeVisitor.searchByAttribute(
				rootNode2, "flexAttribute", "non null value.");
			assertNull("No object has any flex atribute set", found);
        }
        finally {
			Transaction deleteTx = kb.beginTransaction();
            for (int i = 0; i < treeKOs.length; i++) {
                KnowledgeObject ko = treeKOs[i];
                if (ko != null) {
                    kb.delete(ko);
                }
            }
			deleteTx.commit();
        }
    }

    /** Return the suite of tests to perform. 
     */
    public static Test suite () {
		return suite(TestKnowledgeVisitor.class);
    }

}
