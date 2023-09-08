/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.proxy.exchange;

import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import com.top_logic.mail.proxy.AbstractMailServerMessage;

/**
 * Representation of a mail from an exchange server (supports delete).
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ExchangeMail extends AbstractMailServerMessage {

    /** 
     * Create a new instance out of the given message.
     * 
     * @param    aMessage    The message to be represented as mail, must not be <code>null</code>.
     */
    public ExchangeMail(Message aMessage) {
        super(aMessage);
    }

	/**
	 * Delete this message on the server (by setting the {@link Flag#DELETED}.
	 * 
	 * @param directCommit
	 *        <code>true</code> when folder should expunge direct.
	 * @throws MessagingException
	 *         When expunge fails.
	 */
    public void delete(boolean directCommit) throws MessagingException {
        Message theOriginal = this.getMessage();
        Folder  theFolder   = theOriginal.getFolder();

        this.setFlag(Flag.DELETED, true);
        
        if (directCommit) {
            theFolder.expunge();
        }
    }
}
