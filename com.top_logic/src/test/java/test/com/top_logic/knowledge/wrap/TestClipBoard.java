/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.TestPersonSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.Logger;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.Clipboard;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.TLContext;

/** 
 * Test the {@link Clipboard} .
 * 
 */
public class TestClipBoard extends BasicTestCase {

    private Clipboard clip;
	private KnowledgeBase kb;

	/** 
     * Constructor for a special test.
     *
     * @param name function name of the test to execute.
     */
    public TestClipBoard (String name) {
        super (name);
    }
    
    @Override
    protected void setUp() throws Exception {
    	super.setUp();
		Person user = TestPersonSetup.getTestPerson();
		assertNotNull("Clipboard stors clipboard by user, so test user must not be null", user);
		TLContext.getContext().setCurrentPerson(user);
		kb = KBSetup.getKnowledgeBase();
		clip = Clipboard.getInstance(kb);
		assertNotNull(clip);
   }
    
    @Override
    protected void tearDown() throws Exception {
		clip.clear();
		assertTrue(kb.commit());
    	super.tearDown();
    }

	/** Simple and only testCase for now */
	public void testMain() throws Exception {
		assertTrue(!clip.contains(null));

		Document doc = Document.createDocument("test doc 1", "none", kb);
		KnowledgeObject d1 = doc.tHandle();
		kb.commit();

		assertFalse(clip.contains(doc));

		assertTrue(clip.add(doc));
		assertTrue(!clip.add(doc));
		assertTrue(clip.contains(doc));
		assertTrue(clip.remove(doc));
		assertTrue(kb.commit());

		assertTrue(!clip.contains(doc));

		assertTrue(clip.add(doc));
		assertTrue(!clip.add(doc));
		assertTrue(clip.contains(doc));

		clip.clear();
		assertTrue(kb.commit());

		assertTrue(!clip.remove(doc)); // cannot remove twice

		assertTrue(!clip.contains(doc));

		// Hmm , is it ok to have the same object twice with different modes
		// ?
		assertTrue(clip.add(doc, Clipboard.CLIPMODE_CUT));
		assertTrue(!clip.add(doc, Clipboard.CLIPMODE_CUT));
		assertTrue(!clip.add(doc, null));
		assertTrue(!clip.add(doc, Clipboard.CLIPMODE_COPY));
		assertTrue(clip.contains(doc));
		assertTrue(clip.remove(doc));
		assertTrue(kb.commit());
		assertTrue(!clip.remove(doc));

		assertTrue(!clip.contains(doc));

		Clipboard anotherClip = Clipboard.getInstance(kb);

		assertEquals(clip, anotherClip);
	}

    /**
     * Return the suite of tests to perform. 
     */
    public static Test suite () {
		Test t = new TestSuite(TestClipBoard.class);
		t = TestPersonSetup.wrap(t);
		t = ServiceTestSetup.createSetup(t, MimeTypes.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(t);
    }


    public static void main(String[] args) {
        SHOW_TIME             = true;     // for debugging
        Logger.configureStdout();
        junit.textui.TestRunner.run(suite());
    }
    
}
