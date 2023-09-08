/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.proxy;

import javax.mail.Flags.Flag;
import javax.mail.Message;

import com.top_logic.util.error.TopLogicException;

/**
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface MailServerMessage {

	/** Content type for a mixed multipart. */
    public static final String MULTIPART_MIXED = "multipart/mixed";

	/** Content type for an alternative multipart. */
    public static final String MULTIPART_ALTERNATIVE = "multipart/alternative";

    /** 
     * Return the unique identifier of the meeting in the mail server.
     * 
     * @return    The requested ID, never <code>null</code>.
     */
    public String getID();

    /** 
     * Return the name of the meeting within the mail server.
     * 
     * @return    The requested name, never <code>null</code>.
     */
    public String getName();
    
    /** 
     * Check, if this message has attachments.
     * 
     * @return    <code>true</code>, if this message contains attachments.
     * @throws    TopLogicException    If there an error occurred during check.
     */
    public boolean hasAttachements() throws TopLogicException;

    /**
     * Return the attachments located in this mail.
     * 
     * @return    The attachments or <code>null</code>, if this mail has no attachments.
     * @throws    TopLogicException    If reading the attachments from mail server fails for a reason.
     */
    public Attachements getAttachements() throws TopLogicException;

    /** 
     * Return the message, this instance was created from.
     * 
     * This can be used for getting additional information, which are not defined in this
     * interface.
     * 
     * @return    The original message, never <code>null</code>.
     */
    public Message getMessage();

    /** 
     * Return the system flags defined by the {@link #getMessage() mail message}.
     * 
     * @return    The system flags of this meeting.
     * @throws    TopLogicException    If requesting the flags fails for a reason.
     * @see       Message#getFlags()
     */
    public String[] getSystemFlags() throws TopLogicException;

    /** 
     * Return the user flags defined by the {@link #getMessage() mail message}.
     * 
     * @return    The user flags of this meeting.
     * @throws    TopLogicException    If requesting the flags fails for a reason.
     * @see       Message#getFlags()
     */
    public String[] getUserFlags() throws TopLogicException;

    /**
	 * Set the specified flag on this message to the specified value.
	 *
	 * <p>This will result in a <code>MessageChangedEvent</code> being delivered to any
	 * MessageChangedListener registered on this Message's containing folder.</p>
	 * 
	 * @param    aFlag     Flag to be set.
	 * @param    aValue    Value of the flag.
	 * @return   <code>true</code> when setting succeeds.
	 */
    public boolean setFlag(Flag aFlag, boolean aValue);
}
