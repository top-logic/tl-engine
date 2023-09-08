/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.workItem.wrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.top_logic.base.workItem.WorkItem;
import com.top_logic.base.workItem.WorkItemProvider;
import com.top_logic.basic.Logger;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.model.TLClass;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * {@link WorkItemProvider} providing PWItem
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class PersistentWorkItemProvider implements WorkItemProvider {

    public PersistentWorkItemProvider() {
        super();
    }

    @Override
	public Collection getWorkItems(Person aPerson) {
        
        try {
            TLClass   theME = MetaElementFactory.getInstance().getGlobalMetaElement(PersistentWorkItemFactory.STRUCTURE_NAME);
			TLStructuredTypePart theMA = MetaElementUtil.getMetaAttribute(theME, PersistentWrapperWorkItem.ASSIGNEES_ATTRIBUTE);

			Collection theWrappers = WrapperMetaAttributeUtil.getWrappersWithValue(theMA, aPerson);
            
            Collection theResult = new ArrayList(theWrappers.size());
            for (Iterator theIt = theWrappers.iterator(); theIt.hasNext();) {
                Wrapper theWrapper = (Wrapper) theIt.next();
                if (theWrapper instanceof WorkItem) {
                    theResult.add(theWrapper);
                }
            }
            return theResult;
        }
        catch (Exception e) {
            Logger.error("...", e, this);
            return Collections.EMPTY_LIST;
        }
    }

}
