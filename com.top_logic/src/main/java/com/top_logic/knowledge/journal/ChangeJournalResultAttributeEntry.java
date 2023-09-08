/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal;

/**
 * TODO TSA this interface ...
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
public interface ChangeJournalResultAttributeEntry extends
        JournalResultAttributeEntry {
    
    /** Return the values before the change */
    Object getPre();

    /** Return the values after the change */
    Object getPost();

}
