/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.workItem.favorites;

import java.util.Collection;
import java.util.HashSet;

import com.top_logic.base.workItem.WorkItem;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.Mapping;
import com.top_logic.element.workItem.wrap.PersistentWrapperWorkItem;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * The FavoritesUtils contains some usefull methods regarding favorites
 * handling.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class FavoritesUtils {

    public static final String COLUMN_NAME_FAVORITE = "favorites";

    public static final String WORK_ITEM_TYPE_FAVORITE = "favorite";

    private static final String WORKITEM_TYPE = "WorkItem.type.favorite";

    /**
	 * Return all {@link WorkItem}s of type {@value #WORK_ITEM_TYPE_FAVORITE} that are bound to the
	 * given person.
	 * 
	 * @return {@link Collection} of {@link WorkItem}s.
	 */
    public static Collection getFavorites(Person aPerson) {
        Collection<Wrapper> workitemsOfResponsible = PersistentWrapperWorkItem.getWorkitemsOfResponsible(aPerson);
        FilterUtil.filterInline(FavoritesFilter.INSTANCE, workitemsOfResponsible);
        return workitemsOfResponsible;
    }

    /**
     * Delete all {@link WorkItem}s which are referring to the given subject.
     * 
     * @return <code>true</code>, if all {@link WorkItem}s were deleted
     *         successfully
     */
    public static boolean deleteFavoritesOfSubject(Wrapper subject) {
        boolean result = true;
        Collection<Wrapper> workitemsOfSubject = PersistentWrapperWorkItem.getWorkitemsOfSubject(subject);
        FilterUtil.filterInline(FavoritesFilter.INSTANCE, workitemsOfSubject);

		return KBUtils.deleteAll(workitemsOfSubject);
    }

    /**
	 * Get all referred subjects from all {@link WorkItem}s of the given {@link Person}.
	 * 
	 * @return {@link Collection} of {@link Wrapper}, never <code>null</code>
	 */
    public static final Collection getFavoriteSubjects(Person aPerson) {
        Collection theCurrentFavorites = new HashSet();
        CollectionUtil.map(FavoritesUtils.getFavorites(aPerson).iterator(), theCurrentFavorites, new Mapping() {
            @Override
			public Object map(Object aInput) {
                return ((WorkItem) aInput).getSubject();
            }
        });
        return theCurrentFavorites;

    }

    /**
     * Return the {@link WorkItem} for the given subject and {@link Person}, of
     * <code>null</code> of no such item exist.
     */
    public static final WorkItem getFavorite(Wrapper model, Person person) {
        Collection<WorkItem> favorites = getFavorites(person);

        for (WorkItem workItem : favorites) {
            if (workItem.getSubject() == model) {
                return workItem;
            }
        }

        return null;
    }

    /**
     * Filter {@link WorkItem}s which are of type {@link FavoritesUtils#WORK_ITEM_TYPE_FAVORITE}
     * 
     * @author    <a href=mailto:fsc@top-logic.com>fsc</a>
     */
    public static class FavoritesFilter implements Filter<Wrapper> {
        public static final Filter<Wrapper> INSTANCE = new FavoritesFilter();

        @Override
		public boolean accept(Wrapper object) {
			return (object instanceof WorkItem && WORKITEM_TYPE.equals(((WorkItem) object).getWorkItemType()));
        }
    }
}
