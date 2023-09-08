/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.base;

import com.top_logic.mail.proxy.MailMessage;

/**
 * @author     <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
public interface MailPreprocessor {
    
    /**
     * This method performs preprocessing of mails
     * 
     * @return <code>true</code> if preprocessing succeeded and further handling of the mail is possible
     *         <code>false</code> if the mail is not to be processed any further
     */
    public boolean preprocessMail(MailMessage aMail, MailFolderAware aProject);
}
