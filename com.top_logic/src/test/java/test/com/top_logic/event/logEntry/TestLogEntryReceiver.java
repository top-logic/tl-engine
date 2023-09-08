/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.event.logEntry;

import junit.framework.Test;

import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.bus.MonitorEvent;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.dob.DataObjectException;
import com.top_logic.event.ModelTrackingService;
import com.top_logic.event.bus.Bus;
import com.top_logic.event.logEntry.LogEntry;
import com.top_logic.event.logEntry.LogEntryReceiver;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * TODO fma this Test ...
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class TestLogEntryReceiver extends TestLogHelper{

    
	public void testReceiver() throws DataObjectException {
        Bus              theBus   = Bus.getInstance ();
		ServiceConfiguration<LogEntryReceiver> config;
		try {
			config = ApplicationConfig.getInstance().getServiceConfiguration(LogEntryReceiver.class);
		} catch (ConfigurationException ex) {
			throw new RuntimeException(ex);
		}
		LogEntryReceiver receiver =
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
        long             start    = receiver.getNumbersOfEventsProcessed();
        LogEntry.setTestDate(null);
        theBus.subscribe (receiver, ModelTrackingService.getTrackingService());
        try {
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			Transaction tx = kb.beginTransaction();
			Wrapper trigger = createTrigger(kb, "testReceiver-T");
			Wrapper source = createSource(kb, "testReceiver-S");
        	
        	ModelTrackingService.sendEvent(trigger, source, MonitorEvent.CREATED);
        	
        	long end = receiver.getNumbersOfEventsProcessed();
        	assertEquals("event not processed or not counted", start+1, end);  
        	
			tx.commit();
        	(KnowledgeBaseFactory.getInstance ().getDefaultKnowledgeBase ()).commit();
        } finally {
        	theBus.unsubscribe (receiver, ModelTrackingService.getTrackingService());              
        }
    }
    
    
    /** Return the suite of Tests to perform */
    public static Test suite () {        
    	return suite(TestLogEntryReceiver.class);        
    }
 
    /** Main function for direct execution */
    public static void main (String[] args) {
        KBSetup.setCreateTables(false);    // for debugging
        
        junit.textui.TestRunner.run (suite ());
    }
}
