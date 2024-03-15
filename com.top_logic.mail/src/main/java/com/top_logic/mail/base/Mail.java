/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.base;

import java.util.Collection;
import java.util.Date;

import jakarta.mail.Address;

import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.util.error.TopLogicException;

/**
 * A representation of a mail.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface Mail extends Wrapper {

	/**
	 * Name of the {@link KnowledgeAssociation} between {@link Mail} and attached {@link Document}s.
	 */
	String ATTACHED_DOCUMENTS_ASSOCIATION = "hasAttachedDocuments";

    /** The type of KO wrapped by this class */
    public static final String OBJECT_NAME = "Mail";

    /** The unique ID of the mail. */
    public static final String MAIL_ID = "mailID";

    /** The name of the mail (subject). */
    public static final String NAME = "name";

    /** The name of the mail sender. */
    public static final String ATTR_FROM = "from";

    /** The name of the mail recipient. */
    public static final String ATTR_TO = "to";

    /** The name of the mail CC. */
    public static final String ATTR_CC = "cc";
    
    /** The name of the mail BCC. */
    public static final String ATTR_BCC = "bcc";
    
    /** The name of the mail BCC. */
    public static final String ATTR_SENT_DATE = "sentDate";

    /** Flag, if mail has attachments. */
    public static final String HAS_ATTACHEMENT = "attachements";

    /** 
     * Return the unique ID of this mail.
     * 
     * The ID will be taken from IMAP server or will be the ID from the knowledge object,
     * if the mail is taken from an POP-3 server.
     * 
     * @return    The unique ID of this mail, never <code>null</code>.
     */
    public abstract String getMailID() throws NoSuchAttributeException;

    /** 
     * Return the content of the wrapped mail.
     * 
     * @return    The requested content.
     * @throws    TopLogicException    If accessing the mail server fails for a reason.
     */
    public abstract Collection<? extends Wrapper> getContent() throws TopLogicException;

    /** 
     * Return the folder this mail belongs to.
     * 
     * @return    The folder this mail belongs to, never <code>null</code>.
     * @throws    TopLogicException    If accessing the mail server fails for a reason.
     */
    public abstract MailFolder getFolder() throws TopLogicException;

    /** The receiver of the mail. */
    public abstract String[] getTo();

    /** The CC of the mail. */
    public abstract String[] getCC();

    /** The BCC of the mail. */
    public abstract String[] getBCC();

    /** The sender of the mail. */
    public abstract String[] getFrom();

    /** The sent date of the mail. */
    public abstract Date getSentDate();

    /** The message of the mail. */
    public abstract String getMessage();

    /** <code>true</code> when mail has attachments. */
    public abstract boolean hasAttachements();

    /** The attachments of this mail. */
    public abstract Collection<Document> getAttachements();

	/** Set the given addresses in this mail. */
	public void setAddress(String aKey, Address[] someAddresses);
}