/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.workItem.favorites;

import java.util.Collection;

import com.top_logic.base.workItem.WorkItem;
import com.top_logic.base.workItem.WorkItemProvider;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * {@link WorkItemProvider} for all {@link WorkItem}s of type
 * {@link FavoritesUtils#WORK_ITEM_TYPE_FAVORITE}
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class FavoritesWorkItemProvider implements WorkItemProvider {

    public FavoritesWorkItemProvider() {
        super();
    }

    @Override
	public Collection getWorkItems(Person aPerson) {
        return FavoritesUtils.getFavorites(aPerson);
    }

}
