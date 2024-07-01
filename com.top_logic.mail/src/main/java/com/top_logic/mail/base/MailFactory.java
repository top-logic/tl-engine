/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.mail.Address;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.knowledge.objects.KOAttributes;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.mail.proxy.Attachements;
import com.top_logic.mail.proxy.Attachements.Attachement;
import com.top_logic.mail.proxy.MailDataSourceAdaptor;
import com.top_logic.mail.proxy.MailMessage;
import com.top_logic.mail.proxy.MailReceiver;
import com.top_logic.mail.proxy.MailReceiverService;
import com.top_logic.util.error.TopLogicException;


/**
 * Helper functions for mail handling.
 */
public class MailFactory {

	private static final char ADDRESS_SEPARATOR = ',';

    /** 
     * Return the mail folder for the given name.
     * 
     * @param    aName           The full name of the requested mail folder.
     * @param    createOnMiss    Flag, if the folder should be created, if it doesn't exist.
     * @return   The requested mail folder or <code>null</code>, if the folder cannot be found.
     * @throws   TopLogicException    When creating the folder fails for a reason.
     */
	public static MailFolder getFolder(String aName, boolean createOnMiss) throws TopLogicException {
        // MailFolders are stored in the KB by its getFullName(), but
        // aName is not proofed to be aFullname.
        // quick fixed with MailServerProxy.getFullName()
        String        theFullName = MailReceiver.getFullName(null, aName);
		KnowledgeItem theDO = getObjectWithAttribute(MailFolder.OBJECT_NAME, MailFolder.NAME, theFullName);

        if (theDO instanceof KnowledgeObject) {
            return MailFactory.getMailFolder((KnowledgeObject) theDO);
        }
        else if (createOnMiss) {
			return createFolder(aName);
        }
        else {
            return null;
        }
    }

    /** 
     * Return the mail for the given mail ID.
     * 
     * @param    aMailID    The unique mail ID of the requested mail.
     * @return   The requested mail or <code>null</code>, if the mail cannot be found.
     */
	public static Mail getMailByMailID(String aMailID) {
		KnowledgeItem theDO = getObjectWithAttribute(Mail.OBJECT_NAME, Mail.MAIL_ID, aMailID);

        return (theDO instanceof KnowledgeObject) ? MailFactory.getMail((KnowledgeObject) theDO) : null;
    }

    /** 
     * Return the mail for the given knowledge object ID.
     * 
     * @param    aKOID    The knowledge object ID of the requested mail.
     * @return   The requested mail or <code>null</code>, if the mail cannot be found.
     */
	public static Mail getMailByKOID(TLID aKOID) {
		KnowledgeObject theKO = kb().getKnowledgeObject(Mail.OBJECT_NAME, aKOID);

        return (theKO != null) ? MailFactory.getMail(theKO) : null;
    }

    /** 
     * Return the mail folder for the given folder.
     * 
     * @param    aFolder    The folder to get the mail folder for, may be <code>null</code>.
     * @return   The requested folder or <code>null</code>, if nothing found.
     */
	public static MailFolder getMailFolder(Folder aFolder) {
        if (aFolder != null) {
            String        theName = MailReceiver.getFullName(aFolder, null);
			KnowledgeItem theKO = getObjectWithAttribute(MailFolder.OBJECT_NAME, MailFolder.NAME, theName);

            if (theKO instanceof KnowledgeObject) {
            	return MailFactory.getMailFolder((KnowledgeObject) theKO);
            }
            else {
                Logger.warn("No folder named '" + theName + "' found in knowledge base!", MailFactory.class);
            }
        }

        return null;
    }

    /** 
     * Create a new mail folder in the given one.
     * 
     * This method will try, if there is already a folder with the given name in the parent
     * folder (on mail server, not on knowledge base!). If it is so, it'll be used, otherwise 
     * the folder will be created.
     * 
     * Afterwards the folder will be represented by an {@link MailFolder}.
     * 
     * @param    aName      The name of the new folder, must not be <code>null</code>.
     * @param    aParent    The parent folder, must not be <code>null</code>.
     * @return   The requested folder, never <code>null</code>.
     * @throws   MessagingException   When accessing mail server fails.
     * @throws   DataObjectException  When creating knowledge object fails.
     */
	public static MailFolder createFolder(String aName, Folder aParent) throws MessagingException, DataObjectException {
		String theFullName = getFullName(aParent, aName);
		KnowledgeItem theDO = getObjectWithAttribute(MailFolder.OBJECT_NAME, MailFolder.NAME, theFullName);

        if (theDO instanceof KnowledgeObject) {
        	return MailFactory.getMailFolder((KnowledgeObject) theDO);
        }
        else {
			KnowledgeObject theKO = kb().createKnowledgeObject(MailFolder.OBJECT_NAME);

			theKO.setAttributeValue(MailFolder.NAME, theFullName);

			Logger.info("MailFolder " + theFullName + " added to KnowledgeBase", MailFactory.class);

            return MailFactory.getMailFolder(theKO);
        }
    }

