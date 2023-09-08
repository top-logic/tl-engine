/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform.sql;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.dob.IdentifierTypes;
import com.top_logic.knowledge.search.AllOf;
import com.top_logic.knowledge.search.AnyOf;
import com.top_logic.knowledge.search.Attribute;
import com.top_logic.knowledge.search.BinaryOperation;
import com.top_logic.knowledge.search.ContextAccess;
import com.top_logic.knowledge.search.CrossProduct;
import com.top_logic.knowledge.search.Eval;
import com.top_logic.knowledge.search.ExpressionTuple;
import com.top_logic.knowledge.search.ExpressionVisitor;
import com.top_logic.knowledge.search.Filter;
import com.top_logic.knowledge.search.Flex;
import com.top_logic.knowledge.search.GetEntry;
import com.top_logic.knowledge.search.HasType;
import com.top_logic.knowledge.search.InSet;
import com.top_logic.knowledge.search.InstanceOf;
import com.top_logic.knowledge.search.Intersection;
import com.top_logic.knowledge.search.IsCurrent;
import com.top_logic.knowledge.search.Literal;
import com.top_logic.knowledge.search.MapTo;
import com.top_logic.knowledge.search.Matches;
import com.top_logic.knowledge.search.None;
import com.top_logic.knowledge.search.Parameter;
import com.top_logic.knowledge.search.Partition;
import com.top_logic.knowledge.search.Reference;
import com.top_logic.knowledge.search.RequestedHistoryContext;
import com.top_logic.knowledge.search.SetExpressionVisitor;
import com.top_logic.knowledge.search.SetLiteral;
import com.top_logic.knowledge.search.SetParameter;
import com.top_logic.knowledge.search.Substraction;
import com.top_logic.knowledge.search.UnaryOperation;
import com.top_logic.knowledge.search.Union;
import com.top_logic.knowledge.service.db2.expr.sym.DataSymbol;

