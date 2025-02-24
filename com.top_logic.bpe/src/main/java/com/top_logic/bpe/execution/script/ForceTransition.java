/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.script;

import java.util.Date;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.bpe.BPEUtil;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.Task;
import com.top_logic.bpe.execution.model.ProcessExecution;
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
import com.top_logic.util.TLContext;

/**
 * TODO jhu add comment
 *
 * @author <a href="mailto:Jonathan.Hüsing@top-logic.com">Jonathan Hüsing</a>
 */
public class ForceTransition extends GenericMethod {

	protected ForceTransition(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ForceTransition(getName(), arguments);
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

		ProcessExecution processExecution = (ProcessExecution) arguments[0];
		Task targetTask = (Task) arguments[1];
		Node targetNode = targetTask;

		// create new Token
		Token currentToken = processExecution.getActiveTokens().stream().iterator().next();
		Token nextToken = BPEUtil.createFollowupToken(processExecution, targetNode, currentToken);

		// Complete current Token
		currentToken.setFinishDate(new Date());
		currentToken.setFinishBy(TLContext.currentUser());
		processExecution.removeActiveToken(currentToken);

		// add new Token to activeToken
		processExecution.addActiveToken(nextToken);

		return true;
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating an {@link ForceTransition} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ForceTransition> {

		/** Description of parameters for a {@link ForceTransition}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("processInstance")
			.mandatory("node")
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
		public ForceTransition build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new ForceTransition(getConfig().getName(), args);
		}

	}

}
