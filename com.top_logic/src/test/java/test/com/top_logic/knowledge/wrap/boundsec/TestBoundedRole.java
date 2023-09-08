/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap.boundsec;

import junit.framework.Test;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * Test case for Wrapper of the 
 * {@link com.top_logic.knowledge.objects.KnowledgeObject KnowledgeObjects}
 * of type BoundedRole.
 *
 * @author    <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public class TestBoundedRole extends BasicTestCase {

    /**
     * Constructor for TestBoundRole.
     * 
     * @param name name of function to execute
     */
    public TestBoundedRole(String name) {
        super(name);
    }
    
    /** Test the BoundedRole wrapper */
    public void testBoundedRole() throws Exception {
        BoundedRole theWrapper = BoundedRole.createBoundedRole("someName", KBSetup.getKnowledgeBase());
        KnowledgeObject theWrappedKO = theWrapper.tHandle();
		TLID wrapperID = theWrapper.getID();
		TLID wrappedKOID = KBUtils.getObjectName(theWrappedKO);
        String wrapperName = theWrapper.getName();
        String koName = (String) theWrappedKO.getAttributeValue("name");
        assertTrue(wrapperName.equals("someName"));
        assertTrue(wrapperName.equals(koName));
        assertTrue(wrapperID.equals(wrappedKOID));
    }

    /**
     * Return the suite of tests to perform. 
     */
    public static Test suite () {
		return KBSetup.getKBTest(TestBoundedRole.class);
    }

    /** Main function for direct testing.
     */
    public static void main(String[] args) {
        SHOW_TIME = false;
        Logger.configureStdout("WARN");
        junit.textui.TestRunner.run(suite());
    }
}
