/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.Argument;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.dom.Expr.AbstractMethod;

/**
 * Plugin into the search expression evaluation engine that translates {@link AbstractMethod}
 * {@link Expr}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface MethodBuilder<E extends SearchExpression> {

	/**
	 * A {@link MethodBuilder} that implements the interpretation of a pre-defined method in
	 * TL-Script.
	 */
	public interface Config<I extends MethodBuilder<?>> extends NamedConfigMandatory, PolymorphicConfiguration<I> {
		// Pure base config.
	}

	/**
	 * Builds the implementation of the given {@link AbstractMethod}.
	 *
	 * @param expr
	 *        The {@link Expr source expression} to translate (for error reporting only). If
	 *        <code>null</code>, potential errors are are unusable.
	 * @param self
	 *        The translated target of the method call.
	 * @param args
	 *        The translated arguments to the method call.
	 * @return The resulting {@link SearchExpression} for evaluation.
	 * @throws ConfigurationException
	 *         If the expression has errors.
	 */
	default E build(AbstractMethod expr, SearchExpression self, Argument[] args) throws ConfigurationException {
		return build(expr, self, descriptor().unwrap(expr.getName(), args));
	}

	/**
	 * Description of arguments of this method.
	 * 
	 * <p>
	 * When overriding this method, return a constant descriptor instance. This constant can be
	 * build using the utility {@link ArgumentDescriptor#builder()}.
	 * </p>
	 */
	default ArgumentDescriptor descriptor() {
		return ArgumentDescriptorImpl.ANY;
	}

	/**
	 * Builds the implementation of the given {@link AbstractMethod} with only positional arguments.
	 *
	 * @param expr
	 *        The {@link Expr source expression} to translate (for error reporting only). If
	 *        <code>null</code>, potential errors are are unusable.
	 * @param self
	 *        The translated target of the method call.
	 * @param args
	 *        The translated arguments to the method call.
	 * @return The resulting {@link SearchExpression} for evaluation.
	 * @throws ConfigurationException
	 *         If the expression has errors.
	 * 
	 * @see #build(AbstractMethod, SearchExpression, Argument[])
	 */
	E build(Expr expr, SearchExpression self, SearchExpression[] args) throws ConfigurationException;

	/**
	 * The "self" argument of a function is a special unnamed argument that represents the "main"
	 * input to the function. A function with a self argument is normally written as
	 * <code>$input.fun($opt1, ...., $optn)</code>, where <code>$input</code> represents the "self"
	 * argument.
	 * 
	 * <p>
	 * If this methd returns <code>true</code>, the <code>self</code> argument of
	 * {@link #build(Expr, SearchExpression, SearchExpression[])} is filled.
	 * </p>
	 */
	default boolean hasSelf() {
		return true;
	}

	/**
	 * The custom identifier for this builder that triggers its usage when instantiating generic
	 * expression trees from transformation.
	 * 
	 * @return A custom identifier or <code>null</code> which means that the concrete implementation
	 *         class of the built {@link SearchExpression} is used as identifier.
	 * 
	 * @see SearchExpression#getId()
	 */
	default Object getId() {
		return null;
	}
}
