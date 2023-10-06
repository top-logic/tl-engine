/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.boundsec.manager;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundObject;
import com.top_logic.tool.boundsec.simple.SimpleBoundRole;
import com.top_logic.util.TLContext;

/**
 * Test case for {@link AccessManager}.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class TestAccessManager  extends BasicTestCase {
    
    /** The root bound object */
    private static SimpleBoundObject rootObject;

    public TestAccessManager(String name) {
        super(name);
    }
    
    /** 
     * Main test for now
     */
    public void testBoundSecurity() throws Exception {
        TLSecurityDeviceManager   sdm = TLSecurityDeviceManager.getInstance();
        String authenticationDeviceID = sdm.getDefaultAuthenticationDevice().getDeviceID();

        Person personA = null;
        Person personB = null;
        Person personC = null;
        PersonManager pMgr = PersonManager.getManager();

        AccessManager theAM = AccessManager.getInstance();

		{
            // create three different persons
            personA = Person.byName("AA"); 
            if (personA == null) 
                personA = pMgr.createPerson("AA", authenticationDeviceID, Boolean.FALSE);
            assertTrue(KBSetup.getKnowledgeBase().commit());

            personB = Person.byName("BB");
            if (personB == null) 
				personB = pMgr.createPerson("BB", authenticationDeviceID, Boolean.FALSE);
            personC = Person.byName("CC");
            if (personC == null)  
				personC = pMgr.createPerson("CC", authenticationDeviceID, Boolean.FALSE);

            assertTrue(KBSetup.getKnowledgeBase().commit());
			TLContext context = TLContext.getContext();
            context.setCurrentPerson(personA);
    
            // create some bound objects
            rootObject   = new SimpleBoundObject("root");
            SimpleBoundObject  prjO    = new SimpleBoundObject("Project");
            SimpleBoundObject  subPrjO = new SimpleBoundObject("SubProject");
            SimpleBoundObject  taskO   = new SimpleBoundObject("Task");
            
            // initially, no roles are set
            assertTrue("Expected no initial roles for personA.", theAM.getRoles(personA, rootObject).isEmpty());
            assertTrue("Expected no initial roles for personB.", theAM.getRoles(personB, rootObject).isEmpty());
            assertTrue("Expected no initial roles for personC.", theAM.getRoles(personC, rootObject).isEmpty());
    
            // create some roles
            SimpleBoundRole allRole  = new SimpleBoundRole("All");
            SimpleBoundRole someRole = new SimpleBoundRole("Some");
            SimpleBoundRole readOnly = new SimpleBoundRole("ReadOnly");
            SimpleBoundRole special  = new SimpleBoundRole("Special");
            
            // Add some person roles to bound objects
            rootObject.addRoleForPerson(personA, allRole);
            rootObject.addRoleForPerson(personA, special);
            rootObject.addRoleForPerson(personB, someRole);
            rootObject.addRoleForPerson(personC, readOnly);
            prjO .addRoleForPerson(personB, special);
            
            // Build hierarchy of bound objects
            rootObject.addChild(prjO);
            prjO.addChild(subPrjO);
            prjO.addChild(taskO);
            
            assertTrue(KBSetup.getKnowledgeBase().commit());
            
            // roles are set after commit
            assertTrue("Expected allRole for PersonA.", theAM.getRoles(personA, rootObject).contains(allRole));
            assertTrue("Expected someRole for PersonB.", theAM.getRoles(personB, rootObject).contains(someRole));
            assertTrue("Expected readOnly for PersonC.", theAM.getRoles(personC, rootObject).contains(readOnly));
            assertFalse("Expected no readOnly for PersonA.", theAM.getRoles(personA, rootObject).contains(readOnly));
            assertFalse("Expected no allRole for PersonB.", theAM.getRoles(personB, rootObject).contains(allRole));
            assertFalse("Expected no someRole for PersonC.", theAM.getRoles(personC, rootObject).contains(someRole));

            // Add some person roles to bound objects
            rootObject.removeRoleForPerson(personA, allRole);
            rootObject.removeRoleForPerson(personA, special);

            KBSetup.getKnowledgeBase().commit();
            
            // personA lost roles
            assertTrue("Expected no roles for personA after remove.", theAM.getRoles(personA, rootObject).isEmpty());
            assertTrue("Expected someRole for PersonB after remove.", theAM.getRoles(personB, rootObject).contains(someRole));
            assertTrue("Expected readOnly for PersonC after remove.", theAM.getRoles(personC, rootObject).contains(readOnly));
            assertFalse("Expected no allRole for PersonB after remove.", theAM.getRoles(personB, rootObject).contains(allRole));
            assertFalse("Expected no someRole for PersonC after remove.", theAM.getRoles(personC, rootObject).contains(someRole));
    
            if (personA != null) {
				personA.tDelete();
            }
            if (personB != null) {
				personB.tDelete();
            }
            if (personC != null) {
				personC.tDelete();
            }
            KBSetup.getKnowledgeBase().commit();
		}
    }
    
    /**
     * Reset static variables to conserve memory.
     */
    public void cleanup() {
        rootObject = null;
    }

    /**
     * the suite of tests to execute.
     */
     public static Test suite () {
    	 
    	 return PersonManagerSetup.createPersonManagerSetup(TestAccessManager.class, new TestFactory() {
			
			@Override
			public Test createSuite(Class<? extends Test> testCase, String suiteName) {
				TestSuite theSuite = new TestSuite(suiteName);
				theSuite.addTest(new TestAccessManager("testBoundSecurity"));
				theSuite.addTest(new TestAccessManager("cleanup"));
				return theSuite;
			}
		});
     }

     /** main function for direct testing.
      *
      * @param args  you may supply an objectId as an argument
      */  
     public static void main (String[] args) {
         Logger.configureStdout();
         // KBSetup.CREATE_TABLES = false; // Speed up debugging
         junit.textui.TestRunner.run (suite ());
     }

}


