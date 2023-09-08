/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.logEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.knowledge.objects.DestinationIterator;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.util.error.TopLogicException;

/**
 * This class is the interface to get and store LogEntries.
 * 
 * @author <a href="mailto:fma@top-logic.com">fma</a>
 */
public class UserDayEntryManager {

	/**
	 * Configuration for {@link UserDayEntryManager}.
	 */
	public interface Config extends ConfigurationItem {
		/**
		 * The interval (in seconds) in which two LogEntries are duplicate. See
		 * {@link Config#getDuplicateInterval}.
		 */
		String DUPLICATE_INTERVAL = "duplicateInterval";

		/** Getter for {@link Config#DUPLICATE_INTERVAL}. */
		@Name(DUPLICATE_INTERVAL)
		@IntDefault(600)
		int getDuplicateInterval();
	}

	private static UserDayEntryManager instance = new UserDayEntryManager();

	/** must be protected for Singleton Pattern */
	protected UserDayEntryManager() {
    }
    
    /**
	 * Returns a list with {@link UserDayEntry}s for the given dates and the given user
	 */
    public List<UserDayEntry> getEntries(Person aPerson, Date aStartDate, Date anEndDate) {

        List<UserDayEntry> res = ArchiveUserDayEntry.getEntries(aPerson, aStartDate, anEndDate);
        aStartDate = DateUtil.adjustToDayBegin(aStartDate);
        anEndDate  = DateUtil.adjustToDayEnd  (anEndDate);
        res.addAll(getActualEntries(aPerson, aStartDate, anEndDate));       
        
        return res;
    }
    
    /**
     * Retrieve all ActualUserDayEntries for Person between aStartDate and anEndDate.
     *
     * @param    aStartDate    Return only events after aStartDate.
     * @param    anEndDate     Return only events before anEndDate.
     */
    private List<UserDayEntry> getActualEntries(Person aPerson, Date aStartDate, Date anEndDate) {
        List<UserDayEntry> theResult = this.getEntries(aPerson);

		for (Iterator<?> theIt = theResult.iterator(); theIt.hasNext();) {
            ActualUserDayEntry theEntry = (ActualUserDayEntry)theIt.next();
            Date               theDate  = theEntry.getDate();

            if (!DateUtil.inInterval(theDate, aStartDate, anEndDate)) {
                theIt.remove();
            }
        }        

        return theResult;
    }
    
    /**
	 * Returns a list with all {@link ActualUserDayEntry}s for the given {@link Person}.
	 */
    private List<UserDayEntry> getEntries(Person aPerson){
		HashMap theMap = new HashMap<>();
        try {
            DestinationIterator iter = new DestinationIterator(aPerson.tHandle(), LogEntry.ASS_TYPE);
            while(iter.hasNext()){
                KnowledgeObject      ko      = iter.nextKO();
                KnowledgeAssociation ass     = iter.currentKA();
                Date                 theDate = (Date) ass.getAttributeValue(LogEntry.DATE_ATTRIBUTE);
                                     theDate = LogEntry.alignToMiddleOfDay(theDate);
                List<?>              theList = (List<?>) theMap.get(theDate);
                if(theList == null){
                    theMap.put(theDate,theList = new ArrayList());
                }
				theList.add(WrapperFactory.getWrapper(ko));
            }
        }
        catch (Exception e) {
            throw new TopLogicException(UserDayEntryManager.class, "Problem getting entries for "+aPerson);
        }     
        // now we have a map with dates as key and a list of LogEntries as value for each key
		List<UserDayEntry> res = new ArrayList<>(theMap.size());
		Iterator<?> iter = theMap.entrySet().iterator();
        while(iter.hasNext()){
			Map.Entry<?, ?> entry = (Map.Entry<?, ?>) iter.next();
            Date theDate        = (Date) entry.getKey();
			List theLogEntries = (List) entry.getValue();
            theLogEntries       = removeDuplicates(theLogEntries);
            res.add(new ActualUserDayEntry(aPerson, theDate, theLogEntries));
        }       
        return res;       
    } 
    
