/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.exec;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.util.Utils;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.Filter;
import com.top_logic.model.search.expr.Foreach;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.ui.model.AssociationStep;
import com.top_logic.model.search.ui.model.AttributeStep;
import com.top_logic.model.search.ui.model.FilterContainer;
import com.top_logic.model.search.ui.model.IncomingReferenceStep;
import com.top_logic.model.search.ui.model.NavigationStep;
import com.top_logic.model.search.ui.model.NavigationValue;
import com.top_logic.model.search.ui.model.QueryValue;
import com.top_logic.model.search.ui.model.SubQuery;
import com.top_logic.model.search.ui.model.TypeCheckStep;
import com.top_logic.model.search.ui.model.literal.LiteralObjectSet;
import com.top_logic.model.search.ui.model.literal.LiteralObjectValue;
import com.top_logic.model.search.ui.model.literal.LiteralPrimitiveValue;
import com.top_logic.model.search.ui.model.structure.RightHandSide;
import com.top_logic.model.search.ui.model.structure.RightHandSideVisitor;

/**
 * Builds {@link SearchExpression}s from {@link RightHandSide}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
final class RightHandSideBuilder implements RightHandSideVisitor<SearchExpression, Void> {

	private final ExpressionBuilder _expressionBuilder;

	RightHandSideBuilder(ExpressionBuilder expressionBuilder) {
		_expressionBuilder = expressionBuilder;
	}

	@Override
	public SearchExpression visitLiteralObjectValue(LiteralObjectValue value, Void arg) {
		return literal(value.getObjects());
	}

	@Override
	public SearchExpression visitLiteralObjectSet(LiteralObjectSet value, Void arg) {
		return literal(value.getObjects());
	}

	@Override
	public SearchExpression visitLiteralPrimitiveValue(LiteralPrimitiveValue value, Void arg) {
		return literal(value.getCompareValue());
	}

	@Override
	public SearchExpression visitNavigationValue(NavigationValue value, Void arg) {
		SearchExpression base = var(value.getBase().getName());

		return navigate(base, value.getNext());
	}

	private SearchExpression navigate(SearchExpression base, NavigationStep currentStep) {
		if (currentStep == null) {
			return base;
		}
		SearchExpression navigation = createNavigation(base, currentStep);
		NavigationStep nextStep = currentStep.getNext();
		return navigate(navigation, nextStep);
	}

	private SearchExpression createNavigation(SearchExpression base, NavigationStep currentStep) {
		if (currentStep instanceof AttributeStep) {
			return createNavigationInternal(base, (AttributeStep) currentStep);
		}
		if (currentStep instanceof IncomingReferenceStep) {
			return createNavigationInternal(base, (IncomingReferenceStep) currentStep);
		}
		if (currentStep instanceof AssociationStep) {
			return createNavigationInternal(base, (AssociationStep) currentStep);
		}
		if (currentStep instanceof TypeCheckStep) {
			return createNavigationInternal(base, (TypeCheckStep) currentStep);
		}
		throw new UnreachableAssertion(
			"Unexpected " + NavigationStep.class.getSimpleName() + ": " + Utils.debug(currentStep));
	}

	private SearchExpression createNavigationInternal(SearchExpression base, AttributeStep step) {
		TLStructuredTypePart property = step.getAttribute();
		AttributeAccessFunction navigationFunction = new AttributeAccessFunction(property);
		boolean baseMultiplicity = step.getContextMultiplicity();
		boolean stepMultiplicity = property.isMultiple();
		return createNavigationInternal(base, navigationFunction, baseMultiplicity, stepMultiplicity);
	}

	private SearchExpression createNavigationInternal(SearchExpression base, AssociationStep step) {
		TLAssociationEnd incomingPart = (TLAssociationEnd) step.getIncomingPart();
		TLAssociationEnd outgoingPart = (TLAssociationEnd) step.getOutgoingPart();
		AssociationNavigationFunction navigationFunction =
			new AssociationNavigationFunction(incomingPart, outgoingPart);
		boolean baseMultiplicity = step.getContextMultiplicity();
		return createNavigationInternal(base, navigationFunction, baseMultiplicity, true);
	}

	private SearchExpression createNavigationInternal(SearchExpression base, IncomingReferenceStep step) {
		TLReference incomingReference = step.getReference();
		IncomingReferenceNavigationFunction navigationFunction =
			new IncomingReferenceNavigationFunction(incomingReference);
		boolean baseMultiplicity = step.getContextMultiplicity();
		return createNavigationInternal(base, navigationFunction, baseMultiplicity, true);
	}

	private SearchExpression createNavigationInternal(SearchExpression base,
			Function1<SearchExpression, SearchExpression> stepFunction,
			boolean baseMultiplicity, boolean stepMultiplicity) {
		if (baseMultiplicity) {
			if (stepMultiplicity) {
				return flatten(forEach(base, stepFunction));
			} else {
				return forEach(base, stepFunction);
			}
		} else {
			return stepFunction.apply(base);
		}
	}

	private SearchExpression createNavigationInternal(SearchExpression base, TypeCheckStep step) {
		TLClass newType = step.getTypeCast();
		boolean baseMultiplicity = step.getContextMultiplicity();
		return createTypeCastNavigation(base, newType, baseMultiplicity);
	}

	private SearchExpression createTypeCastNavigation(SearchExpression base, TLClass newType,
			boolean baseMultiplicity) {
		if (baseMultiplicity) {
			String varName = newName();
			return filter(base, lambda(varName, instanceOf(var(varName), newType)));
		} else {
			return ifElse(instanceOf(base, newType), base, literal(null));
		}
	}

	private Foreach forEach(SearchExpression base, Function1<SearchExpression, SearchExpression> expression) {
		String varName = newName();
		return foreach(base, lambda(varName, expression.apply(var(varName))));
	}

	@Override
	public SearchExpression visitQueryValue(QueryValue value, Void arg) {
		TLStructuredTypePart part = value.getPart();
		TLClass baseType = (TLClass) part.getOwner();
		String x = newName();
		return foreach(
			filterAll(value, baseType),
			lambda(x, access(var(x), part)));
	}

	@Override
	public SearchExpression visitSubQuery(SubQuery value, Void arg) {
		TLClass baseType = (TLClass) value.getContextType();
		return filterAll(value, baseType);
	}

	private Filter filterAll(FilterContainer value, TLClass baseType) {
		return filter(all(baseType), _expressionBuilder.filterFunction(value));
	}

	private String newName() {
		return _expressionBuilder.newName();
	}

}
