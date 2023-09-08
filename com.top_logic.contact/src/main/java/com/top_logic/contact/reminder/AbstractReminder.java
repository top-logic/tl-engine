/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.reminder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import com.top_logic.base.mail.MailHelper;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;

/**
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractReminder extends AbstractBoundWrapper implements Reminder {

    /** Attribute - state of the reminder */
    public static final String IS_ACTIVATED = "isActivated";

    /** Attribute - e-mail was sent or not */
    public static final String IS_EMAIL_SENT = "isEMailSent";

    /** Attribute - send date of the e-mail */
    public static final String DATE = "sentDate";

    /** Attribute - comma separated ids of person contacts */
    public static final String PERSON_CONTACT_IDS = "personContactIDs";

    /** Constant for 254 */
    private static final int MAX_STRING_VALUE_LENGTH = 254;

    /** List of e-mail addresses which get the e-mail */
    private List addresses;

    /** List of persons which get the e-mail */
    private List contacts;

    public AbstractReminder(KnowledgeObject ko) {
        super(ko);
    }

    public abstract Collection getAllReceivers();

    protected abstract String[][] getTokens();

    protected abstract Properties getConfiguration();

    /**
     * This method returns whether the reminder is active.
     * 
     * @return Returns <i>true</i> if the reminder is active, <i>false</i>
     *         otherwise.
     */
    @Override
	public boolean isActivated() {
        return (this.getBooleanValue(AbstractReminder.IS_ACTIVATED));
    }

    /**
     * This method sets the state (active/inactive) of the reminder.
     * 
     * @param activated
     *            Can be <i>true</i> or <i>false</i>. Please use constants
     *            from the interface {@link Reminder}.
     */
    @Override
	public void setActivated(boolean activated) {
        this.setBoolean(AbstractReminder.IS_ACTIVATED, activated);
    }

    /**
     * This method returns whether the e-mail was sent.
     * 
     * @return Returns <i>true</i> if the e-mail was sent, <i>false</i> otherwise.
     */
    @Override
	public boolean isSent() {
        return (this.getBooleanValue(AbstractReminder.IS_EMAIL_SENT));
    }

    /**
     * This method sets the e-mail state (sent/not sent) of the reminder.
     * 
     * @param sent
     *            An e-mail state (sent/not sent). Please use constants from the
     *            interface {@link Reminder}.
     */
    @Override
	public void setSent(boolean sent) {
        this.setBoolean(AbstractReminder.IS_EMAIL_SENT, sent);
    }

    /**
     * This method sets the e-mail subject.
     * 
     * @param aSubject
     *            A subject.
     */
    @Override
	public void setEMailSubject(String aSubject) {
        this.setValue(MailHelper.SUBJECT, aSubject);
    }

    /**
     * This method returns the e-mail subject.
     * 
     * @param replaceTokens
     *            Boolean to get the body with or without tokens.
     * @return The e-mail subject as a {@link String}.
     */
    @Override
	public String getEMailSubject(boolean replaceTokens) {
        String theSubject = (String) getValue(MailHelper.SUBJECT);

        if (replaceTokens) {
            theSubject = this.replaceTokens(theSubject);
        }

        return (theSubject);
    }

    /**
     * This method sets the e-mail body.
     * 
     * @param aBody
     *            An e-mail body.
     */
    @Override
	public void setEMailBody(String aBody) {
        this.setValue(MailHelper.BODY, aBody);
    }

    /**
     * This method returns the e-mail body.
     * 
     * @param replaceTokens
     *            Boolean to get the body with or without tokens.
     * @return The e-mail body as a {@link String}.
     */
    @Override
	public String getEMailBody(boolean replaceTokens) {
        String theBody = (String) this.getValue(MailHelper.BODY);

        if (replaceTokens) {
            theBody = this.replaceTokens(theBody);
        }

        return (theBody);
    }

    /**
     * This method sets a send date.
     * 
     * @param aDate
     *            A date.
     */
    @Override
	public void setSendDate(Date aDate) {
        if (aDate.before(new Date())) {
            this.setSent(Reminder.EMAIL_NOT_SENT);
        }

        this.setValue(AbstractReminder.DATE, aDate);
    }

    /**
     * This method returns the send date.
     * 
     * @return The send date.
     */
    @Override
	public Date getSendDate() {
        return (Date) this.getValue(AbstractReminder.DATE);
    }

    /**
     * This method returns the e-mail addresses which be no person or group.
     * <br />
     * NOT all e-mail addresses!
     * 
     * @return A list consisting of {@link String}s.
     */
    @Override
	public synchronized List getEMailAddresses() {
        if (this.addresses == null) {
            this.addresses = this.getEMailAddressesFromKB();
        }

        return (this.addresses);
    }

    /**
     * This method returns the person contacts.
     * 
     * @return    A list consisting of {@link PersonContact person contacts}.
     */
    @Override
	public synchronized List getPersonContacts() {
        if (this.contacts == null) {
            this.contacts = this.getPersonsContactsFromKB();
        }

        return (this.contacts);
    }

    /**
     * This method returns ALL e-mail addresses which get a notification. The
     * list contains every e-mail address only one time.
     * 
     * @return    Returns a list with ALL e-mail addresses ({@link String}).
     */
    @Override
	public List getAllEMailAddresses() {
        return MailHelper.getInstance().getEmailAddresses(this.getAllReceivers(), new ArrayList());
    }

    /**
     * This method adds an e-mail address to the reminder.
     * 
     * @param anAddress
     *            An e-mail address.
     */
    @Override
	public void addEMailAddress(String anAddress) {
        List theRecipients = this.getEMailAddresses();

        if (!theRecipients.contains(anAddress)) {
            theRecipients.add(anAddress);

            String theAddresses = StringServices.toString(theRecipients, ",");

            if (theAddresses.length() > AbstractReminder.MAX_STRING_VALUE_LENGTH) {
                Logger.warn("addEMailAddress: Attribute string longer than " + AbstractReminder.MAX_STRING_VALUE_LENGTH, this);
            }

            this.setValue(MailHelper.EMAIL_ADDRESSES, theAddresses);
        }
    }

    /**
     * This method removes an e-mail address from the reminder.
     * 
     * @param aEMailAddress
     *            An e-mail address.
     * @return Returns <i>true</i> if the e-mail address could be removed,
     *         <i>false</i> otherwise.
     */
    @Override
	public boolean removeEMailAddress(String aEMailAddress) {
        List    theList    = this.getEMailAddresses();
        boolean wasRemoved = theList.remove(aEMailAddress);

        if (wasRemoved) {
            String theAddresses = StringServices.toString(theList, ",");

            this.setValue(MailHelper.EMAIL_ADDRESSES, theAddresses);
        }

        return (wasRemoved);
    }

    /**
     * This method removes the e-mail addresses.
     */
    @Override
	public void removeEMailAddresses() {
        if (this.addresses != null) {
            this.addresses.clear();
        }

        this.setValue(MailHelper.EMAIL_ADDRESSES, "");
    }

    /**
     * This method adds a person contact to the list <i>personsContacts</i>.
     * 
     * @param aPersonContact
     *            A {@link PersonContact}.
     */
    @Override
	public void addPersonContact(PersonContact aPersonContact) {
        List theList = this.getPersonContacts();

        if (!theList.contains(aPersonContact)) {
            theList.add(aPersonContact);

            String theIDs = (String) getValue(AbstractReminder.PERSON_CONTACT_IDS);

            if (theIDs == null) {
				this.setValue(AbstractReminder.PERSON_CONTACT_IDS,
					IdentifierUtil.toExternalForm(aPersonContact.getID()));
            }
            else {
				String theString = theIDs + "," + IdentifierUtil.toExternalForm(aPersonContact.getID());

                if (theString.length() > AbstractReminder.MAX_STRING_VALUE_LENGTH) {
                    Logger.warn("addPersonContact: Attribute string longer than " + AbstractReminder.MAX_STRING_VALUE_LENGTH, this);
                }

                this.setValue(AbstractReminder.PERSON_CONTACT_IDS, theString);
            }
        }
    }

    /**
     * This method removes a person contact from the list <i>personsContacts</i>.
     * 
     * @param aPersonContact
     *            A {@link PersonContact}.
     * @return Returns <i>true</i>, if the person contact could be removed,
     *         <i>false</i> otherwise.
     */
    @Override
	public boolean removePersonContact(PersonContact aPersonContact) {
        List    theList    = this.getPersonContacts();
        boolean wasRemoved = theList.remove(aPersonContact);

        if (wasRemoved) {
            ArrayList theIDs = new ArrayList();

            for (Iterator theIt = theList.iterator(); theIt.hasNext(); ) {
                theIDs.add(((PersonContact) theIt.next()).getID());
            }

            this.setValue(PERSON_CONTACT_IDS, StringServices.toString(theIDs, ","));
        }

        return (wasRemoved);
    }

    /**
     * This method removes the person contacts.
     */
    @Override
	public void removePersonContacts() {
        if (this.contacts != null) {
            this.contacts.clear();
        }

        this.setValue(AbstractReminder.PERSON_CONTACT_IDS, "");
    }

    /** 
     * This method replaces all token in the text.
     * 
     * @param aText
     *            The text to replace the value in, must not be
     *            <code>null</code>.
     * @return    The text with the replacements.
     * @see       #getTokens()
     * @see       #getConfiguration()
     * @see       #replaceTokens(String, String[][], Properties)
     */
    protected String replaceTokens(String aText) {
        String[][] theTokens = this.getTokens();
        Properties theConf   = this.getConfiguration();

        return (AbstractReminder.replaceTokens(aText, theTokens, theConf));
    }

    /**
     * Load person contacts from the knowledge base and returns it.
     * 
     * @return    A list consisting of {@link PersonContact person contacts}.
     */
    protected List getPersonsContactsFromKB() {
        ArrayList theList = new ArrayList();
        String    theIDs  = (String) getValue(AbstractReminder.PERSON_CONTACT_IDS);

        if (!StringServices.isEmpty(theIDs)) {
            StringTokenizer theToken   = new StringTokenizer(theIDs, ",");

            while (theToken.hasMoreTokens()) {
				TLID theID = IdentifierUtil.fromExternalForm(theToken.nextToken());
				PersonContact theContact = (PersonContact) WrapperFactory.getWrapper(theID, ContactFactory.OBJECT_NAME);
                theList.add(theContact);
            }
        }

        return (theList);
    }

    /**
     * Load e-mail addresses from the knowledge base and returns it.
     * 
     * @return    A list consisting of {@link String strings}.
     */
    protected List getEMailAddressesFromKB() {
        ArrayList theResult;
        String    theAddresses = (String) getValue(MailHelper.EMAIL_ADDRESSES);

        if (!StringServices.isEmpty(theAddresses)) {
            theResult = (ArrayList) StringServices.toList(theAddresses, ',');
        }
        else {
            theResult = new ArrayList();
        }

        return (theResult);
    }

    /**
     * This method replaces all token in the text.
     * 
     * @param aText
     *            The text to replace the value in, must not be
     *            <code>null</code>.
     * @param aToken
     *            The token to be replaced, must not be <code>null</code>.
     * @param aValue
     *            The value for the token which be replaced, may be
     *            <code>null</code> or empty.
     * @return    The text with the replacements.
     */
    protected static String replaceToken(String aText, String aToken, String aValue, Properties someProps) {
        if (!StringServices.isEmpty(aValue)) {
            return aText.replaceAll("(?i:" + aToken + ")", aValue);
        }
        else {
            String theEmptyString = "";

            if (someProps != null) {
                String theProp = someProps.getProperty("noValueForToken");

                if (!StringServices.isEmpty(theProp)) {
                    theEmptyString = theProp;
                }
            }

            return aText.replaceAll("(?i:" + aToken + ")", theEmptyString);
        }
    }

    /**
     * This method replaces all tokens in the text.
     * 
     * @param aText
     *            The text to replace the value in, must not be
     *            <code>null</code>.
     * @param someTokens
     *            The array of tokens to be replaced (key,value), must not be <code>null</code>.
     * @param someProps
     *            Some properties to be used as fallback, when one of the values is <code>null</code>, 
     *            may be <code>null</code> or empty.
     * @return    The text with the replacements.
     */
    public static String replaceTokens(String aText, String[][] someTokens, Properties someProps) {
        for (int thePos = 0; thePos < someTokens.length; thePos++) {
            aText = AbstractReminder.replaceToken(aText, someTokens[thePos][0], someTokens[thePos][1], someProps);
        }

        return (aText);
    }
}
