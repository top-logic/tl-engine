/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.base;

import com.top_logic.knowledge.wrap.Wrapper;

/**
 * Interface for accessing the mail folder component.
 * 
 * Note: this is still a bit difficult to implement / understand
 * When implementing the getMailFolder() methodm the implementor (theAware)
 * is supposed to dispatch this call to the PosMailServer to retrieve its folder.
 * 
 * This invitites theAware to do the same when implementing the getMailFolderName() method.
 * It can be dispatched to the PosMailServer just as well. However, when doing so this will create
 * a StackOverFlow.
 * 
 * So while one method should be dispatched to the PosMailServer, the other must not.
 * But technically no difference between the both is visible when looking at the interface
 * and at the methods provided by PosMailServer
 * 
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface MailFolderAware extends Wrapper {

    /** 
     * Return the mail folder for this instance.
     * 
     * @return    The requested mail folder or <code>null</code>.
     */
    public MailFolder getMailFolder();

    /** 
     * Return the name of the mail folder.
     * 
     * This name has to be unique and will be used by the MailServer to get
     * the name of the folder on the attached mail server.
     * 
     * @return    The requested name, never <code>null</code> or empty.
     */
    public String getMailFolderName();
}
