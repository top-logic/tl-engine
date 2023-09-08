
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.monitor.bus;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.event.bus.BusEvent;

/**
 * This class buffers a certain number of busevents for later access.
 *
 * It can be used for easy managing of bus events on the system. 
 * !!! The instance returns all BusEvents it received, 
 * so be sure to check the security in the BusEvents, 
 * when a user is accessing the objects in the event.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class EventBuffer {

    /** Stores a number of events for later access. */
    private BusEvent[] events;    
    private int size;
    private int pointer = 0;    
    
    public EventBuffer (int aSize) {
        this.size = aSize;
        this.events = new BusEvent[this.size];
    }    

    /**
     * Returns this buffer's size.
     *
     * @return    The buffer size.
     */
    public int getSize () {
        return this.size;
    }
    
    /**
     * Appends the given event to the buffered events.
     * 
     * If buffer is already full for this object to be appended 
     * the first object is silently removed.
     *
     * @param    anEvent    The event to be added to the buffer.
     */
    public void addEvent (BusEvent anEvent) {
        synchronized (this.events) {
            this.events[this.pointer] = anEvent;   
            this.pointer = (this.pointer < this.size-1)?++this.pointer:0;                                            
        }   
    }    
    
    /**
     * Returns all buffered events.
     * 
     * The first element in the returned list is the last occured event. If
     * the expected size of event is larger than the real amount of events
     * occured, the returned list will return only so many events, as it can
     * find (which have occured).
     *
     * @return   A list of BusEvents.
     */
    public List getAllEvents () {
        return this.getLatestEvents (this.size, true);
    }    

    /**
     * Returns the latest buffered events up to given number.
     *
     * The first element in the returned list is the last occured event. If
     * the expected size of event is larger than the real amount of events
     * occured, the returned list will return only so many events, as it can
     * find (which have occured).
     *
     * @param    maxreturns    The maximum number of events that
     *                         should be returned.
     * @return   A list of BusEvents.
     */
    public List getLatestEvents (int maxreturns) {
        return this.getLatestEvents (maxreturns, true);
    }

    /**
     * Retrieves the latest events from the buffer.
     *
     * The first element in the returned list is the last occured event. If
     * the expected size of event is larger than the real amount of events
     * occured, the returned list will return only so many events, as it can
     * find (which have occured).
     *
     * @param    maxreturns        The maximum number of events that
     *                             should be returned.
     * @param    withDuplicates    true, if more than one event with the 
     *                             same message.
     * @return   A list of BusEvents.
     */
    public List getLatestEvents (int maxreturns, boolean withDuplicates) {
        synchronized (this.events) {  
            maxreturns = (maxreturns > this.size) ? this.size:maxreturns;
            maxreturns = (maxreturns < 0) ? 0 : maxreturns;             
            final List theResult = new ArrayList(maxreturns);  
            boolean isDuplicate = false;                    
            for (int i=0; i<maxreturns; i++) {
                BusEvent theEvent = this.events[(this.pointer > i)?(this.pointer - i - 1):(this.size + this.pointer - i - 1)];
                for(int j=0;(!withDuplicates) && (j<theResult.size()); j++) {
                    if(((BusEvent)theResult.get(j)).getMessage().equals(theEvent.getMessage())) {
                        isDuplicate = true;
                    }
                }
                if((theEvent != null) && (!isDuplicate)) {
                    theResult.add(theEvent);
                }
            }
            return theResult;
        }
    }          
}
