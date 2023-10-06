/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.layout.security;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.layout.scripting.ApplicationTestSetup;
import test.com.top_logic.layout.scripting.runtime.TestedApplicationSession;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.element.structured.wrap.StructuredElementWrapper;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.Application;
import com.top_logic.layout.scripting.runtime.ApplicationSession;
import com.top_logic.layout.scripting.runtime.action.SimpleActionOp;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundMainLayout;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayoutCommandGroupDistributor;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;

/**
 * Test the security based on the layout components
 * 
 * @author    <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
 */
public class TestLayoutBasedSecurity extends BasicTestCase {
    // Fields
    /**
	 * Test elements setup:
	 * 
	 * <pre>
	 *  CompoundSecurityLayouts
	 *   - testPL1:
	 *         commandGroups="read,write" 
	 *         defaultFor="CompoundSecurityChild1ME"
	 *   - testPL2:
	 *         commandGroups="read,write" 
	 *         defaultFor="CompoundSecurityChild1ME,CompoundSecurityChild3ME"
	 *         handles="CompoundSecurityChild2ME"
	 *   - testPL3:
	 *         commandGroups="read,write" 
	 *         defaultFor="CompoundSecurityChild3ME"
	 *         handles="CompoundSecurityChild1ME,CompoundSecurityChild3ME"
	 * </pre>
	 */
	static BoundedRole testRole;
	static Person test1Person;

	private ApplicationSession session;

    /**
     * CTor with test name
     * 
     * @param name the test name
     */
    public TestLayoutBasedSecurity(String name) {
        super(name);
    }
    

    // Setup and Teardown
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception {
        super.setUp();
		SecurityComponentCache.disableCache();
		Application application = ApplicationTestSetup.getApplication();

		session = application.login(PersonManager.getManager().getRoot(), layoutName());

		KnowledgeBase kBase = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
		BoundMainLayout m = (BoundMainLayout) ((TestedApplicationSession) session).getMasterFrame();
		Transaction t = kBase.beginTransaction();
		m.initBoundComponents(kBase);
		t.commit();

		session.invalidate();

		Transaction tx = kBase.beginTransaction();
		// Create a test roles on root
		testRole = BoundedRole.createBoundedRole("testSec", kBase);
		String authenticationDeviceID =
			TLSecurityDeviceManager.getInstance().getDefaultAuthenticationDevice().getDeviceID();

		// Create test users if necessary
		test1Person = Person.byName(kBase, "testSec");
		if (test1Person == null) {
			test1Person = Person.create(kBase, "testSec", authenticationDeviceID);
		}

		if (null == SecurityComponentCache.getSecurityComponent(toComponentName("testPLD"))) {
			PersBoundComp.createInstance(kBase, toComponentName("testPLD"));
			PersBoundComp.createInstance(kBase, toComponentName("testPL1"));
			PersBoundComp.createInstance(kBase, toComponentName("testPL2"));
			PersBoundComp.createInstance(kBase, toComponentName("testPL3"));
			PersBoundComp.createInstance(kBase, toComponentName("boundInPL1"));
			PersBoundComp.createInstance(kBase, toComponentName("boundInPL2"));
			PersBoundComp.createInstance(kBase, toComponentName("boundInPL3"));
        }
        
		tx.commit();

		session = application.login(test1Person, layoutName());

    }

	static String layoutName() {
		return TestLayoutBasedSecurity.class.getName() + ".xml";
	}
    
	static ComponentName toComponentName(String name) {
		return ComponentName.newName(layoutName(), name);
	}

	@Override
	protected void tearDown() throws Exception {
		session.invalidate();
		session = null;
		KnowledgeBase kb = testRole.getKnowledgeBase();
		Transaction tx = kb.beginTransaction();
		testRole.tDelete();
		TestPerson.deletePersonAndUser(test1Person);
		tx.commit();

		SecurityComponentCache.setupCache();

        super.tearDown();
    }

    /**
     * Test the security
     * 
     * @throws Exception if tested code throws one
     */
    public void testSecurity() throws Exception {        
		session.process(ActionFactory.simpleAction(SecurityTestAction.class));
    }
    
