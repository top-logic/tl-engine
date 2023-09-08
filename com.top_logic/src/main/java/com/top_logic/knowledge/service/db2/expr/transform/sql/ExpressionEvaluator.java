/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform.sql;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.knowledge.service.db2.expr.transform.sql.DBSQLFactory.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.db.sql.Conversion;
import com.top_logic.basic.db.sql.SQLBoolean;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.db.sql.SQLTableReference;
import com.top_logic.basic.sql.DBType;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.MetaObject.Kind;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.IdentifiedObject;
import com.top_logic.dob.meta.MOCollection;
import com.top_logic.dob.meta.MOTuple;
import com.top_logic.knowledge.search.AllOf;
import com.top_logic.knowledge.search.AnyOf;
import com.top_logic.knowledge.search.Attribute;
import com.top_logic.knowledge.search.BinaryOperation;
import com.top_logic.knowledge.search.ContextAccess;
import com.top_logic.knowledge.search.CrossProduct;
import com.top_logic.knowledge.search.Eval;
import com.top_logic.knowledge.search.Expression;
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
import com.top_logic.knowledge.search.Operator;
import com.top_logic.knowledge.search.Parameter;
import com.top_logic.knowledge.search.Partition;
import com.top_logic.knowledge.search.Reference;
import com.top_logic.knowledge.search.RequestedHistoryContext;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.SetExpressionVisitor;
import com.top_logic.knowledge.search.SetLiteral;
import com.top_logic.knowledge.search.SetParameter;
import com.top_logic.knowledge.search.Substraction;
import com.top_logic.knowledge.search.TypeCheck;
import com.top_logic.knowledge.search.UnaryOperation;
import com.top_logic.knowledge.search.Union;
import com.top_logic.knowledge.service.db2.expr.sym.DataSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.FlexAttributeSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.ItemSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.NullSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.ReferenceSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.RowAttributeSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.Symbol;
import com.top_logic.knowledge.service.db2.expr.sym.SymbolVisitor;

