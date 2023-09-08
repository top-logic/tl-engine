/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
 package com.top_logic.base.monitor.bus;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.bus.MonitorEvent;

/**
 * Class to write out MonitorEvents of some sort.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
final public class EventWriter extends PrintWriter {

    /**
     * Contruct a new EventWriter from the given Writer
\     */
	public EventWriter (Writer aWriter) throws IOException {
        super (aWriter);
	}
            
    /** 
     * Write given event.
     * 
     * @param anEvent event to be written
     */    
    public void writeEvent (MonitorEvent anEvent) throws IOException {
        final List theList = new ArrayList(1);
        theList.add(anEvent);
        this.writeAllEvents(theList);
    }   
	
    /** 
     * Write given events(last one last).
     * 
     * @param someEvents events to be written
     */   
    public void writeAllEvents (List someEvents) throws IOException {        
        for (int i=0; i<someEvents.size(); i++) {
            ((MonitorEvent)someEvents.get(i)).writeEvent (this);            
        }   
        this.close(); 
    }    
}
