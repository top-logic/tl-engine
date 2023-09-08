/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.exec;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;
import static com.top_logic.model.search.expr.SearchExpressions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.exception.NotYetImplementedException;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLAssociationPart;
import com.top_logic.model.search.expr.Access;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.ui.model.AssociationFilter;
import com.top_logic.model.search.ui.model.AttributeFilter;
import com.top_logic.model.search.ui.model.CombinedFilter;
import com.top_logic.model.search.ui.model.ContextFilter;
import com.top_logic.model.search.ui.model.IncomingReferenceFilter;
import com.top_logic.model.search.ui.model.NavigatingFilter;
import com.top_logic.model.search.ui.model.combinator.AllSearchExpressionCombinator;
import com.top_logic.model.search.ui.model.operator.Operator;
import com.top_logic.model.search.ui.model.operator.Operator.Impl;
import com.top_logic.model.search.ui.model.operator.TypeCheck;
import com.top_logic.model.search.ui.model.structure.SearchFilter;
import com.top_logic.model.search.ui.model.structure.SearchFilterVisitor;

/**
 * Builds {@link SearchExpression}s from {@link SearchFilter}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
final class FilterBuilder implements SearchFilterVisitor<SearchExpression, SearchExpression> {

	private final ExpressionBuilder _expressionBuilder;

	FilterBuilder(ExpressionBuilder expressionBuilder) {
		_expressionBuilder = expressionBuilder;
	}

	@Override
	public SearchExpression visitAttributeFilter(AttributeFilter filter, SearchExpression self) {
		Access navigation = access(self, filter.getAttribute());
		return applyFilter(filter, navigation);
	}

	@Override
	public SearchExpression visitAssociationFilter(AssociationFilter filter, SearchExpression self) {
		if ((!isAssociationEnd(filter.getIncomingPart())) || !isAssociationEnd(filter.getOutgoingPart())) {
			String message = "Only TLAssociationEnds are already supported.";
			throw new NotYetImplementedException(message);
		}
		TLAssociationEnd incomingPart = (TLAssociationEnd) filter.getIncomingPart();
		TLAssociationEnd outgoingPart = (TLAssociationEnd) filter.getOutgoingPart();
		SearchExpression navigation = associationNavigation(self, incomingPart, outgoingPart);
		return applyFilter(filter, navigation);
	}

	private boolean isAssociationEnd(TLAssociationPart associationPart) {
		return associationPart instanceof TLAssociationEnd;
	}

	private SearchExpression applyFilter(NavigatingFilter filter, SearchExpression navigation) {
		String name = newName();

		List<SearchExpression> expressions = new ArrayList<>();
		for (Operator<?> operator : filter.getComparisons()) {
			expressions.add(impl(operator).build(_expressionBuilder, var(name)));
		}
		SearchExpression combinedExpression =
			_expressionBuilder.combine(AllSearchExpressionCombinator.INSTANCE, expressions);

		return let(name, navigation, combinedExpression);
	}

	private Impl<?> impl(Operator<?> operator) {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(operator);
	}

	@Override
	public SearchExpression visitContextFilter(ContextFilter filter, SearchExpression self) {
		String name = filter.getContextExpression().getName();

		return isEqual(self, var(name));
	}

	@Override
	public SearchExpression visitIncomingReferenceFilter(IncomingReferenceFilter filter, SearchExpression self) {
		return applyFilter(filter, referers(self, filter.getReference()));
	}

	@Override
	public SearchExpression visitTypeCheck(TypeCheck filter, SearchExpression self) {
		return _expressionBuilder.toTypeCheckExpression(filter, self);
	}

	@Override
	public SearchExpression visitCombinedFilter(CombinedFilter filter, SearchExpression arg) {
		return filter.getCombinator().combine(descend(filter.getFilters(), arg));
	}

	private List<SearchExpression> descend(Collection<? extends SearchFilter> filters, SearchExpression arg) {
		ArrayList<SearchExpression> result = new ArrayList<>();
		for (SearchFilter filter : filters) {
			result.add(filter.visitSearchFilter(this, arg));
		}
		return result;
	}

	private String newName() {
		return _expressionBuilder.newName();
	}

}
