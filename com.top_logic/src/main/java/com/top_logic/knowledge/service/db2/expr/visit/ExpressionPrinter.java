/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.visit;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.search.AbstractQuery;
import com.top_logic.knowledge.search.AllOf;
import com.top_logic.knowledge.search.AnyOf;
import com.top_logic.knowledge.search.Attribute;
import com.top_logic.knowledge.search.BinaryOperation;
import com.top_logic.knowledge.search.BinarySetExpression;
import com.top_logic.knowledge.search.ContextAccess;
import com.top_logic.knowledge.search.ContextExpression;
import com.top_logic.knowledge.search.CountFunction;
import com.top_logic.knowledge.search.CrossProduct;
import com.top_logic.knowledge.search.Eval;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionTuple;
import com.top_logic.knowledge.search.Filter;
import com.top_logic.knowledge.search.Flex;
import com.top_logic.knowledge.search.GetEntry;
import com.top_logic.knowledge.search.HasType;
import com.top_logic.knowledge.search.HistoryQuery;
import com.top_logic.knowledge.search.InSet;
import com.top_logic.knowledge.search.InstanceOf;
import com.top_logic.knowledge.search.Intersection;
import com.top_logic.knowledge.search.IsCurrent;
import com.top_logic.knowledge.search.Literal;
import com.top_logic.knowledge.search.MapTo;
import com.top_logic.knowledge.search.MappingFunction;
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
import com.top_logic.knowledge.search.QueryParameter;
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
import com.top_logic.knowledge.service.Revision;


