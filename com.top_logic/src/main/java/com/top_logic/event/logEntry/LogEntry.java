/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.logEntry;

import java.util.Date;
import java.util.List;

import com.top_logic.base.bus.MonitorEvent;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperComparator;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;

/**
 * A log entry is created if an appropriate LogEvent occurs.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class LogEntry extends AbstractBoundWrapper {

    public static final long MILLIS_PER_DAY      = 24 * 60 * 60 * 1000L;
    
    public static final long MILLIS_PER_HALF_DAY = MILLIS_PER_DAY >> 1;

    // name of the class    
    public static final String  CLASS_NAME = "LogEntry";
    
    /** Association Type between Person and LogEntry */
    public static final String ASS_TYPE = "hasLogEntry";
    
    /** 
     * Name of Attribute in  "hasLogEntry" KA
     * 
     * This should be a java.sql.Date, but not all DBs, support this.
     * so any value store here must be convert by {@link #alignToMiddleOfDay(Date)}. 
     */
    static final String DATE_ATTRIBUTE = "date";
    
    
    /** The EventType as Integer (e.g. MILESTONE.CREATE). */
    public static final String        TYPE           = "eventType";

    /** The user who triggered this event. */
	public static final String USER_ATTR = "user";

    /** An optional (brief) message describing the reason for this event. */
    public static final String        MESSAGE        = "message";
        // since we do not need a physicalResource we can recyle it.

    /**
     * ID of the object which triggered this event (e.g. Id of the created
     * milestone).
     */
	public static final String TRIGGER_ATTR = "trigger";

    /** Name of the triggering object (e.g. the name of the created milestone). */
    public static final String        TRIGGER_NAME   = "triggerName";

    /**
     * ID of the source object, where the source object is meant as some kind of
     * parent of the triggering object (e.g. the parent project of the created
     * milestone)
     */
	public static final String SOURCE_ATTR = "source";

    /**
     * Name of the source object (e.g. name of the project of the created
     * milestone).
     */
    public static final String        SOURCE_NAME    = "sourceName";

    /** date the LogEntry was created. 
     */
    public static final String        DATE    = "date";    

    static final WrapperComparator DATE_COMPARATOR = 
        new WrapperComparator(DATE, WrapperComparator.DESCENDING);

    /*
     * Only used for testing
     */
    private static Date testDate=null;
    
    public LogEntry(KnowledgeObject ko) {
        super(ko);
    }

    /**
     * Create a new LogEntryfrom the given LogEvent, making it persistent. 
     */
    public static LogEntry createLogEntry(MonitorEvent anEvent) {

        Wrapper theSource  = (Wrapper) anEvent.getSourceObject();
        Wrapper theTrigger = (Wrapper) anEvent.getMessage();
       
//        String theEventType = anEvent.getType();            
		KnowledgeObject theActor = getEventActor(anEvent);
        
        String theSourceName  = null;
		KnowledgeObject sourceItem = null;
        String theTriggerName = null;
		KnowledgeObject triggerItem = null;

        if (theSource != null && theSource.tValid()) {
            theSourceName = theSource.getName();
			sourceItem = theSource.tHandle();
        }

        if (theTrigger != null && theTrigger.tValid()) {
			triggerItem = theTrigger.tHandle();
			if (triggerItem != null) {
                theTriggerName = theTrigger.getName();
            }
            else {
                throw new IllegalArgumentException("ID of destination object is empty in " + theTrigger);
            }
        }           
        
        // create the entry type
        String theEntryType = getLogEntryType(anEvent); 

        // create an appropriate string message for GUI
		ResKey theMessage = _generateMessageKey(anEvent);
        
        
        return createNewEntry(theEntryType, theActor, theMessage,
			theSourceName, sourceItem, theTriggerName,
			triggerItem);
    }
 
	private static LogEntry createNewEntry(String aType, KnowledgeObject person, ResKey aMessage,
           String aSourceName, KnowledgeObject source, String aTriggerName,
			KnowledgeObject trigger) {

		{
			KnowledgeObject eventKO = getDefaultKnowledgeBase().createKnowledgeObject(CLASS_NAME);

            eventKO.setAttributeValue(TYPE   , aType);
			eventKO.setAttributeValue(USER_ATTR, person);
			setTruncated(eventKO, MESSAGE, ResKey.encode(aMessage));
			setTruncated(eventKO, SOURCE_NAME, aSourceName);
			eventKO.setAttributeValue(SOURCE_ATTR, source);
			setTruncated(eventKO, TRIGGER_NAME, aTriggerName);
			eventKO.setAttributeValue(TRIGGER_ATTR, trigger);
            Date theDate = getDateForCreation();
            eventKO.setAttributeValue(DATE, theDate);
            
			return getInstance(eventKO);
        }
    }
    
	private static void setTruncated(KnowledgeObject object, String attribute, String value) throws DataObjectException {
		if (value != null) {
			int maxSize = maxAttributeSize(object, attribute);
			if (value.length() > maxSize) {
				StringBuilder truncateMsg = new StringBuilder();
				truncateMsg.append("Value is too long (max. ");
				truncateMsg.append(maxSize);
				truncateMsg.append(") for attribute '");
				truncateMsg.append(attribute);
				truncateMsg.append("'. Will be truncated.");
				Logger.info(truncateMsg.toString(), LogEntry.class);
				value = value.substring(0, maxSize);
			}
		}
		object.setAttributeValue(attribute, value);
	}

	private static int maxAttributeSize(KnowledgeObject object, String attribute) throws NoSuchAttributeException {
		MOAttribute attr = MetaObjectUtils.getAttribute(object.tTable(), attribute);
		return ((MOAttributeImpl) attr).getSQLSize();
	}
    
    private static String getLogEntryType(MonitorEvent anEvent) {
        return LogEntryConfiguration.getInstance().getTypeFor((Wrapper) anEvent.getMessage()) + "." + anEvent.getType();
    }
    
	private static ResKey _generateMessageKey(MonitorEvent anEvent) {
        return generateMessageKey(anEvent);
    }
    
	protected static ResKey generateMessageKey(MonitorEvent anEvent) {
		ResKey theKey = getResourceKey(anEvent);

        Wrapper theTrigger = (Wrapper) anEvent.getMessage();
        Wrapper theSource  = (Wrapper) anEvent.getSourceObject();
		theKey = Resources.encodeMessage(theKey, theTrigger.getName(), theSource.getName());
        return theKey;
    }
    
	private static ResKey getResourceKey(MonitorEvent anEvent) {
		return LogEntryConfiguration.I18N_MESSAGE_PREFIX.key(getLogEntryType(anEvent));
    }
    
    /**
     * Adds the given entry for the given user for the day of the date of the entry.
     * 
     * @param aPerson the person to connect to the entry
     * @param anEntry the LogEntry to connect to the user
     */
    static void addEntry(Person aPerson, LogEntry anEntry) {
        try {
            KnowledgeBase theBase = aPerson.getKnowledgeBase();
			KnowledgeAssociation ass =
				theBase.createAssociation(aPerson.tHandle(), anEntry.tHandle(), ASS_TYPE);

            ass.setAttributeValue(DATE_ATTRIBUTE, alignToMiddleOfDay(anEntry.getDate()));
        }
        catch (Exception e) {
            Logger.error("Failed to addEntry().", e, LogEntry.class);
        }
    }
    
    /**
     * Use ONLY for testing
     */
	@FrameworkInternal
    public static void setTestDate(Date aDate){
        testDate=aDate;
    }
    private static Date getDateForCreation() {
        if( testDate == null ) {
            return new Date();
        }
        return testDate;
    }
    public static List getAllEntries() {
        return getWrappersFromCollection(
                getDefaultKnowledgeBase().getAllKnowledgeObjects(CLASS_NAME));
    }
    
    /**
	 * Return the actor of the given event.
	 * 
	 * @param anEvent
	 *        The event to extract the actor from.
	 * @return The {@link Person} actor of the given event.
	 */
    protected static KnowledgeObject getEventActor(MonitorEvent anEvent) {
		Person ui = anEvent.getUser();
        if(ui!=null){
			return PersonManager.getManager().getPersonByName(ui.getName()).tHandle();
        }
		return null;
    }    
    
    /**
     * Returns the instance of the wrapper for the given KnowledgeObject.
     *
     * @param    anObject    The object to be wrapped.
     * @return   The wrapper for the object.
     */
    public static LogEntry getInstance(KnowledgeObject anObject) {
        return (LogEntry) WrapperFactory.getWrapper(anObject);
    }       
    
    /**
     * Returns the instance of the wrapper for the given ID.
     *
     * @param    anID    the ID of the LogEntry to return.
     * @return   The wrapper for the object.
     */
    public static LogEntry getInstance(TLID anID) {
        return (LogEntry) WrapperFactory.getWrapper(anID,CLASS_NAME);
    }

    /**
     * the name of the object which triggered this event
     * Note: the triggering object is the actual source of this event. e.g. if a
     *       milestone is created, then this created milestone is the object
     *       which triggers this event
     */
    public String getTriggerName() {
        return getString(TRIGGER_NAME);
    }

    /**
     * the ID of the object which triggered this event
     * Note: the triggering object is the actual source of this event. e.g. if a
     *       milestone is created, then this created milestone is the object
     *       which triggers this event
     */
	public KnowledgeObject getTrigger() {
		return (KnowledgeObject) this.getValue(TRIGGER_ATTR);
    }

    /**
     * Returns the message of the element.
     * 
     * @return description
     * @see #getMessage()
     */
    public String getMessage() {
        return getString(MESSAGE);
    }

    /**
     * Return the birthdate of this event.
     * 
     * @return The birthdate of this event
     */
    public Date getDate(){
        return getDate(DATE);
    }
    
    
    /**
     * the name of the source object
     * Note: the source object is the parentobject of the object which actually
     *       triggered this evet
     */
    public String getSourceName() {
        return getString(SOURCE_NAME);
    }

    /**
     * the id of the source object
     * Note: the source object is the parentobject of the object which actually
     *       triggered this evet
     */
	public KnowledgeObject getSource() {
		return (KnowledgeObject) this.getValue(SOURCE_ATTR);
    }
    
	public String getLogEntryType(){
        return  getString(TYPE);
    }

	@Override
	protected String toStringValues() {
		return super.toStringValues() + ", type: " + this.getLogEntryType() + ", date: " + getDate();
	}
    
    /** 
     * Align util.Date to sql.Date at 12:00:00 in lcal time.
     */
    protected final static java.sql.Date alignToMiddleOfDay(Date aDate) {
		return new java.sql.Date(DateUtil.adjustTime(aDate, 12, 0, 0, 0).getTime());
    }
    
	/**
	 * Whether the other {@link LogEntry} has same {@link #getSource()}, {@link #getTrigger()}, and
	 * {@link #getLogEntryType()}.
	 */
	public final boolean similar(LogEntry other) {
		return Utils.equals(getSource(), other.getSource())
			&& Utils.equals(getTrigger(), other.getTrigger())
			&& Utils.equals(getLogEntryType(), other.getLogEntryType());
	}

}
