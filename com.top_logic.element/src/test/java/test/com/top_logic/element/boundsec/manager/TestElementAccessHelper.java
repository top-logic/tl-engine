/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.element.boundsec.manager.ElementAccessHelper;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLModule;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.list.ListUtil;

/**
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class TestElementAccessHelper extends BasicTestCase {

    private static final String ROLE_TEST_ROLE            = "testRole";
    private static final String STRUCTURE_PROJECT_ELEMENT = "projElement";

    /**
     * Test the locating of classifiers
     */
    public void testGetClassifier() throws Exception {
		TLClassifier theClassifier;
		theClassifier = ElementAccessHelper.getClassifier(ListUtil.TLYESNO_YES);
		assertTrue(theClassifier.getName().equals(ListUtil.TLYESNO_YES));
        theClassifier = ElementAccessHelper.getClassifier("thisisnotanexistingclassifiername");
        assertNull(theClassifier);
    }
    
    /**
     * Test the basic setting, getting, and resetting of access rights.
     */
    public void testAddGetClearAccessRights() throws Exception {
		TLClassifier theClassifier = ElementAccessHelper.getClassifier(ListUtil.TLYESNO_YES);
        BoundedRole theRole       = BoundedRole.getRoleByName(ROLE_TEST_ROLE);
        
        ElementAccessHelper.addAccessRight(theClassifier, SimpleBoundCommandGroup.READ .getID(), theRole);
        ElementAccessHelper.addAccessRight(theClassifier, SimpleBoundCommandGroup.WRITE.getID(), theRole);
        
        Map theAccessRights = ElementAccessHelper.getAccessRights(theClassifier);

        assertEquals(1, theAccessRights.size());
        Collection theRights = (Collection) theAccessRights.get(theRole);
        assertEquals(2, theRights.size());
        assertTrue(theRights.contains(SimpleBoundCommandGroup.READ));
        assertTrue(theRights.contains(SimpleBoundCommandGroup.WRITE));
        
        ElementAccessHelper.clearAccessRights(theClassifier);
        
        theAccessRights = ElementAccessHelper.getAccessRights(theClassifier);

        assertEquals(0, theAccessRights.size()); 
    }
    
    public void testClassificationOfMetaAttributes() throws Exception {
        
    }
        
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        
		TLModule scope = TLModelUtil.findModule(STRUCTURE_PROJECT_ELEMENT);
        BoundedRole       theBR   = BoundedRole.createBoundedRole(ROLE_TEST_ROLE);
		theBR.bind(scope);
        theBR.getKnowledgeBase().commit();
    }
    
    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
	protected void tearDown() throws Exception {
        final BoundedRole   theBR = BoundedRole.getRoleByName(ROLE_TEST_ROLE);
        final KnowledgeBase theKB = theBR.getKnowledgeBase();
        theBR.unbind();
        theBR.tDelete();
        theKB.commit();
        super.tearDown();
    }

    /**  Return the suite of tests to perform. */
    public static Test suite() {
		Test test = new TestSuite(TestElementAccessHelper.class);
		test = ServiceTestSetup.createSetup(test, CommandGroupRegistry.Module.INSTANCE);

		return ElementWebTestSetup.createElementWebTestSetup(test);
    }

    /**
     * Main function for direct testing.
     */
    public static void main(String[] args) throws IOException {
        Logger.configureStdout(); // Set to "INFO" to see failed commits
        junit.textui.TestRunner.run (suite ());
    } 
}