    public static class SecurityTestAction implements SimpleActionOp {
    	@Override
		public void performTest(ActionContext context) throws Exception {
			// Get the Layouts
			KnowledgeBase kBase = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();

	        BoundMainLayout theMain = (BoundMainLayout) context.getMainLayout();
	        
	
			CompoundSecurityLayout thePL1 =
				(CompoundSecurityLayout) theMain.getComponentByName(toComponentName("testPL1"));
			CompoundSecurityLayout thePL2 =
				(CompoundSecurityLayout) theMain.getComponentByName(toComponentName("testPL2"));
			CompoundSecurityLayout thePL3 =
				(CompoundSecurityLayout) theMain.getComponentByName(toComponentName("testPL3"));
	
			Transaction tx1 = kBase.beginTransaction();
	        // Set the role profiles so that testRole has
	        //  - read access for: nothing
	        //  - write acces for: nothing
	        setAccess(testRole,  thePL1, SimpleBoundCommandGroup.READ,  false);  
	        setAccess(testRole,  thePL1, SimpleBoundCommandGroup.WRITE, false);
	        setAccess(testRole,  thePL2, SimpleBoundCommandGroup.READ,  false);
	        setAccess(testRole,  thePL2, SimpleBoundCommandGroup.WRITE, false);
	        setAccess(testRole,  thePL3, SimpleBoundCommandGroup.READ,  false);
	        setAccess(testRole,  thePL3, SimpleBoundCommandGroup.WRITE, false);
	
			DummyType1 dummy1 = DummyType1.newDummyType1("dummy1");
			DummyType2 dummy2 = DummyType2.newDummyType2("dummy2");
			DummyType3 dummy3 = DummyType3.newDummyType3("dummy3");

			tx1.commit();
	        
	            
	        BoundHelper theHelper = BoundHelper.getInstance();
	        
	        // TESTS
	        
	        // 4. View: no role -> no view
	        assertFalse(theHelper.allowView(dummy1, theMain));
	        assertFalse(theHelper.allowView(dummy2, theMain));
	        assertFalse(theHelper.allowView(dummy3, theMain));
	        
	        // 5. View: role without profile -> no view
			Transaction tx2 = kBase.beginTransaction();
	        BoundedRole.assignRole(dummy1, test1Person, testRole);
	        BoundedRole.assignRole(dummy2, test1Person, testRole);
	        BoundedRole.assignRole(dummy3, test1Person, testRole);
			tx2.commit();
	        assertFalse(theHelper.allowView(dummy1, theMain));
	        assertFalse(theHelper.allowView(dummy2, theMain));
	        assertFalse(theHelper.allowView(dummy3, theMain));
	        
			Transaction tx3 = kBase.beginTransaction();
	        // 6. View: role with READ -> view
	        setAccess(testRole,  thePL1, SimpleBoundCommandGroup.READ,  true);          
			tx3.commit();
	        assertTrue(theHelper.allowView(dummy1, theMain));
	        assertFalse(theHelper.allowView(dummy2, theMain));
	        assertFalse(theHelper.allowView(dummy3, theMain));
	        
			Transaction tx4 = kBase.beginTransaction();
	        setAccess(testRole,  thePL2, SimpleBoundCommandGroup.READ,  true);
			tx4.commit();
	        assertTrue(theHelper.allowView(dummy1, theMain));
	        assertFalse(theHelper.allowView(dummy2, theMain));
	        assertTrue(theHelper.allowView(dummy3, theMain));
	
			Transaction tx5 = kBase.beginTransaction();
	        setAccess(testRole,  thePL1, SimpleBoundCommandGroup.READ,  false);          
	        setAccess(testRole,  thePL2, SimpleBoundCommandGroup.READ,  false);          
	        setAccess(testRole,  thePL3, SimpleBoundCommandGroup.READ,  true);
			tx5.commit();
	        assertFalse(theHelper.allowView(dummy1, theMain));
	        assertFalse(theHelper.allowView(dummy2, theMain));
	        assertTrue(theHelper.allowView(dummy3, theMain));
	
	//        setAccess(testRole,  thePL3, SimpleBoundCommandGroup.READ,  false); // Reset last
	//        // 7. View: role with WRITE -> view
	//        setAccess(testRole,  thePL1, SimpleBoundCommandGroup.WRITE,  true);          
	//        assertTrue(kBase.commit());
	//        assertTrue(theHelper.allowView(dummy1, theMain));
	//        assertFalse(theHelper.allowView(dummy2, theMain));
	//        assertFalse(theHelper.allowView(dummy3, theMain));
	//        
	//        setAccess(testRole,  thePL2, SimpleBoundCommandGroup.WRITE,  true);
	//        assertTrue(kBase.commit());
	//        assertTrue(theHelper.allowView(dummy1, theMain));
	//        assertFalse(theHelper.allowView(dummy2, theMain));
	//        assertTrue(theHelper.allowView(dummy3, theMain));
	//
	//        setAccess(testRole,  thePL1, SimpleBoundCommandGroup.WRITE,  false);          
	//        setAccess(testRole,  thePL2, SimpleBoundCommandGroup.WRITE,  false);          
	//        setAccess(testRole,  thePL3, SimpleBoundCommandGroup.WRITE,  true);
	//        assertTrue(kBase.commit());
	//        assertFalse(theHelper.allowView(dummy1, theMain));
	//        assertFalse(theHelper.allowView(dummy2, theMain));
	//        assertTrue(theHelper.allowView(dummy3, theMain));
	        
			Transaction tx6 = kBase.beginTransaction();
	        // 8. View: role with both -> view
	        setAccess(testRole,  thePL3, SimpleBoundCommandGroup.READ,  true);
	        setAccess(testRole,  thePL3, SimpleBoundCommandGroup.WRITE, true);
			tx6.commit();
	        assertFalse(theHelper.allowView(dummy1, theMain));
	        assertFalse(theHelper.allowView(dummy2, theMain));
	        assertTrue(theHelper.allowView(dummy3, theMain));
	        
    	}
    }
    
