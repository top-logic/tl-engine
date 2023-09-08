/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLClass;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.journal.ChangeJournalAttributeEntry;
import com.top_logic.knowledge.journal.JournalAttributeEntry;
import com.top_logic.knowledge.journal.JournalAttributeEntryImpl;
import com.top_logic.knowledge.wrap.AbstractFlexWrapperJournalDelegate;

/**
 * @author    <a href=mailto:kha@top-logic.com>Theo Salattteller</a>
 */
abstract public class AbstractAttributedWrapperJournalDelegate extends
        AbstractFlexWrapperJournalDelegate {

	public AbstractAttributedWrapperJournalDelegate() {
        super();
    }
    
    /**
     * Overwritten to replace flex attribute entries based on attributed attributes.
     *
     * @see com.top_logic.knowledge.journal.JournalEntry#getAttributes()
     */
    @Override
	public List getAttributes() {
        List theAttributes = super.getAttributes();
        Wrapper theWrapper = getWrapper();
        if ((theAttributes != null) && (theWrapper instanceof Wrapper)) {
            Collection theMetaEntries = new ArrayList(theAttributes.size());
            Wrapper theAttributed = (Wrapper) theWrapper;
            Iterator theIt = theAttributes.iterator();
            while (theIt.hasNext()) {
				ChangeJournalAttributeEntry theEntry = (ChangeJournalAttributeEntry) theIt.next();
                String theAttributeName = theEntry.getName();
				boolean hasMeta = theAttributed.tType().getPart(theAttributeName) != null;
                if (hasMeta) {
                    try {
                        JournalAttributeEntry theMetaEntry = getJournalAttributeEntry(theAttributed, theAttributeName, theEntry);
                        theMetaEntries.add(theMetaEntry);
                        theIt.remove();
                    } catch (NoSuchAttributeException e) {
                        Logger.error("Could not determin key for meta attribute "+theAttributeName+". use the attribute name itself for journalling.", e, this);
                    }
                }
            }    
            theAttributes.addAll(theMetaEntries);
        }
        return theAttributes;
    }

	protected JournalAttributeEntry getJournalAttributeEntry(Wrapper theAttributed, String theAttributeName,
			ChangeJournalAttributeEntry oldEntry) throws NoSuchAttributeException {
		String name = this.getI18nKeyForAttribute(theAttributed, theAttributeName);
		Object preValue = oldEntry.getPreValue();
		Object postValue = oldEntry.getPostValue();
		return new JournalAttributeEntryImpl(name, preValue, postValue);
    }
    
    protected String getI18nKeyForAttribute(Wrapper anAttributed, String anAttributeName) throws NoSuchAttributeException {
		TLStructuredTypePart attribute = anAttributed.tType().getPart(anAttributeName);
		if (attribute != null) {
			TLClass theRealMeta = AttributeOperations.getMetaElement(((TLStructuredTypePart) attribute));
			String theKey = theRealMeta.getName() + "." + attribute.getName();
			return theKey;
		} else {
			return anAttributeName;
		}
    }

}
