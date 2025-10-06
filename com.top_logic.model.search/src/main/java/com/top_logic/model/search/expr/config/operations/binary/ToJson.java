/*
 * Copyright (c) 2024 by Top Logic, Inc. All rights reserved.
 */
package com.top_logic.model.search.expr.config.operations.binary;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.json.JSON;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link GenericMethod} converting a Map to a JSON string.
 * 
 * @author <a href="mailto:jhu@top-logic.com">Jonathan Huesing</a>
 */
public class ToJson extends GenericMethod {

	/**
	 * Creates a {@link ToJson}.
	 */
	protected ToJson(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ToJson(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.STRING_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object input = arguments[0];
		if (input == null) {
			return null;
		}
		
		try {
			return JSON.toString(input);
		} catch (Exception ex) {
			throw new TopLogicException(I18NConstants.ERROR_CONVERSION_FAILED__MSG_EXPR.fill(ex.getMessage(), this));
		}
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link ToJson} methods.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ToJson> {

		/**
		 * Configuration for the arguments of the <code>toJson</code> method.
		 */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("input")
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
		public ToJson build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new ToJson(getConfig().getName(), args);
		}
	}
}