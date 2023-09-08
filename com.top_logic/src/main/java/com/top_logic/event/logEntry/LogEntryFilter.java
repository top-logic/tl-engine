/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.logEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.event.layout.LogEntryBuilder;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.mig.html.layout.ComponentName;

/**
 * This is a shared model to be used by the LogEntry GUI.
 * 
 * Use the {@link LogEntryBuilder} to add semantics to this class.
 * 
 * @author     <a href="mailto:kha@top-logic.com">kha</a>
 */
public class LogEntryFilter {
    
    /** Separator for the log entry types. */
    private static char SEPARATOR=',';

    /** Flag for identifying a special user selection. */
    private static final String EMPTY_LIST = "__emptySelection__";
    
    /** Name of attribute in user configuration to store names of configured LogEntries */
    private static String LOGENTRIES = "LogEntries";    


    /** Date of first LogEntry to show (the time is ignored) */
    protected Date startDate;

    /** Date of last LogEntry to show (the time is ignored) */
    protected Date endDate;
    
    /** The set of display group names (Strings) to show, null means "show all" */
    protected Set displayGroupTypes;
    
    /** The set of LogEntry types */
    private Set<String> logEntryTypes;
    
    /** An arbitraty Object set via the SelectabaleMaster of the FilterComponenent. */
    protected Object selection;
    
    long duplicateInterval;
    /**
     * Create a new LogEntryFilter with all settable values.
     */
    public LogEntryFilter(Date aStart, Date anEnd, Set<String> someTypes) {
        this.setDates(aStart, anEnd);
        this.setLogEntryTypes(someTypes);

		this.duplicateInterval = UserDayEntryManager.getInstance().getDuplicateIntervalInMilliseconds();
    }
    
    /**
     * Create new LogEntryFilter with default / configured values.
     */
    public LogEntryFilter(ComponentName aBaseName) {
        this(getInitialStartDate(), getInitialEndDate(), getConfiguredEventTypes(aBaseName));
    }

    /**
     * Provide reasonable String for debugging.
     */
    @Override
	public String toString() {
        return "LogEntryFilter " + startDate + " to " + endDate + 
            (displayGroupTypes == null ? "" : displayGroupTypes.toString())
            + (selection == null ? "" : selection.toString());
    }

    /**
     * Returns the startDate.
     */
    public Date getStartDate() {
        return (startDate);
    }

    /**
     * Returns the endDate.
     */
    public Date getEndDate() {
        return (endDate);
    }

    /**
     * Returns the eventTypes.
     */
    public Set getEventTypes() {
        return (displayGroupTypes);
    }

    public Set getLogEntryTypes() {
        return logEntryTypes;
    }
    
    /**
     * Returns the (optional) selection.
     */
    public Object getSelection() {
        return selection;
    }
    
    /** 
     * Set the selection to given object.
     */
    public void setSelection(Object aSelected) {
        selection = aSelected;
    }

    public void setDates(Date aStartDate, Date aEndDate) {
        startDate = DateUtil.adjustToDayBegin(aStartDate);
        endDate   = DateUtil.adjustToDayEnd(aEndDate);
    }

    /** Default: return Date two days in the past */
    public static Date getInitialStartDate(){
		Calendar theCalendar = CalendarUtil.createCalendar();
        theCalendar.add(Calendar.DAY_OF_YEAR, -2);
        return theCalendar.getTime();   
    }

    public static Date getInitialEndDate(){
        return new Date();
    }

    /**
     * Returns a list with the names of the LogEntries the user has configured.
     * 
     * @param    aBaseName    Used to derive the PersonalConfiguration from.
     * @return   The set of event types defined by the user never <code>null</code>.
     */
    public static Set<String> getConfiguredEventTypes(ComponentName aBaseName) {
        PersonalConfiguration theConfig  = PersonalConfiguration.getPersonalConfiguration();
		String theEntries = (String) theConfig.getValue(aBaseName.qualifiedName() + LOGENTRIES);

        if (theEntries == null) { // try old variant as fallback
            theEntries = (String) theConfig.getValue(LOGENTRIES);
        }

        return (!StringServices.isEmpty(theEntries) 
                ? StringServices.toSet(theEntries, SEPARATOR) 
                : EMPTY_LIST.equals(theEntries) ? new HashSet<>() 
                                                : initializeEntries());
    }

    /**
     * Sets the list of LogEntries the user is interested in.
     * 
     * @param    aBaseName should be name of component holding this model.
     */
    public void setConfiguredEventTypes(ComponentName aBaseName, List<String> aList) {
        PersonalConfiguration theConfig = PersonalConfiguration.getPersonalConfiguration();
        String                theValue  = (aList.isEmpty()) ? EMPTY_LIST 
               : StringServices.toString(aList, String.valueOf(SEPARATOR));

		theConfig.setValue(aBaseName.qualifiedName() + LOGENTRIES, theValue);
        
        setLogEntryTypes(aList);
    }

    private void setLogEntryTypes(Collection<String> someDisplayGroupTypes) {
        LogEntryConfiguration theConf = LogEntryConfiguration.getInstance();

        this.displayGroupTypes = new HashSet<>(someDisplayGroupTypes);
        this.logEntryTypes     = new HashSet<>(someDisplayGroupTypes.size());

        for (String theGroupTypeString : someDisplayGroupTypes) {
            int                  theIndex     = theGroupTypeString.lastIndexOf(".");
            String               theGroupName = theGroupTypeString.substring(0, theIndex);
            String               theEventType = theGroupTypeString.substring(theIndex + 1);
            LogEntryDisplayGroup theGroup     = theConf.getDisplayGroup(theGroupName);

            if (theGroup != null) {
                this.logEntryTypes.addAll(theGroup.getLogEntryTypes(theEventType));
            }
        }
    }
    
