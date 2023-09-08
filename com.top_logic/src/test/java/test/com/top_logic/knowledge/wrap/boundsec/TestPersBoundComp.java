/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap.boundsec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;

/**
 * Testcase for {@link com.top_logic.tool.boundsec.wrap.PersBoundComp}.
 * 
 * @author    <a href="mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 */
public class TestPersBoundComp extends BasicTestCase {

	private KnowledgeBase _kb;

	private Collection<TLObject> _createdItems;

	/**
     * Constructor for TestPersBoundComp.
     */
    public TestPersBoundComp(String name) {
        super(name);
    }

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_kb = KBSetup.getKnowledgeBase();
		_createdItems = new ArrayList<>();
	}

	@Override
	protected void tearDown() throws Exception {
		if (!_createdItems.isEmpty()) {
			try (Transaction tx = _kb.beginTransaction()) {
				_createdItems.forEach(TLObject::tDelete);
				tx.commit();
			}
		}
		super.tearDown();
	}

    /** Test Main function of a PersBoundComp */
    public void testMain() {
		{
			PersBoundComp theView = newPersBoundComp("TestView", toSimpleName("TestView"));
            
			BoundedRole theRole = newBoundedRole("PrinzenRolle");
			BoundCommandGroup theGrp = CommandGroupRegistry.resolve("TestPersBoundComp_write1");
                
            assertNotNull(theView.toString());
 
            theView.addAccess(theGrp, theRole);
            
            assertTrue(_kb.commit());
            assertTrue(theView.rolesForCommandGroup(theGrp).contains(theRole));
            assertTrue(theView.removeAccess(theGrp, theRole));
        }
    }

	private BoundedRole newBoundedRole(String name) {
		BoundedRole result = BoundedRole.createBoundedRole(name, _kb);
		_createdItems.add(result);
		return result;
	}

	private PersBoundComp newPersBoundComp(String secId, ComponentName name) {
		PersBoundComp result = PersBoundComp.createInstance(_kb, name);
		_createdItems.add(result);
		return result;
	}

    /** 
     * Test adding a role more than once.
     */
	public void testMultipleAdd() {
        
		{
			PersBoundComp theView = newPersBoundComp("TestView", toSimpleName("TestView"));
			BoundedRole theRole = newBoundedRole("PrinzenRolle2");
			BoundCommandGroup theGrp = CommandGroupRegistry.resolve("TestPersBoundComp_write1");
                
            assertNotNull(theView.toString());
            assertTrue(theView.addAccess(theGrp, theRole));
			assertTrue(_kb.commit());
            
            Set roles = theView.rolesForCommandGroup(theGrp);
            assertTrue(roles.contains(theRole));
            assertEquals(1, roles.size());

            // add a second time
            
            assertFalse(theView.addAccess(theGrp, theRole));
            // assertTrue(kBase.commit()); // no need for this nothing was changed
            roles = theView.rolesForCommandGroup(theGrp);
            assertTrue(roles.contains(theRole));
            assertEquals(1, roles.size());
            
            // remove
            
            assertTrue(theView.removeAccess(theGrp, theRole));
			assertTrue(_kb.commit());
            // role no longer available for command group
            Set rolesForCommandGroup = theView.rolesForCommandGroup(theGrp);
			assertTrue("There are still roles for '" + theGrp + "' : " + rolesForCommandGroup, rolesForCommandGroup == null || rolesForCommandGroup.size() == 0);
        }
    }

	private ComponentName toSimpleName(String name) {
		return ComponentName.newName(name);
	}

	/**
	 * Test the removeAllAccess() function.
	 */
	public void testRemoveAllAccess() {
		{
			PersBoundComp theComp = newPersBoundComp("DestinationZiel", toSimpleName("DestinationZiel"));
            
			BoundedRole theRole1 = newBoundedRole("PrinzenRolle2");
			BoundedRole theRole2 = newBoundedRole("Sommersault");
			BoundCommandGroup theGrp1 = CommandGroupRegistry.resolve("TestPersBoundComp_write1");
			BoundCommandGroup theGrp2 = CommandGroupRegistry.resolve("TestPersBoundComp_read1");
                
            assertNotNull(theComp.toString());
            assertTrue(theComp.addAccess(theGrp1, theRole1));
            assertTrue(theComp.addAccess(theGrp2, theRole2));
			assertTrue(_kb.commit());
            
            Set roles = theComp.rolesForCommandGroup(theGrp1);
            assertTrue (roles.contains(theRole1));
            assertFalse(roles.contains(theRole2));
            assertEquals(1, roles.size());

            roles = theComp.rolesForCommandGroup(theGrp2);
            assertTrue (roles.contains(theRole2));
            assertFalse(roles.contains(theRole1));
            assertEquals(1, roles.size());

            assertTrue (theComp.removeAllAccess());
			assertTrue(_kb.commit());
            
            Set rolesForCommandGroup1 = theComp.rolesForCommandGroup(theGrp1);
            assertTrue("There are still roles for '" + theGrp1 + "' : " + rolesForCommandGroup1, rolesForCommandGroup1 == null || rolesForCommandGroup1.size() == 0);
            Set rolesForCommandGroup2 = theComp.rolesForCommandGroup(theGrp2);
            assertTrue("There are still roles for '" + theGrp2 + "' : " + rolesForCommandGroup2, rolesForCommandGroup2 == null || rolesForCommandGroup2.size() == 0);

            assertFalse(theComp.removeAllAccess());
        }
    }

	/**
	 * Tests {@link PersBoundComp#setAccess(Map)}
	 */
	public void testSetAccess() {
		PersBoundComp comp;
		BoundedRole role1;
		BoundedRole role2;
		BoundCommandGroup grp1 = CommandGroupRegistry.resolve("TestPersBoundComp_write1");
		BoundCommandGroup grp2 = CommandGroupRegistry.resolve("TestPersBoundComp_read1");
		BoundCommandGroup grp3 = CommandGroupRegistry.resolve("TestPersBoundComp_read2");
		BoundCommandGroup grp4 = CommandGroupRegistry.resolve("TestPersBoundComp_read3");
		try (Transaction tx = _kb.beginTransaction()) {
			comp = newPersBoundComp("DestinationZiel", toSimpleName("DestinationZiel"));
			role1 = newBoundedRole("PrinzenRolle2");
			role2 = newBoundedRole("Sommersault");
			tx.commit();
		}
		try (Transaction tx = _kb.beginTransaction()) {
			Map<BoundedRole, List<BoundCommandGroup>> rights = new MapBuilder<BoundedRole, List<BoundCommandGroup>>()
				.put(role1, list(grp1, grp2))
				.put(role2, list(grp1, grp3))
				.toMap();
			comp.setAccess(rights);
			tx.commit();
			assertEquals(set(role1, role2), comp.rolesForCommandGroup(grp1));
			assertEquals(set(role1), comp.rolesForCommandGroup(grp2));
			assertEquals(set(role2), comp.rolesForCommandGroup(grp3));
			assertEquals(set(), comp.rolesForCommandGroup(grp4));
		}
		try (Transaction tx = _kb.beginTransaction()) {
			Map<BoundedRole, List<BoundCommandGroup>> rights = new MapBuilder<BoundedRole, List<BoundCommandGroup>>()
				.put(role1, list(grp1))
				.put(role2, list(grp1, grp4))
				.toMap();
			comp.setAccess(rights);
			tx.commit();
			assertEquals(set(role1, role2), comp.rolesForCommandGroup(grp1));
			assertEquals(set(), comp.rolesForCommandGroup(grp2));
			assertEquals(set(), comp.rolesForCommandGroup(grp3));
			assertEquals(set(role2), comp.rolesForCommandGroup(grp4));
		}
	}

    /**
     * Return the suite of tests to perform. 
     */
    public static Test suite () {
		return KBSetup.getSingleKBTest(TestPersBoundComp.class,
			ServiceTestSetup.createStarterFactory(CommandGroupRegistry.Module.INSTANCE));
    }

    /** 
     * Main function for direct testing.
     */
    public static void main(String[] args) {
        // SHOW_TIME = false;
        // KBSetup.CREATE_TABLES = false;
        Logger.configureStdout();   // "INFO"
        junit.textui.TestRunner.run(suite());
    }


}