/**
 * Pretty printer of {@link Expression}s
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExpressionPrinter implements QueryVisitor<Void, Void, Void, Void, Void, StringBuffer> {

	/**
	 * Constructor of {@link HistoryQuery}.
	 */
	private static final String CONSTR_HISTORY = "history";

	/**
	 * Constructor of {@link RevisionQuery}.
	 */
	private static final String CONSTR_QUERY = "query";

	/**
	 * Constructor of {@link SumFunction}.
	 */
	private static final String CONSTR_SUM = "sum";

	/**
	 * Constructor of {@link MinFunction}.
	 */
	private static final String CONSTR_MIN = "min";

	/**
	 * Constructor of {@link MaxFunction}.
	 */
	private static final String CONSTR_MAX = "max";

	/**
	 * Constructor of {@link CountFunction}.
	 */
	private static final String CONSTR_COUNT = "count";

	/**
	 * Constructor of {@link Union}.
	 */
	private static final String CONSTR_UNION = "union";

	/**
	 * Constructor of {@link Substraction}.
	 */
	private static final String CONSTR_SUBSTRACTION = "substraction";

	/**
	 * Constructor of {@link Intersection}.
	 */
	private static final String CONSTR_INTERSECTION = "intersection";

	/**
	 * Constructor of {@link MapTo}.
	 */
	private static final String CONSTR_MAP = "map";

	/**
	 * Constructor of {@link Partition}.
	 */
	private static final String CONSTR_PARTITION = "partition";

	/**
	 * Constructor of {@link Filter}.
	 */
	private static final String FILTER = "filter";

	/**
	 * Constructor of {@link CrossProduct}.
	 */
	private static final String CONSTR_CROSSPRODUCT = "crossproduct";

	/**
	 * Constructor of {@link None}.
	 */
	private static final String CONSTR_NONE = "none";

	/**
	 * Constructor of {@link AnyOf}.
	 */
	private static final String CONSTR_ANY = "any";

	/**
	 * Constructor of {@link AllOf}.
	 */
	private static final String CONSTR_ALL = "all";

	/**
	 * Constructor of {@link InSet}.
	 */
	private static final String CONSTR_IN = "in";

	/**
	 * Constructor of {@link ContextAccess}.
	 */
	private static final String CONSTR_CONTEXT = "context";

	/**
	 * Constructor of {@link Eval}.
	 */
	private static final String CONSTR_EVAL = "eval";

	/**
	 * Constructor of {@link Matches}.
	 */
	private static final String CONSTR_MATCHES = "matches";

	/**
	 * Constructor of ascending {@link Order}.
	 */
	private static final String VALUE_ASCENDING = "ascending";

	/**
	 * Constructor of descending {@link Order}.
	 */
	private static final String VALUE_DESCENDING = "descending";

	/**
	 * Constructor of {@link OrderSpec}.
	 */
	private static final String CONSTR_ORDER = "order";

	/**
	 * Constructor of {@link ExpressionTuple}.
	 */
	private static final String CONSTR_TUPLE = "tuple";

	/**
	 * Constructor of {@link InstanceOf}.
	 */
	private static final String CONSTR_INSTANCEOF = "instanceof";

	/**
	 * Constructor of {@link HasType}.
	 */
	private static final String CONSTR_HASTYPE = "hastype";

	/**
	 * Constructor of {@link Flex}.
	 */
	private static final String CONSTR_FLEX = "flex";

	/**
	 * Constructor of {@link Reference}.
	 */
	private static final String CONSTR_REFERENCE = "reference";

	/**
	 * Constructor of {@link GetEntry}.
	 */
	private static final String CONSTR_GET = "get";

	/**
	 * Constructor of {@link Attribute}.
	 */
	private static final String CONSTR_ATTRIBUTE = "attribute";
	
	/**
	 * Constructor of {@link IsCurrent}.
	 */
	private static final String CONSTR_IS_CURRENT = "isCurrent";

	/**
	 * Constructor of {@link RequestedHistoryContext}.
	 */
	private static final String CONSTR_REQUESTED_HISTORY_CONTEXT = "requestedHistoryContext";

	private static final ExpressionPrinter INSTANCE = new ExpressionPrinter();
	
	private static final Void none = null;
	
	private ExpressionPrinter() {
		// Singleton constructor.
	}
	
	/**
	 * Dumps the given expression.
	 */
	public static String toString(QueryPart expr) {
		StringBuffer buffer = new StringBuffer();
		expr.visitQuery(INSTANCE, buffer);
		
		return buffer.toString();
	}
	
	@Override
	public Void visitAttribute(Attribute expr, StringBuffer buffer) {
		buffer.append(CONSTR_ATTRIBUTE);
		buffer.append('(');
		appendContext(expr, buffer);
		buffer.append(expr.getOwnerTypeName());
		buffer.append('.');
		buffer.append(expr.getAttributeName());
		buffer.append(')');
		return none;
	}
	
	@Override
	public Void visitIsCurrent(IsCurrent expr, StringBuffer buffer) {
		buffer.append(CONSTR_IS_CURRENT);
		buffer.append('(');
		appendContext(expr, buffer);
		// No additional argument after context; replace last ','.
		buffer.setCharAt(buffer.length() - 1, ')');
		return none;
	}

	@Override
	public Void visitGetEntry(GetEntry expr, StringBuffer buffer) {
		buffer.append(CONSTR_GET);
		buffer.append('(');
		appendContext(expr, buffer);
		buffer.append(expr.getIndex());
		buffer.append(')');
		return none;
	}
	
	@Override
	public Void visitReference(Reference expr, StringBuffer buffer) {
		buffer.append(CONSTR_REFERENCE);
		buffer.append('(');
		appendContext(expr, buffer);
		buffer.append(expr.getOwnerTypeName());
		buffer.append('.');
		buffer.append(expr.getAttributeName());
		final ReferencePart columnType = expr.getAccessType();
		if (columnType != null) {
			buffer.append(':');
			buffer.append(columnType);
		}
		buffer.append(')');
		return none;

	}

	@Override
	public Void visitFlex(Flex expr, StringBuffer buffer) {
		buffer.append(CONSTR_FLEX);
		buffer.append('(');
		appendContext(expr, buffer);
		buffer.append(expr.getAttributeName());
		buffer.append(':');
		buffer.append(expr.getTypeName());
		buffer.append(')');
		return none;
	}

	@Override
	public Void visitBinaryOperation(BinaryOperation expr, StringBuffer buffer) {
		buffer.append('(');
		expr.getLeft().visit(this, buffer);
		buffer.append(' ');
		Operator operator = expr.getOperator();
		String opSym = getOperatorSymbol(operator);
		buffer.append(opSym);
		buffer.append(' ');
		expr.getRight().visit(this, buffer);
		buffer.append(')');
		return none;
	}

	private String getOperatorSymbol(Operator operator) throws UnreachableAssertion {
		String opSym;
		switch (operator) {
			case AND:
				opSym = "&&";
				break;
			case OR:
				opSym = "||";
				break;
			case EQBINARY:
				opSym = "==";
				break;
			case EQCI:
				opSym = "eqci";
				break;
			case GE:
				opSym = ">=";
				break;
			case GT:
				opSym = ">";
				break;
			case LE:
				opSym = "<=";
				break;
			case LT:
				opSym = "<";
				break;
			case NOT:
				opSym = "!";
				break;
			case IS_NULL:
				opSym = "isNull";
				break;
			case BRANCH:
				opSym = "branch";
				break;
			case REVISION:
				opSym = "rev";
				break;
			case HISTORY_CONTEXT:
				opSym = "historyContext";
				break;
			case TYPE_NAME:
				opSym = "type";
				break;
			case IDENTIFIER:
				opSym = "id";
				break;
			default:
				throw new UnreachableAssertion("No such operator: " + operator);
		}
		return opSym;
	}

	@Override
	public Void visitHasType(HasType expr, StringBuffer buffer) {
		buffer.append(CONSTR_HASTYPE);
		buffer.append('(');
		appendContext(expr, buffer);
		buffer.append(expr.getTypeName());
		buffer.append(')');
		return none;
	}

	@Override
	public Void visitInstanceOf(InstanceOf expr, StringBuffer buffer) {
		buffer.append(CONSTR_INSTANCEOF);
		buffer.append('(');
		appendContext(expr, buffer);
		buffer.append(expr.getTypeName());
		buffer.append(')');
		return none;
	}
	
	@Override
	public Void visitLiteral(Literal expr, StringBuffer buffer) {
		appendLiteral(buffer, expr.getValue());
		return none;
	}

	@Override
	public Void visitParameter(Parameter expr, StringBuffer buffer) {
		appendQueryParameter(expr, buffer);
		return none;
	}
	
	@Override
	public Void visitTuple(ExpressionTuple expr, StringBuffer buffer) {
		List<Expression> expressions = expr.getExpressions();
		buffer.append(CONSTR_TUPLE);
		buffer.append('(');
		for (int n = 0, cnt = expressions.size(); n < cnt; n++) {
			if (n > 0) {
				buffer.append(", ");
			}
			expressions.get(n).visit(this, buffer);
		}
		buffer.append(')');
		
		return none;
	}

	@Override
	public Void visitOrderTuple(OrderTuple tuple, StringBuffer buffer) {
		List<OrderSpec> orderSpecs = tuple.getOrderSpecs();
		
		buffer.append(CONSTR_ORDER);
		buffer.append('(');
		for (int n = 0, cnt = orderSpecs.size(); n < cnt; n++) {
			if (n > 0) {
				buffer.append(", ");
			}
			orderSpecs.get(n).visitOrder(this, buffer);
		}
		buffer.append(')');
		
		return none;
	}
	
	@Override
	public Void visitOrderSpec(OrderSpec orderSpec, StringBuffer buffer) {
		if (orderSpec.isDescending()) {
			buffer.append(VALUE_DESCENDING);
		} else {
			buffer.append(VALUE_ASCENDING);
		}
		buffer.append('(');
		orderSpec.getOrderExpr().visit(this, buffer);
		buffer.append(')');
		
		return none;
	}
	
	private void appendLiteral(StringBuffer buffer, Object value) {
		if (value instanceof String) {
			buffer.append('\'');
			buffer.append(quoteString((String) value));
			buffer.append('\'');
		} else if (value instanceof Number) {
			buffer.append(value);
		} else if (value instanceof Date) {
			buffer.append("date:");
			buffer.append(XmlDateTimeFormat.INSTANCE.format(value));
		} else if (value instanceof KnowledgeItem) {
			KnowledgeItem item = (KnowledgeItem) value;
			appendLiteralKey(buffer, item.tId());
		} else if (value instanceof ObjectKey) {
			ObjectKey key = (ObjectKey) value;
			appendLiteralKey(buffer, key);
		} else if (value instanceof TLID) {
			buffer.append(String.valueOf(value));
		} else if (value instanceof Boolean) {
			buffer.append(String.valueOf(value));
		} else if (value == null) {
			buffer.append("<null>");
		} else {
			buffer.append("<invalid:");
			buffer.append(value);
			buffer.append('>');
		}
	}

	private void appendLiteralKey(StringBuffer buffer, ObjectKey key) {
		buffer.append("item:");
		buffer.append(key.getBranchContext());
		buffer.append('/');
		buffer.append(key.getObjectName());
		buffer.append('/');
		long historyContext = key.getHistoryContext();
		if (historyContext != Revision.CURRENT_REV) {
			buffer.append(historyContext);
		} else {
			buffer.append("current");
		}
	}

	/**
	 * Quotes the given string for usage as contents of a literal string
	 * expression.
	 * 
	 * @param value
	 *        The string to quote.
	 * @return The quoted string.
	 */
	public static String quoteString(String value) {
		String s = value;
		s = s.replaceAll("\'", "\\\\\'");
		s = s.replaceAll("\n", "\\\\n");
		s = s.replaceAll("\r", "\\\\r");
		s = s.replaceAll("\t", "\\\\t");
		return s;
	}
	
	@Override
	public Void visitUnaryOperation(UnaryOperation expr, StringBuffer buffer) {
		Operator operator = expr.getOperator();
		switch (operator) {
			case REVISION:
			case HISTORY_CONTEXT:
			case IDENTIFIER:
			case TYPE_NAME:
			case BRANCH:
				buffer.append(getOperatorSymbol(operator));
				buffer.append('(');
				expr.getExpr().visit(this, buffer);
				buffer.append(')');
				break;

			default:
				buffer.append('(');
				buffer.append(getOperatorSymbol(operator));
				buffer.append(' ');
				expr.getExpr().visit(this, buffer);
				buffer.append(')');
		}
		return none;
	}

	@Override
	public Void visitMatches(Matches expr, StringBuffer buffer) {
		buffer.append(CONSTR_MATCHES);
		buffer.append('(');
		buffer.append('"');
		buffer.append(quoteString(expr.getRegex()));
		buffer.append('"');
		buffer.append(", ");
		expr.getExpr().visit(this, buffer);
		buffer.append(')');
		return none;
	}

	@Override
	public Void visitEval(Eval expr, StringBuffer buffer) {
		buffer.append(CONSTR_EVAL);
		buffer.append('(');
		appendContext(expr, buffer);
		expr.getExpr().visit(this, buffer);
		buffer.append(')');
		return none;
	}
	
	@Override
	public Void visitContext(ContextAccess expr, StringBuffer buffer) {
		buffer.append(CONSTR_CONTEXT);
		buffer.append('(');
		buffer.append(')');
		return none;
	}
	
	@Override
	public Void visitInSet(InSet expr, StringBuffer buffer) {
		buffer.append(CONSTR_IN);
		buffer.append('(');
		appendContext(expr, buffer);
		expr.getSetExpr().visitSetExpr(this, buffer);
		buffer.append(')');
		return none;
	}

	@Override
	public Void visitRequestedHistoryContext(RequestedHistoryContext expr, StringBuffer buffer) {
		buffer.append(CONSTR_REQUESTED_HISTORY_CONTEXT);
		buffer.append('(');
		buffer.append(')');
		return none;
	}

	private void appendContext(ContextExpression expr, StringBuffer buffer) {
		expr.getContext().visit(this, buffer);
		buffer.append(", ");
	}
	
	@Override
	public Void visitAllOf(AllOf expr, StringBuffer buffer) {
		buffer.append(CONSTR_ALL);
		buffer.append('(');
		buffer.append(expr.getTypeName());
		buffer.append(')');
		return none;
	}
	
	@Override
	public Void visitAnyOf(AnyOf expr, StringBuffer buffer) {
		buffer.append(CONSTR_ANY);
		buffer.append('(');
		buffer.append(expr.getTypeName());
		buffer.append(')');
		return none;
	}

	@Override
	public Void visitNone(None expr, StringBuffer buffer) {
		buffer.append(CONSTR_NONE);
		buffer.append('(');
		buffer.append(')');
		return none;
	}
	
	@Override
	public Void visitSetLiteral(SetLiteral expr, StringBuffer buffer) {
		Collection collection = expr.getValues();
		buffer.append('{');
		Iterator it = collection.iterator();
		if (it.hasNext()) {
			while (true) {
				appendLiteral(buffer, it.next());
				if (it.hasNext()) {
					buffer.append(", ");
				} else {
					break;
				}
			}
		}
		buffer.append('}');
		return none;
	}
	
	@Override
	public Void visitSetParameter(SetParameter expr, StringBuffer buffer) {
		appendQueryParameter(expr, buffer);
		return none;
	}

	private void appendQueryParameter(QueryParameter expr, StringBuffer buffer) {
		buffer.append(expr.getName());
	}
	
	@Override
	public Void visitCrossProduct(CrossProduct expr, StringBuffer buffer) {
		buffer.append(CONSTR_CROSSPRODUCT);
		buffer.append('(');
		boolean first = true;
		for (SetExpression setExpr : expr.getExpressions()) {
			if (first) {
				first = false;
			} else {
				buffer.append(", ");
			}
			setExpr.visitQuery(this, buffer);
		}
		buffer.append(')');
		return none;
	}

	@Override
	public Void visitFilter(Filter expr, StringBuffer buffer) {
		buffer.append(FILTER);
		buffer.append('(');
		expr.getSource().visitQuery(this, buffer);
		buffer.append(", ");
		expr.getFilter().visit(this, buffer);
		buffer.append(')');
		return none;
	}

	@Override
	public Void visitPartition(Partition expr, StringBuffer buffer) {
		buffer.append(CONSTR_PARTITION);
		buffer.append('(');
		expr.getSource().visitQuery(this, buffer);
		buffer.append(", ");
		expr.getEquivalence().visit(this, buffer);
		buffer.append(", ");
		expr.getRepresentative().visitFunction(this, buffer);
		buffer.append(')');
		return none;
	}

	@Override
	public Void visitMapTo(MapTo expr, StringBuffer buffer) {
		buffer.append(CONSTR_MAP);
		buffer.append('(');
		expr.getSource().visitQuery(this, buffer);
		buffer.append(", ");
		expr.getMapping().visit(this, buffer);
		buffer.append(')');
		return none;
	}

	@Override
	public Void visitIntersection(Intersection expr, StringBuffer buffer) {
		appendBinarySetExpr(buffer, CONSTR_INTERSECTION, expr);
		return none;
	}
	
	@Override
	public Void visitSubstraction(Substraction expr, StringBuffer buffer) {
		appendBinarySetExpr(buffer, CONSTR_SUBSTRACTION, expr);
		return none;
	}

	@Override
	public Void visitUnion(Union expr, StringBuffer buffer) {
		appendBinarySetExpr(buffer, CONSTR_UNION, expr);
		return none;
	}

	private void appendBinarySetExpr(StringBuffer buffer, String name, BinarySetExpression expr) {
		buffer.append(name);
		buffer.append('(');
		expr.getLeftExpr().visitQuery(this, buffer);
		buffer.append(", ");
		expr.getRightExpr().visitQuery(this, buffer);
		buffer.append(')');
	}

	@Override
	public Void visitCount(CountFunction fun, StringBuffer buffer) {
		buffer.append(CONSTR_COUNT);
		buffer.append('(');
		buffer.append(')');
		return none;
	}

	@Override
	public Void visitMax(MaxFunction fun, StringBuffer buffer) {
		appendProjectionFunction(buffer, CONSTR_MAX, fun);
		return none;
	}

	@Override
	public Void visitMin(MinFunction fun, StringBuffer buffer) {
		appendProjectionFunction(buffer, CONSTR_MIN, fun);
		return none;
	}

	@Override
	public Void visitSum(SumFunction fun, StringBuffer buffer) {
		appendProjectionFunction(buffer, CONSTR_SUM, fun);
		return none;
	}
	
	private void appendProjectionFunction(StringBuffer buffer, String name, MappingFunction fun) {
		buffer.append(name);
		buffer.append('(');
		fun.getExpr().visit(this, buffer);
		buffer.append(')');
	}
	

	@Override
	public Void visitRevisionQuery(RevisionQuery<?> expr, StringBuffer buffer) {
		buffer.append(CONSTR_QUERY);
		buffer.append('(');
		buffer.append(expr.getBranchParam());
		buffer.append(", ");
		buffer.append(expr.getRangeParam());
		buffer.append(", ");
		appendParameterDeclations(buffer, expr);
		buffer.append(", ");
		expr.getSearch().visitQuery(this, buffer);
		buffer.append(')');
		return none;
	}

	@Override
	public Void visitHistoryQuery(HistoryQuery expr, StringBuffer buffer) {
		buffer.append(CONSTR_HISTORY);
		buffer.append('(');
		buffer.append(expr.getBranchParam());
		buffer.append(", ");
		buffer.append(expr.getRevisionParam());
		buffer.append(", ");
		buffer.append(expr.getRangeParam());
		buffer.append(", ");
		appendParameterDeclations(buffer, expr);
		buffer.append(", ");
		expr.getSearch().visitQuery(this, buffer);
		buffer.append(')');
		return none;
	}

	private void appendParameterDeclations(StringBuffer buffer, AbstractQuery expr) {
		List<ParameterDeclaration> searchParams = expr.getSearchParams();
		buffer.append('[');
		for (int n = 0, cnt = searchParams.size(); n < cnt; n++) {
			if (n > 0) {
				buffer.append(", ");
			}
			ParameterDeclaration decl = searchParams.get(n);
			decl.visitQuery(this, buffer);
		}
		buffer.append(']');
	}
	
	@Override
	public Void visitParameterDeclaration(ParameterDeclaration expr, StringBuffer buffer) {
		buffer.append(expr.getName());
		buffer.append(": ");
		buffer.append(expr.getTypeName());
		return none;
	}
	
}
