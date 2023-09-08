/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.proxy;

import java.io.IOException;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import com.top_logic.base.mail.MailHelper;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * Message provided by a mail server.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractMailServerMessage implements MailMessage {

	/** Header attribute for getting the unique ID of a mail message. */
    public static final String MAIL_ID = "Message-ID";

    /** The original message, this meeting has been created from. */
    private Message message;

    private boolean     isInitialized = false;
    private String      body;
    private String      bodyContentType;
    private Attachements attachements;

    /** 
     * Create a new instance out of the given message.
     * 
     * @param    aMessage    The message to be represented as mail, must not be <code>null</code>.
     * @throws   IllegalArgumentException    If the given message is <code>null</code>.
     */
    public AbstractMailServerMessage(Message aMessage) throws IllegalArgumentException {
        if (aMessage == null) {
            throw new IllegalArgumentException("Given message is null");
        }

        this.message = aMessage;
    }

    @Override
	public String toString() {
        return (this.getClass().getName() + " [" + this.toStringValues() + "]");
    }

    @Override
	public String getName() {
        try {
			return this.message.getSubject();
        }
        catch (Exception ex) {
            Logger.error("Unable to get name from message", ex, this);

            return (this.message.toString());
        }
    }

    @Override
	public String getID() {
        try {
            return (AbstractMailServerMessage.getMailID(this.message));
        }
        catch (Exception ex) {
            Logger.error("Unable to extract correct mail ID", ex, this);

            return (Integer.toString(this.message.getMessageNumber()));
        }
    }

    @Override
	public Message getMessage() {
        return this.message;
    }

    @Override
	public String[] getSystemFlags() throws TopLogicException {
        try {
            Flags    theFlags  = this.getMessage().getFlags();
            Flag[]   theSystem = theFlags.getSystemFlags();
            String[] theResult = new String[theSystem.length];

            for (int thePos = 0; thePos < theSystem.length; thePos++) {
                theResult[thePos] = AbstractMailServerMessage.getSystemFlagName(theSystem[thePos]);
            }

			return theResult;
        }
        catch (Exception ex) {
			throw new TopLogicException(I18NConstants.FLAGS_SYSTEM.fill(this.getName()), ex);
        }
    }

    @Override
	public String[] getUserFlags() throws TopLogicException {
        try {
            Flags    theFlags  = this.getMessage().getFlags();
            String[] theUser   = theFlags.getUserFlags();
            String[] theResult = new String[theUser.length];

            for (int thePos = 0; thePos < theUser.length; thePos++) {
                theResult[thePos] = theUser[thePos];
            }

            return (theResult);
        }
        catch (Exception ex) {
			throw new TopLogicException(I18NConstants.FLAGS_USER.fill(this.getName()), ex);
        }
    }

	@Override
	public boolean isReportMail() {
		try {
			return MULTIPART_REPORT.equalsIgnoreCase(this.getContentType());
		} catch (MessagingException ex) {
			Logger.error("Failed to parse content type!", ex, AbstractMailServerMessage.class);
			return false;
		}
	}

	/**
	 * Return the content type of this message.
	 * 
	 * @return The requested content type.
	 * @throws MessagingException
	 *         When accessing mail server fails.
	 */
    protected String getContentType() throws MessagingException {
       String contentString = this.getMessage().getContentType().toLowerCase();
       
       int idx = contentString.indexOf(';');
       if (idx > -1) {
    	   return contentString.substring(0, idx);
       }
       return contentString;
    }
    
    @Override
	public String getBodyContent() {
        if (! this.isInitialized) {
            if (! this.initialize()) {
                return StringServices.EMPTY_STRING;
            }
        }
        
        return this.body;
    }
    
    @Override
	public String getBodyContentType() {
        if (! this.isInitialized) {
            if (! this.initialize()) {
                return StringServices.EMPTY_STRING;
            }
        }
        
        return this.bodyContentType;
    }
    
    private boolean initialize() {
        if (! this.isInitialized) {
            try {
                Folder theFolder = this.message.getFolder();
                
                if (!theFolder.isOpen()) {
                    theFolder.open(Folder.READ_WRITE);
                }
                
                String contentType = this.getContentType();
                Object content = this.message.getContent();
                
                // initialize body and attachments
                this.parseContent(content, contentType);
                
                // If there is no identified body, then display a standard content message
                if (this.body == null) {
                    this.body = this.getNoContentBody(contentType);
                    this.bodyContentType = MailHelper.CONTENT_TYPE_TEXT;
                }
                
                this.isInitialized = true;
            } catch (MessagingException e) {
                Logger.error("Unable to get content and attachments from mail " + this.message, e, AbstractMailServerMessage.class);
            } catch (IOException e) {
                Logger.error("Unable to get content and attachments from mail " + this.message, e, AbstractMailServerMessage.class);
            }
        }
        
        return this.isInitialized;
    }
    
	/**
	 * Return a default message when no content defined for the given content type.
	 * 
	 * @param contentType
	 *        Requested content type.
	 * @return Message explaining that to an user.
	 */
    protected final String getNoContentBody(String contentType) {
		return Resources.getInstance().getMessage(I18NConstants.NO_BODY, contentType);
    }

    /**
     * Parse a {@link BodyPart} as sub part of a {@link Multipart}
     * <p>
     * If no body has been found yet and the {@link BodyPart} contains a textual
     * representation, then is content is used as body.
     * <p>
     * All other content is appended as attachment
     */
    private void parseBodyPart(BodyPart aPart) throws MessagingException, UnsupportedOperationException, IOException {
        
        String contentType = aPart.getContentType().toLowerCase();
        if ((this.body == null && isContentTypeTextual(contentType)) || isContentTypeMultipart(contentType)) {
            parseContent(aPart.getContent(), contentType);
        }
        else {
            if (this.attachements == null) {
                this.attachements = new Attachements();
            }
            
            this.attachements.addAttachement(aPart);
        }
    }
    
    /**
	 * Whether the content type is {@link MailServerMessage#MULTIPART_MIXED},
	 * {@link MailServerMessage#MULTIPART_ALTERNATIVE}, or {@link MailMessage#MULTIPART_RELATED}.
	 */
    private boolean isContentTypeMultipart(String contentType) {
		return contentType.startsWith(MULTIPART_ALTERNATIVE) ||
			contentType.startsWith(MULTIPART_MIXED) ||
			contentType.startsWith(MULTIPART_RELATED);
    }
    
    /**
     * Whether the content type is text/html or text/plain
     */
    private boolean isContentTypeTextual(String contentType) {
        return contentType.startsWith(MailHelper.CONTENT_TYPE_HTML) || contentType.startsWith(MailHelper.CONTENT_TYPE_TEXT);
    }
    
    /**
     * Parse the content of a {@link Part}. The type content is given by contentType.
     */
    private void parseContent(Object content, String contentType) throws MessagingException, UnsupportedOperationException, IOException {

        if (content instanceof String) {
            if (this.body == null) {
                this.body = (String) content;
                this.bodyContentType = contentType;
                return;
            }
        }
        else if (content instanceof Multipart) {
			// There are different types of multipart
            // Parts of multipart/mixed are intended to be displayed serially
            // (which is default), whereas
            // multipart/alternative tells the user agent should choose the
            // "best" type based on the user's environment and preferences
        	// multipart/report should send an information first, that one is relevant
            if (contentType.startsWith(MULTIPART_MIXED)) {
                this.parseMultipartMixed((Multipart) content);
            }
            else if (contentType.startsWith(MULTIPART_ALTERNATIVE)) {
                this.parseMultipartAlternative((Multipart) content);
            }
            else if (contentType.startsWith(MULTIPART_REPORT)) {
            	this.parseMultipartReport((Multipart) content);
            }
			else if (contentType.startsWith(MULTIPART_RELATED)) {
				// multipart/related sontains informations which are not intended to be handled
				// separately.
				this.parseMultipartRelated((Multipart) content);
			}
            else {
                Logger.warn("Cannot parse multipart content of type '" + contentType + "', class was " + content.getClass(), AbstractMailServerMessage.class);
            }
        }
        else if (content != null) {
            Logger.warn("Cannot parse content of type '" + contentType + "', class was " + content.getClass(), AbstractMailServerMessage.class);
        }
    }
    
    @Override
	public boolean hasAttachements() throws TopLogicException {
        if (! this.isInitialized) {
            if (! this.initialize()) {
                return false;
            }
        }
        
        return this.attachements != null;
    }

    @Override
	public Attachements getAttachements() throws TopLogicException {
        if (! this.isInitialized) {
            if (! this.initialize()) {
                return null;
            }
        }
        return this.attachements;
    }

    /**
	 * Find the "best" representation of all parts as body or as attachment.
	 * 
	 * <p>
	 * For the body the "best" is an text/html or text/plain encoded part (in this order).
	 * </p>
	 * 
	 * <p>
	 * If no body is found or a body is still there, the last part will be added as attachment.
	 * </p>
	 */
    protected void parseMultipartAlternative(Multipart someParts) throws MessagingException, IOException, UnsupportedOperationException {
        int theSize  = someParts.getCount();
        
        // If there is already a body, then add the last part as attachment 
        if (this.body != null) {
            this.parseBodyPart(someParts.getBodyPart(theSize-1));
        }
        else {
            // As specified in RFC 1341 we will search backwards 
            for (int thePos = theSize-1; thePos >= 0 && this.body == null; thePos--) {
                BodyPart thePart = someParts.getBodyPart(thePos);
				String contentType = thePart.getContentType().toLowerCase();
				if (isContentTypeTextual(contentType) || isContentTypeMultipart(contentType)) {
					parseContent(thePart.getContent(), contentType);
                }
            }
            
            // If no body was found, then add the last part as attachment
            if (this.body == null) {
                this.parseBodyPart(someParts.getBodyPart(theSize-1));
            }
        }
    }
    
    /**
     * Use the first text/html or text/plain part as body (if no body was found yet) and add the rest as attachments
     */
    protected void parseMultipartMixed(Multipart someParts) throws MessagingException, IOException, UnsupportedOperationException {
        int theSize  = someParts.getCount();
        for (int thePos=0; thePos < theSize; thePos++) {
            BodyPart thePart = someParts.getBodyPart(thePos);
            this.parseBodyPart(thePart);
        }
    }
    
    /**
     * Use the first text/html or text/plain part as body (if no body was found yet) and add the rest as attachments
     */
	protected void parseMultipartRelated(Multipart someParts)
			throws MessagingException, IOException, UnsupportedOperationException {
		int theSize = someParts.getCount();

		for (int thePos = 0; thePos < theSize; thePos++) {
			BodyPart thePart = someParts.getBodyPart(thePos);
			this.parseBodyPart(thePart);
		}
	}

	/**
	 * Use the first text/html or text/plain part as body (if no body was found yet) and add the
	 * rest as attachments
	 */
    protected void parseMultipartReport(Multipart someParts) throws MessagingException, IOException, UnsupportedOperationException {
    	int theSize  = someParts.getCount();

    	for (int thePos=0; thePos < theSize; thePos++) {
    		BodyPart thePart = someParts.getBodyPart(thePos);
    		this.parseBodyPart(thePart);
    	}
    }

    /** 
     * Values to be displayed in {@link #toString()} method.
     * 
     * @return   The values to be displayed in debugging.
     */
    protected String toStringValues() {
        return ("message: " + this.message);
    }

    /**
     * Return a user understandable representation of the message flags.
     * 
     * @param    aFlag    The flag to be converted.
     * @return   The user readable version of the flag.
     */
    public static String getSystemFlagName(Flag aFlag) {
        if (Flags.Flag.ANSWERED == aFlag) {
            return ("ANSWERED");
        }
        else if (Flags.Flag.DELETED == aFlag) {
            return ("DELETED");
        }
        else if (Flags.Flag.DRAFT == aFlag) {
            return ("DRAFT");
        }
        else if (Flags.Flag.FLAGGED == aFlag) {
            return ("FLAGGED");
        }
        else if (Flags.Flag.RECENT == aFlag) {
            return ("RECENT");
        }
        else if (Flags.Flag.SEEN == aFlag) {
            return ("SEEN");
        }
        else if (Flags.Flag.USER == aFlag) {
            return ("USER");
        }
        else {
            return ("UNKNOWN");
        }
    }

    /** 
     * Return the unique ID of the given mail.
     * 
     * @param    aMessage    The mail to get the unique ID for, must not be <code>null</code>.
     * @return   The requested ID, never <code>null</code>.
     * @throws   MessagingException    When accessing header information fails.
     */
	protected static String getMailID(Message aMessage) throws MessagingException {
        String[] theArray = aMessage.getHeader(AbstractMailServerMessage.MAIL_ID);

        if ((theArray != null) && (theArray.length > 0)) {
            return (theArray[0]);
        }
        else {
            return (Integer.toString(aMessage.getMessageNumber()));
        }
    }
    
    @Override
	public boolean setFlag(Flag aFlag, boolean aValue) {
        try {
            this.getMessage().setFlag(aFlag, aValue);
            Logger.info("Set mail flag ("+getSystemFlagName(aFlag)+") for '"+this.getID()+"'", this);
            return true;
        } catch (MessagingException mex) {
            Logger.error("Unable to set flag ("+getSystemFlagName(aFlag)+") for '"+this.getID()+"'", mex, this);
        }
        return false;
    }
    
}
