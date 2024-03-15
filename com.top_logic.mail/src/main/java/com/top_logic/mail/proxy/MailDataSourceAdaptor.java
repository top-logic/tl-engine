/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.proxy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;

import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Store;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.LRUMap;
import com.top_logic.basic.col.LRUWatcher;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.simple.ExampleDataObject;
import com.top_logic.dsa.DAPropertyNames;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.dsa.impl.AbstractDataSourceAdaptor;
import com.top_logic.mail.proxy.Attachements.Attachement;
import com.top_logic.mail.proxy.exchange.ExchangeMail;


/**
 * Provides data access to an IMAP mail server.
 * 
 * The extended configuration of this adaptor needs to define at least:
 * 
 * <ul>
 *   <li>server: Name of the mail server to access.</li>
 *   <li>username: Name of the user to be used for accessing.</li>
 *   <li>password: Password of user to be used for accessing.</li>
 * </ul>
 * 
 * The generated URLs will follow this schema:
 * 
 * &lt;protocol&gt;://&lt;folder 1&gt;/&lt;folder n&gt;?&lt;mail ID&gt;&amp;&lt;attachment no&gt;
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class MailDataSourceAdaptor extends AbstractDataSourceAdaptor {

	/**
	 * Configuration of a {@link MailDataSourceAdaptor}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<MailDataSourceAdaptor> {

		// No special config here

	}

	/** Delimiter for mail name. */
    public static final String MAIL_DELIMITER = "?";

	/** Delimiter for attachment name names. */
    public static final String ATTACH_DELIMITER = "&";

    /** The connection proxy to the mail server. */
    private MailReceiver proxy;

    private LRUMap mailCache;
    
	/**
	 * Creates a new {@link MailDataSourceAdaptor} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link MailDataSourceAdaptor}.
	 */
	public MailDataSourceAdaptor(InstantiationContext context, Config config) {
		this.proxy = MailReceiverService.getMailReceiverInstance();
	}

	/**
	 * Cache of mails currently provided by this adaptor.
	 * 
	 * @return The requested cache.
	 */
    protected LRUMap getMailCache() {
        if (this.mailCache == null) {
            this.mailCache = new LRUMap(60000,0);
            
            LRUWatcher.getLRUWatcher().register(this.mailCache);
        }

        return this.mailCache;
    }

    @Override
	public boolean isStructured() {
        return (true);
    }

    @Override
	public boolean isContainer(String aName) throws DatabaseAccessException {
        boolean theResult = !StringServices.isEmpty(aName);

        if (theResult) {
            int thePos = aName.indexOf(MailDataSourceAdaptor.MAIL_DELIMITER);

            theResult = (thePos < 0);

            if (theResult) {
                Folder theFolder = this.getFolder(aName, true);

                theResult = (theFolder != null);
            }
            else {
            	MailMessage  theMail = this.getMail(aName);

                theResult = theMail.hasAttachements();
            }
        }

        return (theResult);
    }

    @Override
	public boolean isEntry(String aName) throws DatabaseAccessException {
        boolean theResult = !StringServices.isEmpty(aName);

        if (theResult) {
            MailMessage theMail = this.getMail(aName);

            if (theMail == null) {
                Attachement theAtt = this.getAttachement(aName);
                theResult = (theAtt != null);
            }
            else {
                theResult = true;
            }
        }

        return (theResult);
    }

    @Override
	public String getMimeType(String aPath) throws DatabaseAccessException {
        try {
            if (!StringServices.isEmpty(aPath)) {
                int thePos = aPath.indexOf(MailDataSourceAdaptor.ATTACH_DELIMITER);

                if (thePos > 0) {
                    return this.getAttachement(aPath).getMimeType();
                }
                else {
                    thePos = aPath.indexOf(MailDataSourceAdaptor.MAIL_DELIMITER);
    
                    if (thePos > 0) {
                        MailMessage theMail  = this.getMail(aPath);
                        String      theType  = theMail.getBodyContentType();
                        String[]    theParts = StringServices.split(theType, ';');

                        return theParts[0] + "; charset="+ Charset.defaultCharset().name();
                    }
                }
            }
        }
        catch (Exception ex) {
            throw new DatabaseAccessException("Unable to get mime type for '" + aPath + "'!", ex);
        }

        return super.getMimeType(aPath);
    }

    @Override
	public String[] getEntryNames(String aName) throws DatabaseAccessException {
        boolean  theResult = !StringServices.isEmpty(aName);
        String[] theNames  = null;

        if (theResult) {
            int thePos = aName.indexOf(MailDataSourceAdaptor.ATTACH_DELIMITER);

            if (thePos > 0) {
				// Attachments don't have sub attachments for now ^^
                // TODO MGA Extend this.
                theNames = new String[0];
            }
            else {
                thePos = aName.indexOf(MailDataSourceAdaptor.MAIL_DELIMITER);

                if (thePos > 0) {
                    // Mails may have attachments
                    MailMessage theMail = this.getMail(aName);
    
                    if (theMail != null) {
                        Attachements theAttachements = theMail.getAttachements();

                        if (theAttachements != null) {
                            theNames = new String[theAttachements.getCount()];
        
                            for (int theCount = 0; theCount < theNames.length; theCount++) {
                                theNames[theCount] = aName + MailDataSourceAdaptor.ATTACH_DELIMITER + Integer.toString(theCount);
                            }
                        }
                    }
                }
                else {
                    Folder theFolder = this.getFolder(aName, true);

                    if (theFolder != null) {
                        try {
                            if (theFolder.exists()) {
                                Message[] theMessages = theFolder.getMessages();
                                Folder[]  theFolders  = theFolder.list();

                                theNames = new String[theFolders.length + theMessages.length];
                                
                                for (int theCount = 0; theCount < theFolders.length; theCount++) {
									theNames[theCount] =
										aName + theFolder.getSeparator() + theFolders[theCount].getName();
                                }

                                for (int theCount = 0; theCount < theMessages.length; theCount++) {
                                    theNames[theFolders.length + theCount] = aName + MailDataSourceAdaptor.MAIL_DELIMITER + AbstractMailServerMessage.getMailID(theMessages[theCount]);
                                }
                            }
                        }
                        catch (Exception ex) {
                            Logger.error("Unable to get entry names of " + theFolder, ex, this);
                        }
                    }
                }
            }
        }

        return (theNames);
    }

    @Override
	public String getParent(String aName) throws DatabaseAccessException {
        String  theParent = null;
        boolean theResult = !StringServices.isEmpty(aName);

        if (theResult) {
            int thePos = aName.indexOf(MailDataSourceAdaptor.ATTACH_DELIMITER);

            if (thePos > 0) {
                theParent = aName.substring(0, thePos);
            }
            else {
                thePos = aName.indexOf(MailDataSourceAdaptor.MAIL_DELIMITER);

                if (thePos > 0) {
                    theParent = aName.substring(0, thePos);
                }
                else {
					try {
						thePos = aName.indexOf(this.getStore().getDefaultFolder().getSeparator());
					} catch (MessagingException ex) {
						Logger.error("Can not get the folder separator for mail object: '" + aName + "'", ex,
							MailDataSourceAdaptor.class);
					}

                    if (thePos > 0) {
                        theParent = aName.substring(0, thePos);
                    }
                }
            }
        }

        return (theParent);
    }

    @Override
	public String getChild(String aParent, String aName) throws DatabaseAccessException {
        String theName   = null;
        Folder theFolder = this.getFolder(aParent, true);

        if (theFolder != null) {
            // Is the child a folder?
            try {
                Folder theChild = theFolder.getFolder(aName);

                if ((theChild != null) && theChild.exists()) {
					theName = aParent + theFolder.getSeparator() + aName;
                }
            }
			catch (MessagingException ex) {
				throw new DatabaseAccessException("Unable to get sub folder named '" + aName + "'!", ex);
            }

            if (theName == null) {
                // Let's try to get a mail with the given name.
                Message theMail = this.getMail(theFolder, aName);

                if (theMail != null) {
                    theName = aParent + MAIL_DELIMITER + aName;
                }
            }
        }

        return (theName);
    }

    @Override
    public InputStream getEntry(String path, String version) throws DatabaseAccessException {
        // ignore version requests, since versions are not supported by this read only adapter
        return this.getEntry(path);
    }
    
    @Override
	public InputStream getEntry(String aName) throws DatabaseAccessException {
        if (!StringServices.isEmpty(aName)) {
            int thePos = aName.indexOf(MailDataSourceAdaptor.ATTACH_DELIMITER);

            if (thePos > 0) {
                Attachement theAtt = this.getAttachement(aName);

                if (theAtt != null) {
                    try {
                        return (theAtt.getContent());
                    }
					catch (MessagingException | IOException ex) {
                        throw new DatabaseAccessException("Unable to get stream for " + aName + "'!", ex);
                    }
                }
            }
            else {
                thePos = aName.indexOf(MailDataSourceAdaptor.MAIL_DELIMITER);

                if (thePos > 0) {
                    MailMessage theMail = this.getMail(aName);

                    if (theMail != null) {
                        try {
                            String theContent  = theMail.getBodyContent();
                            String systemCharset = Charset.defaultCharset().name();
                            return new ByteArrayInputStream(theContent.getBytes(systemCharset));
                        }
                        catch (Exception ex) {
                            throw new DatabaseAccessException("Unable to get stream for '" + aName + "'!", ex);
                        }
                    }
                }
                
            }
        }
        return null;
    }

    @Override
	public boolean exists(String aName) throws DatabaseAccessException {
        boolean theResult = !StringServices.isEmpty(aName);

        if (theResult) {
            int thePos = aName.indexOf(MailDataSourceAdaptor.ATTACH_DELIMITER);

            if (thePos > 0) {
                Attachement theAtt = this.getAttachement(aName);

                theResult = (theAtt != null);
            }
            else {
                thePos = aName.indexOf(MailDataSourceAdaptor.MAIL_DELIMITER);

                if (thePos > 0) {
                    MailMessage theMail = this.getMail(aName);

                    theResult = (theMail != null);
                }
                else {
                    Folder theFolder = this.getFolder(aName, true);

                    theResult = (theFolder != null);
                }
            }
        }

        return (theResult);
    }

    /** 
     * @see com.top_logic.dsa.impl.AbstractDataSourceAdaptor#getName(java.lang.String)
     */
    @Override
	public String getName(String aName) throws DatabaseAccessException {
        String  theName = null;
        MailMessage theMail = this.getMail(aName);

        if (theMail != null) {
            theName = theMail.getName();
        }
        else {
            Attachement theAttachement = this.getAttachement(aName);

            if (theAttachement != null) {
				theName = theAttachement.getName();
            }
            else {
                Folder theFolder = this.getFolder(aName, false);
    
                if (theFolder != null) {
                    theName = theFolder.getName();
                }
            }            
        }

        return (theName);
    }

    @Override
	public DataObject getProperties(String aName) throws DatabaseAccessException {
		HashMap<String, Object> theMap = new HashMap<>();
        MailMessage    theMail = this.getMail(aName);

        if (theMail != null) {
            try {
                theMap.put(DAPropertyNames.NAME, theMail.getName());
                theMap.put(DAPropertyNames.IS_ENTRY, Boolean.TRUE);
				theMap.put(DAPropertyNames.SIZE, Long.valueOf(theMail.getMessage().getSize()));
            }
            catch (MessagingException ex) {
                Logger.error("Unable to fill properties for " + aName, ex, this);
            }
        }
        else {
            Attachement theAttachement = this.getAttachement(aName);

            if (theAttachement != null) {
                try {
                    theMap.put(DAPropertyNames.NAME, theAttachement.getName());
                    theMap.put(DAPropertyNames.IS_ENTRY, Boolean.TRUE);
					theMap.put(DAPropertyNames.SIZE, Long.valueOf(theAttachement.getSize()));
                }
                catch (MessagingException ex) {
                    Logger.error("Unable to fill properties for " + aName, ex, this);
                }
            }
            else {
                Folder theFolder = this.getFolder(aName, false);
    
                if (theFolder != null) {
                    try {
                        theMap.put(DAPropertyNames.NAME, theFolder.getName());
                        theMap.put(DAPropertyNames.IS_ENTRY, Boolean.FALSE);
						theMap.put(DAPropertyNames.SIZE, Long.valueOf(theFolder.getMessageCount()));
                    }
                    catch (MessagingException ex) {
                        Logger.error("Unable to fill properties for " + aName, ex, this);
                    }
                }
            }            
   
        }

        return new ExampleDataObject(theMap);
    }

    /** 
     * Return the mail identified by the given name.
     * 
     * @param    aName    The full name of the mail, must not be <code>null</code>.
     * @return   The requested mail or <code>null</code>.
     */
    protected Message getMailFromServer(String aName) {
        Message theMail   = null;
        int     theFPos   = aName.indexOf(MailDataSourceAdaptor.MAIL_DELIMITER);
        int     theAPos   = aName.indexOf(MailDataSourceAdaptor.ATTACH_DELIMITER);
        boolean theResult = (theFPos > 0) && (theAPos < 0);

        if (theResult) {
            String theFName  = aName.substring(0, theFPos);
            Folder theFolder = this.getFolder(theFName, true);

            theResult = (theFolder != null);

            if (theResult) {
                String theID = aName.substring(theFPos + 1);

                theMail = this.getMail(theFolder, theID);
            }
            else {
                Logger.warn("Unable to get folder called '" + theFName + "' from mail store (in getMail('" + aName + "'))!", this);
            }
        }

        return (theMail);
    }

	/**
	 * Return the mail with the given name.
	 * 
	 * @param aName
	 *        Requested name.
	 * @return Requested mail, may be <code>null</code>.
	 */
    protected MailMessage getMail(String aName) {
    	LRUMap      theCache = this.getMailCache();
    	MailMessage theMail  = (MailMessage) theCache.get(aName);

    	if (theMail == null) {
    		Message theMessage = this.getMailFromServer(aName);

    		if (theMessage != null) {
    		    theMail = new ExchangeMail(theMessage);
    		    theCache.put(aName, theMail);
    		}
    	}

		return theMail;
    }
    
    /** 
     * Return the mail identified by its ID from the given folder.
     * 
     * @param    aFolder    The folder to get the mail from, must not be <code>null</code>.
     * @param    anID       The unique ID of the mail, must not be <code>null</code>.
     * @return   The requested mail or <code>null</code>.
     */
    protected Message getMail(Folder aFolder, String anID) {
        Message theMail = null;

        try {
            Message[] theMessages = aFolder.getMessages();

            for (int thePos = 0; (theMail == null) && (thePos < theMessages.length); thePos++) {
                Message theMessage = theMessages[thePos];

                if (anID.equals(AbstractMailServerMessage.getMailID(theMessage))) {
                    theMail = theMessage;
                }
            }
        }
        catch (Exception ex) {
            Logger.warn("Unable to get mail called '" + anID + "' from mail folder '" + aFolder+ "'!", ex, this);
        }

        return (theMail);
    }

    /** 
     * Return the named folder from store.
     * 
     * @param    aName        The name of the folder to be looked up, must nor be <code>null</code>.
     * @param    onlyExist    Flag, if this method should only return existing folders.
     * @return   The requested folder or <code>null</code>.
     * @see      #getStore()
     */
    protected Folder getFolder(String aName, boolean onlyExist) {
        Folder theFolder  = null;
        Store  theStore   = this.getStore();

		if (theStore != null) {
            try {
                theFolder = theStore.getFolder(aName);

                if (onlyExist && !theFolder.exists()) {
                    theFolder = null;
                }
                else if (!theFolder.isOpen()) {
                    theFolder.open(Folder.READ_WRITE);
                }
            }
            catch (MessagingException ex) {
                Logger.warn("Unable to get folder called '" + aName + "' from mail store!", ex, this);
            }
        }

		return theFolder;
    }

    /** 
     * Return the attachment identified by the given name. 
     * 
     * @param    aName    The name identifying the attachment, must not be <code>null</code>.
     * @return   The requested attachment or <code>null</code>, if attachment not found.
     */
    protected Attachement getAttachement(String aName) {
        Attachement theAtt = null;
        int         thePos = aName.indexOf(MailDataSourceAdaptor.ATTACH_DELIMITER);

        if (thePos > 0) {
            MailMessage theMail = this.getMail(aName.substring(0, thePos));

            if (theMail != null) {
                Attachements theAttachements = theMail.getAttachements();
    
                if (theAttachements != null) {
                    String theID = aName.substring(thePos + 1);
    
                    try {
                        int theInt = Integer.valueOf(theID).intValue();

                        theAtt = theAttachements.getAttachement(theInt);
                    }
                    catch (Exception ex) {
                        Logger.warn("Unable to get attachment '" + theID + "' from '" + aName + "'!", ex, this);
                    }
                }
            }
        }

        return (theAtt);
    }
    /** 
     * Return the attachments of the given mail.
     * 
     * @param    aMail    The mail to get the attachments for, must not be <code>null</code>.
     * @return   The requested attachments or <code>null</code>, if no attachments found.
     */
    protected Attachements getAttachements(Message aMail) {
        try {
            ExchangeMail theMail = new ExchangeMail(aMail);

            return (theMail.getAttachements());
        }
        catch (Exception ex) {
            Logger.warn("Unable to get attachments from '" + aMail + "'!", ex, this);

            return (null);
        }
    }

    /** 
     * Return the connection to the configured mail server store.
     * 
     * @return    The requested store, never <code>null</code>.
     * @see       MailReceiver#connect()
     */
    protected Store getStore() {
        if (this.proxy == null) {
            this.proxy = MailReceiverService.getMailReceiverInstance();
        }

        return (this.proxy.getStore());
    }

    /** 
     * Return the DSA part of the URL for the given mail and attachment.
     *
     * If the given mail is <code>null</code>, this method will return an empty string. If
     * the given attachment is <code>null</code>, the returned URL will point to the mail,
     * otherwise to the attachment.
     * 
     * @param    aMail    The mail to get the URL for, may be <code>null</code>.
     * @param    anAtt    The attachment to get the URL for, may be <code>null</code>.
     * @return   The requested URL, never <code>null</code>, but may be empty, if mail is <code>null</code>.
     * @throws   MessagingException   When accessing the mail header fails.
     * @see      AbstractMailServerMessage#getMailID(Message)
     */
	public static String getURL(Message aMail, Attachement anAtt) throws MessagingException {
        StringBuffer theResult = new StringBuffer();

        if (aMail != null) {
            Folder theFolder = aMail.getFolder();

            theResult.append(theFolder.getFullName()).append(MailDataSourceAdaptor.MAIL_DELIMITER);
            theResult.append(AbstractMailServerMessage.getMailID(aMail));

            if (anAtt != null) {
                theResult.append(MailDataSourceAdaptor.ATTACH_DELIMITER).append(anAtt.getID());
            }
        }

        return (theResult.toString());
    }
}