/**
 * {@link ExpressionVisitor} that creates an {@link SQLExpression} which evaluate the
 * {@link Expression} within the database.
 * 
 * <p>
 * {@link SetExpressionVisitor} that creates an {@link SQLExpression} which evaluates to each member
 * of the {@link SetExpression} within the database (for {@link SetExpression}s of primitive type).
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class ExpressionEvaluator implements ExpressionVisitor<SQLExpression, Void>,
		SetExpressionVisitor<SQLExpression, Void> {

	private static final Void none = null;

	static final Mapping<Object, SQLExpression> ITEM_TO_TUPLE_MAPPING = new Mapping<>() {

		@Override
		public SQLExpression map(Object input) {
			if (input instanceof IdentifiedObject) {
				input = ((IdentifiedObject) input).tId();
			}
			return ReferenceSymbol.newTuple((ObjectKey) input);
		}
	};

	private static final Conversion ITEM_TO_TUPLE_CONVERSION = new Conversion() {
		@Override
		public Object convert(Object argument, Map<String, Integer> argumentIndexByName, Object[] arguments) {
			return Mappings.map(ITEM_TO_TUPLE_MAPPING, (Collection<?>) argument);
		}
	};

	private final SymbolVisitor<SQLExpression, Void> _dereferencer;

	private final JoinBuilder _joinBuilder;

	private ReferenceAccess _referenceAccess = new ReferenceAccess();

	private final JoinCollector _joinCollector;

	private final WhereBuilder _whereBuilder;

	private final SymbolVisitor<SQLExpression, Symbol> _symbolMatcher;

	public ExpressionEvaluator(JoinCollector joinCollector,
			JoinBuilder joinBuilder,
			SymbolVisitor<SQLExpression, Symbol> symbolMatcher,
			SymbolVisitor<SQLExpression, Void> dereferencer, WhereBuilder whereBuilder) {
		_joinCollector = joinCollector;
		_joinBuilder = joinBuilder;
		_symbolMatcher = symbolMatcher;
		_dereferencer = dereferencer;
		_whereBuilder = whereBuilder;
	}

	@Override
	public SQLExpression visitAttribute(Attribute expr, Void arg) {
		RowAttributeSymbol symbol = (RowAttributeSymbol) expr.getSymbol();
		return _dereferencer.visitRowAttributeSymbol(symbol, none);
	}

	@Override
	public SQLExpression visitFlex(Flex expr, Void arg) {
		FlexAttributeSymbol symbol = (FlexAttributeSymbol) expr.getSymbol();
		return _dereferencer.visitFlexAttributeSymbol(symbol, none);
	}

	@Override
	public SQLExpression visitGetEntry(GetEntry expr, Void arg) {
		Symbol symbol = expr.getSymbol();
		return symbol.visit(_dereferencer, none);
	}

	@Override
	public SQLExpression visitBinaryOperation(BinaryOperation expr, Void arg) {
		Operator symbol = expr.getOperator();
		Expression left = expr.getLeft();
		Expression right = expr.getRight();
		switch (symbol) {
			case AND: {
				SQLExpression leftSql = left.visit(this, arg);
				SQLExpression rightSql = right.visit(this, arg);
				return SQLFactory.and(leftSql, rightSql);
			}
			case OR: {
				SQLExpression leftSql = left.visit(this, arg);
				SQLExpression rightSql = right.visit(this, arg);
				return SQLFactory.or(leftSql, rightSql);
			}

			case EQCI:
				/* TODO: Ticket #18743: implement search case insensitive for databases which does
				 * not support case-(in)sensitive columns. */
			case EQBINARY: {
				MetaObject leftType = left.getPolymorphicType();

				switch (leftType.getKind()) {
					case NULL:
						return isNull(right.visit(this, arg));
					case item:
					case alternative:
					case tuple: {
						Symbol leftSym = left.getSymbol();
						Symbol rightSym = right.getSymbol();

						return SQLBuilder.createMatch((ItemSymbol) leftSym, rightSym, true, true);
					}

					case primitive: {
						SQLExpression leftSql = left.visit(this, arg);
						switch (right.getPolymorphicType().getKind()) {
							case NULL:
								return isNull(leftSql);
							default:
								SQLExpression rightSql = right.visit(this, arg);
								return eq(leftSql, rightSql);
						}

					}

					default: {
						throw new UnreachableAssertion("Type kind must not be used: " + leftType.getKind());
					}
				}
			}

			case LT: {
				SQLExpression leftSql = left.visit(this, arg);
				SQLExpression rightSql = right.visit(this, arg);
				return lt(leftSql, rightSql);
			}
			case GT: {
				SQLExpression leftSql = left.visit(this, arg);
				SQLExpression rightSql = right.visit(this, arg);
				return gt(leftSql, rightSql);
			}
			case LE: {
				SQLExpression leftSql = left.visit(this, arg);
				SQLExpression rightSql = right.visit(this, arg);
				return le(leftSql, rightSql);
			}
			case GE: {
				SQLExpression leftSql = left.visit(this, arg);
				SQLExpression rightSql = right.visit(this, arg);
				return ge(leftSql, rightSql);
			}

			default:
				throw new UnreachableAssertion("No such operator: " + symbol);
		}
	}

	@Override
	public SQLExpression visitEval(Eval expr, Void arg) {
		return expr.getExpr().visit(this, arg);
	}

	@Override
	public SQLExpression visitInSet(InSet expr, Void arg) {
		SetExpression setExpr = expr.getSetExpr();
		if (setExpr instanceof SetLiteral) {
			SetLiteral literal = (SetLiteral) setExpr;
			Collection<? extends Object> literalValues = literal.getValues();
			if (literalValues.isEmpty()) {
				return SQLBoolean.FALSE;
			}
			SQLExpression values = setExpr.visitSetExpr(this, arg);
			SQLExpression condition = expr.getContext().visit(this, arg);
			return inSet(condition, values);
		} else if (setExpr instanceof SetParameter) {
			SQLExpression values = setExpr.visitSetExpr(this, arg);
			SQLExpression condition = expr.getContext().visit(this, arg);
			return inSet(condition, values);
		}
		
		Symbol setSymbol = setExpr.getSymbol();
		
		SQLTableReference setJoin = this._joinBuilder.createJoin(setExpr);
		
		SQLExpression condition;
		Expression testExpr = expr.getContext();
		Symbol contextSymbol = testExpr.getSymbol();
		MetaObject testType = testExpr.getPolymorphicType();
		if (testType instanceof MOPrimitive) {
			condition = eq(testExpr.visit(this, none), setExpr.visitSetExpr(this, none));
		} else {
			condition = and(
				eq(
					testExpr.visit(_referenceAccess, ReferenceAspect.branch),
					setExpr.visitSetExpr(_referenceAccess, ReferenceAspect.branch)),
				eq(
					testExpr.visit(_referenceAccess, ReferenceAspect.name),
					setExpr.visitSetExpr(_referenceAccess, ReferenceAspect.name))
				);
		}
		
		condition = _joinBuilder.prependInSetLifeperiodJoinConditio(contextSymbol, setSymbol, condition);

		if (setSymbol instanceof DataSymbol) {
			((DataSymbol) setSymbol).setPotentiallyNull(true);
		}
		_joinCollector.addLeftJoin(setJoin, condition);

		// Descend to set expression to build the WHERE part.
		SQLExpression innerWhere = setExpr.visitSetExpr(_whereBuilder, arg);
		
		return and(innerWhere, not(setSymbol.visit(_symbolMatcher, NullSymbol.INSTANCE)));
	}

	@Override
	public SQLExpression visitContext(ContextAccess expr, Void arg) {
		return expr.getSymbol().visit(EvalValue.INSTANCE, none);
	}

	@Override
	public SQLExpression visitHasType(HasType expr, Void arg) {
		return visitTypeCheck(expr);
	}

	@Override
	public SQLExpression visitInstanceOf(InstanceOf expr, Void arg) {
		return visitTypeCheck(expr);
	}

	private SQLExpression visitTypeCheck(TypeCheck expr) {
		ItemSymbol contextSym = (ItemSymbol) expr.getContext().getSymbol();
		SQLExpression test = SQLBoolean.FALSE;
		for (MetaObject type : expr.getConcreteTrueTypes()) {
			test = or(test, createTypeTest(contextSym, type));
		}
		return test;
	}

	private SQLExpression createTypeTest(ItemSymbol contextSym, MetaObject type) {
		return eq(contextSym.createTypeExpr(), literalString(type.getName()));
	}

	@Override
	public SQLExpression visitLiteral(Literal expr, Void arg) {
		Object value = expr.getValue();

		return literal(DBType.fromLiteralValue(value), value);
	}

	@Override
	public SQLExpression visitParameter(Parameter expr, Void arg) {
		DBType sqlType = primitiveSQLType(expr.getDeclaredType());
		return parameter(sqlType, expr.getName());
	}

	private DBType[] sqlType(MetaObject declaredType) {
		switch (declaredType.getKind()) {
			case item:
				return ReferenceSymbol.ITEM_TUPLE_DBTYPE;
			case primitive:
				return new DBType[] { primitiveSQLType(declaredType) };
			case tuple:
				List<MetaObject> entryTypes = ((MOTuple)declaredType).getEntryTypes();
				switch(entryTypes.size()) {
					case 0:
						return new DBType[0];
					case 1:
						return sqlType(entryTypes.get(0));
					default:
						DBType[][] types = new DBType[entryTypes.size()][0];
						int completeSize = 0;
						for (int i = 0; i < entryTypes.size(); i++) {
							types[i] = sqlType(entryTypes.get(i));
							completeSize += types[i].length;
						}
						DBType[] result = new DBType[completeSize];
						int destPos = 0;
						for (int i = 0; i < types.length; i++) {
							int length = types[i].length;
							System.arraycopy(types[i], 0, result, destPos, length);
							destPos += length;
						}
						return result;
				}
			case collection:
				return sqlType(((MOCollection) declaredType).getElementType());
			default:
				throw new UnsupportedOperationException();
		}
	}

	private DBType primitiveSQLType(MetaObject declaredType) {
		return ((MOPrimitive) declaredType).getDefaultSQLType();
	}

	private Conversion conversionForCollection(MetaObject declaredType) {
		Conversion conversion;
		if (declaredType.getKind() == Kind.item) {
			conversion = ITEM_TO_TUPLE_CONVERSION;
		} else {
			conversion = Conversion.IDENTITY;
		}
		return conversion;
	}

	@Override
	public SQLExpression visitMatches(Matches expr, Void arg) {
		// TODO bhu Automatically created
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLExpression visitReference(Reference expr, Void arg) {
		ReferenceSymbol symbol = (ReferenceSymbol) expr.getSymbol();
		return _dereferencer.visitReferenceSymbol(symbol, none);
	}

	@Override
	public SQLExpression visitTuple(ExpressionTuple expr, Void arg) {
		// TODO bhu Automatically created
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLExpression visitUnaryOperation(UnaryOperation expr, Void arg) {
		switch (expr.getOperator()) {
			case NOT:
				return not(expr.getExpr().visit(this, arg));
			case IS_NULL:
				return isNull(expr.getExpr().visit(this, arg));
			case BRANCH:
				return ((DataSymbol) expr.getExpr().getSymbol()).createBranchExpr();
			case REVISION:
				return ((ItemSymbol) expr.getExpr().getSymbol()).createRevisionExpr();
			case HISTORY_CONTEXT:
				return ((ItemSymbol) expr.getExpr().getSymbol()).createHistoryContextExpr();
			case IDENTIFIER:
				return ((DataSymbol) expr.getExpr().getSymbol()).createIdentifierExpr();
			case TYPE_NAME:
				return ((DataSymbol) expr.getExpr().getSymbol()).createTypeExpr();
			default:
				throw new UnreachableAssertion("No such operator: " + expr.getOperator());
		}
	}

	@Override
	public SQLExpression visitIsCurrent(IsCurrent expr, Void arg) {
		SQLExpression contextRevision = ((ItemSymbol) expr.getContext().getSymbol()).createRevisionExpr();
		return eq(contextRevision, currentRevision());
	}

	@Override
	public SQLExpression visitRequestedHistoryContext(RequestedHistoryContext expr, Void arg) {
		return requestedHistoryContext();
	}

	@Override
	public SQLExpression visitNone(None expr, Void arg) {
		throw noEval();
	}

	@Override
	public SQLExpression visitSetLiteral(SetLiteral expr, Void arg) {
		Collection<? extends Object> values = expr.getValues();
		MetaObject polymorphicType = expr.getPolymorphicType();
		if (polymorphicType.getKind() == Kind.item) {
			return setLiteral(Mappings.map(ITEM_TO_TUPLE_MAPPING, values), ReferenceSymbol.ITEM_TUPLE_DBTYPE);
		} else {
			if (values.isEmpty()) {
				throw new IllegalArgumentException("Empty SetLiteral was removed before.");
			}
			DBType[] types;
			Object exampleValue = values.iterator().next();
			if (exampleValue instanceof Tuple) {
				Tuple exampleTuple = (Tuple) exampleValue;
				types = new DBType[exampleTuple.size()];
				for (int i = 0; i < types.length; i++) {
					types[i] = DBType.fromLiteralValue(exampleTuple.get(i));
				}
			} else {
				types = new DBType[] { DBType.fromLiteralValue(exampleValue) };
			}
			return setLiteral(values, types);
		}
	}

	@Override
	public SQLExpression visitSetParameter(SetParameter expr, Void arg) {
		MetaObject polymorphicType = expr.getPolymorphicType();
		Conversion conversion = conversionForCollection(polymorphicType);
		MetaObject declaredType = expr.getDeclaredType();
		return setParameter(conversion, expr.getName(), sqlType(declaredType));
	}

	@Override
	public SQLExpression visitAllOf(AllOf expr, Void arg) {
		throw noEval();
	}

	@Override
	public SQLExpression visitAnyOf(AnyOf expr, Void arg) {
		throw noEval();
	}

	@Override
	public SQLExpression visitSubstraction(Substraction expr, Void arg) {
		throw noEval();
	}

	@Override
	public SQLExpression visitIntersection(Intersection expr, Void arg) {
		throw noEval();
	}

	@Override
	public SQLExpression visitUnion(Union expr, Void arg) {
		throw noEval();
	}

	@Override
	public SQLExpression visitCrossProduct(CrossProduct expr, Void arg) {
		throw noEval();
	}

	@Override
	public SQLExpression visitFilter(Filter expr, Void arg) {
		throw noEval();
	}

	@Override
	public SQLExpression visitMapTo(MapTo expr, Void arg) {
		return expr.getMapping().visit(this, arg);
	}

	@Override
	public SQLExpression visitPartition(Partition expr, Void arg) {
		throw noEval();
	}

	private UnreachableAssertion noEval() {
		return new UnreachableAssertion("No evaluation possible.");
	}
}
