/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.compile;

import com.top_logic.basic.treexf.DefaultMatch;
import com.top_logic.basic.treexf.Node;
import com.top_logic.basic.treexf.Transformation;
import com.top_logic.basic.treexf.TreeMaterializer;
import com.top_logic.basic.treexf.TreeTransformer;
import com.top_logic.dob.meta.TypeContext;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.TLModel;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.compile.transform.CreateTableAccess;
import com.top_logic.model.search.expr.compile.transform.FilterCompiler;
import com.top_logic.model.search.expr.compile.transform.HasSideEffects;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.interpreter.ConstantFolding;
import com.top_logic.model.search.expr.interpreter.DefResolver;
import com.top_logic.model.search.expr.interpreter.transform.Transformations;
import com.top_logic.model.search.expr.interpreter.transform.TreeBuilder;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Strategy for transforming {@link SearchExpression}s for optimized execution.
 * 
 * @see #compile(SearchExpression)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SearchExpressionCompiler {

	private static final Transformation[] TRANSFORMS = {
		Transformations.emptyUnion1(),
		Transformations.emptyUnion2(),
		Transformations.emptyIntersection1(),
		Transformations.emptyIntersection2(),

		Transformations.trueFilter(),
		Transformations.falseFilter(),
		Transformations.emptyFilter(),

		Transformations.trueIfElse(),
		Transformations.falseIfElse(),

		Transformations.booleanIfElse(),

		Transformations.andFalse1(),
		Transformations.andFalse2(),
		Transformations.andTrue1(),
		Transformations.andTrue2(),

		Transformations.orFalse1(),
		Transformations.orFalse2(),
		Transformations.orTrue1(),
		Transformations.orTrue2(),

		Transformations.literalNot(),
		Transformations.duplicateNot(),

		Transformations.distributeNotAnd(),
		Transformations.distributeNotOr(),
		Transformations.distributeOrAnd1(),
		Transformations.distributeOrAnd2(),

		Transformations.pulloutUnionFromFilter()
	};

	private final CreateTableAccess _createSource;

	private final FilterCompiler _filterCompiler;

	/**
	 * Creates a {@link SearchExpressionCompiler}.
	 *
	 * @param typeContext
	 *        The table types in use.
	 */
	public SearchExpressionCompiler(TypeContext typeContext) {
		_createSource = new CreateTableAccess(typeContext);
		_filterCompiler = new FilterCompiler(typeContext);
	}

	/**
	 * Optimizes the given {@link SearchExpression}.
	 * 
	 * @param expr
	 *        The {@link SearchExpression} to compile.
	 * @return An optimized version for more efficient interpretation. See
	 *         {@link QueryExecutor#interpret(KnowledgeBase, TLModel, SearchExpression)}.
	 */
	public SearchExpression compile(SearchExpression expr) {
		expr.visit(new DefResolver(), null);

		// Generate queries for type instance access.
		expr = expr.visit(_createSource, null);

		boolean sideEffectFree = isSideEffectFree(expr);
		if (sideEffectFree) {
			// Remove redundant variable bindings.
			// TODO #22261: Optimization is broken - it removes top-level functions whose arguments
			// are unused. This changes the semantics of the expression.
			// expr = InlineLocals.transform(expr);

			// Execute operations on literals.
			expr = ConstantFolding.transform(expr);

			// Structurally simplify the expression (union pull-out, and pull-out)
			expr = SearchExpressionCompiler.applyTransform(expr, TRANSFORMS);
		} else {
			// Execute operations on literals.
			expr = ConstantFolding.transform(expr);
		}

		// (Partially) compile filter functions to DB queries.
		expr = expr.visit(_filterCompiler, null);

		if (sideEffectFree) {
			// Extract independent parts of e.g. filter functions that were not compiled into the DB
			// query to speed up interpretation of the resulting expression.

			// TODO #22261: Optimization is broken - it pulls out subexpressions to far causing
			// undefined variable accesses.
			// expr = SubExpressionPullOut.transform(expr);
		}

		return expr;
	}

	private boolean isSideEffectFree(SearchExpression expr) {
		return !expr.visit(HasSideEffects.INSTANCE, null);
	}

	/**
	 * Utility for applying syntactic transformations on a {@link SearchExpression} tree.
	 * 
	 * @param expr
	 *        The original expression.
	 * @param transforms
	 *        List of transformations to apply.
	 * @return The resulting expression.
	 */
	public static SearchExpression applyTransform(SearchExpression expr, Transformation... transforms) {
		Node transformExpr = expr.visit(TreeBuilder.INSTANCE, null);
		TreeTransformer transformer = new TreeTransformer(transforms);
		transformExpr = transformer.transform(new DefaultMatch(), transformExpr);
		TreeMaterializer treeMaterializer = SearchBuilder.getInstance().getTreeMaterializer();
		SearchExpression result = (SearchExpression) treeMaterializer.materialize(transformExpr);
		result.visit(new DefResolver(), null);
		return result;
	}

}