	private static String getFullName(Folder parent, String name) throws MessagingException {
		int retryCount = 3;
		while (true) {
			reconnectIfNeeded();
			try {
				Folder theFolder = parent.getFolder(name);

				if (!theFolder.exists()) {
					if (!theFolder.create(Folder.HOLDS_MESSAGES & Folder.HOLDS_FOLDERS)) {
						throw new TopLogicException(
							I18NConstants.ERROR_FOLDER_CREATE.fill(name, parent.getName()));
					}
				}

				return MailReceiver.getFullName(parent, name);
			} catch (Exception e) {
				if (retryCount-- > 0) {
					Logger.info("Problem getting folder, retry " + retryCount + " times.", MailFactory.class);
				}
				throw e;
			}
		}
	}

	private static void reconnectIfNeeded() {
		MailReceiver mailServerProxy = MailReceiverService.getMailReceiverInstance();
		if (!mailServerProxy.isConnected()) {
			Logger.info("Reconnect mailstore", MailFactory.class);
			mailServerProxy.connect();
		}
	}

	/** 
	 * Create a mail wrapper for the given mail object.
	 * 
	 * @param    aMail   The mail message to create a wrapper for.
	 * @return   The requested wrapper.
	 * @throws   DataObjectException    When creating the wrapper fails.
	 * @throws   MessagingException     When accessing data from given mail message fails.
	 */
	public static Mail createMail(MailMessage aMail) throws DataObjectException, MessagingException {
		String theMailName = aMail.getName();
		String theMailID   = aMail.getID();
		Mail theMail = MailFactory.getMail(kb().createKnowledgeObject(Mail.OBJECT_NAME));

		Message theMessage     = aMail.getMessage();
		boolean hasAttachments = aMail.hasAttachements();

		theMail.setValue(Mail.MAIL_ID, theMailID);
		theMail.setValue(Mail.NAME, StringServices.minimizeString(theMailName, 150, 147));
		theMail.setValue(Mail.ATTR_SENT_DATE,  theMessage.getSentDate());
		theMail.setValue(KOAttributes.PHYSICAL_RESOURCE, getMailURL(theMessage, null));

		theMail.setAddress(Mail.ATTR_FROM, theMessage.getFrom());
		theMail.setAddress(Mail.ATTR_TO, theMessage.getRecipients(RecipientType.TO));
		theMail.setAddress(Mail.ATTR_CC, theMessage.getRecipients(RecipientType.CC));
		theMail.setAddress(Mail.ATTR_BCC, theMessage.getRecipients(RecipientType.BCC));

		theMail.setValue(Mail.HAS_ATTACHEMENT, hasAttachments);
		if (hasAttachments) {
			addAttachments(theMail, theMessage, aMail.getAttachements());
        }

		return theMail;
	}

    /** 
	 * Return the list of string representations of some addresses.
	 * 
	 * @param    someAddresses    The addresses to be converted to string.
	 * @return   The list of string representations, never null.
	 */
	public static List<String> asList(Address[] someAddresses) {
		List<String> theResult = new ArrayList<>();
	
	    if (someAddresses != null) {
	        for (Address theAddress : someAddresses) {
				theResult.add(toString(theAddress));
	        }
	    }
	
	    return theResult;
	}

	/** 
	 * Return the string representation of some addresses.
	 * 
	 * @param    someAddresses    The addresses to be converted to string.
	 * @return   The string representation.
	 */
	public static String toString(Address[] someAddresses) {
		return StringServices.toString(asList(someAddresses), Character.toString(MailFactory.ADDRESS_SEPARATOR));
	}

	/** 
	 * Convert the string representation of some addresses as string array.
	 * 
	 * @param    anAddressString    The string of addresses to be converted to an array.
	 * @return   The array of strings.
	 */
	public static String[] toArray(String anAddressString) {
		return StringServices.split(anAddressString, MailFactory.ADDRESS_SEPARATOR);
	}

    /** 
	 * Return the string representation of an address.
	 * 
	 * @param    anAddress    The address to be converted to string, must not be <code>null</code>.
	 * @return   The string representation.
	 */
	protected static String toString(Address anAddress) {
	    if (anAddress instanceof InternetAddress) {
			InternetAddress theAddress = (InternetAddress) anAddress;

			return theAddress.toUnicodeString();
	    }
	    else if (anAddress != null) {
	        return anAddress.toString();
	    }
	    else {
	    	return "";
	    }
	}

