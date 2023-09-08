/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.monitor.bus;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.top_logic.base.monitor.bus.EventBuffer;
import com.top_logic.event.bus.BusEvent;
import com.top_logic.event.bus.Sender;

/**
 * Tests {@link com.top_logic.base.monitor.bus.EventBuffer}.
 *
 * @author    <a href="mailto:hans-henning.wiesner@top-logic.com">Hans-Henning Wiesner</a>
 */
public class TestEventBuffer extends TestCase {
    
   public void testBuffer () throws Exception {
        int theSize = 100;
        int theNumber = 10000;
        EventBuffer theBuffer = new EventBuffer(theSize);
        BusEvent theEvent;
        for(int i=0; i<theNumber; i++) {
            theEvent = new BusEvent (new Sender(),null,"","test" + i);
            theBuffer.addEvent(theEvent);
        } 
        
        //all
        List theEvents = theBuffer.getAllEvents();
        for(int i=0; i<theEvents.size(); i++) {
            theEvent = (BusEvent)theEvents.get(i);
            assertEquals("test" + (theNumber - (i + 1)),theEvent.getMessage());
//            System.out.println(theEvent.getMessage());
        }   
       
       //more than all
        theEvents = theBuffer.getLatestEvents(2*theSize);
        for(int i=0; i<theEvents.size(); i++) {
            theEvent = (BusEvent)theEvents.get(i);
            assertEquals("test" + (theNumber - (i + 1)),theEvent.getMessage());
//            System.out.println(theEvent.getMessage());            
        } 
        
       //some
        theEvents = theBuffer.getLatestEvents(theSize/2);
        for(int i=0; i<theEvents.size(); i++) {
            theEvent = (BusEvent)theEvents.get(i);
            assertEquals("test" + (theNumber - (i + 1)),theEvent.getMessage());
//            System.out.println(theEvent.getMessage());            
        }         
        
        //strange   
        theEvents = theBuffer.getLatestEvents(0);   
        assertEquals(0, theEvents.size());   
        theEvents = theBuffer.getLatestEvents(-theSize);   
        assertEquals(0, theEvents.size());   
        
        //duplicates
        for(int i=0; i<theNumber; i++) {
            theEvent = new BusEvent (new Sender(),null,"","test");
            theBuffer.addEvent(theEvent);
        }             
       
        theEvents = theBuffer.getLatestEvents(theSize, false);
        assertEquals(1, theEvents.size());           
        theEvent = (BusEvent)theEvents.get(0);        
        assertEquals("test",theEvent.getMessage());
//        System.out.println(theEvent.getMessage());                                                            
   }

  /**
   * Constructor for JUnit.
   */
  public TestEventBuffer(String aName) {
    super(aName);
  }

  /**
   * Start method for JUnit.
   *
   * @return Test
   */
  static public Test suite() {
        return new TestSuite(TestEventBuffer.class); 
  }

  /**
   * Start method for Standalone.
   */
  static public void main(String[] argv) {
    TestRunner.run(suite());
  }
}
