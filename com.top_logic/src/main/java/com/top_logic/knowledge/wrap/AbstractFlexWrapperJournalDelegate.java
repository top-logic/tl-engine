/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.journal.JournalAttributeEntry;
import com.top_logic.knowledge.journal.JournalAttributeEntryExtractor;
import com.top_logic.knowledge.journal.JournalEntry;
import com.top_logic.knowledge.objects.ChangeInspectable;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.util.WrapperUtil;

/**
 * Delegate to proviede journalling functionality for flex wrappers
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
abstract public class AbstractFlexWrapperJournalDelegate implements JournalEntry {

	/** The change event of the {@link KnowledgeObject} */
	final ItemChange _change;

    /** A collection of additional {@link JournalAttributeEntry}s */
    Collection additionalAttributes;
    
    /**
     * the wrapper delegating the journalling
     */
    protected abstract AbstractWrapper getWrapper();
    
	public AbstractFlexWrapperJournalDelegate() {
        // get storages of ko attributes
        Wrapper theWrapper = getWrapper();
        KnowledgeObject theKO = theWrapper.tHandle();
        if (theKO instanceof ChangeInspectable) {
        	ChangeInspectable changeInspectable = (ChangeInspectable) theKO;
			_change = changeInspectable.getChange();
        }
        else {
			_change = null;
        }
    }

    
    /** TODO TSA the mthods to delegates to .. */ 
    public JournalEntry getJournalEntry() {
        try {
			if (_change == null) {
                return null;
            }
            return this;
        } catch (Exception e) {
            Logger.error("Could not initialize wrapper journal entry.", e, this);
            return null;
        }
    }
    
    public static String getJournalType(AbstractWrapper aWrapper) {
    	return aWrapper.tTable().getName();
    }
    
    
    @Override
	public synchronized void addAttribute(JournalAttributeEntry anAttribute) {
        if (this.additionalAttributes == null) {
            {
                {
                    this.additionalAttributes = new ArrayList();
                }
            }
        }
        this.additionalAttributes.add(anAttribute);
    }
    
    /**
     * Get the {@link JournalAttributeEntry}s representing the changes made to this object.
     * 
     * @return a collection of {@link JournalAttributeEntry}s
     *
     * @see com.top_logic.knowledge.journal.JournalEntry#getAttributes()
     */
    @Override
	public List getAttributes() {
		List theAttributes = this.getAttributeEntries();
		theAttributes.addAll(theAttributes);
        if (this.additionalAttributes != null) {
            theAttributes.addAll(this.additionalAttributes);
        }
        return theAttributes;
        
    }
    
	private List getAttributeEntries() {
		// check if nothing changed
		if (_change == null) {
			return new ArrayList();
		}
		return JournalAttributeEntryExtractor.INSTANCE.createEntries(_change);
    }

    @Override
	public TLID getIdentifier() {
            return KBUtils.getWrappedObjectName(getWrapper());
    }

    @Override
	public String getType() {
    	return WrapperUtil.getMetaObjectName(getWrapper());
    }
    
}
