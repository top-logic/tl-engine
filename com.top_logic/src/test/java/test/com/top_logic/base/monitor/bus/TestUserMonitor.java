/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.monitor.bus;

import java.io.File;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.bus.UserEvent;
import com.top_logic.base.monitor.bus.UserMonitor;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.event.bus.Bus;
import com.top_logic.event.bus.Sender;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * Tests {@link com.top_logic.base.monitor.bus.UserMonitor}.
 * 
 * @author    <a href="mailto:tri@top-logic.com">Thomas Richter</a>
 */
public class TestUserMonitor extends BasicTestCase {
         
    public void testUserMonitor () throws Exception {

        // delete file to run test more than once
        File theFile = new File("tmp/users.log");
        if (theFile.exists()) {
            assertTrue(theFile.delete());
        }

        Sender theSender = new Sender (Bus.CHANGES, Bus.USER);
        String theUserId = "root";
		Person theUser = Person.byName(theUserId);
        if(theUser == null) {
            fail("User "+theUserId+" not found.");
        } 
		ServiceConfiguration<UserMonitor> config;
		try {
			config = ApplicationConfig.getInstance().getServiceConfiguration(UserMonitor.class);
		} catch (ConfigurationException ex) {
			throw new RuntimeException(ex);
		}
		UserMonitor theMonitor = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
        theMonitor.subscribe();
        
        theSender.send(new UserEvent(theSender,theUser, "SessionX", "localhost", UserEvent.LOGGED_IN));
        theSender.send(new UserEvent(theSender,theUser, "SessionX" , "localhost", UserEvent.LOGGED_OUT));                
        
        //getting the Received events
        List theReceivedEvents    = theMonitor.getAllUserEvents();        
        int numberEvents = theReceivedEvents.size();
        
        //we sent 2 events, so we expect 2 to receive
        assertEquals(2,numberEvents);
        
        for(int i = 0;i < numberEvents; i++) {
            UserEvent tmp = (UserEvent)theReceivedEvents.get(i);
            tmp.getType();
        }
        theMonitor.unsubscribe(); // Do not listen to all the following stuff
    }

    /**
     * Constructor for JUnit.
     */
    public TestUserMonitor(String aName) {
      super(aName);
    }
    
    /**
     * Start method for JUnit.
     *
     * @return Test
     */
    static public Test suite() {
		Test innerTest = new TestSuite(TestUserMonitor.class);
		innerTest = ServiceTestSetup.createSetup(innerTest, Bus.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(innerTest); 
    }
  
    /**
     * Start method for Standalone.
     */
    static public void main(String[] argv) {
      
      // SHOW_TIME        = false;     // for debugging
      Logger.configureStdout("ERROR");    
        
      TestRunner.run(suite());
    }    
}
