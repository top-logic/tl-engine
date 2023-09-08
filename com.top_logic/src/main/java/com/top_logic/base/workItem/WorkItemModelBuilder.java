/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.workItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class WorkItemModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link WorkItemModelBuilder} instance.
	 */
	public static final WorkItemModelBuilder INSTANCE = new WorkItemModelBuilder();

	private WorkItemModelBuilder() {
		// Singleton constructor.
	}

    /** 
     * Create the model by getting all work items for the current user.
     * @param    aComponent    The component calling.
     * 
     * @return   The requested model.
     * @see      com.top_logic.mig.html.ModelBuilder#getModel(Object, com.top_logic.mig.html.layout.LayoutComponent)
     */
    @Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		{
            Collection theWorkItems = WorkItemManager.getManager().getWorkItems(PersonManager.getManager().getCurrentPerson());

            return (theWorkItems instanceof List) ? theWorkItems : new ArrayList(theWorkItems);
        }
    }

    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
        return true;
    }

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		return listElement instanceof WorkItem;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
		return contextComponent.getModel();
	}

}
