
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.monitor.bus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.bus.MonitorEvent;
import com.top_logic.basic.Logger;

/**
 * This reads the events written by {@link com.top_logic.base.monitor.bus.EventWriter}.
 *
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class EventReader extends BufferedReader{
        
    /**
     * Contructs EventReader on given Reader.
     */
    public EventReader (Reader aReader) throws IOException {
        super(aReader);
    }    

    /**
     * Read all events .
     * 
     * @return   read events.
     */
    public List readAllEvents (DefaultMonitor anEventMonitor) throws IOException {
        final List theEvents = new ArrayList();
        MonitorEvent theEvent = null;
        do {
            try {
                theEvent = anEventMonitor.readEvent(this);
                if(theEvent != null) {
                    theEvents.add(theEvent);
                }
            } catch (IOException e) {
                Logger.error ("Unable to read event, reason is: " + e, this);                
            }                      
        } while (theEvent != null);
        this.close();
        return theEvents;
    }
}
