/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.List;
import java.util.Set;

import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.search.BinaryOperation;
import com.top_logic.knowledge.search.CrossProduct;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.Filter;
import com.top_logic.knowledge.search.Function;
import com.top_logic.knowledge.search.HasType;
import com.top_logic.knowledge.search.InSet;
import com.top_logic.knowledge.search.InstanceOf;
import com.top_logic.knowledge.search.Intersection;
import com.top_logic.knowledge.search.Literal;
import com.top_logic.knowledge.search.MapTo;
import com.top_logic.knowledge.search.None;
import com.top_logic.knowledge.search.Partition;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.SetLiteral;
import com.top_logic.knowledge.search.Substraction;
import com.top_logic.knowledge.search.UnaryOperation;
import com.top_logic.knowledge.search.Union;
import com.top_logic.knowledge.service.db2.expr.visit.LiteralValue;
import com.top_logic.util.Utils;

/**
 * Transforms an {@link SetExpression} by evaluating operations on constants.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConstantFolding extends InlineExpressionTransformer<Void> {

	/**
	 * Singleton {@link ConstantFolding} instance.
	 */
	public static final ConstantFolding INSTANCE = new ConstantFolding();
	
	private static final Void none = null;

	private ConstantFolding() {
		// Singleton constructor.
	}

	public static SetExpression foldConstants(SetExpression search) {
		return search.visitSetExpr(INSTANCE, none);
	}
	
	public static Expression foldConstants(Expression search) {
		return search.visit(INSTANCE, none);
	}
	
	
	@Override
	protected Expression processBinary(BinaryOperation expr, Expression left, Expression right, Void arg) {
		switch (expr.getOperator()) {
		case AND: {
			if (LiteralValue.isLiteralTrue(left)) {
				return right;
			}
			if (LiteralValue.isLiteralTrue(right)) {
				return left;
			}
			if (LiteralValue.isLiteralFalse(left)) {
				return left;
			}
			if (LiteralValue.isLiteralFalse(right)) {
				return right;
			}
			break;
		}
		case OR: {
			// Local constant folding.
			if (LiteralValue.isLiteralFalse(left)) {
				return right;
			}
			if (LiteralValue.isLiteralFalse(right)) {
				return left;
			}
			if (LiteralValue.isLiteralTrue(left)) {
				return left;
			}
			if (LiteralValue.isLiteralTrue(right)) {
				return right;
			}
			break;
		}
		case EQCI:
		case EQBINARY: {
			if (left instanceof Literal) {
				if (right instanceof Literal) {
					if (Utils.equals(((Literal) left).getValue(), ((Literal) right).getValue())) {
						return literal(true);
					}
				}
			}
			break;
		}
		case GE:
		case GT:
		case LE:
		case LT:
			break;
			
		default: 
			throw new IllegalArgumentException("Not a binary operator: " + expr.getOperator());
		}
		return super.processBinary(expr, left, right, arg);
	}
	
	@Override
	protected Expression process(UnaryOperation expr, Expression expression, Void arg) {
		switch (expr.getOperator()) {
			case NOT: {
				if (LiteralValue.isLiteralTrue(expression)) {
					return literal(false);
				} else if (LiteralValue.isLiteralFalse(expression)) {
					return literal(true);
				} else {
					break;
				}
			}
			case IS_NULL: {
				if (LiteralValue.isLiteral(expression)) {
					return literal(LiteralValue.isLiteralNull(expression));
				} else {
					break;
				}
			}
			case BRANCH:
			case REVISION:
			case HISTORY_CONTEXT:
			case IDENTIFIER:
			case TYPE_NAME:
				break;

			default:
				throw new IllegalArgumentException("Not an unary operator: " + expr.getOperator());
		}
		return super.process(expr, expression, arg);
	}
	
	@Override
	protected Expression process(InSet expr, Expression testExpr, SetExpression set) {
		if (set instanceof None) {
			return literal(false);
		}
		if (testExpr instanceof Literal && set instanceof SetLiteral) {
			if (((SetLiteral) set).getValues().contains(((Literal) testExpr).getValue())) {
				return literal(true);
			} else {
				return literal(false);
			}
		}
		return super.process(expr, testExpr, set);
	}
	
	@Override
	protected SetExpression process(Intersection expr, SetExpression left, SetExpression right) {
		if (left instanceof None) {
			return left;
		}
		if (right instanceof None) {
			return right;
		}
		return super.process(expr, left, right);
	}
	
	@Override
	protected SetExpression process(Substraction expr, SetExpression left, SetExpression right) {
		if (left instanceof None) {
			return left;
		}
		if (right instanceof None) {
			return left;
		}
		return super.process(expr, left, right);
	}
	
	@Override
	protected SetExpression process(Union expr, SetExpression left, SetExpression right) {
		if (left instanceof None) {
			return right;
		}
		if (right instanceof None) {
			return left;
		}
		return super.process(expr, left, right);
	}
	
	@Override
	protected SetExpression process(CrossProduct expr, List<SetExpression> expressions) {
		for (SetExpression entryExpr : expressions) {
			if (entryExpr instanceof None) {
				return none();
			}
		}
		return super.process(expr, expressions);
	}
	
	@Override
	protected SetExpression process(Filter expr, SetExpression source, Expression filter) {
		if (source instanceof None) {
			return none();
		}
		if (filter instanceof Literal) {
			boolean value = (Boolean) ((Literal) filter).getValue();
			if (value) {
				return source;
			} else {
				return none();
			}
		}
		return super.process(expr, source, filter);
	}
	
	@Override
	protected SetExpression process(Partition expr, SetExpression source, Expression equivalence, Function representative) {
		if (source instanceof None) {
			return none();
		}
		return super.process(expr, source, equivalence, representative);
	}
	
	@Override
	protected SetExpression process(MapTo expr, SetExpression source, Expression mapping) {
		if (source instanceof None) {
			return none();
		}
		return super.process(expr, source, mapping);
	}
	
	@Override
	public SetExpression visitSetLiteral(SetLiteral expr, Void arg) {
		if (expr.getValues().isEmpty()) {
			return none();
		}
		return super.visitSetLiteral(expr, arg);
	}
	
	@Override
	protected Expression processHasType(HasType expr, Expression context, Void arg) {
		Set<? extends MetaObject> potentialContextTypes = expr.getContext().getConcreteTypes();
		if (potentialContextTypes != null && potentialContextTypes.size() < 2) {
			boolean value = potentialContextTypes.contains(expr.getDeclaredType());
			return literal(value);
		} else {
			return super.processHasType(expr, context, arg);
		}
	}
	
	@Override
	protected Expression processInstanceOf(InstanceOf expr, Expression context, Void arg) {
		Set<? extends MetaObject> potentialContextTypes = expr.getContext().getConcreteTypes();
		if (potentialContextTypes != null && potentialContextTypes.size() < 2) {
			if (potentialContextTypes.isEmpty()) {
				return literal(false);
			} else {
				boolean value = potentialContextTypes.iterator().next().isSubtypeOf(expr.getDeclaredType());
				return literal(value);
			}
		} else {
			return super.processInstanceOf(expr, context, arg);
		}
	}

}
