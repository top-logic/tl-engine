/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.string;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.element.structured.util.GenerateNumberException;
import com.top_logic.element.structured.util.NumberHandler;
import com.top_logic.element.structured.util.NumberHandlerFactory;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link GenericMethod} generating a Sequence Id by giving the specified context to the specified
 * {@link NumberHandler}
 * 
 * 
 * @author <a href="mailto:jhu@top-logic.com">Jonathan Hüsing</a>
 */
public class GenerateSequenceId extends GenericMethod {

	/**
	 * Creates a {@link GenerateSequenceId} expression.
	 */
	protected GenerateSequenceId(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new GenerateSequenceId(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.STRING_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		// extract the context from the arguments for generating the Sequence Id
		Object contextArg = arguments[0];

		// if the context is null do not continue with the evaluation
		if (contextArg == null) {
			return null;
		}

		// extract the numberHandlerName from the arguments
		String numberHandlerNameArg = asString(arguments[1]);

		// if no numberHandlerName was entered do not continue with the evaluation
		if (numberHandlerNameArg == "") {
			return null;
		}

		// find the corresponding numberHandler identified by the name
		NumberHandlerFactory numberHandlerFactory = NumberHandlerFactory.getInstance();
		NumberHandler wantedNumberHandler = numberHandlerFactory.getNumberHandler(numberHandlerNameArg);

		// return the generated Id from the NumberHandler using the extracted contextArg
		try {
			return asString(wantedNumberHandler.generateId(contextArg));
		} catch (GenerateNumberException ex) {
			return null;
		}
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating an {@link GenerateSequenceId} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<GenerateSequenceId> {

		/** Description of parameters for a {@link GenerateSequenceId}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("context")
			.mandatory("numberHandlerName")
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
		public GenerateSequenceId build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new GenerateSequenceId(getConfig().getName(), args);
		}

	}

}