	public void testStructure() throws Exception {
		session.process(ActionFactory.simpleAction(StructureTestAction.class));
	}

	public static class StructureTestAction implements SimpleActionOp {
		@Override
		public void performTest(ActionContext context) throws Exception {
			KnowledgeBase kBase = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();

			Transaction tx1 = kBase.beginTransaction();
			StructuredElementWrapper theProject = DummyType1.newDummyType1("Project");
			BoundedRole.assignRole(theProject, test1Person, testRole);
			tx1.commit();
			assertTrue(theProject.getLocalAndGlobalAndGroupRoles(test1Person).contains(testRole));

			MainLayout theMain = context.getMainLayout();

			CompoundSecurityLayout thePLD =
				(CompoundSecurityLayout) theMain.getComponentByName(toComponentName("testPLD"));
			CompoundSecurityLayout thePL1 =
				(CompoundSecurityLayout) theMain.getComponentByName(toComponentName("testPL1"));
			CompoundSecurityLayout thePL2 =
				(CompoundSecurityLayout) theMain.getComponentByName(toComponentName("testPL2"));
			CompoundSecurityLayout thePL3 =
				(CompoundSecurityLayout) theMain.getComponentByName(toComponentName("testPL3"));

			thePLD.removeAllAccess();
			thePL1.removeAllAccess();
			thePL2.removeAllAccess();
			thePL3.removeAllAccess();

			Collection theRoles;

			theRoles = thePLD.getRolesForCommandGroup(SimpleBoundCommandGroup.READ);
			assertTrue(theRoles.isEmpty());

			theRoles = thePL1.getRolesForCommandGroup(SimpleBoundCommandGroup.READ);
			assertTrue(theRoles.isEmpty());

			theRoles = thePL2.getRolesForCommandGroup(SimpleBoundCommandGroup.READ);
			assertTrue(theRoles.isEmpty());

			theRoles = thePL3.getRolesForCommandGroup(SimpleBoundCommandGroup.READ);
			assertTrue(theRoles.isEmpty());

			Transaction tx2 = kBase.beginTransaction();
			// Setup role profiles
			setAccess(testRole, thePLD, SimpleBoundCommandGroup.READ, true);
			setAccess(testRole, thePL1, SimpleBoundCommandGroup.READ, true);
			setAccess(testRole, thePL2, SimpleBoundCommandGroup.READ, false);
			setAccess(testRole, thePL3, SimpleBoundCommandGroup.READ, false);

			tx2.commit();

			theRoles = thePLD.getRolesForCommandGroup(SimpleBoundCommandGroup.READ);
			assertTrue(theRoles.contains(testRole));

			theRoles = thePL1.getRolesForCommandGroup(SimpleBoundCommandGroup.READ);
			assertTrue(theRoles.contains(testRole));

			theRoles = thePL2.getRolesForCommandGroup(SimpleBoundCommandGroup.READ);
			assertTrue(theRoles.isEmpty());

			theRoles = thePL3.getRolesForCommandGroup(SimpleBoundCommandGroup.READ);
			assertTrue(theRoles.isEmpty());

			// Test access to layouts

			if (false) {
				/* Does not work because checker is DelegateStructureHTMLTree which does not
				 * invalidates cache. */
				assertTrue(thePLD.allow(theProject));
			}
			assertTrue(thePL1.allow(theProject));
			assertFalse(thePL2.allow(theProject));
			assertFalse(thePL3.allow(theProject));

			// test handles and defaultFor

			BoundHelper theBH = BoundHelper.getInstance();

			Collection<BoundChecker> theCheckers;

			theCheckers = theBH.getBoundCheckers(DummyType1.newDummyType1("dummy1"), (BoundChecker) theMain, null);
			assertCountIterator(2, theCheckers.iterator());

			theCheckers = theBH.getBoundCheckers(DummyType2.newDummyType2("dummy2"), (BoundChecker) theMain, null);
			assertCountIterator(0, theCheckers.iterator());

			theCheckers = theBH.getBoundCheckers(DummyType3.newDummyType3("dummy3"), (BoundChecker) theMain, null);
			assertCountIterator(2, theCheckers.iterator());

			// assertNotNull("SubProject may not be null", project);
		}
	}

