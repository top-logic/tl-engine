/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.options.structure;

import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.func.Function2;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.search.ModelBasedSearch;
import com.top_logic.model.search.ModelBasedSearch.SearchConfig;
import com.top_logic.model.search.ui.model.AssociationFilter;
import com.top_logic.model.search.ui.model.AttributeFilter;
import com.top_logic.model.search.ui.model.CombinedFilter;
import com.top_logic.model.search.ui.model.ContextFilter;
import com.top_logic.model.search.ui.model.FilterContainer;
import com.top_logic.model.search.ui.model.IncomingReferenceFilter;
import com.top_logic.model.search.ui.model.ReferenceValue;
import com.top_logic.model.search.ui.model.operator.TypeCheck;
import com.top_logic.model.search.ui.model.options.ContextExpressionOptions;
import com.top_logic.model.search.ui.model.options.SearchTypeUtil;
import com.top_logic.model.search.ui.model.structure.SearchFilter;
import com.top_logic.model.search.ui.model.structure.SearchPart;

/**
 * Option provider for {@link FilterContainer#getFilters()}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class FilterOptions extends Function2<List<SearchFilter>, SearchPart, TLType> {

	/** The {@link FilterOptions} instance. */
	public static final FilterOptions INSTANCE = new FilterOptions();

	private FilterOptions() {
		// Singleton constructor.
	}

	@Override
	public List<SearchFilter> apply(SearchPart context, TLType type) {
		ArrayList<SearchFilter> result = new ArrayList<>();
		if (!containsNull(context, type)) {
			addFiltersForType(result, type, context.getConfigName());
			result.add(TypedConfiguration.newConfigItem(CombinedFilter.class));
			addContextFilters(context, type, result);
		}
		return result;
	}

	private void addContextFilters(SearchPart context, TLType type, ArrayList<SearchFilter> result) {
		List<ReferenceValue> definitions = ContextExpressionOptions.INSTANCE.apply(context, type);
		for (ReferenceValue definition : definitions) {
			result.add(contextFilter(definition));
		}
	}

	private static void addFiltersForType(List<SearchFilter> result, TLType type, String searchName) {
		if (!(type instanceof TLClass)) {
			return;
		}

		TLClass classType = (TLClass) type;
		Collection<? extends TLStructuredTypePart> mainProperties = getMainProperties(classType, searchName);
		for (TLStructuredTypePart mainProperty : mainProperties) {
			result.add(attributeFilter(mainProperty));
		}
		if (mainProperties.size() < getAllParts(classType, searchName).size()) {
			result.add(attributeFilter(null));
		}
		result.add(referenceFilter());
		result.add(associationFilter(null));
		addTypeCheck(result, classType, searchName);
	}

	static void addTypeCheck(List<SearchFilter> result, TLClass type, String searchName) {
		if (SearchTypeUtil.hasSubtypes(type, searchName)) {
			result.add(typeCheck());
		}
	}

	private static List<? extends TLStructuredTypePart> getMainProperties(TLClass type, String searchName) {
		List<String> mainPropertyNames = DisplayAnnotations.getMainProperties(type);
		if (mainPropertyNames.isEmpty()) {
			return getFirstParts(type, searchName);
		}
		return refineMainProperties(type, mainPropertyNames, searchName);
	}

	private static List<? extends TLStructuredTypePart> getFirstParts(TLClass type, String searchName) {
		int maxAttributesShown = getSearchConfig(searchName).getMaxAttributesShown();
		List<TLClassPart> allParts = getAllParts(type, searchName);
		if (allParts.size() > maxAttributesShown) {
			return allParts.subList(0, maxAttributesShown);
		}
		return allParts;
	}

	private static List<TLClassPart> getAllParts(TLClass type, String searchName) {
		List<? extends TLClassPart> allParts = type.getAllClassParts();
		return ModelBasedSearch.getInstance().filterModel(allParts, searchName);
	}

	private static SearchConfig getSearchConfig(String searchName) {
		return ModelBasedSearch.getInstance().getSearchConfig(searchName);
	}

	private static List<TLStructuredTypePart> refineMainProperties(TLClass type, List<String> mainPropertyNames,
			String searchName) {
		List<TLStructuredTypePart> mainProperties = new ArrayList<>();
		for (String mainPropertyName : mainPropertyNames) {
			TLStructuredTypePart part = type.getPart(mainPropertyName);
			// There is for example no part for "_self".
			if (part != null) {
				mainProperties.add(part);
			}
		}
		TLStructuredTypePart namePart = type.getPart(AbstractWrapper.NAME_ATTRIBUTE);
		if ((namePart != null) && !mainProperties.contains(namePart)) {
			mainProperties.add(0, namePart);
		}
		return ModelBasedSearch.getInstance().filterModel(mainProperties, searchName);
	}

	private static ContextFilter contextFilter(ReferenceValue definition) {
		ContextFilter contextFilter = TypedConfiguration.newConfigItem(ContextFilter.class);
		contextFilter.setContextExpression(definition);
		return contextFilter;
	}

	private static TypeCheck typeCheck() {
		return TypedConfiguration.newConfigItem(TypeCheck.class);
	}

	private static SearchFilter attributeFilter(TLStructuredTypePart part) {
		AttributeFilter result = TypedConfiguration.newConfigItem(AttributeFilter.class);
		result.update(result.descriptor().getProperty(AttributeFilter.ATTRIBUTE), part);
		return result;
	}

	private static SearchFilter associationFilter(TLStructuredTypePart part) {
		AssociationFilter result = TypedConfiguration.newConfigItem(AssociationFilter.class);
		result.update(result.descriptor().getProperty(AssociationFilter.OUTGOING_PART), part);
		return result;
	}

	private static SearchFilter referenceFilter() {
		return TypedConfiguration.newConfigItem(IncomingReferenceFilter.class);
	}

}
