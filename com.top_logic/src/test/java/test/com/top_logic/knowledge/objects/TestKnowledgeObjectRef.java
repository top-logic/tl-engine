/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.objects;

import junit.framework.Test;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.KnowledgeObjectRef;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * Test for {@link KnowledgeObjectRef}.
 *
 * @author  <a href="klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 */
public class TestKnowledgeObjectRef extends BasicTestCase  {
    
    /**
     * Default Constructor.
     *
     * @param name (function-) name of test to perform.
     */                                                    
    public TestKnowledgeObjectRef(String name) {
        super(name);
    }
    
    
    /** Main test */
    public void testMain() throws Exception {
        
        KnowledgeBase   kb = KBSetup.getKnowledgeBase();
		KnowledgeObject ko =
			(KnowledgeObject) kb.getObjectByAttribute(Person.OBJECT_NAME, Person.NAME_ATTRIBUTE, "guest_de");
        
        KnowledgeObjectRef koref  = new KnowledgeObjectRef(ko);
        KnowledgeObjectRef koref2 = (KnowledgeObjectRef) assertSerializable(koref);
        
        assertSame  (ko   , koref2.getKnowledgeObject());
        assertEquals(koref, koref2);
        
		koref2 = new KnowledgeObjectRef(kb, Person.OBJECT_NAME, ko.getObjectName());
        KnowledgeObjectRef koref3 = (KnowledgeObjectRef) assertSerializable(koref);

        assertEquals(koref, koref2);
        assertEquals(koref, koref3);

        assertSame(ko, koref3.getKnowledgeObject());
    }
    
    /** Test serializing null objects */
    public void testNull() throws Exception {
        
        KnowledgeBase   kb = KBSetup.getKnowledgeBase();
        KnowledgeObject ko = null;
        
        KnowledgeObjectRef koref  = new KnowledgeObjectRef(ko);
        KnowledgeObjectRef koref2 = (KnowledgeObjectRef) assertSerializable(koref);
        
        assertSame  (ko   , koref2.getKnowledgeObject());
        assertEquals(koref, koref2);
        
		koref2 = new KnowledgeObjectRef(kb, Person.OBJECT_NAME, kb.createID());
        KnowledgeObjectRef koref3 = (KnowledgeObjectRef) assertSerializable(koref);

        assertNull(koref3.getKnowledgeObject());
        assertSame(ko, koref3.getKnowledgeObject());
    }

    /** Test some trivial functions */
    public void testTrivial() throws Exception {
        
        KnowledgeBase   kb = KBSetup.getKnowledgeBase();
		KnowledgeObject ko =
			(KnowledgeObject) kb.getObjectByAttribute(Person.OBJECT_NAME, Person.NAME_ATTRIBUTE, "guest_de");
        
        KnowledgeObjectRef koref1 = new KnowledgeObjectRef(ko);
        KnowledgeObjectRef koref2 = new KnowledgeObjectRef(
			kb.getName(), Person.OBJECT_NAME, ko.getObjectName());

        assertNotNull(koref1.toString());
        assertNotNull(koref2.toString());
        assertEquals(koref1,koref2); 
        assertEquals(koref1.hashCode(),koref2.hashCode()); 

        assertEquals(kb.getName()             ,  koref1.getKBName()); 
		assertEquals(Person.OBJECT_NAME, koref1.getKOType());
		assertEquals(ko.getObjectName(), koref1.getKOId());

        KnowledgeObjectRef empty = new KnowledgeObjectRef(null);
        assertEquals(0, empty.hashCode()); 
    }

    /**
     * the suite of tests to perform
     */
    public static Test suite () {
        return KBSetup.getKBTest(TestKnowledgeObjectRef.class);
    }

    /**
     * main function for direct Testing.
     */
    public static void main (String[] args) {

        Logger.configureStdout();
        junit.textui.TestRunner.run (suite ());
    }

    
}
