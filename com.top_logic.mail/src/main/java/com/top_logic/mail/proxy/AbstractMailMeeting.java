/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.util.PropertyKey;


/**
 * Base implementation of a mail meeting.
 * 
 * This class served some basic operations for implementing a {@link MailMeeting}.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractMailMeeting extends AbstractMailServerMessage implements MailMeeting {

    /** Array of participants of the meeting. */
    private String[] participants;

    /** Unique ID of this meeting. */
    protected final String id;

    /**
	 * Read properties of the initial message (message contains structured information so be sure to
	 * access only unique keys via this map!).
	 */
    private Properties properties;

    /** 
     * Create a new instance out of the given message.
     * 
     * @param    aMessage    The message to be represented as meeting, must not be <code>null</code>.
     * @throws   IllegalArgumentException    If the given message is <code>null</code>.
     * @throws   IOException                 If reading the content fails for a communication error.
     * @throws   MessagingException          If accessing the message fails for another reason.
     */
    public AbstractMailMeeting(Message aMessage) throws IllegalArgumentException, MessagingException, IOException {
        super(aMessage);

        this.extractProperties(aMessage);

        this.id = this.properties.getProperty("UID");

        this.extractParticipants(this.properties);
    }

    @Override
	public String getID() {
		return this.id;
    }

    @Override
	public Date getStartDate() {
        return this.convertDate(this.getDateString(this.getProperties(), "DTSTART"));
    }

    @Override
	public Date getEndDate() {
        return this.convertDate(this.getDateString(this.getProperties(), "DTEND"));
    }

    @Override
	public String getLocation() {
		return this.getProperties().getProperty("LOCATION");
    }

    @Override
	public String getDescription() {
        String theString = this.getProperties().getProperty("DESCRIPTION");

		return (theString != null) ? StringServices.replace(theString, "\\N", "\n") : "";
    }

    @Override
	public String getStatus() {
		return this.getProperties().getProperty("STATUS");
    }

    @Override
	public String[] getParticipant() {
		return this.participants;
    }

    @Override
	public Properties getProperties() {
		return this.properties;
    }

    @Override
	protected String toStringValues() {
		return "ID: " + this.id;
    }

    /** 
     * Read property values from given stream into given properties instance.
     * 
     * @param    aStream      The stream to get the values from.
     * @param    someProps    Properties to store the values in
     * @throws   IOException  If accessing the stream fails.
     */
    public void readProperties(InputStream aStream, Properties someProps) throws IOException {
		Reader theReader = new InputStreamReader(aStream);

		try {
			BufferedReader theBuffer = new BufferedReader(theReader);
			String         theStack  = "";

			do {
				String theLine = theBuffer.readLine();

				if (StringServices.isEmpty(theLine)) {
					continue;
				}
				if (theLine.charAt(0) == ' ') {
					theStack += theLine.substring(1, theLine.length());
				} else {
					int theDel = theStack.indexOf(':');

					if (theDel > 0) {
						extractMultiKey(someProps, theStack);
						// this.properties.put(theStack.substring(0, theDel),
						// theStack.substring(theDel + 1));

						theStack = "";
					}

					theStack = theLine;
				}
			} while (theBuffer.ready());
		} finally {
			theReader.close();
		}
    }

    /** 
     * Extract the participants out of the given message.
     * 
     * This method will be called by the constructor so be careful with overiding it.
     * 
     * @param    someProps    The properties to extract the participants from, must not be <code>null</code>.
     * @throws   MessagingException    If extracting fails.
     */
    protected void extractParticipants(Properties someProps) throws MessagingException {
		Collection<String> theColl = new ArrayList<>();

		for (Object theObject : someProps.keySet()) {
			PropertyKey theKey   = (PropertyKey) theObject;
            String      theInner = theKey.getKey();

            if ("ATTENDEE".equals(theInner)) {
                this.addParticipant(theColl, someProps, theKey);
            }
            else if ("ORGANIZER".equals(theInner)) {
                this.addParticipant(theColl, someProps, theKey);
            }
        }

		this.participants = theColl.toArray(new String[theColl.size()]);
    }

    /** 
     * Add a participant to the given list of participants.
     * 
     * This method can decide, if a participant has to be added according to the 
     * additional information from the mail server (parameter: ROLE).
     * 
     * @param     aColl        The collection to append the participant to, must not be <code>null</code>.
     * @param     someProps    The properties containing the information, must not be <code>null</code>.
     * @param     aKey         The key to be used for look up, must not be <code>null</code>.
     * @return    The mail address of the participant added, may be <code>null</code>.
     */
	protected String addParticipant(Collection<String> aColl, Properties someProps, PropertyKey aKey) {
        String theEntry = this.convertMailTo2String(someProps.getProperty(aKey.getString()));

        aColl.add(theEntry);

		return theEntry;
    }

    /**
	 * Convert an array of addresses to a collection of strings.
	 * 
	 * @param    anAddress    The mailto:xxx to extract the value from.
	 * @return   The requested mail address.
	 * @see      #addParticipant(Collection, Properties, PropertyKey)
	 */
    protected String convertMailTo2String(String anAddress) {
        int thePos = anAddress.indexOf(':');

        if (thePos >= 0) {
            return anAddress.substring(thePos + 1).toLowerCase();
        }
        else {
            return anAddress.toLowerCase();
        }
    }
    
    /** 
     * Return the date looked up in a message (for start or end date).
     * 
     * @param    someProps    The properties to be used for look up.
     * @param    aString      The key of the requested date (start or end).
     * @return   The requested key, may be <code>null</code>.
     */
    protected String getDateString(Properties someProps, String aString) {
        String theTZ     = someProps.getProperty("TZID");
        String theString = aString;

        if (!StringServices.isEmpty(theTZ)) {
            theString += ";TZID=\"" + theTZ + '\"';
        }

        return someProps.getProperty(theString);
    }

    /** 
     * Convert a date read from the properties as date.
     * 
     * @param    aString    The string representation of the date.
     * @return   The parsed date or <code>null</code> if parsing fails for a reason.
     */
    protected Date convertDate(String aString) {
        try {
            return AbstractMailMeeting.getDateFormat().parse(aString);
        }
        catch (ParseException ex) {
            Logger.error("Unable to parse '" + aString + "', returning null", ex, AbstractMailMeeting.class);
    
            return (null);
        }
    }

    /** 
     * Extract the properties out of the given message.
     * 
     * This method will be called by the constructor so be careful with overriding it.
     * 
     * @param    aMessage              The message to extract the properties from, must not be <code>null</code>.
     * @throws   IOException           If reading the properties fails for a communication error.
     * @throws   MessagingException    If extracting fails.
     */
    protected void extractProperties(Part aMessage) throws IOException, MessagingException {
        Object theContent = aMessage.getContent();

        this.properties = new Properties();

        if (theContent instanceof Multipart) {
            Multipart theMultiPart = (Multipart) theContent;
            int       theSize      = theMultiPart.getCount();

            for (int thePos=0; thePos < theSize; thePos++) {
                BodyPart thePart = theMultiPart.getBodyPart(thePos);
                String   theType = thePart.getContentType().toLowerCase();

                if (theType.startsWith("text/calendar")) {
                    this.readProperties((InputStream) thePart.getContent(), this.properties);
                }
                else if (theType.startsWith("multipart/alternative")) {
                    this.extractProperties(thePart);
                }
            }
        }
        else if (aMessage.getContentType().startsWith("text/calendar")) {
            this.readProperties((InputStream) aMessage.getContent(), this.properties);
        }
        else {
            Logger.error("Content from mail meeting " + aMessage + " cannot be identified as text/calendar!", AbstractMailMeeting.class);
        }
    }

    /** 
     * Split the given value into key and value and store that in the given properties.
     * 
     * @param someProps    The properties to be stored in.
     * @param aString      The string to be split up.
     */
    protected static void extractMultiKey(Properties someProps, String aString) {
    	String[] theValues = StringServices.split(aString, ':');

        someProps.put(new PropertyKey(theValues[0]), theValues[1]);
    }

    /** 
     * Write the properties stored in the given stream to stdout.
     * 
     * @param    aStream    The stream to get the properties from.
     * @throws   IOException    When accessing the stream fails.
     */
    public static void writeProperties(InputStream aStream) throws IOException {
        Reader         theReader = new InputStreamReader(aStream);
        BufferedReader theBuffer = null;

        try {
			theBuffer = new BufferedReader(theReader);

	        do {
	            System.out.println(theBuffer.readLine());
	        } while (theBuffer.ready());
        }
        finally {
        	if (theBuffer != null) {
        		theBuffer.close();
        	}

        	theReader.close();
        }
    }

	/** Standard time format for meeting information. */
	public static DateFormat getDateFormat() {
		return CalendarUtil.newSimpleDateFormat("yyyyMMdd'T'HHmmSS");
	}
}
