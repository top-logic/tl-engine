/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.script;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.bpe.BPEUtil;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.util.TLModelUtil;

/**
 * A {@link GenericMethod} implementation that retrieves all reachable {@link Node}s from a given
 * {@link Token}.
 *
 * @author <a href="mailto:Jonathan.Hüsing@top-logic.com">Jonathan Hüsing</a>
 */
public class ReachableNodes extends GenericMethod {

	/**
	 * Creates a new {@link ReachableNodes}.
	 */
	protected ReachableNodes(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ReachableNodes(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.OBJECT_TYPE);
	}

	/**
	 * Retrieves all {@link Node}s that can be reached from the given {@link Token}.
	 * 
	 * @param arguments
	 *        args[0]: source token from which to find reachable nodes
	 * @param definitions
	 *        evaluation context
	 * @return set of reachable nodes, false if token is invalid
	 */
	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		if (arguments.length < 1 || arguments[0] == null) {
			return false;
		}
		Token currentToken = (Token) arguments[0];
		return BPEUtil.getPossibleTransitions(currentToken).keySet();
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating an {@link ReachableNodes} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ReachableNodes> {

		/** Description of parameters for a {@link ReachableNodes}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("currentToken")
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
		public ReachableNodes build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new ReachableNodes(getConfig().getName(), args);
		}

	}

}