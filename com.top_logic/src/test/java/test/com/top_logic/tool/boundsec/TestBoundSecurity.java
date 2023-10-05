/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.boundsec;

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.tool.boundsec.simple.SimpleBoundChecker;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringID;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.tree.model.BoundObjectTreeModel;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundObject;
import com.top_logic.tool.boundsec.simple.SimpleBoundRole;
import com.top_logic.util.TLContext;

/**
 * Playground to check the BoundSecurity ideas.
 * 
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestBoundSecurity extends BasicTestCase {
    
    /** The root bound checker */
    private static SimpleBoundChecker rootV;
    
    /** The root bound object */
    private static SimpleBoundObject rootO;

    public TestBoundSecurity(String name) {
        super(name);
    }
    
    /** 
     * Main tescase for now
     */
    public void testBoundSecurity() throws Exception {
		String authenticationDeviceID = TLSecurityDeviceManager.getInstance().getDefaultAuthenticationDevice().getDeviceID();

        Person personA = null;
        Person personB = null;
        Person personC = null;
        PersonManager pMgr = PersonManager.getManager();

		{
            // create three different persons
            personA = pMgr.getPersonByName("AA"); 
            if (personA == null) 
				personA = pMgr.createPerson("AA", authenticationDeviceID, Boolean.FALSE);
            personB = pMgr.getPersonByName("BB");
            if (personB == null) 
				personB = pMgr.createPerson("BB", authenticationDeviceID, Boolean.FALSE);
            personC = pMgr.getPersonByName("CC");
            if (personC == null)  
				personC = pMgr.createPerson("CC", authenticationDeviceID, Boolean.FALSE);
            assertTrue(personA.getKnowledgeBase().commit());
			TLContext context = TLContext.getContext();
            context.setCurrentPerson(personA);
    
            // create some bound objects
            rootO   = new SimpleBoundObject("root");
            SimpleBoundObject  prjO    = new SimpleBoundObject("Project");
            SimpleBoundObject  subPrjO = new SimpleBoundObject("SubProject");
            SimpleBoundObject  taskO   = new SimpleBoundObject("Task");
    
            // create some roles
            SimpleBoundRole allRole  = new SimpleBoundRole("All");
            SimpleBoundRole someRole = new SimpleBoundRole("Some");
            SimpleBoundRole readOnly = new SimpleBoundRole("ReadOnly");
            SimpleBoundRole special  = new SimpleBoundRole("Special");
            
            // Add some person roles to bound objects
            rootO.addRoleForPerson(personA, allRole);
            rootO.addRoleForPerson(personA, special);
            rootO.addRoleForPerson(personB, someRole);
            rootO.addRoleForPerson(personC, readOnly);
            prjO .addRoleForPerson(personB, special);
            
            // Build hierarchy of bound objects
            rootO.addChild(prjO);
            prjO.addChild(subPrjO);
            prjO.addChild(taskO);
    
            // Create some checkers
            rootV   = new SimpleBoundChecker("RootView",     rootO);
            SimpleBoundChecker prjV    = new SimpleBoundChecker("ProjectView",  rootO);
            SimpleBoundChecker officeV = new SimpleBoundChecker("OfficeView",   rootO);
            SimpleBoundChecker adminV  = new SimpleBoundChecker("AdminView",    rootO);
    
            // Build hierarchy of checkers
            prjV.addChild(officeV);
            rootV.addChild(prjV);
            //rootV.addChild(officeV);
            rootV.addChild(adminV);
    
            // Add some comands to checkers
            rootV  .addCommandGroup(SimpleBoundCommandGroup.READ);
            rootV  .addCommandGroup(SimpleBoundCommandGroup.WRITE);
            prjV   .addCommandGroup(SimpleBoundCommandGroup.READ);
            prjV   .addCommandGroup(SimpleBoundCommandGroup.WRITE);
            officeV.addCommandGroup(SimpleBoundCommandGroup.READ);
            adminV .addCommandGroup(SimpleBoundCommandGroup.READ);
            prjV   .addCommandGroup(SimpleBoundCommandGroup.WRITE);
            
            // Add some roles for commands in checkers
            rootV.addRoleForCommandGroup(SimpleBoundCommandGroup.READ, allRole);
            prjV.addRoleForCommandGroup(SimpleBoundCommandGroup.READ, readOnly);
            prjV.addRoleForCommandGroup(SimpleBoundCommandGroup.READ, someRole);
            prjV.addRoleForCommandGroup(SimpleBoundCommandGroup.READ, allRole);
            officeV.addRoleForCommandGroup(SimpleBoundCommandGroup.READ, allRole); 
    
            // Make some assertions
            assertTrue(rootO.getRoles(personA).contains(allRole));
            assertTrue(rootO.getRoles(personB).contains(someRole));
            assertTrue(rootO.getRoles(personC).contains(readOnly));
            assertFalse(rootO.getRoles(personA).contains(readOnly));
            assertFalse(rootO.getRoles(personB).contains(allRole));
            assertFalse(rootO.getRoles(personC).contains(someRole));
            
            assertTrue(rootO.getRoles().contains(allRole));  // for Person A
            assertTrue(rootO.getRoles().contains(special));  // for Person A
            assertTrue(rootO.getRoles().contains(someRole)); // for Person B
            assertTrue(rootO.getRoles().contains(readOnly)); // for Person C
            assertTrue(rootO.getRoles().size()==4);
    
			assertNull(rootV.hideReason(null));
			assertFalse(rootV.allow(SimpleBoundCommandGroup.WRITE, null));
            context.setCurrentPerson(personB);
			assertNotNull(rootV.hideReason(null)); // -> Person B has not the role for default
													// command
												// in rootV
            
			Collection<?> theCommands = rootV.getCommandGroups();
            assertTrue(theCommands.size()==2);      // READ and WRITE
            
            if (personA != null) {
				pMgr.deleteUser(personA);
            }
            if (personB != null) {
				pMgr.deleteUser(personB);
            }
            if (personC != null) {
				pMgr.deleteUser(personC);
            }
            KBSetup.getKnowledgeBase().commit();
        }
    }
    
    public void testBoundObjectTreeModel() throws Exception {
        assertNotNull("The root bound object should not be null.", rootO );
        
        BoundObjectTreeModel objectModel = new BoundObjectTreeModel(rootO);
        BoundObject prjO    = (BoundObject) objectModel.getChild(rootO  , 0);
        BoundObject subPrjO = (BoundObject) objectModel.getChild(prjO   , 0);
        BoundObject taskO   = (BoundObject) objectModel.getChild(prjO   , 1);
        
        assertNotNull("Project should not be null."   , prjO);
        assertNotNull("SubProject should not be null.", subPrjO);
        assertNotNull("Task should not be null"       , taskO );
        
        assertTrue("Index of Project should be 0.", objectModel.getIndexOfChild(rootO, prjO) == 0);
        assertTrue("Index of SubProject should be -1.", objectModel.getIndexOfChild(rootO, subPrjO) == -1);
        assertTrue("There should be one children of root object", objectModel.getChildCount(rootO)==1);
        assertTrue("There should be two children of Project", objectModel.getChildCount(prjO)==2);
        
        BoundObject child1 = (BoundObject) objectModel.getChild(prjO, 1);
		assertTrue("ID of object should be Task", child1.getID().equals(StringID.valueOf("Task")));
        assertTrue ("rootO should be the root of the tree model.", objectModel.getRoot().equals(rootO));
        assertFalse("The root object should not be a leaf.", objectModel.isLeaf(rootO));
        assertTrue ("The Task should be a leaf", objectModel.isLeaf(taskO));  
    }
    
    /**
     * Reset static variables to conserve memory.
     */
    public void cleanup() {
        rootV = null;
        rootO = null;
    }

    /**
     * the suite of tests to execute.
     */
     public static Test suite () {
    	 return PersonManagerSetup.createPersonManagerSetup(TestBoundSecurity.class, new TestFactory() {
			
			@Override
			public Test createSuite(Class<? extends Test> testCase, String suiteName) {
				TestSuite suite = new TestSuite(suiteName);
				suite.addTest(new TestBoundSecurity("testBoundSecurity"));
				suite.addTest(new TestBoundSecurity("testBoundObjectTreeModel"));
				suite.addTest(new TestBoundSecurity("cleanup"));
				return setupBoundHelper(suite);
			}

			private Test setupBoundHelper(Test test) {
				return ServiceTestSetup.createSetup(test, BoundHelper.Module.INSTANCE);
			}
		});
     }

     /** main function for direct testing.
      *
      * @param args  you may supply an objectId as an argument
      */  
     public static void main (String[] args) {
         Logger.configureStdout();
         KBSetup.setCreateTables(false); // Speed up debugging
         junit.textui.TestRunner.run (suite ());
     }

}
