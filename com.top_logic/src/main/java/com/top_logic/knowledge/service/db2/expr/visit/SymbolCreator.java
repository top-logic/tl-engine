/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.visit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Protocol;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.MetaObject.Kind;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.TypeSystem;
import com.top_logic.knowledge.search.AbstractQuery;
import com.top_logic.knowledge.search.AllOf;
import com.top_logic.knowledge.search.AnyOf;
import com.top_logic.knowledge.search.Attribute;
import com.top_logic.knowledge.search.BinaryOperation;
import com.top_logic.knowledge.search.ContextAccess;
import com.top_logic.knowledge.search.ContextExpression;
import com.top_logic.knowledge.search.CountFunction;
import com.top_logic.knowledge.search.CrossProduct;
import com.top_logic.knowledge.search.Eval;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionTuple;
import com.top_logic.knowledge.search.Filter;
import com.top_logic.knowledge.search.Flex;
import com.top_logic.knowledge.search.Function;
import com.top_logic.knowledge.search.GetEntry;
import com.top_logic.knowledge.search.HasType;
import com.top_logic.knowledge.search.HistoryQuery;
import com.top_logic.knowledge.search.InSet;
import com.top_logic.knowledge.search.InstanceOf;
import com.top_logic.knowledge.search.Intersection;
import com.top_logic.knowledge.search.IsCurrent;
import com.top_logic.knowledge.search.Literal;
import com.top_logic.knowledge.search.MapTo;
import com.top_logic.knowledge.search.Matches;
import com.top_logic.knowledge.search.MaxFunction;
import com.top_logic.knowledge.search.MinFunction;
import com.top_logic.knowledge.search.None;
import com.top_logic.knowledge.search.Operator;
import com.top_logic.knowledge.search.Order;
import com.top_logic.knowledge.search.OrderSpec;
import com.top_logic.knowledge.search.OrderTuple;
import com.top_logic.knowledge.search.Parameter;
import com.top_logic.knowledge.search.ParameterDeclaration;
import com.top_logic.knowledge.search.Partition;
import com.top_logic.knowledge.search.QueryPart;
import com.top_logic.knowledge.search.QueryVisitor;
import com.top_logic.knowledge.search.Reference;
import com.top_logic.knowledge.search.RequestedHistoryContext;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.SetLiteral;
import com.top_logic.knowledge.search.SetParameter;
import com.top_logic.knowledge.search.Substraction;
import com.top_logic.knowledge.search.SumFunction;
import com.top_logic.knowledge.search.UnaryOperation;
import com.top_logic.knowledge.search.Union;
import com.top_logic.knowledge.service.db2.expr.sym.ErrorSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.FlexAttributeSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.ItemSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.LiteralItemSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.NullSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.PrimitiveSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.Symbol;
import com.top_logic.knowledge.service.db2.expr.sym.TableSymbol;
import com.top_logic.knowledge.service.db2.expr.sym.TupleSymbol;
import com.top_logic.knowledge.service.db2.expr.transform.ExpressionCompileProtocol;

