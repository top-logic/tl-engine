/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.script;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.bpe.BPEUtil;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.execution.engine.ExecutionEngine;
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
 * TODO jhu add comment
 *
 * @author <a href="mailto:Jonathan.Hüsing@top-logic.com">Jonathan Hüsing</a>
 */
public class TransitionProcessInstance extends GenericMethod {

	protected TransitionProcessInstance(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new TransitionProcessInstance(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.BOOLEAN_TYPE); // Return boolean
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		if (arguments.length < 2 || arguments[0] == null || arguments[1] == null) {
			return false;
		}

		Token token = (Token) arguments[0];
		Node target = (Node) arguments[1];

		// Find path to target
		List<Edge> path = BPEUtil.getPossibleTransitions(token).getOrDefault(target, new ArrayList<>());
		if (path.isEmpty()) {
			return false;
		}

		ExecutionEngine.getInstance().execute(token, path, null);
		return true;
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating an {@link TransitionProcessInstance} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<TransitionProcessInstance> {

		/** Description of parameters for a {@link TransitionProcessInstance}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("currentToken")
			.mandatory("target")
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
		public TransitionProcessInstance build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new TransitionProcessInstance(getConfig().getName(), args);
		}

	}



}
