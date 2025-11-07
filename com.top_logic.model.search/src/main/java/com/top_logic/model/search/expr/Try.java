/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.exception.I18NException;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link SearchExpression} implementing try-catch functionality for exception handling.
 *
 * <p>
 * Executes a try function and provides fallback behavior through a catch function if an exception
 * occurs.
 * </p>
 *
 * @author <a href="mailto:jhu@top-logic.com">jhu</a>
 */
public class Try extends SearchExpression {

	private SearchExpression _tryBlock;

	private SearchExpression _catchBlock;

	/**
	 * Creates a new {@link Try}.
	 *
	 * @param tryBlock
	 *        The expression to execute in the try block.
	 * @param catchBlock
	 *        The optional expression to execute when an exception occurs, may be null.
	 */
	public Try(SearchExpression tryBlock, SearchExpression catchBlock) {
		_tryBlock = tryBlock;
		_catchBlock = catchBlock;
	}

	/**
	 * The {@link SearchExpression} being executed in the try block.
	 */
	public SearchExpression getTryBlock() {
		return _tryBlock;
	}

	/**
	 * @see #getTryBlock()
	 */
	public void setTryBlock(SearchExpression tryBlock) {
		_tryBlock = tryBlock;
	}

	/**
	 * The optional {@link SearchExpression} being executed when an exception occurs.
	 */
	public SearchExpression getCatchBlock() {
		return _catchBlock;
	}

	/**
	 * @see #getCatchBlock()
	 */
	public void setCatchBlock(SearchExpression catchBlock) {
		_catchBlock = catchBlock;
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitTry(this, arg);
	}

	@Override
	protected Object internalEval(EvalContext definitions, Args args) {
		try {
			// Execute the try block
			return _tryBlock.evalWith(definitions, Args.none());
		} catch (Exception e) {
			// If no catch function provided, return null
			if (_catchBlock == null) {
				return null;
			}
			
			// Determine the error message to pass to the catch block
			Object errorMessage;
			if (e instanceof I18NException) {
				errorMessage = ((I18NException) e).getErrorKey();
			} else {
				errorMessage = e.getMessage();
			}
			
			// Execute the catch block with the error message as argument
			return _catchBlock.evalWith(definitions, Args.some(errorMessage));
		}
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link Try} expressions.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<Try> {

		/**
		 * Description of parameters for a {@link Try}.
		 */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("eval")
			.optional("catch")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public Try build(Expr expr, SearchExpression[] args) {
			return new Try(args[0], args.length >= 2 ? args[1] : literal(null));
		}
	}

}