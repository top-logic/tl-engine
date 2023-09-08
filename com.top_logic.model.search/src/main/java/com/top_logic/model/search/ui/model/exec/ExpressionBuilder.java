/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.exec;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;
import static com.top_logic.model.search.expr.SearchExpressions.*;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.model.search.expr.And;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.TupleExpression.Coord;
import com.top_logic.model.search.ui.GUISearchExpressionEditor;
import com.top_logic.model.search.ui.model.AbstractTypeSearch;
import com.top_logic.model.search.ui.model.FilterContainer;
import com.top_logic.model.search.ui.model.Search;
import com.top_logic.model.search.ui.model.SubSearch;
import com.top_logic.model.search.ui.model.TupleSearch;
import com.top_logic.model.search.ui.model.TupleSearch.CoordDef;
import com.top_logic.model.search.ui.model.TupleSearch.NullableSpec;
import com.top_logic.model.search.ui.model.UnionSearch;
import com.top_logic.model.search.ui.model.UnionSearchPart;
import com.top_logic.model.search.ui.model.combinator.AllSearchExpressionCombinator;
import com.top_logic.model.search.ui.model.combinator.SearchExpressionCombinator;
import com.top_logic.model.search.ui.model.misc.NameGenerator;
import com.top_logic.model.search.ui.model.operator.TypeCheck;
import com.top_logic.model.search.ui.model.parameters.PredefinedSearchParameter;
import com.top_logic.model.search.ui.model.structure.RightHandSideVisitor;
import com.top_logic.model.search.ui.model.structure.SearchFilter;
import com.top_logic.model.search.ui.model.structure.SearchFilterVisitor;

/**
 * Creates {@link SearchExpression} for {@link GUISearchExpressionEditor}s.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ExpressionBuilder implements SubSearch.SubSearchVisitor<SearchExpression, Void> {

	private static final String VARIABLE_NAME_PREFIX = "$";

	private final NameGenerator _nameGenerator = new NameGenerator();

	private final SearchFilterVisitor<SearchExpression, SearchExpression> _transformFilter;

	private final RightHandSideVisitor<SearchExpression, Void> _transformValue;

	/**
	 * Creates an {@link ExpressionBuilder}.
	 */
	public ExpressionBuilder() {
		_transformFilter = new FilterBuilder(this);
		_transformValue = new RightHandSideBuilder(this);
	}

	/**
	 * The {@link RightHandSideBuilder}.
	 */
	public RightHandSideVisitor<SearchExpression, Void> getRightHandSideBuilder() {
		return _transformValue;
	}

	/**
	 * Translates the given {@link TypeCheck} GUI fragment.
	 */
	public And toTypeCheckExpression(TypeCheck typeCheck, SearchExpression self) {
		return and(instanceOf(self, typeCheck.getTypeCast()), call(filterFunction(typeCheck), self));
	}

	/**
	 * null, if nothing is searched.
	 */
	public SearchExpression toExpression(Search context) {
		return context.visitSubSearch(this, null);
	}

	@Override
	public SearchExpression visit(Search search, Void args) {
		SearchExpression inner = createUnions(search, args);
		if (inner == null) {
			return null;
		}
		for (PredefinedSearchParameter parameter : search.getPredefinedParameters()) {
			inner = let(parameter.getName(), literal(parameter.getValue()), inner);
		}
		return inner;
	}

	@Override
	public SearchExpression visit(UnionSearch search, Void args) {
		return createUnions(search, args);
	}

	private SearchExpression createUnions(UnionSearch search, Void args) {
		SearchExpression result = null;
		for (UnionSearchPart part : search.getUnions()) {
			SearchExpression inner = part.visitSubSearch(this, args);
			if (result == null) {
				result = inner;
			} else {
				result = union(result, inner);
			}
		}
		return result;
	}

	@Override
	public SearchExpression visit(AbstractTypeSearch search, Void args) {
		return filter(all(search.getType()), filterFunction(search));
	}

	@Override
	public SearchExpression visit(TupleSearch search, Void args) {
		List<CoordDef> coordDefs = search.getCoords();
		Coord[] coords = new Coord[coordDefs.size()];
		for (int cnt = coords.length, n = 0; n < cnt; n++) {
			CoordDef coordDef = coordDefs.get(n);
			SearchExpression expr = coordDef.getExpr().visitSubSearch(this, args);
			coords[n] = coord(coordDef.getNullable() == NullableSpec.NULLABLE, coordDef.getName(), expr);
		}
		return tuple(coords);
	}

	/**
	 * Translates the given filter expression to a {@link SearchExpression} function.
	 */
	public SearchExpression filterFunction(FilterContainer filterContainer) {
		String name = filterContainer.getName();

		List<SearchExpression> expressions = new ArrayList<>();
		for (SearchFilter filter : filterContainer.getFilters()) {
			expressions.add(filter.visitSearchFilter(_transformFilter, var(name)));
		}
		SearchExpression combinedExpression = combine(AllSearchExpressionCombinator.INSTANCE, expressions);
		
		return lambda(name, combinedExpression);
	}

	SearchExpression combine(SearchExpressionCombinator combinator, List<SearchExpression> expressions) {
		return combinator.combine(expressions);
	}

	String newName() {
		return VARIABLE_NAME_PREFIX + _nameGenerator.newName();
	}

}
