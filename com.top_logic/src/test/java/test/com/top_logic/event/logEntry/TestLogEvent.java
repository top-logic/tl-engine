/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.event.logEntry;

import junit.framework.Test;

import com.top_logic.base.bus.MonitorEvent;
import com.top_logic.base.security.SecurityContext;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.dob.DataObjectException;
import com.top_logic.event.ModelTrackingService;
import com.top_logic.event.bus.Bus;
import com.top_logic.event.bus.Sender;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * Test Sending {@link MonitorEvent}s.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class TestLogEvent extends TestLogHelper {
    
    
	public void testConstructor() throws DataObjectException {
        Sender sender=null;
        Wrapper trigger=null;        
        Wrapper source=null;
        String type=null;
        UserInterface user = null;
        
        try{
            new MonitorEvent(sender, trigger, source, user, type);
            fail("Sender may not be null");
        }
        catch(NullPointerException e ){
            // expected
        }
        sender = new Sender(ModelTrackingService.getTrackingService());
        type   = MonitorEvent.MODIFIED;
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction tx = kb.beginTransaction();
		source = createSource(kb, "testConstructor-S");
		tx.commit();
        MonitorEvent event = new MonitorEvent(sender, trigger, source, user, type);
        assertEquals("Wrong source", source, event.getSourceObject());
        
        // BAD WORDING
        assertEquals("Wrong sender", sender, event.getSource());
        
        assertEquals("WrongType", type, event.getType());
    }
        
	public void testSendEvent() throws DataObjectException {
        try {
            ThreadContext.pushSuperUser();
            DummyReceiver receiver = new DummyReceiver();   
            
            Bus    theBuse  = Bus.getInstance();
            Sender sender   = new Sender(ModelTrackingService.getTrackingService());
            Wrapper trigger = null;        
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			Transaction tx = kb.beginTransaction();
			Wrapper source = createSource(kb, "testSendEvent-S");
			tx.commit();
            String type     = MonitorEvent.MODIFIED;
                       
            theBuse.subscribe (receiver, ModelTrackingService.getTrackingService());
            
            assertNull(receiver.getReceivedEvent());
            MonitorEvent event = new MonitorEvent(sender, trigger, source, SecurityContext.getCurrentUser(), type); 
            assertNull(receiver.getReceivedEvent());      
            sender.send(event);
            assertEquals("Event not received",event,receiver.getReceivedEvent() );
            theBuse.unsubscribe (receiver, ModelTrackingService.getTrackingService());
        } finally {
            ThreadContext.popSuperUser();
        }       

    }
    
    /** Return the suite of Tests to perform */
    public static Test suite () {
    	return suite(TestLogEvent.class);
    }
 
    /** Main function for direct execution */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }
    
    
}
