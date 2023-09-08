/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal;

import java.util.ArrayList;
import java.util.Collection;

import com.top_logic.basic.TLID;

/**
 * Implementation of {@link com.top_logic.knowledge.journal.JournalResultEntry}
 * that provides easy construction and addition of attribute entries.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 *
 */
public class JournalResultEntryImpl implements JournalResultEntry {

	TLID id;
    
    /** A modifiable collection of {@link JournalResultAttributeEntry JournalResultAttributeEntries}  **/
    Collection<JournalResultAttributeEntry> attributes;
    
	public JournalResultEntryImpl(TLID anId, Collection<JournalResultAttributeEntry> someAttributes) {
        this.id = anId;
        if (someAttributes != null) {
            this.attributes = someAttributes;
        } else {
            this.attributes = new ArrayList<>();
        }
    }

    @Override
	public TLID getIdentifier() {
        return this.id;
    }

    @Override
	public Collection<JournalResultAttributeEntry> getAttributEntries() {
        return this.attributes;
    }
    
    public void add(JournalResultAttributeEntry anEntry) {
        this.attributes.add(anEntry);
    }
    
    public void add(Collection<JournalResultAttributeEntry> someAttributes) {
        this.attributes.addAll(someAttributes);
    }
        

}