    /**
     * Removes the duplicates from the given List of LogEntries.
     * 
     * A duplicate is a LogEntry equal to some other LogEntry where the
     * time differences is less than the configured {@link Config#DUPLICATE_INTERVAL}.
     * To make things more complex successive occurrences of the same event
     * int that same interval will removed as well. 
     * (TODO FMA/KHA This needs more testing) 
     * 
     * @param aList a List with LogEntries
     * @return the given list without the duplicates
     */
    protected List removeDuplicates(List aList) {

		long duplicateInterval = getDuplicateIntervalInMilliseconds();

        // sort list descending by date
        
        // two entries are equal iff
        // ID of source is equal
        // ID of target is equal
        // type of event is the same
        // time difference is less than n minutes
        
        if(aList!=null && aList.size()>1){
            Collections.sort(aList, LogEntry.DATE_COMPARATOR);
            
			List<LogEntry> validEntries = new ArrayList<>(aList.size());
			Iterator<?> allEntries = aList.iterator();
            // we have at least two elements in aList
            LogEntry lastValidEntry = (LogEntry)allEntries.next();
            validEntries.add(lastValidEntry);
            while(allEntries.hasNext()){
                LogEntry actualEntry = (LogEntry)allEntries.next();
                Date actualDate = actualEntry.getDate();
                Date lastDate   = lastValidEntry.getDate();
                if( (lastDate.getTime()-actualDate.getTime()) > duplicateInterval ){
                    // the entries have a distant date, so the actual entry is not a duplicate
                    lastValidEntry = actualEntry;
                    validEntries.add(lastValidEntry);                    
                }
                else {
                    // check for all valid entries if there is a duplicate
                    boolean addEntry = true;
                    for(int i=validEntries.size()-1 ;i>=0;i--){
                      
						LogEntry compareEntry = validEntries.get(i);
                        Date compareDate = compareEntry.getDate();
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
    
    
    /**
     * Returns an ActualUserDayEntry for the given Person and Date if it has at minimum one LogEntry.
     * 
     * Else, i.e. if there is no LogEntry for the given day, null is returned.
     * The ActualUserDayEntry is set up with the entries belonging to the user.
     * Duplicates are removed!
     */
    public ActualUserDayEntry getActualEntry(Person aPerson, Date aDate){
       
		List hlp = new ArrayList<>();
        java.sql.Date theDate = LogEntry.alignToMiddleOfDay(aDate);
        try {
			Iterator<?> iter = aPerson.tHandle()
                .getOutgoingAssociations(LogEntry.ASS_TYPE);
            while(iter.hasNext()) {
                KnowledgeAssociation ass =(KnowledgeAssociation)iter.next();
                if (theDate.equals(ass.getAttributeValue(LogEntry.DATE_ATTRIBUTE))){
					hlp.add(WrapperFactory.getWrapper(ass.getDestinationObject()));
                }
            }
        }
        catch (Exception e) {
            throw new TopLogicException(UserDayEntryManager.class, "Problem getting entry for "+aPerson+" and "+aDate);
        }
        if(hlp.size()>0){
            // only if there are LogEntries a new ActualLogEntry is created
            hlp = removeDuplicates(hlp);
            return new ActualUserDayEntry(aPerson, aDate, hlp);
        }
        return null;
    }
    
    
    /**
     * adds a LogEntry for the given user
     */
    public void addLogEntry(Person aPerson, LogEntry anEntry) {
        LogEntry.addEntry(aPerson, anEntry);
    }

    /**
     * Returns a DayUserEntry for the given user and day
     * Null is returned if no such entry exists
     */
    public UserDayEntry getEntry(Person aPerson, Date aDate) {
		List<UserDayEntry> res = getEntries(aPerson, aDate, aDate);
        int size = res.size();
        if(size > 0){
			return (res.get(0));
            // TODO log warning when size > 1 ?
        }
        return null;
    }

	/**
	 * Getter for the instance of {@link UserDayEntryManager}.
	 */
    public static UserDayEntryManager getInstance() {
        return instance;
    }
    
	/**
	 * Getter for the configuration.
	 */
	public Config getConfig() {
		return ApplicationConfig.getInstance().getConfig(Config.class);
	}

	/**
	 * Gets the {@link Config#DUPLICATE_INTERVAL}.
	 */
	public int getDuplicateInterval() {
		return getConfig().getDuplicateInterval();
	}

	/**
	 * Gets the {@link Config#DUPLICATE_INTERVAL} in milliseconds.
	 */
	public long getDuplicateIntervalInMilliseconds() {
		return 1000 * getDuplicateInterval();
	}

}
