/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.journal.ChangeJournalAttributeEntry;
import com.top_logic.knowledge.journal.JournalAttributeEntry;
import com.top_logic.knowledge.journal.JournalAttributeEntryExtractor;
import com.top_logic.knowledge.journal.JournalEntry;
import com.top_logic.knowledge.journal.Journallable;
import com.top_logic.knowledge.objects.ChangeInspectable;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;

/**
 * Special FlexWrapper that allows the journalling of the flexattributes 
 * via the journalling manager.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
public class JournallableWrapper extends AbstractBoundWrapper implements Journallable {

    /**
     * Create a new instance of this wrapper.
     * 
     * @param    ko         The knowledge object to be wrapped.
     */
    public JournallableWrapper(KnowledgeObject ko) {
        super(ko);
    }

    /**
     * Retunrs a WrapperJournalEntry that will reflect the changes to the FlexAttributes.
     * 
     * This is called by the JournalMananager during the commit. Be carefull
     * not to call <em>any</em> function that uses the Knowledgebase, it
     * will lead to a certain deadlock.
     * 
     * @param someChanged ignored here, may be used by subclasses.
     * @param someCreated ignored here, may be used by subclasses.
     * @param someRemoved ignored here, may be used by subclasses.
     * 
     * @return null when nothing cna be journaled.
     */
    @Override
	public JournalEntry getJournalEntry(Map someChanged, Map someCreated, Map someRemoved) {
		KnowledgeObject ko = tHandle();
    	if (ko instanceof ChangeInspectable) {
    		ItemChange changes = ((ChangeInspectable) ko).getChange();
    		if (changes == null) {
    			return null; // no need when there is nothing to store
    		}
    		try {
				return new WrapperJournalEntry(KBUtils.getWrappedObjectName(this), getJournalType(), changes);
    		} catch (Exception e) {
    			Logger.error("Could not initiate wrapper journal entry.", e, this);
    			return null;
    		}
    	} else {
    		return null;
    	}
    }
    
    /**
     * By default the Journal type is the MetaObject type.
     * 
     * Override this for polymorphic usage of Objects.
     * 
     * @return getMetaObject().getName() (or null for invalid wrapper)
     */
    @Override
	public String getJournalType() {
        if (this.tValid()) {
        	return this.tTable().getName();
        } else {
        	return null;
        }
    }

    /**
     * Inner class to monitor the changed Attributes for a Wrapper.
     * 
     * Other classe my use this implementation, too.
     */
    public static class WrapperJournalEntry implements JournalEntry {
        
        /** Identifier of original Wrapper */
		protected TLID identifier;
        
        /** Type of original Wrapper */
        protected String type;
        
		/** Event containing change informations. */
		private final ItemChange _change;
        
        /**
         * Create a new WrapperJournalEntry.
         */
		public WrapperJournalEntry(TLID anIdentifier, String aType, ItemChange change) {
            identifier = anIdentifier;
            type       = aType;
			_change = change;
        }
        
        /**
         * Not implemented, do not use.
         *
         * @throws NoSuchMethodError always.
         */
        @Override
		public void addAttribute(JournalAttributeEntry anAttribute) {
            throw new NoSuchMethodError("Not implemented, do not use");
        }
        
        /**
         * Compare the old an new Attributes and return the changed ones.
         * 
         * @see com.top_logic.knowledge.journal.JournalEntry#getAttributes()
         */
        @Override
		public List getAttributes() {
			if (_change == null) {
				return new ArrayList<ChangeJournalAttributeEntry>();
			}
			return JournalAttributeEntryExtractor.INSTANCE.createEntries(_change);
            
        }

        /** Return identifier of original Wrapper */
        @Override
		public TLID getIdentifier() {
            return identifier;
        }

        /** Return type of original Wrapper */
        @Override
		public String getType() {
            return type;
        }
        
    }
}
