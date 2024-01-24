/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.event.logEntry;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.service.KBTestMeta;

import com.top_logic.base.locking.LockService;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.dob.DataObjectException;
import com.top_logic.event.ModelTrackingService;
import com.top_logic.event.bus.Bus;
import com.top_logic.event.bus.BusEvent;
import com.top_logic.event.bus.IReceiver;
import com.top_logic.event.logEntry.DayEntryArchiverTask;
import com.top_logic.event.logEntry.LogEntry;
import com.top_logic.event.logEntry.LogEntryConfiguration;
import com.top_logic.event.logEntry.LogEntryReceiver;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * Abstarct superclass fro the Testcases in this package.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public abstract class TestLogHelper extends BasicTestCase {

    public TestLogHelper() {
        super();
    }

    /** 
     * Create a new TestLogHelper for ginven test-name.
     */
    public TestLogHelper(String aName) {
        super(aName);
    }
    
    
	protected void createLogEntry(Date aDate) throws DataObjectException {
        
        Bus theBus = Bus.getInstance ();
        
        LogEntry.setTestDate(aDate);
		ServiceConfiguration<LogEntryReceiver> config;
		try {
			config = ApplicationConfig.getInstance().getServiceConfiguration(LogEntryReceiver.class);
		} catch (ConfigurationException ex) {
			throw new RuntimeException(ex);
		}
		LogEntryReceiver receiver = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
        long             start    = receiver.getNumbersOfEventsProcessed();
        theBus.subscribe (receiver, ModelTrackingService.getTrackingService());
        String           dd = StringServices.getIso8601Format().format(aDate);
        
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction tx = kb.beginTransaction();
		Wrapper trigger = createTrigger(kb, dd);
		Wrapper source = createSource(kb, dd);
		tx.commit();

        ModelTrackingService.sendModifyEvent(trigger, source);
        
        long end = receiver.getNumbersOfEventsProcessed();
        assertEquals("event not processed or not counted", start+1, end);       
        
        theBus.unsubscribe(receiver, ModelTrackingService.getTrackingService());
    }

	public static Wrapper createSource(KnowledgeBase kb, String name) throws DataObjectException {
		Wrapper source =
			WrapperFactory.getWrapper(
				kb.createKnowledgeObject(KBTestMeta.TEST_C));
		source.setValue(KBTestMeta.TEST_C_NAME, name);
		return source;
	}

	public static Wrapper createTrigger(KnowledgeBase kb, String name) throws DataObjectException {
		Wrapper trigger =
			WrapperFactory.getWrapper(
				kb.createKnowledgeObject(KBTestMeta.TEST_B));
		trigger.setValue(KBTestMeta.TEST_B_NAME, name);
		return trigger;
	}

	protected void createLogEntry() throws DataObjectException {
        createLogEntry(new Date());
 
        assertTrue(KBSetup.getKnowledgeBase().commit());
    }
    
    /**
     * Remove all Log Related KOs and KAs (before starting a new test).
     */
    protected void deleteOldKOsAndKAs() throws Exception {

        // In case of mixed KBases we must delete the BridgeKAs first 
        Person        root       = PersonManager.getManager().getRoot();
        if (root != null) {
            KnowledgeBase personBase = root.getKnowledgeBase();
            Iterator<KnowledgeObject> iter = personBase.getAllKnowledgeObjects("Person").iterator();
            while(iter.hasNext()){
                KnowledgeObject personKO = iter.next();
                deleteAllPeronsKAs(personBase, personKO);
            }
        }

        // delete LogEntry
        KnowledgeBase theKB = KBSetup.getKnowledgeBase();
		theKB.deleteAll(theKB.getAllKnowledgeObjects("LogEntry"));
        
        // delete hasLogEntry (is done automatically)
        // delete ArchiveDayUserEntry
		theKB.deleteAll(theKB.getAllKnowledgeObjects("ArchiveUserDayEntry"));
    }        
        
    protected void deleteAllPeronsKAs(KnowledgeBase personBase, KnowledgeObject aPersonKO) throws DataObjectException {
        ArrayList<KnowledgeAssociation> tmp = new ArrayList<>();
        Iterator<KnowledgeAssociation> iter = aPersonKO.getOutgoingAssociations(LogEntry.ASS_TYPE);
        while(iter.hasNext()) {
            tmp.add(iter.next());
        }       
		personBase.deleteAll(tmp);
    }

    /**
     * Allow access by overriding some protecetd methods
     */
    protected class TestedArchiver extends DayEntryArchiverTask {

		public TestedArchiver(String name) {
			super(name);
		}

        /**
         * Overridden to allow access.
         */
         @Override
		public int archive() {
            return super.archive();
        }
    }

    protected class DummyReceiver implements IReceiver{

        private BusEvent receivedEvent;
        
        @Override
		public void receive(BusEvent aBusEvent) throws RemoteException {
            receivedEvent=aBusEvent;
        }

        public BusEvent getReceivedEvent(){
            return receivedEvent;
        }
        
        @Override
		public void subscribe() {
            // dummy only
        }

        @Override
		public void unsubscribe() {
            // dummy only
        }
    }

    protected static Test suite(Class<? extends TestLogHelper> testClass) {
    	return PersonManagerSetup.createPersonManagerSetup(testClass, new TestFactory() {
    		
    		@Override
			public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
				TestSuite suite = new TestSuite(testCase);
				suite.setName(suiteName);
				Test test = suite;
				return ServiceTestSetup.createSetup(test, Bus.Module.INSTANCE, LogEntryConfiguration.Module.INSTANCE,
					ModelTrackingService.Module.INSTANCE, LockService.Module.INSTANCE);
    		}
    	});
    }

}
