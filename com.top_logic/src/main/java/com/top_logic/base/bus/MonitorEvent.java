/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.bus;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.dob.DataObject;
import com.top_logic.event.bus.BusEvent;
import com.top_logic.event.bus.Sender;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * The monitor event is used to publish messages of changes on the system.
 *
 * This instance will normaly be used for sending information about changes
 * on knowledge objects. The event itself is not protected by the security
 * system, but the send message will be checked for security reasons. This
 * means, that the message will be checked, whether it is a KnowledgeObject
 * and if it is, the security access will be checked too.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class MonitorEvent extends BusEvent {

    /** Message type for creation messages. */    
    public static final String CREATED = "created";

    /** Message type for modified messages. */    
    public static final String MODIFIED = "modified";

    /** Message type for deleted messages. */    
    public static final String DELETED = "deleted";

    /** Message type for unknown messages. */    
    public static final String UNKNOWN = "unknown";


    /** The format for dates. */
    private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss.SSS";

    /** The user executing the monitorable event. */
	private Person user;

    /** The date, the event occured. */
    private Date date;

    /** 
     * The object acted as source for this operation. 
     *
     * In case of documents, this might be an WebFolder, in case of
     * WebFolders this can be another WebFolder
     * and so on.
     */
    protected Object sourceObject;

    /**
	 * Creates a {@link MonitorEvent} with current date.
	 * 
	 * @see #MonitorEvent(Sender, Object, Object, Person, Date, String)
	 */
    public MonitorEvent (Sender aSender, Object aMessage, 
			Object aSource, Person aUser,
                         String aType) {
       this (aSender, aMessage, aSource, aUser, new Date(), aType);
    }

    /**
	 * Constructs a prototypical {@link MonitorEvent} with <code>source = Sender</code> by calling
	 * the superclass´ constructor.
	 *
	 * @param aSender
	 *        The sender (source) on which the event initially occurred.
	 * @param aMessage
	 *        The message to be sent to Receiver objects subscribed to the service(s).
	 * @param aSource
	 *        The object the act as source (KO) for the message.
	 * @param aUser
	 *        The user performing this action. May be <code>null</code>.
	 * @param aDate
	 *        The time, this event occurred.
	 * @param aType
	 *        The type of the message to be sent.
	 */
    public MonitorEvent (Sender aSender, Object aMessage, 
			Object aSource, Person aUser,
                         Date aDate,     String aType) {
       super (aSender, aSender.getService (), aType, aMessage);

       this.sourceObject  = aSource;
       this.user    = aUser;
       this.date    = aDate;
    }

    /**
     * Returns a String representation of this EventObject.
     *
     * @return  A String representation of this EventObject.
     */
    @Override
	public String toString () {
        return this.getClass ().getName () + 
                                        "[type: " + this.getType () +
                                        ", user: " + this.user +
                                        ", source: " + this.getSource () +
                                        ", date: " + this.getFormattedDate () +
                                        ']';
    }

    /**
     * Set the object acting as source for this event.
     *
     * @param  aSource  The object acting as source for this event.
     */
    protected void setSourceObject (Object aSource) {
         this.sourceObject = aSource;
    }      

    /**
     * Returns the object acting as source for this event.
     *
     * This source is another source than the actor of this event!
     *
     * @return    The source object.
     * @see       #sourceObject
     */
    public Object getSourceObject () {
        return this.sourceObject;
    }
    
    /**
	 * Setter for {@link #getUser()}
	 */
	protected void setUser(Person aUser) {
         this.user = aUser;
    }      

    /**
	 * The user performing the action.
	 *
	 * @return The user performing the action. May be <code>null</code>, e.g. when the action was
	 *         initiated by system.
	 */
	public Person getUser() {
         return (this.user);
    }
    
    /**
     * Set the date, the message has been performed.
     *
     * @param  aDate  The date, the message has been performed.
     */
    protected void setDate (Date aDate) {
         this.date = aDate;
    }    

    /**
     * Returns the date, the message has been performed.
     *
     * @return    The date, the message has been performed.
     */
    public Date getDate () {
         return (this.date);
    }
    
    /**
     * Returns the date, the message has been performed.
     *
     * @return    The date, the message has been performed.
     */
    public String getFormattedDate () {
         return (getFormattedDate (this.getDate ()));
    }

    /**
     * Returns the format for dates.
     *
     * @return    The requested format.
     */
    protected static DateFormat getDateFormat () {
		return CalendarUtil.newSimpleDateFormat(DATE_FORMAT);
    }

    /**
     * Returns the given date in a standard format (yyy-MM-dd hh:mm:ss.SSS).
     *
     * @param    aDate    The date to be formatted.
     * @return   The formatted date.
     */
    public static String getFormattedDate (Date aDate) {
         return (getDateFormat ().format (aDate));
    }
           
//########################################################################
// WRITE/READ EVENT (get rid of EventWriter and EventReader Hierarchie)
//########################################################################

    /** 
     * Try to extract an id from the given Object.
     * 
     * @param anObject usually a Dataobject.
     * 
     * @return id of the Dataobject, object.toString() or "" in case
     *          is is no Dataobject
     */
	public static Object getID(Object anObject) {
		if (anObject instanceof KnowledgeItem) {
			return IdentifierUtil.toExternalForm(((KnowledgeItem) anObject).getObjectName());
    	}
    	else if (anObject instanceof DataObject) {
			return IdentifierUtil.toExternalForm((((DataObject) anObject).getIdentifier()));
        }
        else {
            if (anObject != null) {
				return anObject;
            }
            else {
                return ("");
            }
        }
    }   

    /** 
     * Try to extract a type name form the given Object.
     * 
     * @param anObject usually a Dataobject.
     * 
     * @return type of the Dataobject, object.toString() or "" in case
     *          is is no Dataobject
     */
    public static String getType (Object anObject) {
        if (anObject instanceof DataObject) {
            return (((DataObject) anObject).tTable ().getName ());
        }
        else {
            if (anObject != null) {
                return (anObject.toString ());
            }
            else {
                return ("");
            }
        }
    }
    
	public static Person getUser(String aName) {
        if ("not available".equals (aName)) {
            return (null);
        }
        else {
			return PersonManager.getManager().getPersonByName(aName);
        }
    }    
    
    /** 
     * Try to extract a user name from the given Object.
     * 
     * @param anObject should represent some sort of User.
     * 
     * @return name of the user, object.toString() or "" in case
     *          no user could be extracted.
     */
    public static String getUser (Object anObject) {
		if (anObject instanceof Person) {
			return (((Person) anObject).getName());
        }
        if (anObject != null) {
            return (anObject.toString ());
        }
        return ("");
    }    
    
    /** 
     * Returns an object identified by the given parameters.
     * 
     * In this special case we would return a KnowledgeObject, if the
     * parameters are representing one. If not, this method returns
     * <code>false</code>.
     * 
     * @param    aType    The type of KnowledgeObject.
     * @param    anID     The ID of the object.
     * @return   The found KnowledgeObject or <code>null</code>, if not found
     *           or one of the given parameters is <code>null</code>.
     */
    public static Object getKO (String aType, TLID anID) {
        if (StringServices.isEmpty (aType) || StringServices.isEmpty (anID)) {
            return (null);
        }
        else {
            return (KnowledgeBaseFactory.getInstance ().getDefaultKnowledgeBase ().getKnowledgeObject(aType, anID));
        }
    }    

	/**
	 * Returns the {@link Wrapper} identified by the given parameters.
	 * 
	 * @param aType
	 *        The type of {@link KnowledgeObject}.
	 * @param anID
	 *        The ID of the object.
	 * @return The {@link Wrapper} for the found KnowledgeObject or <code>null</code>, if not found
	 *         or one of the given parameters is <code>null</code>.
	 */
	public static Wrapper getWrapper(String aType, TLID anID) {
		KnowledgeObject ko = (KnowledgeObject) getKO(aType, anID);
		return WrapperFactory.getWrapper(ko);
	}

    /**
     * Method to write MonitorEvent to PrintWriter.
     * 
     * @param aWriter writer to write to
     */  
    public void writeEvent (PrintWriter aWriter) throws IOException {
        // not implemented here (but needed ? KHA)
    }      
}
