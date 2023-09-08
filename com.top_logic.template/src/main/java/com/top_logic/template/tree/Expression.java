/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.tree;

import com.top_logic.template.model.VarSymbol;

/**
 * A {@link TemplateNode} representing an expression.
 * 
 * @see BinaryExpression
 * @see UnaryExpression
 * @see ConstantExpression (A literal within an {@link Expression}.)
 * @see Reference
 * @see FunctionCall
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public abstract class Expression extends TemplateNode {

	/**
	 * Represents an operator in the template language.
	 * <p>
	 * Currently, there are only boolean operators.
	 * </p>
	 */
	public enum Operator {
		/** ==  */
		EQ,
		
		/**	!= */
		NE,
		
		/**	>= */
		GE,
		
		/** <= */
		LE,
		
		/** > */
		GT,
		
		/** < */
		LT,
	
		/** && */
		AND,
		
		/** || */
		OR,
		
		/** ! */
		NOT
	}

	private VarSymbol vs;
	
	/**
	 * Initializes the {@link VarSymbol} representing the result of this expression.
	 * <p>
	 * Cannot be set if it was already set and is not <code>null</code> anymore. <br/>
	 * An assertion will fail otherwise.
	 * </p>
	 */
	public void setVarSymbol(VarSymbol vs) {
		assert this.vs == null : "Symbol may be initialized only once.";

		this.vs = vs;
	}
	
	/**
	 * Getter for the {@link VarSymbol} representing the result of this expression.
	 * <p>
	 * Might be <code>null</code>.
	 * </p>
	 */
	public VarSymbol getVarSymbol() {
		return this.vs;
	}

	/**
	 * Parse the {@link Operator} out of the given {@link String}.
	 */
	protected Operator parseOperator(String operator) {
		if ("==".equals(operator)) {
			return Operator.EQ;
		}
		else if("!=".equals(operator)) {
			return Operator.NE;
		}
		else if(">=".equals(operator)) {
			return Operator.GE;
		}
		else if("<=".equals(operator)) {
			return Operator.LE;
		}
		else if(">".equals(operator)) {
			return Operator.GT;
		}
		else if("<".equals(operator)) {
			return Operator.LT;
		}
		else if("&&".equals(operator)) {
			return Operator.AND;
		}
		else if("||".equals(operator)) {
			return Operator.OR;
		}
		else if("!".equals(operator)) {
			return Operator.NOT;
		}
		else {
			throw new IllegalArgumentException("Operator '" + operator + "' is not supported");
		}
	}
	
}