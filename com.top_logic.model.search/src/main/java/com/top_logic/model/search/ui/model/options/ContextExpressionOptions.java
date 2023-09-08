/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.options;

import static com.top_logic.basic.col.FilterUtil.*;
import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.list;
import static com.top_logic.model.search.ui.model.SearchPartFactory.*;
import static com.top_logic.model.util.TLModelUtil.*;
import static java.util.Collections.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.func.Function2;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.model.search.ui.model.ContextFilter;
import com.top_logic.model.search.ui.model.NamedDefinition;
import com.top_logic.model.search.ui.model.ReferenceValue;
import com.top_logic.model.search.ui.model.Search;
import com.top_logic.model.search.ui.model.ValueContext;
import com.top_logic.model.search.ui.model.parameters.PredefinedSearchParameter;
import com.top_logic.model.search.ui.model.structure.SearchPart;

/**
 * Option provider for {@link ContextFilter#getContextExpression()}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ContextExpressionOptions extends Function2<List<ReferenceValue>, SearchPart, TLType> {

	/** The {@link ContextExpressionOptions} instance. */
	public static final ContextExpressionOptions INSTANCE = new ContextExpressionOptions();

	/**
	 * Find compatible definition expressions in the context of the given expression.
	 * <p>
	 * A definition is compatible with given {@link ValueContext}, if their types have
	 * potentially common instances.
	 * </p>
	 * 
	 * @param expectedType
	 *        The expected definition type.
	 * @param context
	 *        The context to start the search in.
	 * @return Compatible definition in the current scope.
	 */
	@Override
	public List<ReferenceValue> apply(SearchPart context, TLType expectedType) {
		if (containsNull(context, expectedType)) {
			return emptyList();
		}
		return collectEnclosingNamedDefinitions(context, expectedType);
	}

	private List<ReferenceValue> collectEnclosingNamedDefinitions(SearchPart context, TLType expectedType) {
		List<ReferenceValue> result = list();
		SearchPart ancestor = context;
		while (ancestor != null) {
			if (ancestor instanceof NamedDefinition) {
				NamedDefinition definition = (NamedDefinition) ancestor;
				TLType definitionType = getType(definition);
				if (haveCommonInstances(expectedType, definitionType)) {
					result.add(referenceValue(definition));
				}
			} else if (ancestor instanceof Search) {
				result.addAll(predefinedReferenceValues(getMatchingParameters(expectedType, (Search) ancestor)));
			}
			ancestor = ancestor.getContext();
		}
		return result;
	}

	private List<PredefinedSearchParameter> getMatchingParameters(TLType expectedType, Search search) {
		return filterByType(expectedType, search.getPredefinedParameters());
	}

	private List<PredefinedSearchParameter> filterByType(TLType type, List<PredefinedSearchParameter> parameters) {
		Filter<NamedDefinition> filter = parameter -> isCompatibleType(type, parameter.getValueType());
		return filterList(filter, parameters);
	}

	private TLType getType(NamedDefinition definition) {
		return definition.getValueType();
	}

	private static boolean haveCommonInstances(TLType t1, TLType t2) {
		if (Objects.equals(t1, t2)) {
			return true;
		}
		if (!(t1 instanceof TLClass && t2 instanceof TLClass)) {
			return false;
		}
		TLClass c1 = (TLClass) t1;
		TLClass c2 = (TLClass) t2;
		if (c1.isFinal() || c2.isFinal()) {
			/* Performance optimization */
			return isGeneralization(c1, c2) || isGeneralization(c2, c1);
		}
		Set<TLClass> subtypes1 = getConcreteSpecializations(c1);
		Set<TLClass> subtypes2 = getConcreteSpecializations(c2);
		return !CollectionUtil.intersection(subtypes1, subtypes2).isEmpty();
	}

}