/**
 * Visitor that attaches {@link Symbol} information to an expression.
 * 
 * The return value is the resulting {@link Symbol} of the expression.
 * 
 * The argument is the symbol of the surrounding context expression.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SymbolCreator implements QueryVisitor<Symbol, Symbol, Symbol, Symbol, Symbol, Symbol> {

	private static final Symbol INITIAL_ARG = null;
	private static final Symbol NO_SYMBOL = null;
	
	private final ExpressionCompileProtocol log;
	private final boolean requireSymbols;

	private final Map<String, Symbol> parameterSymbols = new HashMap<>();
	private final TypeSystem typeSystem;
	
	private SymbolCreator(TypeSystem typeSystem, ExpressionCompileProtocol log, boolean requireSymbols) {
		this.log = log;
		this.requireSymbols = requireSymbols;
		this.typeSystem = typeSystem;
	}

	/**
	 * Applies the {@link SymbolCreator} to the given expression and logs error if not possible to
	 * determine symbols.
	 * 
	 * @see #computeSymbols(TypeSystem, ExpressionCompileProtocol, AbstractQuery, boolean)
	 */
	public static void computeSymbols(TypeSystem typeSystem, ExpressionCompileProtocol log, AbstractQuery<?> query) {
		computeSymbols(typeSystem, log, query, true);
	}

	/**
	 * Applies the {@link SymbolCreator} to the given expression.
	 * 
	 * @param typeSystem
	 *        the {@link TypeSystem} to resolve subtype information from.
	 * @param log
	 *        The {@link Protocol} to report errors to.
	 * @param query
	 *        The query to compute concrete symbols for.
	 * @param requireSymbols
	 *        whether it is necessary to determine symbols. If some symbol can not determined, an
	 *        error is logged
	 */
	public static void computeSymbols(TypeSystem typeSystem, ExpressionCompileProtocol log, AbstractQuery<?> query,
			boolean requireSymbols) {
		SymbolCreator builder = new SymbolCreator(typeSystem, log, requireSymbols);
		query.visitQuery(builder, INITIAL_ARG);
	}
	
	@Override
	public Symbol visitNone(None expr, Symbol arg) {
		throw new AssertionError("Should have been eliminated before.");
	}

	@Override
	public Symbol visitSetLiteral(SetLiteral expr, Symbol arg) {
		Kind kind = expr.getPolymorphicType().getKind();
		switch (kind) {
			case item:
				return setSymbol(expr, new LiteralItemSymbol(expr));
			case NULL:
				return setSymbol(expr, NullSymbol.INSTANCE);
			default:
				return setSymbol(expr, NO_SYMBOL);
		}
	}
	
	@Override
	public Symbol visitSetParameter(SetParameter expr, Symbol arg) {
		return setSymbol(expr, parameterSymbols.get(expr.getName()));
	}

	@Override
	public Symbol visitAllOf(AllOf expr, Symbol arg) {
		return setSymbol(expr, new TableSymbol(expr, expr.getPolymorphicType()));
	}
	
	@Override
	public Symbol visitAnyOf(AnyOf expr, Symbol arg) {
		return setSymbol(expr, new TableSymbol(expr, expr.getPolymorphicType()));
	}

	@Override
	public Symbol visitCrossProduct(CrossProduct expr, Symbol arg) {
		List<SetExpression> expressions = expr.getExpressions();
		List<Symbol> symbols = new ArrayList<>(expressions.size());
		for (SetExpression subExr : expressions) {
			symbols.add(subExr.visitSetExpr(this, arg));
		}
		TupleSymbol result = new TupleSymbol(expr, symbols);
		for (Symbol entrySym : symbols) {
			entrySym.initParent(result);
		}
		return setSymbol(expr, result);
	}

	@Override
	public Symbol visitFilter(Filter expr, Symbol arg) {
		Symbol sourceSymbol = expr.getSource().visitSetExpr(this, arg);
		expr.getFilter().visit(this, sourceSymbol);
		
		return setSymbol(expr, sourceSymbol);
	}

	@Override
	public Symbol visitIntersection(Intersection expr, Symbol arg) {
		throw new IllegalArgumentException("Intersection should have been eliminated before.");
	}

	@Override
	public Symbol visitUnion(Union expr, Symbol arg) {
		expr.getLeftExpr().visitSetExpr(this, arg);
		expr.getRightExpr().visitSetExpr(this, arg);

		return setSymbol(expr, NO_SYMBOL);
	}
	
	@Override
	public Symbol visitSubstraction(Substraction expr, Symbol arg) {
		throw new IllegalArgumentException("Substraction should have been eliminated before.");
	}
	
	@Override
	public Symbol visitPartition(Partition expr, Symbol arg) {
		Symbol sourceSymbol = expr.getSource().visitSetExpr(this, arg);
		expr.getEquivalence().visit(this, sourceSymbol);
		Function representativeExpr = expr.getRepresentative();
		Symbol symbol = representativeExpr.visitFunction(this, sourceSymbol);
		// TODO: symbol = makeDefinition(representativeExpr, symbol);
		return setSymbol(expr, symbol);
	}

	@Override
	public Symbol visitMapTo(MapTo expr, Symbol arg) {
		Symbol sourceSymbol = expr.getSource().visitSetExpr(this, arg);
		Expression mapping = expr.getMapping();
		Symbol symbol = mapping.visit(this, sourceSymbol);
		symbol = makeDefinition(mapping, symbol);
		return setSymbol(expr, symbol);
	}

	@Override
	public Symbol visitAttribute(Attribute expr, Symbol contextSymbol) {
		Symbol contextsSymbol = setContextSymbol(expr, contextSymbol);
		Set<? extends MetaObject> potentialContextTypes = expr.getContext().getConcreteTypes();
		if (potentialContextTypes.size() == 0) {
			error(expr, "No context type in attribute accesss: " + expr);
			return setSymbol(expr, new ErrorSymbol(expr));
		} else if (potentialContextTypes.size() > 1) {
			if (requireSymbols) {
				error(expr, "Ambiguous context type in attribute accesss: " + getTypeNames(potentialContextTypes));
			}
			return setSymbol(expr, new ErrorSymbol(expr));
		} else {
			MOClass targetType = (MOClass) potentialContextTypes.iterator().next();
			TableSymbol dereference = ((ItemSymbol) contextsSymbol).dereference(targetType);
			Symbol symbol = dereference.getAttributeSymbol(expr);
			return setSymbol(expr, symbol);
		}
	}

	@Override
	public Symbol visitReference(Reference expr, Symbol contextSymbol) {
		Symbol contextsSymbol = setContextSymbol(expr, contextSymbol);
		Set<? extends MetaObject> potentialContextTypes = expr.getContext().getConcreteTypes();
		if (potentialContextTypes.size() == 0) {
			error(expr, "No possible context type in reference accesss: " + expr);
			return setSymbol(expr, new ErrorSymbol(expr));
		} else if (potentialContextTypes.size() > 1) {
			if (requireSymbols) {
				error(expr, "Ambiguous context type in reference accesss: " + getTypeNames(potentialContextTypes));
			}
			return setSymbol(expr, new ErrorSymbol(expr));
		} else {
			MOClass targetType = (MOClass) potentialContextTypes.iterator().next();
			TableSymbol tableSymbol = ((ItemSymbol) contextsSymbol).dereference(targetType);
			Symbol symbol = tableSymbol.getAttributeSymbol(expr);
			return setSymbol(expr, symbol);
		}
	}

	private static String getTypeNames(Collection<? extends MetaObject> types) {
		StringBuilder buffer = new StringBuilder();
		for (Iterator<? extends MetaObject> it = types.iterator(); it.hasNext(); ) {
			MetaObject metaObject = it.next();
			buffer.append(metaObject.getName());
			if (it.hasNext()) {
				buffer.append(", ");
			}
		}
		return buffer.toString();
	}

	@Override
	public Symbol visitFlex(Flex expr, Symbol contextSymbol) {
		Symbol contextsSymbol = setContextSymbol(expr, contextSymbol);
		String attribute = expr.getAttributeName();
		MetaObject type = expr.getDeclaredType();
		FlexAttributeSymbol symbol = ((ItemSymbol) contextsSymbol).getFlexSymbol(expr, typeSystem, type, attribute);
		return setSymbol(expr, symbol);
	}
	
	@Override
	public Symbol visitBinaryOperation(BinaryOperation expr, Symbol contextSymbol) {
		expr.getLeft().visit(this, contextSymbol);
		expr.getRight().visit(this, contextSymbol);
		return setSymbol(expr, NO_SYMBOL);
	}

	@Override
	public Symbol visitUnaryOperation(UnaryOperation expr, Symbol contextSymbol) {
		switch (expr.getOperator()) {
			case BRANCH:
			case REVISION:
			case HISTORY_CONTEXT:
			case IDENTIFIER:
			case TYPE_NAME:
			case IS_NULL:
			case NOT: {
				expr.getExpr().visit(this, contextSymbol);
				return setSymbol(expr, NO_SYMBOL);
			}
			default:
				throw Operator.noSuchOperator(expr.getOperator());
		}
	}

	@Override
	public Symbol visitIsCurrent(IsCurrent expr, Symbol arg) {
		setContextSymbol(expr, arg);
		return setSymbol(expr, NO_SYMBOL);
	}

	@Override
	public Symbol visitRequestedHistoryContext(RequestedHistoryContext expr, Symbol arg) {
		return setSymbol(expr, NO_SYMBOL);
	}

	@Override
	public Symbol visitEval(Eval expr, Symbol contextSymbol) {
		Symbol newContextSymbol = setContextSymbol(expr, contextSymbol);
		Symbol symbol = expr.getExpr().visit(this, newContextSymbol);
		return setSymbol(expr, symbol);
	}
	
	@Override
	public Symbol visitContext(ContextAccess expr, Symbol contextSymbol) {
		return setSymbol(expr, contextSymbol);
	}

	@Override
	public Symbol visitGetEntry(GetEntry expr, Symbol contextSymbol) {
		TupleSymbol tupleSymbol = (TupleSymbol) setContextSymbol(expr, contextSymbol);
		Symbol symbol = tupleSymbol.getSymbols().get(expr.getIndex());
		return setSymbol(expr, symbol);
	}

	@Override
	public Symbol visitInSet(InSet expr, Symbol contextSymbol) {
		setContextSymbol(expr, contextSymbol);
		expr.getSetExpr().visitSetExpr(this, INITIAL_ARG);
		return setSymbol(expr, NO_SYMBOL);
	}

	@Override
	public Symbol visitHasType(HasType expr, Symbol contextSymbol) {
		setContextSymbol(expr, contextSymbol);
		return setSymbol(expr, NO_SYMBOL);
	}

	@Override
	public Symbol visitInstanceOf(InstanceOf expr, Symbol contextSymbol) {
		setContextSymbol(expr, contextSymbol);
		return setSymbol(expr, NO_SYMBOL);
	}
	

	@Override
	public Symbol visitLiteral(Literal expr, Symbol contextSymbol) {
		Kind kind = expr.getPolymorphicType().getKind();
		switch (kind) {
			case item:
				return setSymbol(expr, new LiteralItemSymbol(expr));
			case NULL:
				return setSymbol(expr, NullSymbol.INSTANCE);
			default:
				return setSymbol(expr, NO_SYMBOL);
		}
	}
	
	@Override
	public Symbol visitParameter(Parameter expr, Symbol contextSymbol) {
		return setSymbol(expr, parameterSymbols.get(expr.getName()));
	}

	@Override
	public Symbol visitMatches(Matches expr, Symbol contextSymbol) {
		expr.getExpr().visit(this, contextSymbol);
		return setSymbol(expr, NO_SYMBOL);
	}

	@Override
	public Symbol visitTuple(ExpressionTuple expr, Symbol contextSymbol) {
		List<Expression> expressions = expr.getExpressions();
		List<Symbol> symbols = new ArrayList<>(expressions.size());
		
		ArrayList<Symbol> localDefinitions = null;
		for (Expression subExr : expressions) {
			Symbol entrySymbol = subExr.visit(this, contextSymbol);
			if (entrySymbol == null) {
				entrySymbol = makeDefinition(subExr, entrySymbol);
				
				if (localDefinitions == null) {
					localDefinitions = new ArrayList<>();
				}
				
				localDefinitions.add(entrySymbol);
			}
			symbols.add(entrySymbol);
		}
		
		TupleSymbol symbol = new TupleSymbol(expr, symbols);
		if (localDefinitions != null) {
			for (Symbol localSymbol : localDefinitions) {
				localSymbol.initParent(symbol);
			}
		}
		return setSymbol(expr, symbol);
	}


	@Override
	public Symbol visitCount(CountFunction fun, Symbol contextSymbol) {
		return setSymbol(fun, NO_SYMBOL);
	}


	@Override
	public Symbol visitMax(MaxFunction fun, Symbol contextSymbol) {
		fun.getExpr().visit(this, contextSymbol);
		return setSymbol(fun, NO_SYMBOL);
	}


	@Override
	public Symbol visitMin(MinFunction fun, Symbol contextSymbol) {
		fun.getExpr().visit(this, contextSymbol);
		return setSymbol(fun, NO_SYMBOL);
	}


	@Override
	public Symbol visitSum(SumFunction fun, Symbol contextSymbol) {
		fun.getExpr().visit(this, contextSymbol);
		return setSymbol(fun, NO_SYMBOL);
	}

	
	private static Symbol makeDefinition(Expression expr, Symbol intrinsicSymbol) {
		if (intrinsicSymbol == null) {
			Symbol definitionSymbol = new PrimitiveSymbol(expr);
			expr.setSymbol(definitionSymbol);
			return definitionSymbol;
		} else {
			return intrinsicSymbol;
		}
	}

	private Symbol setContextSymbol(ContextExpression expr, Symbol contextSymbol) {
		return expr.getContext().visit(this, contextSymbol);
	}

	/**
	 * Sets the given <code>symbol</code> to the given <code>expr</code> and returns it.
	 * 
	 * @param expr
	 *        the expression to the the symbol to
	 * @param symbol
	 *        the symbol to set
	 * @return the given symbol
	 */
	private static Symbol setSymbol(QueryPart expr, Symbol symbol) {
		expr.setSymbol(symbol);
		return symbol;
	}

	private void error(QueryPart expr, String message, Object... args) {
		log.error(expr, message, args);
	}

	@Override
	public Symbol visitHistoryQuery(HistoryQuery expr, Symbol arg) {
		return visitAbstractQuery(expr, arg);
	}

	private Symbol visitAbstractQuery(AbstractQuery<?> expr, Symbol arg) {
		List<ParameterDeclaration> searchParams = expr.getSearchParams();
		for (ParameterDeclaration decl : searchParams) {
			parameterSymbols.put(decl.getName(), decl.visitQuery(this, arg));
		}

		SetExpression search = expr.getSearch();

		return search.visitSetExpr(this, arg);
	}

	@Override
	public Symbol visitRevisionQuery(RevisionQuery<?> expr, Symbol arg) {
		Symbol symbol = visitAbstractQuery(expr, arg);
		Order order = expr.getOrder();
		if (order != null) {
			order.visitOrder(this, symbol);
		}
		return symbol;
	}

	@Override
	public Symbol visitParameterDeclaration(ParameterDeclaration expr, Symbol arg) {
		Symbol sym;
		if (expr.getPolymorphicType().getKind() == Kind.item) {
			sym = new LiteralItemSymbol(expr);
		} else {
			sym = NO_SYMBOL;
		}
		return sym;
	}

	@Override
	public Symbol visitOrderSpec(OrderSpec expr, Symbol arg) {
		expr.getOrderExpr().visit(this, arg);
		return null;
	}

	@Override
	public Symbol visitOrderTuple(OrderTuple expr, Symbol arg) {
		for (OrderSpec spec : expr.getOrderSpecs()) {
			spec.visitOrder(this, arg);
		}
		return null;
	}
}
