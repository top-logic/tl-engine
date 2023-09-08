/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.reminder;

import java.util.Date;
import java.util.List;

import com.top_logic.contact.business.PersonContact;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * This interface describes a reminder.
 * 
 * A reminder 
 * <ul>
 *  <li>sends an e-mail at a certain time to a set of recipients.</li> 
 *  <li>can be active or inactive.</li>
 *  <li>knows whether the e-mail was sent.</li>
 * </ul>
 * 
 * @author    <a href=mailto:tdi@top-logic.com>Thomas Dickhut</a>
 */
public interface Reminder {

    /** Constant for <i>true</i>. */
    public static final boolean ACTIVATED          = true;
    /** Constant for <i>false</i>. */
    public static final boolean DEACTIVATED        = false;
    /** Constant for <i>true</i>. */
    public static final boolean EMAIL_SENT         = true;
    /** Constant for <i>false</i>. */
    public static final boolean EMAIL_NOT_SENT     = false;
    /** Constant for <i>true</i>. */
    public static final boolean REPLACE_TOKENS     = true;
    /** Constant for <i>false</i>. */
    public static final boolean NOT_REPLACE_TOKENS = false;
    
    /**
     * This method returns whether the reminder is activated.
     * 
     * @return Returns true, if the reminder is activated, false otherwise.
     */
    public boolean isActivated();

    /**
     * This method activates/deactivates the reminder. Please use the constants
     * <i>Reminder.ACTIVATED</i> and <i>Reminder.DEACTIVATED</i>.
     * 
     * @param activated Sets the reminder active/inactive.
     */
    public void setActivated(boolean activated);

    /** 
     * This method returns if the e-mail was sent.
     * 
     * @return Returns true, if the e-mail was sent, false otherwise.
     */
    public boolean isSent();
    

    /**
     * This method sets the e-mail as sent/not sent. <br />
     * Please use the constants <i>Reminder.EMAIL_SENT</i> and
     * <i>Reminder.EMAIL_NOT_SENT</i>.
     * 
     * @param sent An e-mail state (sent/not sent).
     */
    public void setSent(boolean sent);
    
    /**
     * This method returns the kind of the reminder (green, yellow, red,
     * individual).
     * 
     * @return Returns the kind of the reminder as a string.
     */
    public String getKind();

    /**
     * This method sets the e-mail subject.
     * 
     * @param aSubject The e-mail subject which be sent.
     */
    public void setEMailSubject(String aSubject);

    /**
     * This method returns the e-mail subject with or without
     * tokens. A token could be par example: %CURRENT_DATE%.
     * 
     * @param aValue Please use <i>REPLACE_TOKENS</i> and
     *               <i>NOT_REPLACE_TOKENS</i>.
     * 
     * @return Returns the e-mail subject.
     */
    public String getEMailSubject(boolean aValue);

    /**
     * This method sets the e-mail body.
     * 
     * @param aEMailBody The e-mail body which be sent.
     */
    public void setEMailBody(String aEMailBody);

    /**
     * This method returns the e-mail body with or without
     * tokens. A token could be par example: %CURRENT_DATE%.
     *  
     * @param aValue Please use <i>REPLACE_TOKENS</i> and
     *               <i>NOT_REPLACE_TOKENS</i>.
     * 
     * @return Returns the e-mail body as a {@link String}.
     */
    public String getEMailBody(boolean aValue);

    /**
     * This method returns only receivers which be no person or group. <br />
     * NOT all e-mail addresses!
     * 
     * @return Returns a list with receivers which be no person or group.
     */
    public List getEMailAddresses();

    /**
     * This method adds an e-mail address to the receiver list.
     * 
     * @param aEMailAddress The new e-mail address.
     */
    public void addEMailAddress(String aEMailAddress);

    /**
     * This method removes an e-mail address from the receiver list.
     * 
     * @param aEMailAddress The e-mail address which can be removed.
     */
    public boolean removeEMailAddress(String aEMailAddress);

    /** 
     * This method removes the e-mail adddresses.
     */
    public void removeEMailAddresses();
                
    /**
     * This method returns all e-mail addresses which get notifications.
     * 
     * @return Returns a list with all e-mail addresses (strings).
     */
    public List getAllEMailAddresses();

    /**
     * This method sets the send date.
     * 
     * @param aDate The send date.
     */
    public void setSendDate(Date aDate);

    /**
     * This method returns the send date.
     * 
     * @return Returns the send date.
     */
    public Date getSendDate();

    /**
     * This method adds a person contact to the receiver list.
     * 
     * @param aPersonContact The person contact which be added to the receiver
     *        list.
     */
    public void addPersonContact(PersonContact aPersonContact);
    
    /** 
     * This method removes a person from the receiver list.
     * 
     * @param aPersonContact The person contact which be removed from the receiver list.
     * @return Returns true, if the person contact could be removed, false otherwise.
     */
    public boolean removePersonContact (PersonContact aPersonContact);
    
    /** 
     * This method removes the persons contacts.
     */
    public void removePersonContacts();
    
    /**
     * This method returns all persons contacts from the receiver list. <br />
     * NOT all e-mail addresses!
     * 
     * @return Returns a list with persons contacts.
     */
    public List getPersonContacts();
    
    /**
     * This method adds a person to the receiver list.
     * 
     * @param aPerson The person which be added to the receiver list.
     */
    public void addPerson(Person aPerson);

    /**
     * This method removes a person from the receiver list.
     * 
     * @param aPerson The person which be removed from the receiver list.
     * @return Returns true, if the person could be removed, false otherwise.
     */
    public boolean removePerson(Person aPerson);

    /** 
     * This method removes the persons.
     */
    public void removePersons();
    
    /**
     * This method returns all persons from the receiver list. <br />
     * NOT all e-mail addresses!
     * 
     * @return Returns a list with persons.
     */
    public List getPersons();

    /**
     * This method adds a group to the receiver list.
     * 
     * @param aGroup The group which be added to the receiver list.
     */
    public void addGroup(Group aGroup);

    /**
     * This method removes a group from the receiver list.
     * 
     * @param aGroup The group which be removed from the receiver list.
     * @return Returns true, if the group could be removed, false otherwise.
     */
    public boolean removeGroup(Group aGroup);

    /** 
     * This method removes the groups.
     */
    public void removeGroups();
    
    /**
     * This method returns all groups from the receiver list. <br />
     * NOT all e-mail addresses!
     * 
     * @return Returns a list with groups.
     */
    public List getGroups();
}
