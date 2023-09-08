/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal;

import java.util.Collection;

import com.top_logic.basic.TLID;

/**
 * Holds the change information for a single knowledge object.
 * (Multiple KO's may be commited together, these are held together by JournalResult)
 *
 * @author   <a href="mailto:dkh@top-logic.com">Dirk K&ouml;hlhoff</a>
 */
public interface JournalResultEntry {
    
    /** Get the identifier of teh KO this entry is for */
    public TLID getIdentifier();
    
    /** 
     * Get a collection of {@link JournalResultAttributeEntry}s
     * that represent the journalled attributes for the given ko id.
     */
    public Collection<JournalResultAttributeEntry> getAttributEntries();
}
