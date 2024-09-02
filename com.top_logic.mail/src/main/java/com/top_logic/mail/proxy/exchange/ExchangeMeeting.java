/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.proxy.exchange;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Stack;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.BodyPart;
import jakarta.mail.Flags.Flag;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage.RecipientType;
import jakarta.mail.internet.MimeMultipart;

import com.top_logic.base.mail.MailSenderService;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.mail.proxy.AbstractMailMeeting;
import com.top_logic.mail.proxy.MailMeeting;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * MS-Exchange implementation for the {@link MailMeeting} interface. 
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ExchangeMeeting extends AbstractMailMeeting {

    /** The user receiving this mail (needed for reply later on). */
    private String user;

    /** Stack for parsing the message during reply. */
	private Stack<String> stack = new Stack<>();

    /** Name of current section during reply. */
    private String currentArea;

    /** 
     * Create a new instance out of the given message.
     * 
     * @param    aMessage    The message to be represented as meeting, must not be <code>null</code>.
     * @throws   IllegalArgumentException    If the given message is <code>null</code>.
     * @throws   IOException                 If reading the content fails for a communication error.
     * @throws   MessagingException          If accessing the message fails for another reason.
     */
    public ExchangeMeeting(Message aMessage, String aUserMail) throws IllegalArgumentException, IOException, MessagingException {
        super(aMessage);

        this.user = aUserMail;
    }

    @Override
	public boolean hasAttachments() throws TopLogicException {
        try {
            String[] theHeaders = this.getMessage().getHeader("X-MS-Has-Attach");

            if (theHeaders != null) {
				return (theHeaders.length == 1) && "yes".equals(theHeaders[0]);
            }
        }
        catch (MessagingException ex) {
			Logger.warn("Failed to check attachments on " + this.getName(), ex, ExchangeMeeting.class);
        }

		return false;
    }

    /**
	 * Reply to this meeting.
	 * 
	 * This method will send an automatic reply for the message held (the meeting). Depending on the
	 * parameters the meeting will be accepted or rejected.
	 * 
	 * @param isOK
	 *        <code>true</code> for accepting this meeting.
	 * @return <code>true</code>, if everything worked.
	 * @throws IOException
	 *         Getting content of given message fails.
	 * @throws MessagingException
	 *         Creating a reply message fails.
	 */
    public boolean reply(boolean isOK) throws IOException, MessagingException {
		return this.reply(isOK, null);
    }

    /**
	 * Reply to this meeting.
	 * 
	 * This method will send an automatic reply for the message held (the meeting). Depending on the
	 * parameters the meeting will be accepted or rejected.
	 * 
	 * @param isOK
	 *        <code>true</code> for accepting this meeting.
	 * @param aMessage
	 *        The message to be send to the recipient, may be <code>null</code>.
	 * @return <code>true</code>, if everything worked.
	 * @throws IOException
	 *         Getting content of given message fails.
	 * @throws MessagingException
	 *         Creating a reply message fails.
	 */
    public boolean reply(boolean isOK, String aMessage) throws IOException, MessagingException {
        Message theOriginal = this.getMessage();
        Message theMessage  = this.getReply(theOriginal, isOK);

        if (aMessage != null) {
            theMessage.setText(aMessage);
        }
        
        if (MailSenderService.isConfigured()) {
			MailSenderService.getInstance().send(theMessage, new ArrayList<>(), true);
        }

        //Transport.send(theMessage);

        return (theMessage != null);
    }

    /** 
     * Delete this meeting from the servers INBOX.
     * 
     * @param    directCommit    <code>true</code> for direct committing the removal from mail server INBOX.
     * @throws   MessagingException    If removing fails for a reason.
     */
    public void delete(boolean directCommit) throws MessagingException {
        Message theOriginal = this.getMessage();
        Folder  theFolder   = theOriginal.getFolder();
        this.setFlag(Flag.DELETED, true);
//        theFolder.setFlags(new Message[] {theOriginal}, new Flags(Flags.Flag.DELETED), true);

        if (directCommit) {
            theFolder.expunge();
        }
    }

    /**
	 * Create a reply message to the one given.
	 * 
	 * @param aMessage
	 *        Message to reply to.
	 * @param isOK
	 *        <code>true</code> for accepting this meeting.
	 * @return The created reply message.
	 * @throws IOException
	 *         Getting content of given message fails.
	 * @throws MessagingException
	 *         Creating a reply message fails.
	 */
    protected Message getReply(Message aMessage, boolean isOK) throws IOException, MessagingException {
        Message theMessage = this.createMessage(aMessage);

        theMessage.setSubject((isOK ? "Zusage: " : "Absage: ") + aMessage.getSubject());

        String[] theHeader = aMessage.getHeader("Content-class");
        if (!StringServices.isEmpty(theHeader)) {
            theMessage.addHeader("Content-class", theHeader[0]);
        }
        
        theHeader = aMessage.getHeader("Thread-Topic");
        if (!StringServices.isEmpty(theHeader)) {
            theMessage.addHeader("Thread-Topic", theHeader[0]);
        }
        
        theHeader = aMessage.getHeader("thread-index");
        if (!StringServices.isEmpty(theHeader)) {
            theMessage.addHeader("thread-index", theHeader[0] + new Date().getTime());
        }

        if (aMessage.getContent() instanceof Multipart) {
            Part theReal = null;

            if (aMessage.getContentType().toLowerCase().startsWith("multipart/mixed")) {
                Multipart theMultiPart = (Multipart) aMessage.getContent();
                int       theSize      = theMultiPart.getCount();

                for (int thePos = 0; (theReal == null) && (thePos < theSize); thePos++) {
                    BodyPart theBody = theMultiPart.getBodyPart(thePos);
                    String   theType = theBody.getContentType();

                    if (theType.startsWith("multipart/alternative")) {
                        theReal = theBody;
                    }
                }
            }

            if (theReal == null) {
                theReal = aMessage;
            }
            
            this.fillReplyMessage(theMessage, theReal, isOK);
        }
        else {
            theMessage.addRecipients(RecipientType.TO, aMessage.getFrom());
            Multipart theMulti = new MimeMultipart("alternative");
            MimeBodyPart theBody = new MimeBodyPart();
            String theType = aMessage.getContentType();
            String theContent = null;

            if (theType.toLowerCase().startsWith("text/calendar")) {
                theContent = this.getCalendarContent(aMessage, isOK);

                theBody.setDataHandler(new CalendarDataHandler(theContent, theType));
            }
            if (theContent != null) {
                theMulti.addBodyPart(theBody);
            }
            theMessage.setContent(theMulti);
        }
		return theMessage;
    }

    private void fillReplyMessage(Message aReply, Part aOriginal, boolean isOK) throws MessagingException, IOException {
		Multipart theMultiPart = (Multipart) aOriginal.getContent();
        MimeMultipart theMulti     = new MimeMultipart("alternative");
        int           theSize      = theMultiPart.getCount();

        for (int thePos = 0; thePos < theSize; thePos++) {
            BodyPart     theBody    = theMultiPart.getBodyPart(thePos);
            String       theType    = theBody.getContentType();
            String       theLower   = theType.toLowerCase();
            MimeBodyPart thePart    = new MimeBodyPart();
            String       theContent = null;

            try {
                if (theLower.startsWith("text/calendar")) {
                    theContent = this.getCalendarContent(theBody, isOK);

                    thePart.setDataHandler(new CalendarDataHandler(theContent, theType));
                }
                else if (theLower.startsWith("text/plain") || theLower.startsWith("text/html")) {
                    theContent = Resources.getInstance().getString((isOK ? I18NConstants.MEETING_ACCEPT_TEXT : I18NConstants.MEETING_DECLINE_TEXT));

                    thePart.setContent(theContent, theType);
                }
                else {
                    // Will be ignored (may be files or other stuff).
                }

                if (theContent != null) {
                    theMulti.addBodyPart(thePart);
                }
            }
            catch (MessagingException ex) {
				throw new TopLogicException(I18NConstants.REPLY_CREATE_FILL.fill(aReply.getSubject()), ex);
            }
        }

        aReply.setContent(theMulti);
    }

    private Message createMessage(Message aMessage) throws MessagingException {
        return aMessage.reply(false); 
    }

    private String getCalendarContent(Part aBody, boolean isOK) throws IOException, MessagingException {
		InputStream theStream = aBody.getInputStream();
        Reader          theReader = new InputStreamReader(theStream);
        BufferedReader  theBuffer = new BufferedReader(theReader);
        StringBuffer    theResult = new StringBuffer();
        String          theStack  = "";

        do {
            String theLine = theBuffer.readLine();

            if (!StringServices.isEmpty(theLine) && theLine.charAt(0) == ' ') {
                theStack += theLine.substring(1, theLine.length());
            }
            else {
                int theDel = theStack.indexOf(':');

                if (theDel > 0) {
                    String theString = this.translateMessage(theStack, isOK);

                    if (theString != null) {
                        this.addStringFolding(theResult, theString);
                        theResult.append('\n');
                    }

                    theStack = "";
                }

                theStack = theLine;
            }
        } while (theBuffer.ready());

        if (!this.stack.isEmpty()) {
			theResult.append("END:" + this.stack.pop() + '\n');
        }

        return (theResult.toString());
    }

    private String translateMessage(String aString, boolean isOK) {
        
        if (this.stack.isEmpty()) {
            return null;
        }
        
        if (aString.startsWith("BEGIN:")) {
            String theString = aString.substring("BEGIN:".length());

            this.stack.push(theString);

            this.currentArea = theString;
        }        
        else if (aString.startsWith("END:")) {
			String theString = this.stack.pop();

            if (!("END:" + theString).equals(aString)) {
				throw new TopLogicException(I18NConstants.TRANSLATE_MESSAGE.fill(aString, theString));
            }
            else {
				this.currentArea = this.stack.peek();
            }
        }        
        else if ("VALARM".equals(this.currentArea)) {
            return (null);
        }
        else if (aString.startsWith("ACTION")) {
            return (null);
        }
        else if (aString.startsWith("ATTENDEE")) {
            String theString = aString.substring(aString.lastIndexOf(':') + 1);

            if (this.user.equalsIgnoreCase(theString)) {
                String theContent = (isOK)? "ACCEPTED" : "DECLINED";

                return (StringServices.replace(aString, "NEEDS-ACTION", theContent));
            }
            else {
                return (null);
            }
        }
        else if (aString.startsWith("CREATED:")) {
            return ("CREATED:" + this.createTime());
        }
        else if (aString.startsWith("DESCRIPTION:")) {
            if ("VEVENT".equals(this.currentArea)) {
                return ("COMMENT:OK, ich komme!");
            }
        }
        else if (aString.startsWith("LAST-MODIFIED:")) {
            return ("LAST-MODIFIED:" + this.createTime());
        }
        else if (aString.equals("METHOD:REQUEST")) {
            return ("METHOD:REPLY");
        }
        if (aString.startsWith("PRODID:")) {
            return ("PRODID:<i>TopLogic</i> Mail module");
        }
        else if (aString.startsWith("STATUS:")) {
            return ("STATUS:TENTATIVE");
        }
        else if (aString.startsWith("SEQUENCE:")) {
            return ("SEQUENCE:0");
        }
        else if (aString.startsWith("SUMMARY:")) {
            String theContent = (isOK)? "Zugesagt" : "Abgesagt";

            return ("SUMMARY:" + theContent + ": " + aString.substring("SUMMARY:".length()));
        }
        else if (aString.startsWith("TRIGGER")) {
            return (null);
        }
        else if (aString.startsWith("X-MICROSOFT-CDO-INSTTYPE:0")) {
            return ("X-MICROSOFT-CDO-INSTTYPE:0\nX-MICROSOFT-CDO-REPLYTIME:" + this.createTime());
        }
        else if (aString.startsWith("X-MICROSOFT-CDO-OWNERAPPTID:")) {
            return ("X-MICROSOFT-CDO-OWNERAPPTID:-1\nX-MICROSOFT-CDO-APPT-SEQUENCE:0");
        }

        return aString;
    }

    private String createTime() {
        return (AbstractMailMeeting.getDateFormat().format(new Date()) + "Z");
    }

    private void addStringFolding(StringBuffer aResult, String aString) {
        if (aString == null) {
            return;
        }
        else if (aString.length() <= 75) {
            aResult.append(aString);
        }
        else {
            aResult.append(aString.substring(0, 75));

            aString = aString.substring(75);

            int theLength = aString.length();

            while (theLength > 0) {
                if (theLength > 74) {
                    aResult.append(' ').append(aString.substring(0, 74));

                    aString   = aString.substring(74);
                    theLength = aString.length();
                }
                else {
                    aResult.append("\n ").append(aString);
                    theLength = 0;
                }
            }
        }
    }

    /**
	 * Handle calendar values in a stream.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
    private static class CalendarDataHandler extends DataHandler {

        public CalendarDataHandler(String aContent, String aType) {
            super(new CalendarDataSource(aContent, aType));
        }
    }

    /**
	 * Data source for calendar values.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
    private static class CalendarDataSource implements DataSource {

        private String content;
        private String type;

        public CalendarDataSource(String aContent, String aType) {
            this.content = aContent;
            this.type    = aType;
        }

        @Override
		public String getContentType() {
            return (StringServices.replace(this.type, "REQUEST", "REPLY"));
        }

        @Override
		public InputStream getInputStream() throws IOException {
            return (new ByteArrayInputStream(this.content.getBytes("UTF-8")));
        }

        @Override
		public String getName() {
            return "CalendarDataSource";
        }

        @Override
		public OutputStream getOutputStream() throws IOException {
            return null;
        }
    }
}