	/** 
     * Create a mail folder for the given name.
     * 
     * The new folder will be created in the default folder of the mail server.
     * 
     * @param    aName    The name of the requested mail folder.
     * @return   The requested mail folder, never <code>null</code>.
     */
	protected static MailFolder createFolder(String aName) {
        String        theFullName = MailReceiver.getFullName(null, aName);
		KnowledgeItem theDO = getObjectWithAttribute(MailFolder.OBJECT_NAME, MailFolder.NAME, theFullName);

        if (theDO instanceof KnowledgeObject) {
			throw new TopLogicException(I18NConstants.ERROR_FOLDER_CREATE_EXISTS.fill(aName, theFullName));
        }
        else {
            MailReceiver theProxy = MailReceiverService.getMailReceiverInstance();

			if (theProxy != null && theProxy.connect() != null) {
				// the parameter aName is correct because createFolder() will call
				// MailServerProxy.getFullName() again
				try {
					return createFolder(aName, theProxy.getInbox());
				} catch (Exception ex) {
					throw new TopLogicException(I18NConstants.ERROR_FOLDER_CREATE_OTHER.fill(aName), ex);
                }
			} else {
				throw new TopLogicException(I18NConstants.ERROR_FOLDER_CREATE_CONNECT.fill(aName));
            }
        }
    }

	private static KnowledgeItem getObjectWithAttribute(String aType, String anAttr, String aValue)
			throws TopLogicException {
        try {
			Iterator<KnowledgeItem> theKOs = kb().getObjectsByAttribute(aType, anAttr, aValue);

        	return theKOs.hasNext() ? theKOs.next() : null;
        }
        catch (UnknownTypeException ex) {
			throw new TopLogicException(I18NConstants.ERROR_OBJECTS_GET.fill(aType, anAttr));
        }
    }

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	private static void addAttachments(Mail aMail, Message aMailMessage, Attachements someAttachements)
			throws MessagingException, DataObjectException {
        KnowledgeObject theKO = aMail.tHandle();
        KnowledgeBase   theKB = theKO.getKnowledgeBase();

        for (Attachement theAtt : someAttachements.attachements) {
			Document theDoc = createAttachment(aMailMessage, theAtt, theKB);
			KnowledgeAssociation theKA  = theKB.createAssociation(theKO, theDoc.tHandle(), Mail.ATTACHED_DOCUMENTS_ASSOCIATION);

            if (theKA == null) {
                Logger.warn("Unable to append attachement to mail wrapper for mail with ID '" + aMail.getMailID() + "'!", MailFactory.class);
            }
        }
	}

	private static Document createAttachment(Message aMailMessage, Attachement anAttachment, KnowledgeBase aKB)
			throws MessagingException {
		String theURL = getMailURL(aMailMessage, anAttachment);

		return Document.createDocument(anAttachment.getName(), theURL, aKB, anAttachment.getSize(),
			anAttachment.getMimeType());
	}

	private static String getMailURL(Message aMessage, Attachement anAttachment) throws MessagingException {
		return (aMessage != null) ? MailFolder.MAIL_DSA_PREFIX + MailDataSourceAdaptor.getURL(aMessage, anAttachment) : "";
	}

    /** 
     * Return the mail folder for the given knowledge object.
     * 
     * @param    aKO    The knowledge object to be wrapped.
     * @return   The requested mail folder.
     */
    public static MailFolder getMailFolder(KnowledgeObject aKO) {
		return (MailFolder) aKO.getWrapper();
    }

    /** 
     * Return the mail for the given knowledge object.
     * 
     * @param    aKO    The knowledge object to be wrapped.
     * @return   The requested mail.
     */
    public static Mail getMail(KnowledgeObject aKO) {
		return (Mail) aKO.getWrapper();
    }

    /** 
     * Check, if the given object is a mail.
     * 
     * @param    anObject    The object to be checked, may be <code>null</code>.
     * @return   <code>true</code>, if the object is an instance of {@link Mail}.
     */
    public static boolean isMail(KnowledgeObject anObject) {
        return (anObject != null) && anObject.isInstanceOf(Mail.OBJECT_NAME);
    }

    /** 
     * Check, if the given object is a mail folder.
     * 
     * @param    anObject    The object to be checked, may be <code>null</code>.
     * @return   <code>true</code>, if the object is an instance of {@link MailFolder}.
     */
    public static boolean isMailFolder(KnowledgeObject anObject) {
        return (anObject != null) && anObject.isInstanceOf(MailFolder.OBJECT_NAME);
    }

}
