/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.logEntry;

import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * The LogEntryDisplayGroup is a holder for a part of the configuration for {@link LogEntry}s
 * It contains the mapping of a displayGroup to object and logEventTypes.
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class LogEntryDisplayGroup implements Comparable {

    /** the name of the group */
    private String name;
   
    /** the event types linked to this group */
    private Set<String> logEntryTypes;

    /** Map<String anEventType, Set<String logEntryTypes>> */
    private Map<String,Set<String>> mapEventTypesToLogEntryTypes;
    
    private Collator collator = Collator.getInstance(TLContext.getLocale());

    /** 
     * Creates a {@link LogEntryDisplayGroup}.
     */
    public LogEntryDisplayGroup(String aGroupName) {
        this.name          = aGroupName;
        this.logEntryTypes = new HashSet<>(3);
        this.mapEventTypesToLogEntryTypes = new HashMap<>(3);
    }
    
    public void addObjectType(String anObjectType, Collection someEventTypes) {
        for (Iterator theIter = someEventTypes.iterator(); theIter.hasNext(); ) {
            String      theEventType     = (String) theIter.next();
            String      theLogEntryType  = anObjectType + "." + theEventType;
            Set<String> theLogEntryTypes = this.mapEventTypesToLogEntryTypes.get(theEventType);

            this.logEntryTypes.add(theLogEntryType);
            
            if (theLogEntryTypes == null) {
                theLogEntryTypes = new HashSet<>(1);
                this.mapEventTypesToLogEntryTypes.put(theEventType, theLogEntryTypes);
            }

            theLogEntryTypes.add(theLogEntryType);
        }
    }
    
    public String getName() {
        return this.name;
    }
    
	public Collection<String> getEventTypes() {
        return this.mapEventTypesToLogEntryTypes.keySet();
    }
    
    public String getKey() {
        return this.getName();
    }
    
    public String getKey(String anEventType) {
        return getName() + "." + anEventType;
    }
    
	public ResKey getResourceKey() {
		return LogEntryConfiguration.I18N_CONFIG_PREFIX.key(getName());
    }
    
	public ResKey getResourceKey(String anEventType) {
		return LogEntryConfiguration.I18N_CONFIG_PREFIX.append(getName()).key(anEventType);
    }

    public String getDisplayLabel() {
		return Resources.getInstance().getString(getResourceKey());
    }

	public Collection<String> getLogEntryTypes() {
        return this.logEntryTypes;
    }
    
    public Set<String> getLogEntryTypes(String anEventType) {
        Set theReturn = mapEventTypesToLogEntryTypes.get(anEventType);

        return theReturn != null ? theReturn : Collections.EMPTY_SET;
    }

    public boolean check(String anObjectType, String anEventType) {
        return this.logEntryTypes.contains(anObjectType +"."+ anEventType);
    }

    @Override
	public int compareTo(Object aObject) {
        return collator.compare(getDisplayLabel(), ((LogEntryDisplayGroup)aObject).getDisplayLabel());
    }

}
