/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.base;

import jakarta.mail.event.MessageCountListener;

import com.top_logic.knowledge.wrap.ContainerWrapper;
import com.top_logic.mail.proxy.MailMessage;

/**
 * A representation of a mail folder.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface MailFolder extends ContainerWrapper {

	/** Name of association for binding other {@link MailFolder} or {@link Mail} to this folder. */
	String CONTENTS_ASSOCIATION = "hasMailFolderContent";

    /** The type of KO wrapped by this class */
    public static final String OBJECT_NAME = "MailFolder";

    /** Name of the association to the parent element (e.g. WebFolder). */
    public static final String PARENT_LINK = "hasMailBox";

    /** The name of the mail (subject). */
    public static final String NAME = "name";

    /** Prefix for accessing the default mail data source adaptor. */
    public static final String MAIL_DSA_PREFIX = "mail://";

    /** 
     * Return the mail identified by its unique mail ID.
     * 
     * @param    aMailID    The unique ID of the mail, may be <code>null</code>.
     * @return   The requested mail or <code>null</code>, if mail cannot be found in this folder.
     */
    public abstract Mail getMail(String aMailID);

    /** 
     * Create a new mail from the given original mail message (from mail server).
     * 
     * @param    aMail    The mail message to be stored, may be <code>null</code>.
     * @return   The new created mail, may be <code>null</code>.
     */
    public abstract Mail createMail(MailMessage aMail);

    /** 
     * Return the information about the last write or delete 
     * access to this instance.
     * 
     * @return   The requested message, may be <code>null</code>.
     */
    public abstract String getMessage();

    /** 
     * Return the parent folder of this one.
     * 
     * @return    The requested folder or <code>null</code>, if this folder is the root folder.
     */
    public abstract MailFolder getParent();

    /** 
     * Return the child folder named by the parameter.
     * 
     * @param    aName           The name of the folder to be found.
     * @param    createIfMiss    <code>true</code>, if folder has to be created, when not found.
     * @return   The requested folder or <code>null</code>.
     */
    public abstract MailFolder getFolder(String aName, boolean createIfMiss);
    
    /** 
     * Move the given mail server message to this folder.
     * 
     * The given message is currently not represented by a Mail.
     * 
     * @param    aMessage    Message to be moved.
     * @return   <code>true</code>, if move succeeds.
     */
    public abstract boolean move(MailMessage aMessage);

    /** 
     * Connect this folder to the mail server.
     * 
     * This is needed to enable the full functionality of the folder representation.
     * 
     * @return    <code>true</code>, if connecting succeeds.
     */
    public abstract boolean connect();
    
    /** 
     * Disconnect this folder from the mail server.
     * 
     * @return    <code>true</code>, if disconnecting succeeds.
     */
    public abstract boolean disconnect();

    /** 
     * Add a listener who will be informed, when new mails arrive in the folder.
     * 
     * @param    aListener    The listener to be added.
     */
    public abstract void addMessageCountListener(MessageCountListener aListener);

    /** 
     * Remove a listener from the inner list.
     * 
     * @param    aListener    The listener to be removed.
     */
    public abstract void removeMessageCountListener(MessageCountListener aListener);
}