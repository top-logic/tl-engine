/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.top_logic.dob.IdentifierTypes;
import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.search.AbstractQuery;
import com.top_logic.knowledge.search.AllOf;
import com.top_logic.knowledge.search.Eval;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.HistoryQuery;
import com.top_logic.knowledge.search.InSet;
import com.top_logic.knowledge.search.Order;
import com.top_logic.knowledge.search.QueryPart;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.SetExpression;

/**
 * Adds to each {@link AllOf} in the given query an equality check for the branch parameter. The
 * list of {@link MetaObject} which is needed to transform the requested branch to the correct one
 * (for types that are not branched this is needed to get the data branch) is added to the
 * transformed query.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class BranchExpressionEnhancement extends ExpressionTransformer<List<MetaObject>> {

	private static final BranchExpressionEnhancement INSTANCE = new BranchExpressionEnhancement();

	private BranchExpressionEnhancement() {
		// singleton instance
	}

	/**
	 * Applies an {@link BranchExpressionEnhancement} to the the given query.
	 * 
	 * @see BranchExpressionEnhancement
	 */
	public static <E extends AbstractQuery> E addBranchExpressions(AbstractQuery query) {
		ArrayList<MetaObject> result = new ArrayList<>();
		E visitResult = (E) query.visit(INSTANCE, result);
		visitResult.setAllOfTypes(result);
		return visitResult;
	}

	@Override
	public SetExpression visitAllOf(AllOf expr, List<MetaObject> arg) {
		SetExpression transformedAllOf = super.visitAllOf(expr, arg);
		Set<MetaObject> concreteTypes = expr.getConcreteTypes();
		if (concreteTypes.size() != 1) {
			throw new IllegalStateException("expected AllOf has only one concrete Type");
		}
		SetExpression result = filter(transformedAllOf, eqBinary(branch(), param(getBranchParamName(arg.size()))));
		arg.add(concreteTypes.iterator().next());
		return result;
	}

	private String getBranchParamName(int paramIndex) {
		return "_branch_" + paramIndex;
	}

	@Override
	public QueryPart visitRevisionQuery(RevisionQuery<?> expr, List<MetaObject> arg) {
		List<QueryPart> paramsResult = descendParts(expr, expr.getSearchParams(), arg);
		SetExpression searchResult = descendSet(expr, expr.getSearch(), arg);
		Order order = expr.getOrder();
		Order orderResult;
		if (order != null) {
			orderResult = descendOrder(expr, order, arg);
		} else {
			orderResult = null;
		}
		addParameterDeclarations(arg, paramsResult);
		expr.setSearchParams((List) paramsResult);
		expr.setSearch(searchResult);
		expr.setOrder(orderResult);
		return expr;
	}

	private void addParameterDeclarations(List<MetaObject> types, List<QueryPart> paramsDeclaration) {
		for (int i = 0; i < types.size(); i++) {
			paramsDeclaration.add(paramDecl(IdentifierTypes.BRANCH_REFERENCE_MO_TYPE, getBranchParamName(i)));
		}
	}

	@Override
	public QueryPart visitHistoryQuery(HistoryQuery expr, List<MetaObject> arg) {
		List<QueryPart> paramsResult = descendParts(expr, expr.getSearchParams(), arg);
		SetExpression search = descendSet(expr, expr.getSearch(), arg);

		addParameterDeclarations(arg, paramsResult);
		expr.setSearchParams((List) paramsResult);
		expr.setSearch(search);
		return expr;
	}

	@Override
	public Expression visitInSet(InSet inSet, List<MetaObject> arg) {
		// Do not descend within the inSet-expression, because this would restrict reference
		// navigation to the branch in which the query is evaluated.
		return inSet;
	}

	@Override
	public Expression visitEval(Eval eval, List<MetaObject> arg) {
		// Do not descend within the inSet-expression, because this would restrict reference
		// navigation to the branch in which the query is evaluated.
		return eval;
	}
}

