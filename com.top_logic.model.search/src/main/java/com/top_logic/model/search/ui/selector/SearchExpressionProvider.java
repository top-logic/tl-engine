/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.selector;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.dob.filt.DOTypeNameFilter;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.mapBasedPersistancy.MapBasedPersistancySupport;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.search.persistency.expressions.SearchExpression;
import com.top_logic.model.search.persistency.expressions.SearchExpressionImpl;
import com.top_logic.model.search.persistency.expressions.SearchExpressionStructureFactory;
import com.top_logic.util.TLContext;

/**
 * Returns the {@link SearchExpression}s stored in the {@link KnowledgeBase}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SearchExpressionProvider implements Provider<List<SearchExpressionImpl>> {

	private static final String KO_TYPE_NAME = SearchExpressionStructureFactory.KO_NAME_SEARCH_EXPRESSION_NODE;

	private static final Filter<?> KO_TYPE_FILTER = new DOTypeNameFilter(KO_TYPE_NAME);

	/** The {@link SearchExpressionProvider} instance. */
	public static final SearchExpressionProvider INSTANCE = new SearchExpressionProvider();

	@Override
	public List<SearchExpressionImpl> get() {
		if (showEveryonesSearches()) {
			return getAllStoredSearches();
		}
		return getStoredSearchesForCurrentUser();
	}

	private List<SearchExpressionImpl> getStoredSearchesForCurrentUser() {
		Person currentPerson = TLContext.getContext().getCurrentPersonWrapper();
		return getStoredSearches(currentPerson);
	}

	private List<SearchExpressionImpl> getStoredSearches(Person person) {
		boolean checkGroups = true;
		List<?> searches =
			MapBasedPersistancySupport.getContainers(person, KO_TYPE_FILTER, checkGroups);
		/* A query may be registered in multiple groups, in which case it is returned once per
		 * group. It is therefore necessary to remove duplicates. */
		searches = CollectionUtil.removeDuplicates(searches);
		return CollectionUtil.dynamicCastView(SearchExpressionImpl.class, searches);
	}

	private boolean showEveryonesSearches() {
		return ThreadContext.isAdmin() || Person.isAdmin(TLContext.getContext().getPerson());
	}

	private List<SearchExpressionImpl> getAllStoredSearches() {
		Collection<KnowledgeObject> knowledgeObjects = getKnowledgeBase().getAllKnowledgeObjects(KO_TYPE_NAME);
		return WrapperFactory.getWrappersForKOs(SearchExpressionImpl.class, knowledgeObjects);
	}

	private KnowledgeBase getKnowledgeBase() {
		return PersistencyLayer.getKnowledgeBase();
	}

}