	static void setAccess(BoundedRole aRole, CompoundSecurityLayout aChecker, BoundCommandGroup aGroup,
			boolean isAllowed) {
        if (aRole == null || aChecker == null || aGroup == null) {
            return;
        }
        setAccess(aRole, aChecker, aGroup, isAllowed, true);
        aChecker.acceptVisitorRecursively(new CompoundSecurityLayoutCommandGroupDistributor());

    }

    /**
     * Allow or deny access for the current role to the BoundChecker using the CommandGroup.
     * (This is the role profile...)
     * 
     * @param aRole         the role
     * @param aChecker      the checker (that owns aCmdGrp)
     * @param aCmdGrp       the cmd group
     * @param isAllowed     if true add the checker to the role profil, if fals remove it from the profile
     * @param recursive     ir true add/remove the access to/from all child checkers of aChecker
     */
    private static void setAccess(BoundedRole aRole, BoundChecker aChecker, BoundCommandGroup aCmdGrp,  boolean isAllowed, boolean recursive) {
        if (aChecker instanceof BoundComponent ) {
			PersBoundComp persBoundComp = ((BoundComponent) aChecker).getPersBoundComp();
            if (isAllowed) {
				persBoundComp.addAccess(aCmdGrp, aRole);
            }
            else {
				persBoundComp.removeAccess(aCmdGrp, aRole);
            }
        } else if (aChecker instanceof CompoundSecurityLayout ) {
			PersBoundComp persBoundComp = ((CompoundSecurityLayout) aChecker).getPersBoundComp();
            if (isAllowed) {
				persBoundComp.addAccess(aCmdGrp, aRole);
            }
            else {
				persBoundComp.removeAccess(aCmdGrp, aRole);
            }
        } else {
            return;
        }
        
        if (recursive) {
			Collection<? extends BoundChecker> theCh = aChecker.getChildCheckers();
            if (theCh != null) {
				Iterator<? extends BoundChecker> theChildren = theCh.iterator();
                while (theChildren.hasNext()) {
					BoundChecker theChild = theChildren.next();
                    setAccess(aRole, theChild, aCmdGrp,  isAllowed, recursive);
                }
            }
        }
    }

    /** 
     * Return the suite of tests to perform. 
     * 
     * @return the suite
     */
    public static Test suite () {
		return ApplicationTestSetup.setupTestApplication(TestLayoutBasedSecurity.class);
    }

}
