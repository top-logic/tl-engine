/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search.quick;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.top_logic.base.search.Query;
import com.top_logic.base.services.SearchFactory;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.factory.CollectionFactory;
import com.top_logic.element.layout.meta.search.quick.QuickSearchCommand.SearchTypeConfig;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.analyze.SearchResult;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.searching.QueryConfig;
import com.top_logic.knowledge.searching.SearchResultSet;
import com.top_logic.knowledge.searching.SearchService;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;
import com.top_logic.util.model.ModelService;

/**
 * {@link Command} that searches objects using the {@link SearchService}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractSearchCommand implements Command {

	/**
	 * Search result entry constant to indicate that there are more results.
	 */
	public static final Object MORE_RESULTS_REFINE_SEARCH = new NamedConstant("moreResultsRefineSearch");

	/**
	 * Static configuration of some {@link AbstractSearchCommand}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class SearchConfig {

		private int _maxResults = 20;

		private int _minSearchStringLength = 3;

		private final Set<TLType> _includeTypes = new HashSet<>();

		private final Set<TLType> _excludeTypes = new HashSet<>();

		private final Map<TLType, LayoutComponent> _gotoTargets = new HashMap<>();

		private final Map<TLClass, List<TLClassPart>> _searchAttributes = new HashMap<>();

		/**
		 * Set of {@link TLType}s which an object must have to be added into the result list. If the
		 * set is empty, all results are added.
		 */
		public Set<TLType> getIncludeTypes() {
			return _includeTypes;
		}

		/**
		 * Set of {@link TLType}s which an object must have to be <b>not</b> added into the result
		 * list. If the set is empty, no search result is rejected explicitly.
		 * 
		 * <p>
		 * Entries in this set override entries in {@link #getIncludeTypes()}.
		 * </p>
		 */
		public Set<TLType> getExcludeTypes() {
			return _excludeTypes;
		}

		/**
		 * Configuration of the targets to go to when some search result is navigated.
		 */
		public Map<TLType, LayoutComponent> getGotoTargets() {
			return _gotoTargets;
		}

		/**
		 * Map of {@link TLClass}es with their configured attributes that should be used for the
		 * search exclusively.
		 */
		public Map<TLClass, List<TLClassPart>> getSearchAttributes() {
			return (_searchAttributes);
		}

		/**
		 * Setter for {@link #getMaxResults()}.
		 */
		public void setMaxResults(int maxResults) {
			_maxResults = maxResults;
		}

		/**
		 * The maximum results shown in the drop-down dialog box.
		 */
		public int getMaxResults() {
			return _maxResults;
		}

		/**
		 * Setter for {@link #getMinSearchStringLength()}.
		 */
		public void setMinSearchStringLength(int minLength) {
			_minSearchStringLength = minLength;
		}

		/**
		 * The minimum length that the search string must have to execute the search.
		 */
		public int getMinSearchStringLength() {
			return _minSearchStringLength;
		}

		/**
		 * Service method that resolves a sequence of types.
		 */
		public static Set<TLType> resolveTypes(List<String> typeNames) {
			if (typeNames.isEmpty()) {
				return Collections.emptySet();
			}
			TLModel applicationModel = ModelService.getApplicationModel();
			Set<TLType> resolvedTypes = CollectionFactory.set();
			for (String typeName : typeNames) {
				TLType resolvedType = TLModelUtil.findType(applicationModel, typeName);
				if (resolvedType instanceof TLClass) {
					resolvedTypes.addAll(TLModelUtil.getConcreteSpecializations((TLClass) resolvedType));
				} else {
					resolvedTypes.add(resolvedType);
				}
			}
			return resolvedTypes;
		}

		/**
		 * Resolves the attributes of a {@link TLClass} that shall be used for the search.
		 * 
		 * @param searchAttributesConfig
		 *        {@link Map} of search attributes with the qualified name of the {@link TLClass} as
		 *        the key and a {@link SearchConfig} as the value.
		 * @return {@link Map} of search attributes with the {@link TLClass} as the key and a
		 *         {@link List} of attributes as the value.
		 */
		public static Map<TLClass, List<TLClassPart>> resolveAttributes(
				Map<String, SearchTypeConfig> searchAttributesConfig) {
			Map<TLClass, List<TLClassPart>> attributes = new HashMap<>();
			Collection<SearchTypeConfig> typeConfigs = searchAttributesConfig.values();
			for (SearchTypeConfig typeConfig : typeConfigs) {
				{
					TLClass type = (TLClass) TLModelUtil.resolveQualifiedName(typeConfig.getType());
					List<TLClassPart> searchAttributes = new ArrayList<>();
					for (String attribute : typeConfig.getAttributes()) {
						searchAttributes.add((TLClassPart) type.getPartOrFail(attribute));
					}
					attributes.put(type, searchAttributes);
				}
			}
			return attributes;
		}
	}

	/** Supplier delivering the string to search for. */
	private Supplier<String> _searchString;

	private SearchConfig _searchConfig;

	/**
	 * Creates a new {@link AbstractSearchCommand}.
	 */
	public AbstractSearchCommand(Supplier<String> searchString, SearchConfig searchConfig) {
		_searchString = searchString;
		_searchConfig = searchConfig;
	}

	/**
	 * This method returns the static {@link SearchConfig} of this command.
	 */
	public SearchConfig getSearchConfig() {
		return _searchConfig;
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		String searchText = getSearch();

		if (!StringServices.isEmpty(searchText)) {
			int minLength = getSearchConfig().getMinSearchStringLength();
			if (searchText.length() >= minLength) {
				List<Object> searchResult = search(searchText);

				return displayResult(context, searchResult);
			} else {
				InfoService.showInfo(I18NConstants.SEARCH_STRING_TOO_SHORT__MIN.fill(minLength));
				return HandlerResult.DEFAULT_RESULT;
			}
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Displays the given search results.
	 * 
	 * @param context
	 *        {@link DisplayContext} used to execute this command.
	 * @param searchResult
	 *        The search result.
	 * @return Return value of {@link #executeCommand(DisplayContext)}.
	 */
	protected abstract HandlerResult displayResult(DisplayContext context, List<Object> searchResult);

	/**
	 * Delivers the string to search for.
	 */
	protected String getSearch() {
		return _searchString.get();
	}

	/**
	 * Search via the {@link SearchFactory#getSearchService()}s for the given string.
	 * 
	 * @param searchText
	 *        The quick search string.
	 * @return The objects containing the requested string. The last item can be
	 *         {@link #MORE_RESULTS_REFINE_SEARCH} and is used as a hint that there are more
	 *         results.
	 */
	public List<Object> search(String searchText) {
		QueryConfig query = getQueryConfiguration(searchText);
		SearchResultSet searchResult = SearchFactory.getSearchService().search(query);

		searchResult.waitForClosed(50000);

		return convertSearchResultSet(searchResult);
	}

	/**
	 * Return the query configuration to be send to the search engines.
	 * 
	 * @param searchText
	 *        The requested full text string.
	 * @return The requested query configuration.
	 */
	protected QueryConfig getQueryConfiguration(String searchText) {
		QueryConfig query = new QueryConfig();

		query.setQuery(Query.getFullTextQuery(searchText, true, false));

		return query;
	}

	/**
	 * Convert the given result set into a valid collection of searched objects.
	 * 
	 * @param resultSet
	 *        The result to be converted.
	 * @return The requested list of objects. The last item can be
	 *         {@link #MORE_RESULTS_REFINE_SEARCH} and is used as a hint that there are more
	 *         results.
	 */
	protected List<Object> convertSearchResultSet(SearchResultSet resultSet) {
		List<Object> rawResult = new ArrayList<>();

		List<? extends SearchResult> searchResults = resultSet.getSearchResults();

		for (SearchResult result : searchResults) {
			Wrapper wrapper = findWrapper((KnowledgeItem) result.getResult());
			if (wrapper != null && hasSearchType(wrapper)) {
				rawResult.add(wrapper);
			}
		}

		sortResult(rawResult);

		return applySecurityWithLimit(rawResult);
	}

	/**
	 * Filters the given search result such that only those results are displayed which the user is
	 * allowed to see.
	 * 
	 * <p>
	 * Adds {@link #MORE_RESULTS_REFINE_SEARCH} as last element as a hint that there are more
	 * results.
	 * </p>
	 * 
	 * @param rawResult
	 *        Raw search result. May contain {@link #MORE_RESULTS_REFINE_SEARCH} as a hint that
	 *        there are more results.
	 * 
	 * @See QuickSearchCommand#getTableModel(List).
	 */
	protected List<Object> applySecurityWithLimit(List<Object> rawResult) {
		List<Object> result = new ArrayList<>();
		int count = 0;
		for (Object tmp : rawResult) {
			LayoutComponent target = gotoTargetForObject(tmp);
			if (GotoHandler.canShow(tmp, target)) {
				if (count++ <= getSearchConfig().getMaxResults()) {
					result.add(tmp);
				} else {
					result.add(MORE_RESULTS_REFINE_SEARCH);
					break;
				}
			}
		}
		return result;

	}

	/**
	 * Fetches the Goto target for the given search result.
	 * 
	 * @param item
	 *        Some search result.
	 * 
	 * @return May be <code>null</code>.
	 */
	protected LayoutComponent gotoTargetForObject(Object item) {
		return TLModelUtil.getBestMatch(item, getSearchConfig().getGotoTargets());
	}

	/**
	 * Sorts the given search result to be displayed on the client.
	 * 
	 * @param rawResult
	 *        All objects matching the {@link #getSearch() search string}.
	 */
	protected abstract void sortResult(List<Object> rawResult);

	/**
	 * Whether the given search result has the correct {@link TLType} to be added to final search
	 * result.
	 */
	protected boolean hasSearchType(Wrapper wrapper) {
		Set<TLType> excludeTypes = getSearchConfig().getExcludeTypes();
		if (!excludeTypes.isEmpty() && excludeTypes.contains(wrapper.tType())) {
			return false;
		}
		Set<TLType> includeTypes = getSearchConfig().getIncludeTypes();
		return includeTypes.isEmpty() || includeTypes.contains(wrapper.tType());
	}

	/**
	 * {@link Wrapper} for the given KB result.
	 */
	protected Wrapper findWrapper(KnowledgeItem result) {
		return WrapperFactory.getWrapper(result);
	}

}

