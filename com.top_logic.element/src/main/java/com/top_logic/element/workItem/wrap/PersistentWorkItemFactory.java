/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.workItem.wrap;

import java.util.Collection;
import java.util.Iterator;

import com.top_logic.base.workItem.WorkItem;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.kbbased.AbstractWrapperResolver;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * Factory for {@link PersistentWrapperWorkItem}s.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public final class PersistentWorkItemFactory extends AbstractWrapperResolver {

    /** Name of the structure as named in above config file */
    public static final String STRUCTURE_NAME      = "WorkItem";
    /** Name of the token ctx */
    public static final String  TOKEN_CONTEXT_NAME  = "WorkItemEdit";
    
    /**
     * the singleton instance of this manager
     */
    public static PersistentWorkItemFactory getInstance(){
		return (PersistentWorkItemFactory) DynamicModelService.getFactoryFor(STRUCTURE_NAME);
    }

    @Override
	protected String getDynamicTypeSuffix(KnowledgeItem item) {
        return PersistentWrapperWorkItem.KO_TYPE;
    }

	public PersistentWrapperWorkItem createWorkItem() {
        return ((PersistentWrapperWorkItem) this.createNewWrapper(PersistentWrapperWorkItem.KO_TYPE));
    }

    /** 
     * Return the work item for the given (potential) subject where the current person is assigned to.
     * 
     * @param    aWrapper    The wrapper to get the work item for, may be <code>null</code>.
     * @return   The requested work item or <code>null</code> if no item assigned.
     */
    public WorkItem getWorkItem4Wrapper(Wrapper aWrapper) {
        if (aWrapper == null) {
            return (null);
        }
        else {
            try {
                TLClass   theME   = MetaElementFactory.getInstance().getGlobalMetaElement(STRUCTURE_NAME);
				TLStructuredTypePart theMA = MetaElementUtil.getMetaAttribute(theME, PersistentWrapperWorkItem.SUBJECT_ATTRIBUTE);
				Collection theList = WrapperMetaAttributeUtil.getWrappersWithValue(theMA, aWrapper);
    
                if (theList.size() > 0) {
                    for (Iterator theIter = theList.iterator(); theIter.hasNext(); ) {
                        WorkItem theItem = (WorkItem) theIter.next();
                        if (!theItem.getAssignees().contains(TLContext.getContext().getCurrentPersonWrapper())) {
                            theIter.remove();
                        }
                    }
                }
                
                return ((WorkItem) CollectionUtil.getSingleValueFromCollection(theList));
            }
            catch (Exception ex) {
                throw new TopLogicException(this.getClass(), "workItem.wrapper", ex);
            }
        }
    }
    
}
