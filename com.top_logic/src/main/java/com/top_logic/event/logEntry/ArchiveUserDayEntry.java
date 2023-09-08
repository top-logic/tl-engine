/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.logEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;

/**
 * This class represents the LogEntries for a person for one day.
 * 
 * The LogEntries are stored as a List of Strings, using their KO-IDs.
 * Instances of this class are created by the DayEntryArchiver
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ArchiveUserDayEntry extends AbstractBoundWrapper implements UserDayEntry {
    
    /**
     * Do not use directly but only via WrapperFactory.
     */
    public ArchiveUserDayEntry(KnowledgeObject ko) {
        super(ko);
    }

    public static final String KO_NAME = "ArchiveUserDayEntry";
    
	private static final String USER_ATTR = "user";
    private static final String DATE    = "date";
    
	private static final String[] USER_DATE = new String[] { USER_ATTR, DATE };

    protected static final String ENTRY_IDS="entryIDs";
    
    protected static final char SEPARATOR = ',';

    
    /**
     * Creates an ArchiveUserDayEntry from the given ActualUserDayEntry
     * 
     * @param anEntry the entry to be archived
     */
    static ArchiveUserDayEntry createEntry(ActualUserDayEntry anEntry){
        ArchiveUserDayEntry theEntry = null;
        
        try {                 
            KnowledgeObject theKO = createEntryKO(
				getDefaultKnowledgeBase(), anEntry.getPerson().tHandle()
                    ,DateUtil.adjustToNoon(anEntry.getDate()));
            theEntry = getInstance (theKO);
            theEntry.setUpList(anEntry.getLogEntries());
        } catch (Exception ex) {
            Logger.error ("failed to createEntry()", ex, ArchiveUserDayEntry.class);
        }
        return theEntry;
    }
    
    /**
     * Creates an ArchiveUserDayEntryKO from the given values
     */
    static KnowledgeObject createEntryKO(KnowledgeBase kBase, KnowledgeObject personItem, Date aDate) 
                throws DataObjectException {
		KnowledgeObject theKO = kBase.createKnowledgeObject(KO_NAME);

		theKO.setAttributeValue(USER_ATTR, personItem);
        theKO.setAttributeValue(DATE   , new java.sql.Date(aDate.getTime()));      
        
        return theKO;
    }

    /**
     * Returns a list with all entries for the given user in the given period.
     * 
     * The list contains at most one ArchiveUserDayEntry per day of the given period.
     */
    static List<UserDayEntry> getEntries(Person aPerson, Date aStartDate, Date anEndDate) {
        List<UserDayEntry> theResult   = new ArrayList<>();
		Calendar theCalendar = CalendarUtil.createCalendar(aStartDate);

        while(theCalendar.getTimeInMillis() <= anEndDate.getTime()) {
			ArchiveUserDayEntry theEntry = getExistingEntry(aPerson, theCalendar.getTime());

            if (theEntry != null) {
                theResult.add(theEntry);
            }

            theCalendar.add(Calendar.DAY_OF_YEAR,1);
        }
        
        return theResult;
    }
    
    /**
     *  returns an existing UserDayEntry for the given user and day if such an entry exists
     *  If none exists, null is returned
     */
    public static ArchiveUserDayEntry getExistingEntry(Person user, Date aDate) {
        
        ArchiveUserDayEntry result = null;
		{
            KnowledgeBase kb     = getDefaultKnowledgeBase();
                            aDate  = LogEntry.alignToMiddleOfDay(aDate);
			Object[] values = new Object[] { user.tHandle(), aDate };
			Iterator iter = kb.getObjectsByAttribute(KO_NAME, USER_DATE, values);
            
            if(iter.hasNext()) {
                KnowledgeObject ko = (KnowledgeObject)iter.next();
                result = (ArchiveUserDayEntry)WrapperFactory.getWrapper(ko);
            }
            if(iter.hasNext()) { 
                Logger.warn("More than one ArchiveUserDayEntry for " + Arrays.asList(values), ArchiveUserDayEntry.class);
            }
        } 
        return result;
    }

    /**
     *  returns an existing UserDayEntry for the given user and day if such an entry exists
     *  If none exists, null is returned
     */
    public static KnowledgeObject getExistingEntryKO(KnowledgeBase aBase, KnowledgeObject personItem, Date aDate) 
        throws DataObjectException {
        
        KnowledgeObject result = null;
        java.sql.Date   date   = new java.sql.Date(aDate.getTime());
		Object[] values = new Object[] { personItem, date };
		Iterator iter = aBase.getObjectsByAttribute(KO_NAME, USER_DATE, values);
        
        if(iter.hasNext()) {
           result = (KnowledgeObject)iter.next();
        }
        if(iter.hasNext()) { 
			Logger.warn("More than one ArchiveUserDayEntry for " + Arrays.asList(values), ArchiveUserDayEntry.class);
        }
        return result;
    }

    /**
     * Create the list of IDs of this by the given List of LogEntries
     */
    private void setUpList(List aListOfLogEntries) {
       
        if(aListOfLogEntries!=null){
            int          size = aListOfLogEntries.size();
            StringBuffer res = new StringBuffer(size*32);
            if(size > 0){
				TLID theID = ((LogEntry) aListOfLogEntries.get(0)).getID();
                res.append(IdentifierUtil.toExternalForm(theID));
                for(int i=1;i<size;i++){
                    res.append(SEPARATOR);
                    theID=((LogEntry)aListOfLogEntries.get(i)).getID();
                    res.append(IdentifierUtil.toExternalForm(theID));
                }
            }
            setString(ENTRY_IDS, res.toString());
        }
        else{
            setString(ENTRY_IDS, null);
        }
    }
    
    @Override
	public Person getPerson() {
		KnowledgeObject userItem = (KnowledgeObject) this.getValue(USER_ATTR);
		return PersonManager.getManager().getPersonByKO(userItem);
    }

    @Override
	public Date getDate(){
        return getDate(DATE);
    }
    
    /**
     * a List with the IDs af all LogEntries belonging to this
     */
    @Override
	public List<LogEntry> getLogEntries() {
        String existing = getString(ENTRY_IDS);
		{
            if(existing!=null) {
                List<String>   theIDs    = StringServices.toList(existing,SEPARATOR);
                int            theSize   = theIDs.size();
                List<LogEntry> theResult = new ArrayList<>(theSize);

                for (String theID : theIDs) {
                    theResult.add(LogEntry.getInstance(IdentifierUtil.fromExternalForm(theID))); 
                }

                return theResult;
            }
        }

        return new ArrayList<>();
    }
    
    
    /**
     * Returns the instance of the wrapper for the given KnowledgeObject.
     *
     * @param    anObject    The object to be wrapped.
     * @return   The wrapper for the object.
     * @throws   IllegalArgumentException    If the object is no Document.
     */
    static ArchiveUserDayEntry getInstance(KnowledgeObject anObject) {
        return (ArchiveUserDayEntry) WrapperFactory.getWrapper(anObject);
    } 
    
}
