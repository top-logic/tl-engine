/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap.boundsec;

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.objects.KOAttributes;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * Testcase for the {@link com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper}.
 *
 * @author    <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public class TestAbstractBoundWrapper extends BasicTestCase {

    /** Some DSN for the document to be wrapped by ConcreteBoundWrapper */
	private static final String DSN = "file://" + ModuleLayoutConstants.SRC_TEST_DIR
		+ "/test/com/top_logic/knowledge/wrap/boundsec/TestAbstractBoundWrapper.java";

    /**
     * Constructor for TestAbstractBoundWrapper.
     * 
     * @param name name of function to execute
     */
    public TestAbstractBoundWrapper(String name) {
        super(name);
    }
    
    /** Test simple implementation of AbstractBoundWrapper */
    public void testBoundWrapper() throws Exception {
        
        KnowledgeBase theKB = KBSetup.getKnowledgeBase();
        Document    doc     = null;
        Person      person1 = null;
        Person      person2 = null;
        BoundedRole role1   = null;
        BoundedRole role2   = null;
        BoundedRole role3   = null;
        BoundedRole role4   = null;
        try {
            doc = Document.createDocument("aName", DSN, theKB);
			ConcreteBoundWrapper boundObject = new ConcreteBoundWrapper(doc.tHandle());
                                                                    
            // Now we have the wrapper, let's test it
            person1 = Person.byName(theKB, "guest_de");
            person2 = Person.byName(theKB, "guest_en");
            role1=BoundedRole.createBoundedRole("superMan", theKB);
            role2=BoundedRole.createBoundedRole("heroOfTheDay", theKB);
            role3=BoundedRole.createBoundedRole("hotLatina", theKB);
            role4=BoundedRole.createBoundedRole("sweetGirl", theKB);

            BoundedRole.assignRole(boundObject, person1, role1);
            BoundedRole.assignRole(boundObject, person1, role2);
            BoundedRole.assignRole(boundObject, person2, role3);
            BoundedRole.assignRole(boundObject, person2, role4);
            
            assertTrue("commit failed " + theKB, theKB.commit ());

            Collection allRoles = boundObject.getRoles();
            assertTrue("There should be four roles.", allRoles.size()==4);
            Collection rolesPerson1 = boundObject.getRoles(person1);
            assertTrue("There should be two roles for person \"aMan\"", rolesPerson1.size()==2);
            Collection rolesPerson2 = boundObject.getRoles(person2);
            assertTrue("There should be two roles for person \"aWoman\"", rolesPerson2.size()==2);
            assertTrue(rolesPerson2.contains(role4));
        }
        finally {
            // cleanup
            if (doc != null) { 
                doc.tDelete();
            }
            if (role1 != null) {
                role1.tDelete();
            }
            if (role2 != null) { 
                role2.tDelete();
            }
            if (role3 != null) { 
                role3.tDelete();
            }
            if (role4 != null) { 
                role4.tDelete();
            }
                
            theKB.commit();        
        }
    }

    /** Test that wrapper is serializable */
    public void testSerializable() throws Exception {
        
        KnowledgeBase  theKB   = KBSetup.getKnowledgeBase();
		Transaction tx = theKB.beginTransaction();
		KnowledgeObject doc = theKB.createKnowledgeObject("Document");
        try {
			doc.setAttributeValue(AbstractBoundWrapper.NAME_ATTRIBUTE, "testSerializable");
			doc.setAttributeValue(KOAttributes.PHYSICAL_RESOURCE, DSN);
			Document boundObject = (Document) WrapperFactory.getWrapper(doc);
			Document serObject = (Document) assertSerializable(boundObject);
            
            assertSame(boundObject, serObject);
        }
        finally {
            // cleanup
			doc.delete();
                
			tx.rollback();
        }
    }

    /**
     * Simple implementation of 
     * {@link com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper}
     * for testing purposes.
     */
	public static class ConcreteBoundWrapper extends AbstractBoundWrapper {

        /** Constructor */
        public ConcreteBoundWrapper(KnowledgeObject ko) {
            super(ko);
        }


        /** We don't want to test object hierarchy right now */
        @Override
		public Collection<? extends BoundObject> getSecurityChildren() {
            return null;
        }

        /** We don't want to test object hierarchy right now */
        @Override
		public BoundObject getSecurityParent() {
            return null;
        }
    }
    
    /**
     * Return the suite of tests to perform. 
     */
    public static Test suite () {
		Test t = new TestSuite(TestAbstractBoundWrapper.class);
		t = ServiceTestSetup.createSetup(t, MimeTypes.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(t);
    }

    /** Main function for direct testing.
     */
    public static void main(String[] args) {
        SHOW_TIME = false;
        Logger.configureStdout(); // "WARN"
        TestRunner.run(suite());
    }
    

}
