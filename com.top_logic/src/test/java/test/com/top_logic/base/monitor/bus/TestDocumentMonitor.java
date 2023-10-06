
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.monitor.bus;

import java.io.File;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.bus.DocumentEvent;
import com.top_logic.base.monitor.bus.DocumentMonitor;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.event.bus.Bus;
import com.top_logic.event.bus.Sender;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.TLContext;

/**
 * Tests {@link com.top_logic.base.monitor.bus.DocumentMonitor}.
 *
 * @author  <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class TestDocumentMonitor extends BasicTestCase {

	@Override
	protected void tearDown() throws Exception {
		// delete file to run test more than once
		File theFile = new File("tmp/documents.log");
		if (theFile.exists()) {
			assertTrue("Failed to delete " + theFile, theFile.delete());
		}

		super.tearDown();
	}

    public void testDocumentMonitor () throws Exception {
		Person thePerson = Person.byName("root");
		assertNotNull("no root Person", thePerson);
		TLContext.getContext().setCurrentPerson(thePerson);
		String theDocumentId = "NoSecurityDoc";
		final KnowledgeBase kb = KBSetup.getKnowledgeBase();
		Document.createDocument(theDocumentId, null, kb);

		Sender theSender = new Sender(Bus.CHANGES, Bus.DOCUMENT);
		Document theDocument = Document.createDocument(theDocumentId, null, kb);
		ServiceConfiguration<DocumentMonitor> config;
		config = ApplicationConfig.getInstance().getServiceConfiguration(DocumentMonitor.class);

		DocumentMonitor theMonitor =
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		theMonitor.subscribe();

		assertNotNull(theDocument);
		theSender.send(DocumentEvent.getDocumentEvent(theSender, theDocument, DocumentEvent.CREATED));
		theSender.send(DocumentEvent.getDocumentEvent(theSender, theDocument, DocumentEvent.MODIFIED));
		theSender.send(DocumentEvent.getDocumentEvent(theSender, theDocument, DocumentEvent.DELETED));

		// getting the Received events
		this.check(theMonitor.getAllDocumentEvents(), 3);

        // getting the Received events
		this.check(theMonitor.getAllDocumentEvents(), 3);

        // Get the last received events with double document appearence
		this.check(theMonitor.getLatestDocumentEvents(3, true), 3);

        // Get the last received events without double document appearence
		this.check(theMonitor.getLatestDocumentEvents(3, false), 1);

		theMonitor.unsubscribe();
    }
    
    
	private void check(List<? extends DocumentEvent> aList, int aNumber) {
        int  numberEvents = aList.size ();
    
        //we sent 3 events, so we expect 3 to receive
        assertEquals (aNumber, numberEvents);
    
        for (int i = 0;i < numberEvents; i++) {
			DocumentEvent tmp = aList.get(i);

            this.log("Received Event " + (i + 1) + " - " + tmp.getType());
        }
    }    
    
    private void log(String message) {
        if( Logger.isDebugEnabled(this))
            Logger.debug(message,this);
    }    

    
    /**
     * Start method for JUnit.
     *
     * @return Test
     */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(TestDocumentMonitor.class, Bus.Module.INSTANCE, MimeTypes.Module.INSTANCE));
    }
  
}
