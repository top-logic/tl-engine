/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.search.Query;
import com.top_logic.base.search.QueryException;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.knowledge.searching.Filter;
import com.top_logic.knowledge.searching.Precondition;
import com.top_logic.knowledge.searching.QueryConfig;
import com.top_logic.knowledge.searching.SearchAttribute;
import com.top_logic.knowledge.searching.SearchEngine;
import com.top_logic.knowledge.searching.SearchMessage;
import com.top_logic.knowledge.searching.SearchResultSet;
import com.top_logic.knowledge.searching.SearchResultSetImpl;
import com.top_logic.knowledge.searching.SearchResultSetSPI;
import com.top_logic.knowledge.searching.SearchService;

/**
 * The class {@link SearchFactory} provides the {@link SearchService}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SearchFactory extends ManagedClass implements SearchService {

	/**
	 * Configuration for the {@link SearchFactory}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ManagedClass.ServiceConfiguration<SearchFactory> {

		/**
		 * All {@link SearchEngine} that can be used.
		 */
		List<PolymorphicConfiguration<SearchEngine>> getSearchEngines();

	}

	private List<SearchEngine> _searchEngines;

	/**
	 * Creates a new {@link SearchFactory}.
	 */
	public SearchFactory(InstantiationContext context, Config config) {
		super(context, config);
		_searchEngines = TypedConfiguration.getInstanceList(context, config.getSearchEngines());
	}

	public static final SearchService getSearchService() {
		return Module.INSTANCE.getImplementationInstance();
	}

	@Override
	public SearchResultSet search(String aQuery) {
		try {
			return search(Query.parse(aQuery));
		} catch (QueryException ex) {
			SearchResultSetSPI result = createEmptyResultSet();
			SearchMessage errorMessage = SearchMessage.error(ex.getMessage(), null);
			result.addSearchMessage(errorMessage);
			return result;
		}
	}

	@Override
	public SearchResultSet search(Query aQuery) {
		QueryConfig queryConfig = new QueryConfig();

		queryConfig.setQuery(aQuery);

		return search(queryConfig);
	}

	@Override
	public SearchResultSet search(QueryConfig aQuery) {
		List<SearchEngine> theList = null;
		Precondition thePre = aQuery.getPrecondition();
		Filter theFilter = aQuery.getFilter();

		if (thePre != null) {
			theList = thePre.getSearchEngines();
		}

		if ((theList == null) || (theList.size() == 0)) {
			theList = this.getSearchEngines();
		}

		SearchResultSetSPI theSet = this.createResultSet(theList);

		for (SearchEngine theEngine : theList) {
			theEngine.search(aQuery, theSet);
		}

		if (theFilter != null) {
			// wait until search has been finished
			theSet.waitForClosed(1000);

			int theSize = theSet.getSearchResults().size();

			theSet = (SearchResultSetSPI) theFilter.filter(theSet);

			if (Logger.isDebugEnabled(this)) {
				int theSize2 = theSet.getSearchResults().size();

				Logger.debug("Removed " + (theSize - theSize2) +
					" hits for " + aQuery.getQuery() +
					" by filter " + theFilter,
					this);
			}
		}
		return (theSet);
	}

	@Override
	public List<SearchEngine> getSearchEngines() {
		return (new ArrayList<>(this.internalGetSearchEngines()));
	}

	@Override
	public Precondition createPrecondition() {
		Precondition theCond = new Precondition();

		for (SearchEngine theEngine : getSearchEngines()) {
			List<SearchAttribute> theAttrs = theEngine.getSearchAttributes();

			if (theCond.addSearchEngine(theEngine)) {
				for (SearchAttribute theAttr : theAttrs) {
					theCond.addSearchAttributes(theAttr);
				}
			}
		}

		return (theCond);
	}

	/**
	 * Return the list of known search engines.
	 * 
	 * This method will return the original list of engines, so be shure to handle this right.
	 * 
	 * @return The list of known search engines.
	 * @see #getSearchEngines()
	 */
	protected List<SearchEngine> internalGetSearchEngines() {
		return _searchEngines;
	}

	/**
	 * Create a new search result set.
	 * 
	 * @return The new created search result set.
	 */
	protected SearchResultSetSPI createEmptyResultSet() {
		return (this.createResultSet(this.getSearchEngines()));
	}

	/**
	 * Create a new search result set.
	 * 
	 * @param someEngines
	 *        The engines to be used for the search.
	 * @return The new created search result set.
	 */
	protected SearchResultSetSPI createResultSet(List<SearchEngine> someEngines) {
		return (new SearchResultSetImpl(someEngines));
	}

	public static final class Module extends TypedRuntimeModule<SearchFactory> {

		public static final Module INSTANCE = new Module();

		@Override
		public Class<SearchFactory> getImplementation() {
			return SearchFactory.class;
		}

	}

}
