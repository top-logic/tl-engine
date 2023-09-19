/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.monitor.bus;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.bus.MonitorEvent;
import com.top_logic.base.bus.UserEvent;
import com.top_logic.base.monitor.bus.EventWriter;
import com.top_logic.base.user.UserInterface;
import com.top_logic.event.bus.Sender;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * Tests {@link com.top_logic.base.monitor.bus.EventWriter}.
 *
 * @author    <a href="mailto:hans-henning.wiesner@top-logic.com">Hans-Henning Wiesner</a>
 */
public class TestEventWriter extends BasicTestCase {
    
    /**
     * Constructor for JUnit.
     */
    public TestEventWriter(String aName) {
      super(aName);
    }

   public void testWriter () throws Exception {
        int theNumber = 10000;
        MonitorEvent theEvent;
        
        FileWriter single = new FileWriter("tmp/writer_single.txt");
        FileWriter all    = new FileWriter("tmp/writer_all.txt");
        EventWriter theWriter_single = new EventWriter(single);
        EventWriter theWriter_all    = new EventWriter(all);        
        List theEvents = new ArrayList(theNumber);
        UserInterface rootUser =  Person.getUser(PersonManager.getManager().getRoot());
        for(int i=0; i<theNumber; i++) {
            theEvent = new UserEvent (new Sender(),rootUser, (String) null, null, UserEvent.LOGGED_IN);
            theWriter_single.writeEvent(theEvent);
            theEvents.add(theEvent);
        }     
        theWriter_all.writeAllEvents(theEvents);  
        theWriter_single.close();    
        theWriter_all.close();            
   }    


   /**
    * Start method for JUnit.
    *
    * @return Test
    */
    static public Test suite() {
    	return PersonManagerSetup.createPersonManagerSetup(new TestSuite(TestEventWriter.class));
    }

    /**
     * Start method for Standalone.
     */
    static public void main(String[] argv) {
        TestRunner.run(suite());
    }
}
