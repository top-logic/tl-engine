/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.base.imap;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import jakarta.mail.Flags.Flag;
import jakarta.mail.Folder;
import jakarta.mail.FolderClosedException;
import jakarta.mail.FolderNotFoundException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.event.ConnectionEvent;
import jakarta.mail.event.MessageCountEvent;
import jakarta.mail.event.MessageCountListener;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.knowledge.objects.KOAttributes;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.wrap.AbstractContainerWrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.mail.base.Mail;
import com.top_logic.mail.base.MailFactory;
import com.top_logic.mail.base.MailFolder;
import com.top_logic.mail.proxy.Attachments.Attachment;
import com.top_logic.mail.proxy.MailDataSourceAdaptor;
import com.top_logic.mail.proxy.MailMessage;
import com.top_logic.mail.proxy.MailReceiver;
import com.top_logic.mail.proxy.MailReceiverService;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;


/**
 * Wrapper representation of a mail folder on a mail server.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class IMAPMailFolder extends AbstractContainerWrapper implements MailFolder {

	private static final AssociationSetQuery<KnowledgeAssociation> CONTENT =
		AssociationQuery.createOutgoingQuery("contents", CONTENTS_ASSOCIATION);

	private static final boolean NOFORCE = false;
	
    private String message;
    
	private Set<MessageCountListener> listeners;

	private Set<Message> messages;

	private Set<String> messageIDs;
    
	Folder origFolder;
    
    public IMAPMailFolder(KnowledgeObject ko) {
        super(ko);
    }

	@Override
	public Collection<? extends TLObject> getContent() {
		return resolveWrappers(CONTENT);
	}

    @Override
	protected String toStringValues() {
		String result = super.toStringValues();
		result += ", DSN: " + this.getDSN();

        return result;
    }

    @Override
	protected boolean _add(TLObject newChild) {
		boolean theResult = false;

		if (newChild != null) {
            KnowledgeObject folderKO = this.tHandle();
			KnowledgeObject newChildKO = (KnowledgeObject) newChild.tHandle();
			KnowledgeAssociation link;
			try {
				link = folderKO.getKnowledgeBase().createAssociation(folderKO, newChildKO, CONTENTS_ASSOCIATION);
			} catch (DataObjectException ex) {
				throw errorAddContentFailed(ex);
			}

            theResult = (link != null);
        }

        return (theResult);
    }

    @Override
	public void clear() {
		KBUtils.deleteAll(this.getContent());
    }
	
    @Override
	protected boolean _remove(TLObject oldChild) {
    	if (oldChild == null) {
    		return false;
    	}

		KnowledgeObject oldChildKO = (KnowledgeObject) oldChild.tHandle();
		Iterator<KnowledgeAssociation> theIt = 
			this.tHandle().getOutgoingAssociations(CONTENTS_ASSOCIATION, oldChildKO);

        // There can only be one association within a WebFolder.
        if (theIt.hasNext()) {
        	this.removeFolder(oldChildKO);
        	return true;
        } else {
        	return false;
        }
    }

    @Override
	public MailFolder getParent() {
        try {
            Folder theParent = this.getOriginalFolder().getParent();

            if (theParent != null) {
				return (MailFactory.getMailFolder(theParent));
            }
            else {
                return (null);
            }
        }
        catch (Exception ex) {
			throw new TopLogicException(I18NConstants.ERROR_FOLDER_GET_PARENT__FOLDER.fill(this.toString()), ex);
        }
    }

    @Override
	public MailFolder getFolder(String aName, boolean createIfMiss) {
		TLObject theContainer = this.getChildByName(aName);

        if (theContainer instanceof MailFolder) {
            return (MailFolder) theContainer;
        } 
        else if ((theContainer == null) && createIfMiss) {
            Folder theOrig = this.getOriginalFolder();

            try {
				MailFolder newFolder = MailFactory.createFolder(aName, theOrig);
                this.add(newFolder);
                return newFolder;
            }
            catch (Exception ex) {
				throw new TopLogicException(I18NConstants.ERROR_FOLDER_GET_CHILD__NAME.fill(aName), ex);
            }
        } else {
        	return null;
        }
    }

    @SuppressWarnings("deprecation")
	@Override
	public TLObject getChildByName(String aName) {
		return super.getChildByName(aName);
    }

    @Override
	public Mail getMail(String aMailID) {
        Mail theMail = null;

        if (!StringServices.isEmpty(aMailID)) {
            try {
				for (Iterator<? extends TLObject> theIt = this.getContent().iterator(); (theMail == null)
					&& theIt.hasNext();) {
					TLObject theObject = theIt.next();

                    if (theObject instanceof Mail) {
                        Mail theCurrent = (Mail) theObject;

                        if (aMailID.equals(theCurrent.getMailID())) {
                            theMail = theCurrent;
                        }
                    }
                }
            }
            catch (Exception ex) {
                Logger.error("Unable to find mail with ID '" + aMailID + "'!", ex, this);
            }
        }

        return (theMail);
    }

    @Override
	public Mail createMail(MailMessage aMail) {
        if (aMail != null) {
        	String      theMailID = aMail.getID();
			Transaction theTX     = this.tHandle().getKnowledgeBase().beginTransaction();

            try {
				Mail theMail = MailFactory.createMail(aMail);

				if (this.add(theMail)) {
					theTX.commit();

					this.getMessageIDs().add(theMailID);

					Logger.info("Mail '" + theMailID + "' created in folder " + this, IMAPMailFolder.class);

					return theMail;
                }
            }
            catch (Exception ex) {
                Logger.error("Unable to create a mail wrapper for mail with ID '" + theMailID + "'!", ex, IMAPMailFolder.class);
            }
            finally {
				theTX.rollback();
			}
        }

        return null;
    }

    /** 
     * Remove the given knowledge object from this mail folder.
     * 
     * @param    anObject    The object to be removed, may be <code>null</code>.
     * @return   <code>true</code>, if the object has been removed.
     */
    public boolean removeFolder(KnowledgeObject anObject) {
        try {
            if (MailFactory.isMailFolder(anObject)) {
                String theMessage = this.removeMailFolder (anObject);
            
                if (StringServices.isEmpty(theMessage)) {
                    this.removeKO(anObject);
                }
            
                this.setMessage(theMessage);
            
                return (StringServices.isEmpty(theMessage));
            }
            else if (MailFactory.isMail(anObject)) {
                return (this.removeMail(anObject));
            }
            else {
                return (false);
            }
        } 
        catch (DataObjectException ex) {
            return false;
        }
    }

    /** 
     * Return the information about the last write or delete 
     * access to this instance.
     * 
     * @return   The requested message, may be <code>null</code>.
     */
    @Override
	public String getMessage() {
        return (this.message);
    }

    /** 
     * Move the given message into this folder.
     * 
     * @param    aMessage    The message to be child of this folder, must not be <code>null</code>.
     * @return   <code>true</code>, when moving succeeds.
     * 
     * @see com.top_logic.mail.base.MailFolder#move(com.top_logic.mail.proxy.MailMessage)
     */
    @Override
	public boolean move(MailMessage aMessage) {
        Folder theFolder = this.getOriginalFolder();

        try {
            Message   theMessage  = aMessage.getMessage();
            String    theOrigName = theMessage.getFolder().getName();
            Folder    theOrig     = MailReceiverService.getMailReceiverInstance().getFolder(theOrigName);
            Message[] theMessages = new Message[] {theMessage};

			// retry handling for FolderClosedException
			int retryCount = 3;
			while (true) {
				try {
					theFolder.appendMessages(theMessages);
					Logger.debug("Appended messages to " + this.getName(), IMAPMailFolder.class);
				} catch (FolderNotFoundException fnfex) {
					Logger.info("Folder '" + theFolder + "' not found, try to create", IMAPMailFolder.class);
					if (theFolder.create(Folder.HOLDS_MESSAGES & Folder.HOLDS_FOLDERS)) {
						theFolder.appendMessages(theMessages);
					} else {
						Logger.warn("Folder '" + theFolder + "' not found, create failed", IMAPMailFolder.class);
					}
				} catch (FolderClosedException fcex) {
					Folder theDamned = fcex.getFolder();
					String theName = theDamned.getName();
					MailReceiverService.getMailReceiverInstance().closed(new ConnectionEvent(theDamned, ConnectionEvent.CLOSED));
					if (retryCount-- > 0) {
						Logger.info("Folder '" + theName + "' is closed, try to reopen it '" + retryCount + " times.",
							fcex, IMAPMailFolder.class);
						// try to reopen the folder
						MailReceiverService.getMailReceiverInstance().getFolder(theName);
						continue;
					} else {
						Logger.warn("Folder '" + theName + "' is closed", IMAPMailFolder.class);
						return false;
					}
				}
				break;
			}
            
            this.notifyMessageCountAddListeners(this.getMessageCountEvent());
            
            if (!theOrig.isOpen()) {
                theOrig = MailReceiverService.getMailReceiverInstance().getFolder(theOrigName);
            }
            
            aMessage.setFlag(Flag.DELETED, true);

            Logger.info("Move mail " + aMessage.getID() + " to folder '" + theFolder + "'!", IMAPMailFolder.class);

            return (true);
        }
        catch (MessagingException ex) {
            Logger.error("Unable to move mail "+aMessage.getID()+" to this folder (is '" + theFolder + "')! " + Thread.currentThread().toString(), ex, IMAPMailFolder.class);
            undeleteMessage(aMessage);
        }

        return (false);
    }
    
    private boolean undeleteMessage(MailMessage aMessage) {
        return aMessage.setFlag(Flag.DELETED, false) && aMessage.setFlag(Flag.SEEN, false);
    }
    
    /** 
     * Create a message count event by reading the messages
     * 
     * @return the event
     */
    private MessageCountEvent getMessageCountEvent() {
		Set<Message> theAllMsgs = this.getMessagesFromServer();
    	if (theAllMsgs == null) {
    		return new MessageCountEvent(this.getOriginalFolder(), MessageCountEvent.ADDED, false, new Message[0]);
    	}
    	
		Set<Message> theNew = new HashSet<>(theAllMsgs);

    	if (this.messages != null) {
    	    theNew.removeAll(this.messages);
    	}

    	Message[] theNewArr = new Message[theNew.size()];
    	theNew.toArray(theNewArr);
    	
    	Logger.info("Found " + theNewArr.length + " added messages", this);
    	
    	return new MessageCountEvent(this.getOriginalFolder(), MessageCountEvent.ADDED, false, theNewArr);
	}

	private Set<Message> getMessagesFromServer() {
		Folder theFolder = this.getOriginalFolder();
        if (theFolder != null) {
            try {
                Set<Message> theMessages   = new HashSet<>(Arrays.asList(theFolder.getMessages()));
				boolean debug = Logger.isDebugEnabled(IMAPMailFolder.class);

				for (Iterator<Message> theIter = theMessages.iterator(); theIter.hasNext();) {
					Message theMessage = theIter.next();

					if (theMessage.isExpunged()) {
						if (debug) {
							StringBuilder ignoreExpunged = new StringBuilder();
							ignoreExpunged.append("Expunged message number ");
							ignoreExpunged.append(theMessage.getMessageNumber());
							ignoreExpunged.append(" ignored.");
							Logger.debug(ignoreExpunged.toString(), IMAPMailFolder.class);
						}
						continue;
					}
                }
                
                return theMessages;
            } catch (Exception e) {
				Logger.error("Problem getting mails", e, IMAPMailFolder.class);
            }
        }

		return Collections.emptySet();
    }
    
	private Set<Message> getMessagesFromKnowledgeBase() {
		Folder theFolder = this.getOriginalFolder();
        if (theFolder != null) {
            try {
				Collection<? extends TLObject> contents = getContent(); // get MailMessages from KB
                Set<String>  theMessageIDs   = this.getMessageIDs();

                theMessageIDs.clear();

				for (TLObject content : contents) {
					if (!(content instanceof IMAPMail)) {
						continue;
					}
					IMAPMail mailMessage = (IMAPMail) content;
					String theID = mailMessage.getMailID();
                    if (!theMessageIDs.add(theID)) {
                        Logger.warn("IMAPMailFolder '"+this.getName()+"' contains multiple MailMessages with Message-ID "+theID, IMAPMailFolder.class);
                    }
                }
                
				Set<Message> theMessages = new HashSet<>(Arrays.asList(theFolder.getMessages()));

				for (Iterator<Message> theIter = theMessages.iterator(); theIter.hasNext();) {
					Message theMessage = theIter.next();
					if (theMessage.isExpunged()) {
						theIter.remove();
						continue;
					}
                }
                
                return theMessages;
            } catch (Exception e) {
                Logger.error("Problem getting mails", e, IMAPMailFolder.class);
            }
        }

		return Collections.emptySet();
    }
    
	/** 
     * Return the original mail folder from the mail server.
     * 
     * @return    The requested mail folder.
     */
    protected synchronized Folder getOriginalFolder() {
        if (this.origFolder == null || !this.origFolder.isOpen()) {
			String theName = this.getName();

			try {
				Logger.debug("Reopen '" + theName + "'!", IMAPMailFolder.class);
                MailReceiver theServer = MailReceiverService.getMailReceiverInstance();
    
                if (theServer.connect() != null) {
					this.origFolder = theServer.getFolder(theName); // this returns a new folder
																	// instance, opened in
																	// read/write mode
    //                if (theFolder != null && getSlowServer()) {
                    if (this.origFolder != null) {
						this.origFolder.addConnectionListener(new IMAPMailFolderConnectionListener(this));
                    }
                }
            }
            catch (Exception ex) {
				throw new TopLogicException(I18NConstants.ERROR_FOLDER_GET_ORIGINAL__FOLDER.fill(this.toString()), ex);
            }
        }
        return this.origFolder;
    }

    /** 
     * Store the given message, which contains information about the last write or delete 
     * access to this instance.
     * 
     * @param    aMessage    The message to be stored.
     */
    protected void setMessage(String aMessage) {
        this.message = aMessage;
    }
    /**
     * This method returns the messageIDs.
     * 
     * @return    Returns the messageIDs.
     */
	public Set<String> getMessageIDs() {
        if (this.messageIDs == null) {
			this.messageIDs = new HashSet<>();
        }

        return (this.messageIDs);
    }

    /** 
     * Return the URL to create a {@link DataAccessProxy} for accessing mail information.
     * 
     * @param    aMessage         The mail to get the URL for, may be <code>null</code>.
     * @param    anAttachment    The attachment to get the URL for, may be <code>null</code>.
     * @return   The requested URL, never <code>null</code>, but may be empty, if mail is <code>null</code>.
     * @throws   MessagingException    When getting URL fails.
     * @throws   IOException           When accessing the mail server fails.
     */
    protected String getMailURL(Message aMessage, Attachment anAttachment) throws MessagingException, IOException {
        if (aMessage == null) {
            return ("");
        }
        else {
            return MailFolder.MAIL_DSA_PREFIX + MailDataSourceAdaptor.getURL(aMessage, anAttachment);
        }
    }

    /** 
     * Remove the given mail.
     * 
     * @param    aKO    The knowledge object to be removed, may be <code>null</code>.
     * @return   <code>true</code>, if removing succeeds.
     */
    protected boolean removeMail(KnowledgeObject aKO) {
        try {
            return (this.removeKO(aKO) == null);
        }
        catch (DataObjectException ex) {
			throw new TopLogicException(I18NConstants.ERROR_MAIL_REMOVE__MAIL.fill(aKO.getWrapper().toString()), ex);
        }
    }

    /** 
     * Remove the given knowledge object from knowledge base.
     * 
     * @param    anObject    The object to be removed, must not be <code>null</code>.
     * @return   Always <code>null</code>.
     * @throws   DataObjectException    When deleting fails for a reason.
     */
    protected String removeKO(KnowledgeObject anObject) throws DataObjectException {
        anObject.delete();

        return (null);
    }

    /** 
     * Remove the mail folder from the mail server.
     * 
     * After calling this method the given mail folder will be marked as deleted on the mail server.
     * 
     * @param    anObject    The knowledge object representing the mail folder.
     * @return   <code>null</code>, if removing succeeds, otherwise the error message.
     */
    protected String removeMailFolder(KnowledgeObject anObject) {
        DataAccessProxy theProxy;
        String          theResult = null;
        String          theName = "Unknown";

        try {
            theName = (String) anObject.getAttributeValue(KOAttributes.PHYSICAL_RESOURCE);

            if (!StringServices.isEmpty(theName)) {
                theProxy = new DataAccessProxy(theName);

                theProxy.delete(NOFORCE);

                if (Logger.isDebugEnabled(this)) {
                    Logger.debug("removeMailFolder(): deleted: " + theProxy, this);
                }
            }
        }
        catch (Exception ex) {
            theResult = "Unable to remove \"" + theName + '"';

            Logger.error(theResult, ex, this);
        }

        return (theResult);
    }

    /** 
     * Return the mail folder for the given knowledge object.
     * 
     * @param    aKO    The knowledge object to be wrapped, must not be <code>null</code>.
     * @return   The requested mail folder, never <code>null</code>.
     */
	public static IMAPMailFolder getInstance(KnowledgeObject aKO) {
		return (IMAPMailFolder) WrapperFactory.getWrapper(aKO);
    }

    @Override
	public boolean connect() {
        return (this.connectFolder() != null);
    }

    /** 
     * Connect to the real folder on the mail server.
     * 
     * @return    The requested folder from the mail server.
     */
    protected synchronized Folder connectFolder() {
        Folder theFolder = this.getOriginalFolder();

        if (theFolder != null) {
            try {
                if (theFolder.exists()) {
                    if (!theFolder.isOpen()) {
                        theFolder.open(Folder.READ_WRITE);
                    }

                    if (this.messages == null) {
                        // INBOX
                    	if (theFolder.getParent() == null) {
							this.messages = new HashSet<>();
                    	}
                    	// subfolder
                    	else {
                    		this.messages = this.getMessagesFromKnowledgeBase();
                    	}
                    }
                }
                else {
                    theFolder = null;
                    this.messages = null;
                }
            }
            catch (MessagingException ex) {
				Logger.error("Unable to connect to folder " + theFolder, ex, IMAPMailFolder.class);
            }
			Logger.debug("Connection successful!", IMAPMailFolder.class);
        }

        return (theFolder);
    }

    @Override
	public boolean disconnect() {
        return this.disconnectFolder();
    }
    
    private boolean disconnectFolder() {
        Folder theFolder = this.getOriginalFolder();
        if (theFolder != null) {
			String theName = theFolder.getName();
            try {
                if (theFolder.isOpen()) {
                    theFolder.close(true);
                    this.messages   = null;
                    this.messageIDs = null;
					Logger.debug("Folder '" + theName + "' has been closed!", IMAPMailFolder.class);
                    return true;
                }
            } catch (FolderClosedException fex) {
				Logger.debug("Folder '" + theName + "' is already closed!", fex, IMAPMailFolder.class);
                return true;
            } catch (MessagingException mex) {
				Logger.error("Unable to close folder '" + theName, mex, IMAPMailFolder.class);
                return false;
            }
        }
        return false;
    }
    
	@Override
	public void addMessageCountListener(MessageCountListener aListener) {
        Folder theFolder = this.connectFolder();

        if (theFolder != null) {
        	if (this.listeners == null) {
				this.listeners = new HashSet<>();
        	}
        	
        	this.listeners.add(aListener);
//            theFolder.addMessageCountListener(aListener);
        }
    }

    /** 
     * Notify the registered listeners.
     * 
     * @param anEvent    Event to be send to listeners.
     */
    public void notifyMessageCountAddListeners(MessageCountEvent anEvent) {
    	if (this.listeners != null) {
			for (MessageCountListener theListener : this.listeners) {
				theListener.messagesAdded(anEvent);
			}
    	}
    }

    @Override
	public void removeMessageCountListener(MessageCountListener aListener) {
        Folder theFolder = this.connectFolder();

        if (theFolder != null) {
        	if (this.listeners != null) {
        		this.listeners.remove(aListener);
        	}
//            theFolder.removeMessageCountListener(aListener);
        }
    }

	private TopLogicException errorAddContentFailed(DataObjectException ex) {
		return new TopLogicException(I18NConstants.ERROR_FOLDER_CONTENT_ADD, ex);
	}

}
