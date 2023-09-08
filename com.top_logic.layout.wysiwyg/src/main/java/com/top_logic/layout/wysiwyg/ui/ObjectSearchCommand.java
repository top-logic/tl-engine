/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.layout.meta.search.quick.AbstractSearchCommand;
import com.top_logic.knowledge.searching.DefaultFullTextBuffer;
import com.top_logic.knowledge.searching.FullTextSearchable;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.list.model.ListModelUtilities;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Command to perform a full-text object search.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ObjectSearchCommand extends AbstractSearchCommand {

	private final ListModel<Wrapper> _listModel;

	/**
	 * Creates a {@link ObjectSearchCommand} for the given {@link StringField} and options.
	 * 
	 * @param searchField
	 *        The search pattern field.
	 * @param listModel
	 *        Model of the options.
	 * @param searchConfig
	 *        Configuration of the search.
	 */
	public ObjectSearchCommand(StringField searchField, ListModel<Wrapper> listModel, SearchConfig searchConfig) {
		super(searchField::getAsString, searchConfig);
		_listModel = listModel;
	}

	@Override
	protected HandlerResult displayResult(DisplayContext context, List<Object> searchResult) {
		List<TLObject> newSearchResult = filterSearchResult(searchResult);
		ListModelUtilities.replaceAll((DefaultListModel<Wrapper>) _listModel, newSearchResult);
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Filters the given search result based on the configured search attributes of every
	 * {@link TLStructuredType} of the search result elements.
	 * 
	 * <p>
	 * If no search attributes are configured for the {@link TLStructuredType} of the element under
	 * consideration the element will be added automatically as a valid result to the filtered
	 * search result list.
	 * </p>
	 * 
	 * @see com.top_logic.element.layout.meta.search.quick.AbstractSearchCommand.SearchConfig#getSearchAttributes()
	 * 
	 * @param searchResult
	 *        The unfiltered {@link List} of search results. May contain
	 *        {@link AbstractSearchCommand#MORE_RESULTS_REFINE_SEARCH} as last element as a hint for
	 *        more results.
	 * @return A {@link List} of search results that only contains elements whose relevant
	 *         attributes match the search text.
	 */
	private List<TLObject> filterSearchResult(List<Object> searchResult) {
		Set<TLObject> filteredSearchResult = new LinkedHashSet<>();
		SearchConfig searchConfig = getSearchConfig();
		for (Object element : searchResult) {
			if (AbstractSearchCommand.MORE_RESULTS_REFINE_SEARCH.equals(element)) {
				continue;
			}

			if (element instanceof TLObject) {
				TLObject resultElement = (TLObject) element;
				TLStructuredType type = resultElement.tType();
				Map<TLClass, List<TLClassPart>> searchAttributes = searchConfig.getSearchAttributes();
				if (searchAttributes.containsKey(type)) {
					if (hasMatchingAttribute(searchAttributes.get(type), resultElement)) {
						filteredSearchResult.add(resultElement);
					}
				} else {
					filteredSearchResult.add(resultElement);
				}
			}
		}
		return new ArrayList<>(filteredSearchResult);
	}

	/**
	 * If at least one of the attributes of the object matches the search text.
	 * 
	 * @param searchAttributes
	 *        The configured attributes that shall be used for the search. Other attributes will be
	 *        ignored.
	 * @param object
	 *        The {@link TLObject} whose attributes will be checked if they match the search.
	 */
	private boolean hasMatchingAttribute(List<TLClassPart> searchAttributes, TLObject object) {
		for (TLClassPart searchAttribute : searchAttributes) {
			Object value = object.tValue(searchAttribute);
			if (value instanceof String) {
				String stringValue = (String) value;
				if (matchesSearchText(stringValue)) {
					return true;
				}
			} else if (value instanceof FullTextSearchable) {
				FullTextSearchable ftsValue = (FullTextSearchable) value;
				DefaultFullTextBuffer buffer = new DefaultFullTextBuffer();
				ftsValue.generateFullText(buffer);
				if (matchesSearchText(buffer.toString())) {
					return true;
				}
			} else if (value instanceof ResKey) {
				for (String language : ResourcesModule.getInstance().getSupportedLocaleNames()) {
					Resources resources = Resources.getInstance(language);
					String stringValue = resources.getString((ResKey) value);
					if (matchesSearchText(stringValue)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Checks case insensitive if the given string contains the search text.
	 * 
	 * @param value
	 *        The String to check.
	 * @return <code>true</code> if the value contains the search text.
	 */
	private boolean matchesSearchText(String value) {
		if (value.toLowerCase().contains(getSearch().toLowerCase())) {
			return true;
		}
		return false;
	}

	/**
	 * Sorts the list of search results by their label from a to z.
	 * 
	 * @param searchResult
	 *        List of found objects from the full text object search.
	 */
	@Override
	protected void sortResult(List<Object> searchResult) {
		Collections.sort(searchResult, Comparator.comparing(x -> MetaLabelProvider.INSTANCE.getLabel(x)));
	}

}
