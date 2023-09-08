/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.options;

import static com.top_logic.basic.col.FilterUtil.*;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.list;
import static com.top_logic.model.search.ui.model.SearchPartFactory.*;
import static com.top_logic.model.util.TLModelUtil.*;
import static java.util.Collections.*;

import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.func.Function2;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.search.ui.model.FilterContainer;
import com.top_logic.model.search.ui.model.NamedDefinition;
import com.top_logic.model.search.ui.model.NavigationValue;
import com.top_logic.model.search.ui.model.ReferenceValue;
import com.top_logic.model.search.ui.model.Search;
import com.top_logic.model.search.ui.model.TupleSearch;
import com.top_logic.model.search.ui.model.parameters.PredefinedSearchParameter;
import com.top_logic.model.search.ui.model.structure.SearchPart;

/**
 * All {@link FilterContainer} values from context wrapped into {@link ReferenceValue}s.
 * 
 * @see NavigationValue#getBase() Property option provider.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DefinitionsInScope extends Function2<List<ReferenceValue>, TLType, SearchPart> {

	/** The {@link DefinitionsInScope} instance. */
	public static final DefinitionsInScope INSTANCE = new DefinitionsInScope();

	@Override
	public List<ReferenceValue> apply(TLType expectedType, SearchPart parent) {
		if (parent == null) {
			return emptyList();
		}
		List<ReferenceValue> result = list();
		SearchPart ancestor = parent;
		SearchPart current = null;
		while (ancestor != null) {
			if (ancestor instanceof NamedDefinition) {
				result.add(referenceValue((NamedDefinition) ancestor));
			}
			else if (ancestor instanceof TupleSearch) {
				result.addAll(collectTupleDefinitions(current, (TupleSearch) ancestor));
			} else if (ancestor instanceof Search) {
				result.addAll(predefinedReferenceValues(getMatchingParameters(expectedType, (Search) ancestor)));
			}
			current = ancestor;
			ancestor = ancestor.getContext();
		}
		return result;
	}

	/** Collect all coordinate definitions before the current one. */
	private List<ReferenceValue> collectTupleDefinitions(SearchPart current, TupleSearch tupleSearch) {
		List<ReferenceValue> coordinateDefinitions = list();
		for (TupleSearch.CoordDef coord : tupleSearch.getCoords()) {
			if (coord == current) {
				break;
			}
			coordinateDefinitions.add(referenceValue(coord));
		}
		return coordinateDefinitions;
	}

	private List<PredefinedSearchParameter> getMatchingParameters(TLType expectedType, Search search) {
		return filterByType(expectedType, search.getPredefinedParameters());
	}

	private List<PredefinedSearchParameter> filterByType(TLType type, List<PredefinedSearchParameter> parameters) {
		Filter<NamedDefinition> filter = parameter -> hasUsableType(type, parameter);
		return filterList(filter, parameters);
	}

	private boolean hasUsableType(TLType expectedType, NamedDefinition parameter) {
		if (parameter.getValueType() instanceof TLStructuredType) {
			/* TLStructuredTypes can be used to navigate to a value with the correct type. They are
			 * therefore accepted, too. */
			return true;
		}
		return isCompatibleType(expectedType, parameter.getValueType());
	}

}