    /**
     * Returns a List with all LogEntries for the given user and given range.
     * 
     * This is done using the UserDayEntries for the given user 
     */
    public List<LogEntry> getLogEntries(Person aPerson) {
        ArrayList<LogEntry> theList;

        if (startDate.before(endDate)) {
            List<UserDayEntry> theEntries = UserDayEntryManager.getInstance().getEntries(aPerson,startDate,endDate);

            theList = new ArrayList<>(theEntries.size() << 6); // * 64 averg. of Events per day ...

            for (UserDayEntry theEntry : theEntries) {
                if (this.logEntryTypes == null) {
                    theList.addAll(theEntry.getLogEntries());
                }
                else {
                    for (LogEntry theLogEntry : theEntry.getLogEntries()) {
                        if (this.logEntryTypes.contains(theLogEntry.getLogEntryType())) { // this one must differ between entry type (trigger) and the mapping from trigger type to display group
                            theList.add(theLogEntry);
                        }
                    }
                }
            }
        }
        else {
            theList = new ArrayList<>();
        }

        List<LogEntry> theResult = this.removeDuplicates(theList);

        // If the selction is unequals null keep only log entries that source
		// object or trigger object match to the selection.
        final Object currentSelection = this.getSelection();

        if (currentSelection instanceof Wrapper) {
			final KnowledgeItem selectedItem = ((Wrapper) currentSelection).tHandle();

        	theResult = CollectionUtil.toList(FilterUtil.filterList(new Filter<LogEntry>() {
			
				@Override
				public boolean accept(LogEntry anEntry) {
					KnowledgeItem theSource = anEntry.getSource();
					KnowledgeItem theTrigger = anEntry.getTrigger();
			
					return selectedItem.equals(theSource) || selectedItem.equals(theTrigger);
				}
			}, theResult));
        }
        
        return theResult;
    }

    /** 
     * Return the events activated by default.
     * 
     * This method will be used by {@link #getConfiguredEventTypes(ComponentName)} to get
     * the starting point of configurations for a default user.
     * 
     * @return    The string set of known configuration types.
     */
    protected static HashSet<String> initializeEntries() {
        LogEntryConfiguration theManager = LogEntryConfiguration.getInstance();
        HashSet<String>       theResult  = new HashSet<>();

        for (Iterator theIt = theManager.getDisplayGroups().iterator(); theIt.hasNext(); ) {
            LogEntryDisplayGroup theGroup = (LogEntryDisplayGroup) theIt.next();

            for (Iterator theElements = theGroup.getLogEntryTypes().iterator(); theElements.hasNext(); ) {
                String theEventType = (String) theElements.next();
//                String theKey  = theGroup.getResourceKey(theEventType);

                theResult.add(theEventType);
            }
        }    

        return (theResult);
    }

    /**
     * Removes the duplicates from the given List of LogEntries.
     * 
     * A duplicate is a LogEntry equal to some other LogEntry where the
     * time differences is less than the configured {@link UserDayEntryManager.Config#DUPLICATE_INTERVAL}.
     * To make things more complex successive occurrences of the same event
     * in that same interval will removed as well. 
     * (TODO FMA/KHA This needs more testing) 
     * 
     * @param aList a List with LogEntries
     * @return the given list without the duplicates
     */
    protected List<LogEntry> removeDuplicates(List<LogEntry> aList) {

        // sort list descending by date
        
        // two entries are equal if
        // ID of source is equal
        // ID of target is equal
        // type of event is the same
        // time difference is less than n minutes
        
        if (aList != null && aList.size() > 1){
            Collections.sort(aList, LogEntry.DATE_COMPARATOR);
            
            List<LogEntry> validEntries = new ArrayList<>(aList.size());
            Iterator       allEntries   = aList.iterator();
            // we have at least two elements in aList
            LogEntry lastValidEntry = (LogEntry)allEntries.next();

            validEntries.add(lastValidEntry);

            while (allEntries.hasNext()){
                LogEntry actualEntry = (LogEntry)allEntries.next();
                Date actualDate = actualEntry.getDate();
                Date lastDate   = lastValidEntry.getDate();

                if ((lastDate.getTime() - actualDate.getTime()) > duplicateInterval ){
                    // the entries have a distant date, so the actual entry is not a duplicate
                    lastValidEntry = actualEntry;
                    validEntries.add(lastValidEntry);                    
                }
                else {
                    // check for all valid entries if there is a duplicate
                    boolean addEntry = true;

                    for(int i=validEntries.size()-1 ;i>=0;i--){
                        LogEntry compareEntry = validEntries.get(i);
                        Date     compareDate  = compareEntry.getDate();
                        
                        if(actualDate.getTime() - compareDate.getTime() > duplicateInterval) {
                            // TODO can this actually happen ? 
                            // we are far away from actual date, no duplicate was found, so 
                            // actualEntry has no duplicate
                            break;
                        }
                        else{
							if (actualEntry.similar(compareEntry)) {
                                // the LogEntry are equal, so the actualEntry is a duplicate of compareEntry
                                addEntry=false;
                                break;
                            }
                        }
                        if(addEntry){
                            lastValidEntry=actualEntry;
                            validEntries.add(lastValidEntry);
                            break;
                        }
                        
                    } // end for(int i=validEntries.size() ;i>0;i--){
                } // else (lastDate.getTime()-actualDate.getTime()) > numOfSeconds*60*1000)               
            } // allEntries.hasNext()
            aList=validEntries;
        }// aList!=null && aList.size()>1        
        return aList;
    }

}
