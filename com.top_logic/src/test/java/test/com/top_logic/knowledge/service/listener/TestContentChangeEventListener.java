/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.listener;

import junit.framework.Test;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.AbstractDocumentTest;

import com.top_logic.base.bus.DocumentEvent;
import com.top_logic.base.bus.MonitorEvent;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.event.bus.AbstractReceiver;
import com.top_logic.event.bus.AbstractReceiver.Config;
import com.top_logic.event.bus.Bus;
import com.top_logic.event.bus.BusEvent;
import com.top_logic.knowledge.objects.KOAttributes;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.listener.ContentChangeEventListener;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.TLContext;

/**
 * TestCase for ContentChangeEventListener.
 *
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class TestContentChangeEventListener extends BasicTestCase {

    /**
     * Constructor for TestContentChangeEventListener.
     */
    public TestContentChangeEventListener(String aTest) {
        super(aTest);
    }
    
    public void testDocumentEvents() throws Exception {
        
        KnowledgeBase kBase   = KBSetup.getKnowledgeBase();
		TLContext context = TLContext.getContext();
        Person        root    = PersonManager.getManager().getRoot();
        assertNotNull("No root Person", root);
        context.setCurrentPerson(root);
		Config config;
		try {
			config = (Config) ApplicationConfig.getInstance().getServiceConfiguration(AbstractReceiver.class);
		} catch (ConfigurationException ex) {
			throw new RuntimeException(ex);
		}
		TestReceiver theReceiver = new TestReceiver(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
        theReceiver.subscribe();
        try {
			Transaction tx1 = kBase.beginTransaction();
            // create webfolder to hold the document
            DataAccessProxy theDAP = new DataAccessProxy("repository://");
            String theFolderName =
                theDAP.createNewEntryName(
                    "TestContentChangeEventListenerTestFolder",
                    "");
            DataAccessProxy theFolderDAP =
                theDAP.createContainerProxy(theFolderName);
            WebFolder theFolder = WebFolder.createRootFolder(kBase, theFolderDAP.getPath());
            
            // create new document
			Wrapper theDocument = WrapperFactory.getWrapper(AbstractDocumentTest.createDocumentKO(kBase, null));
			theDocument.setValue("name", "TestContentChangeEventListenerTestDocument");
			theFolder.add(theDocument);

            assertNull("Receiver got unexpected event.", theReceiver.event);
            
            // set physical resource
			theDocument.setValue(KOAttributes.PHYSICAL_RESOURCE, "test");
            theReceiver.event = null;
			tx1.commit();
            
            BusEvent theEvent = theReceiver.event;
            assertNotNull("Event still null.", theEvent);
            assertEquals ("Event no create event.", theEvent.getType(), MonitorEvent.CREATED);
            assertTrue  ("Event no document event", theEvent instanceof DocumentEvent);
			assertEquals("Wrong contained object.", theDocument, theEvent.getMessage());
            
			Transaction tx2 = kBase.beginTransaction();
            // change physical resource through change of attribute
			theDocument.setValue(KOAttributes.PHYSICAL_RESOURCE, "test2");
            theReceiver.event = null;
			tx2.commit();
			Wrapper stableDocument = WrapperHistoryUtils.getWrapper(tx2.getCommitRevision(), theDocument);
            theEvent = theReceiver.event;
            assertEquals("Event no modify event.", theEvent.getType(), MonitorEvent.MODIFIED);
            assertTrue("Event no document event", theEvent instanceof DocumentEvent);
			assertEquals("Wrong contained object.", theDocument, theEvent.getMessage());
            
			Transaction tx3 = kBase.beginTransaction();
            theReceiver.event = null;
			theDocument.tDelete();
			tx3.commit();
            theEvent = theReceiver.event;
            assertNotNull("No delete event received", theEvent);
            assertEquals ("Event no delete event."  , MonitorEvent.DELETED, theEvent.getType());
            assertTrue   ("Event no document event" , theEvent instanceof DocumentEvent);
			assertEquals("Wrong contained object.", stableDocument, theEvent.getMessage());
            
        } finally {
            theReceiver.unsubscribe();
        }
    }

    /**
     * Class for evaluating the tests.
     *
     * This receiver checks the messages send to it. If one test fails, the
     * fail() method will be called with the matching error.
     *
     * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
     */ 
    private class TestReceiver extends AbstractReceiver {

        public BusEvent event;

		public TestReceiver(InstantiationContext context, Config config) {
			super(context, config);
        }

        @Override
		public void receive (BusEvent anEvent) {
            this.event = anEvent; 
        }
        
    }

    public static Test suite() {
		Test innerTest =
			ServiceTestSetup.createSetup(TestContentChangeEventListener.class, Bus.Module.INSTANCE,
				MimeTypes.Module.INSTANCE, ContentChangeEventListener.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(innerTest);
    }

    public static void main(String[] args) {
        Logger.configureStdout(); // "INFO"
        junit.textui.TestRunner.run(suite());
    }


}