/**
 * {@link ExpressionVisitor} and {@link SetExpressionVisitor} that creates an {@link SQLExpression}
 * that evaluates to the given {@link ReferenceAspect} of an expression of item type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class ReferenceAccess implements ExpressionVisitor<SQLExpression, ReferenceAspect>,
		SetExpressionVisitor<SQLExpression, ReferenceAspect> {

	@Override
	public SQLExpression visitLiteral(Literal expr, ReferenceAspect arg) {
		throw noReferenceType();
	}

	@Override
	public SQLExpression visitParameter(Parameter expr, ReferenceAspect arg) {
		throw new UnsupportedOperationException("TODO: Object parameters.");
	}

	@Override
	public SQLExpression visitAttribute(Attribute expr, ReferenceAspect arg) {
		throw noReferenceType();
	}

	@Override
	public SQLExpression visitUnaryOperation(UnaryOperation expr, ReferenceAspect arg) {
		throw noReferenceType();
	}

	@Override
	public SQLExpression visitBinaryOperation(BinaryOperation expr, ReferenceAspect arg) {
		throw noReferenceType();
	}

	@Override
	public SQLExpression visitIsCurrent(IsCurrent expr, ReferenceAspect arg) {
		throw noReferenceType();
	}

	@Override
	public SQLExpression visitHasType(HasType expr, ReferenceAspect arg) {
		throw noReferenceType();
	}

	@Override
	public SQLExpression visitInstanceOf(InstanceOf expr, ReferenceAspect arg) {
		throw noReferenceType();
	}

	@Override
	public SQLExpression visitTuple(ExpressionTuple expr, ReferenceAspect arg) {
		throw noReferenceType();
	}

	@Override
	public SQLExpression visitReference(Reference expr, ReferenceAspect arg) {
		return createAccess((DataSymbol) expr.getSymbol(), arg);
	}

	@Override
	public SQLExpression visitFlex(Flex expr, ReferenceAspect arg) {
		throw noReferenceType();
	}

	@Override
	public SQLExpression visitGetEntry(GetEntry expr, ReferenceAspect arg) {
		throw noReferenceType();
	}

	@Override
	public SQLExpression visitMatches(Matches expr, ReferenceAspect arg) {
		throw noReferenceType();
	}

	@Override
	public SQLExpression visitEval(Eval expr, ReferenceAspect arg) {
		return expr.getExpr().visit(this, arg);
	}

	@Override
	public SQLExpression visitRequestedHistoryContext(RequestedHistoryContext expr, ReferenceAspect arg) {
		throw noReferenceType();
	}

	@Override
	public SQLExpression visitContext(ContextAccess expr, ReferenceAspect arg) {
		return createAccess((DataSymbol) expr.getSymbol(), arg);
	}

	@Override
	public SQLExpression visitInSet(InSet expr, ReferenceAspect arg) {
		throw noReferenceType();
	}

	private SQLExpression createAccess(DataSymbol symbol, ReferenceAspect arg) throws UnreachableAssertion {
		switch (arg) {
			case branch:
				return symbol.createBranchExpr();
			case name:
				return symbol.createIdentifierExpr();
			case revMin:
				return symbol.createRevMinExpr();
			case revMax:
				return symbol.createRevMaxExpr();
			case type:
				return symbol.createTypeExpr();
			default:
				throw new UnreachableAssertion("No such reference part: " + arg);
		}
	}

	private Error noReferenceType() {
		return new UnreachableAssertion("No reference type.");
	}

	@Override
	public SQLExpression visitNone(None expr, ReferenceAspect arg) {
		switch (arg) {
			case branch:
				return SQLFactory.literalNull(IdentifierTypes.BRANCH_REFERENCE_MO_TYPE.getDefaultSQLType());
			case name:
				return SQLFactory.literalNull(IdentifierTypes.REFERENCE_MO_TYPE.getDefaultSQLType());
			case revMin:
				return SQLFactory.literalNull(IdentifierTypes.REVISION_REFERENCE_MO_TYPE.getDefaultSQLType());
			case revMax:
				return SQLFactory.literalNull(IdentifierTypes.REVISION_REFERENCE_MO_TYPE.getDefaultSQLType());
			case type:
				return SQLFactory.literalNull(IdentifierTypes.TYPE_REFERENCE_MO_TYPE.getDefaultSQLType());
			default:
				throw new UnreachableAssertion("No such reference part: " + arg);
		}
	}

	@Override
	public SQLExpression visitSetLiteral(SetLiteral expr, ReferenceAspect arg) {
		throw cannotEvaluate();
	}

	@Override
	public SQLExpression visitSetParameter(SetParameter expr, ReferenceAspect arg) {
		throw cannotEvaluate();
	}

	@Override
	public SQLExpression visitAllOf(AllOf expr, ReferenceAspect arg) {
		return createAccess((DataSymbol) expr.getSymbol(), arg);
	}

	@Override
	public SQLExpression visitAnyOf(AnyOf expr, ReferenceAspect arg) {
		return createAccess((DataSymbol) expr.getSymbol(), arg);
	}

	@Override
	public SQLExpression visitSubstraction(Substraction expr, ReferenceAspect arg) {
		return createAccess((DataSymbol) expr.getSymbol(), arg);
	}

	@Override
	public SQLExpression visitIntersection(Intersection expr, ReferenceAspect arg) {
		return createAccess((DataSymbol) expr.getSymbol(), arg);
	}

	@Override
	public SQLExpression visitUnion(Union expr, ReferenceAspect arg) {
		return createAccess((DataSymbol) expr.getSymbol(), arg);
	}

	@Override
	public SQLExpression visitCrossProduct(CrossProduct expr, ReferenceAspect arg) {
		throw cannotEvaluate();
	}

	@Override
	public SQLExpression visitFilter(Filter expr, ReferenceAspect arg) {
		return createAccess((DataSymbol) expr.getSymbol(), arg);
	}

	@Override
	public SQLExpression visitMapTo(MapTo expr, ReferenceAspect arg) {
		return expr.getMapping().visit(this, arg);
	}

	@Override
	public SQLExpression visitPartition(Partition expr, ReferenceAspect arg) {
		return createAccess((DataSymbol) expr.getSymbol(), arg);
	}

	private Error cannotEvaluate() {
		throw new UnreachableAssertion("Cannot evaluate expression to SQL.");
	}

}
