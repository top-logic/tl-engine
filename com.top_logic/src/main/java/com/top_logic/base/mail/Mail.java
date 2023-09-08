/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;

import com.top_logic.base.security.SecurityContext;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.util.DataAccessProxyDataSource;

/** 
 * Class for easily sending mails.
 *
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class Mail {

    /** The receiver of this message. */
    private Address sender;

    /** The list of primary receivers of this message. */
    private List<Address> receiver;

    /** The list of "cc" receivers of this message. */
    private List<Address> ccReceiver;

    /** The list of "bcc" receivers of this message. */
    private List<Address> bccReceiver;

    /** The list of "replyTo" addresses of this message. */
    private List<Address> replyTo;

    /** The title of the mail. */
    private String title;

    /** The message of the mail. */
    private String message;

    /** The type of the content of this message. */
    private String type;

    /** The attachments of this message. */
	private List<DataSource> attachments;

	/**
	 * The constructor constructing a mail where the sender is the email address.
	 * 
	 * @param aSender
	 *        The sender of the mail.
	 */
    public Mail(String aSender) {
        this.sender = createAddress(appendDomain(aSender));
    }

    /**
     * Returns the debug descsription of this instance.
     * 
     * @return    The debugging description.
     */
    @Override
	public String toString() {
        return (
            this.getClass().getName()
                + " ["
                + "recv: "
                + this.receiver
                + ", title: "
                + this.title
                + ']');
    }

    /**
     * Returns the mime type of the content in this mail.
     * 
     * This method only concentrates on the direct content, if there are more
     * embedded contents, the mime type is not able to give a correct type!
     * 
     * @return    The mime type of this message.
     */
    public String getType() {
        if (this.type == null) {
            this.type = MailHelper.CONTENT_TYPE_TEXT;
        }

        return (this.type);
    }

    /**
     * Set the mime type of the content in this mail.
     * 
     * @param    aType    The mime type of this message.
     */
    public void setType(String aType) {
        this.type = aType;
    }

    /**
     * Returns the sender of this mail.
     * 
     * @return    The sender of this mail.
     */
    public Address getSender() {
        return (this.sender);
    }

    /**
     * Set the title of this mail.
     * 
     * @param    aTitle    The title of this mail.
     */
    public void setTitle(String aTitle) {
        this.title = aTitle;
    }

    /**
     * Return the title of this mail.
     * 
     * @return    The title of this mail.
     */
    public String getTitle() {
        return (this.title);
    }

    /**
     * Return the content of this mail.
     * 
     * @return    The content of this mail.
     */
    public String getContent() {
        return (this.message);
    }

    /**
     * Set the content of this mail.
     * 
     * @param    aMessage    The content of this mail.
     */
    public void setContent(String aMessage) {
        this.message = aMessage;
    }

    /**
     * Adds the given file to the mail.
     * 
     * @param    aFile    The file to be attachted to this mail.
     */
    public void addAttachment(File aFile) {
        if ((aFile != null) && aFile.exists()) {
            this.addAttachment(new FileDataSource(aFile));
        }
    }

    /**
     * Adds the given DataSource to the Map of DataSource objects.
     * The List attachments contains the DataSource objects.
     * 
     * @param    aDataSource    DataSource to be added as attachment. 
     */
    public void addAttachment(DataSource aDataSource) {
        if (aDataSource == null) {
            Logger.error("Could not add Attachment, because it is null.", this);
        } 
        else {
            this.getAttachments().add(aDataSource);
        }
    }
    
    
    /**
     * Adds the given DataAccessProxy as DataSource to the Map of DataSource objects.
     * The List attachments contains the DataSource objects
     * to be added as attchment.
     * 
     * @param    aDataAccessProxy    the object to be added as attachment. 
     */
    public void addAttachment(DataAccessProxy aDataAccessProxy) {
        if (aDataAccessProxy == null) {
            Logger.error("Could not add Attachment, because it is null.", this);
        } 
        else {
            DataSource theDataSource = new DataAccessProxyDataSource(aDataAccessProxy);
            this.getAttachments().add(theDataSource);
        }
    }

    /**
     * Returns the list of all attachments of this mail.
     * 
     * This list contains only datasources.
     * 
     * @return    The list of held attachments.
     */
	public List getAttachments() {
        if (this.attachments == null) {
			this.attachments = new ArrayList<>();
        }

        return this.attachments;
    }

	/**
	 * Adds the given {@link Address}es to the given {@link List}.
	 */
	public void addAddresses(List aList, Address[] someAddr) {
        for (int thePos = 0; thePos < someAddr.length; thePos++) {
            aList.add(someAddr[thePos]);
        }
    }

    /**
     * Append the given receiver to the list of receivers of this mail.
     * 
     * @param    aRecv    The receiver to be added.
     * @return   true, if adding succeeds.
     */
    public boolean addReceiver(String aRecv) {
        return this.add(this.getReceiverList(), aRecv);
    }

    /**
     * Append the given receiver to the list of "cc" receivers of this mail.
     * 
     * @param    aRecv    The receiver to be added.
     * @return   true, if adding succeeds.
     */
    public boolean addCcReceiver(String aRecv) {
        return this.add(this.getCcReceiverList(), aRecv);
    }

    /**
     * Append the given receiver to the list of "bcc" receivers of this mail.
     * 
     * @param    aRecv    The receiver to be added.
     * @return   true, if adding succeeds.
     */
    public boolean addBccReceiver(String aRecv) {
       return this.add(this.getBccReceiverList(), aRecv);
    }

    /**
     * Append the given receiver to the list of "replyTo" receivers of this mail.
     * 
     * @param    aRecv    The receiver to be added.
     * @return   true, if adding succeeds.
     */
    public boolean addReplyToReceiver(String aRecv) {
        return this.add(this.getReplyToReceiverList(), aRecv);
    }

    /**
     * Convert the address into a {@link InternetAddress} and 
     * add it to the list, if its not already contained.
     * 
     * @return true, if the address was added.
     */
    private boolean add(List<Address> aList, String anAddress) {
        Address theRecv = createAddress(anAddress);
        if (theRecv == null) {     // the mail address was not valid
            return false;
        }
        
        boolean theResult = ! aList.contains(theRecv);

        if (theResult) {
            theResult = aList.add(theRecv);
        }

        return (theResult);
    }

    /**
     * Returns the whole list of receivers of this mail.
     * 
     * @return    The list of receivers of this mail.
     */
    public List<Address> getReceiverList() {
        if (this.receiver == null) {
            this.receiver = this.createList();
        }

        return (this.receiver);
    }

    /**
     * Returns the whole list of "cc" receivers of this mail.
     * 
     * @return    The list of "cc" receivers of this mail.
     */
    public List<Address> getCcReceiverList() {
        if (this.ccReceiver == null) {
            this.ccReceiver = this.createList();
        }

        return (this.ccReceiver);
    }

    /**
     * Returns the whole list of "bcc" receivers of this mail.
     * 
     * @return    The list of "bcc" receivers of this mail.
     */
    public List<Address> getBccReceiverList() {
        if (this.bccReceiver == null) {
            this.bccReceiver = this.createList();
        }

        return (this.bccReceiver);
    }

    /**
     * Returns the whole list of "replyTo" receivers of this mail.
     * 
     * @return    The list of "replyTo" receivers of this mail.
     */
    public List<Address> getReplyToReceiverList() {
        if (this.replyTo == null) {
            this.replyTo = this.createList();
        }

        return (this.replyTo);
    }

    /**
	 * Append all held attachments from this mail to the given multi part.
	 * 
	 * This method iterates through the whole attachments an appends every object found to the multi
	 * part.
	 * 
	 * @param aMultipart
	 *        The multipart to be filled.
	 */
    protected void addAttachments(Multipart aMultipart) {
        String theName = null;

        try {
            DataSource  theDataSource;
            BodyPart    theBodyPart;
            DataHandler theHandler;
			Iterator<?> theEntries = this.getAttachments().iterator();

            while (theEntries.hasNext()) {
                theDataSource = (DataSource) theEntries.next();
                theBodyPart   = new MimeBodyPart();
                theHandler    = new DataHandler(theDataSource);

                theBodyPart.setDataHandler(theHandler);

                if (theDataSource instanceof FileDataSource) {
                    String theDSName = theDataSource.getName();
                    theBodyPart.setFileName(theDSName);
                    theBodyPart.setDescription(theDSName);
                } 
                else {
                    String theDSName = theDataSource.getName();
                    theBodyPart.setFileName(theDSName);
                    theBodyPart.setDescription(theDSName);
                }

                // Adding the Bodypart to the multipart of the message.
                aMultipart.addBodyPart(theBodyPart);
            }
        }
        catch (MessagingException exc) {
            Logger.error("Could not append handle attachment with name " + 
                         theName, exc, this);
        }
    }

    /**
     * Create an address out of the given name.
     * 
     * The name should be a "normal" internet address for a user (like 
     * "mga@top-logic.com"). If the creation of the address fails,
     * this method returns a <code>null</code> pointer.
     * 
     * @param    aName    The name of the user for the address.
     * @return   The address for the user.
     */
    protected static Address createAddress(String aName) {
        try {
            return new InternetAddress(aName);
        } catch (AddressException ex) {
            Logger.info("Unable to create an address for '" + aName + "'!", ex, Mail.class);
            return (null);
        }
    }
    
	/**
	 * Creates a new Mail from the current user.
	 */
	public static Mail newMailFromCurrentUser() {
		return new Mail(getCurrentUserAddress());
	}
    
    /**
     * Return the email address of the current user.
     * If no email data is stored, the email will
     * be composed of "username@company".
     */
    public static String getCurrentUserAddress() {
        
        UserInterface theUser = SecurityContext.getCurrentUser();
        String theAddress = null;
        if (theUser != null) {
            //anAddress = theUser.getUserName();
            theAddress = theUser.getInternalMail();
            if (theAddress == null || theAddress.length() < 2){
                theAddress = theUser.getExternalMail();
            }
            if (theAddress == null || theAddress.length() < 2){
                theAddress = theUser.getUserName();
            }
            
        } else {
            theAddress = "demo";
        }

        return theAddress;
    }
    
    /**
     * Validate a string address.
     * 
     * The validation checks, whether there is an '@' in the address. If not,
     * it'll be appended and the company address will be appended to the result.
     * 
     * @param    anAddress    The address to be checked.
     * @return   A (hopefully) valid address.
     */
    protected static String appendDomain(String anAddress) {
        if (! StringServices.isEmpty(anAddress)) {
            anAddress = anAddress.trim();
            if (! StringServices.isEmpty(anAddress) && anAddress.indexOf('@') < 0) {
                anAddress = anAddress + '@' + MailSenderService.getMailDomain();
            }

            return anAddress;
        }
        return null;
    }

    /**
     * Create an empty list.
     * 
     * @return    The new created empty list.
     */
    private List<Address> createList() {
        return new ArrayList<>();
    }

}
