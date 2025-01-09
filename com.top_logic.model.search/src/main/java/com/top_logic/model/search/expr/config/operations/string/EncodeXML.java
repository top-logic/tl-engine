/*
 * Copyright (c) 2025 Business Operation Systems GmbH. All Rights Reserved
 */
package com.top_logic.model.search.expr.config.operations.string;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagUtil;
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
 * A {@link GenericMethod} implementation that provides XML encoding functionality for strings. This
 * Function is responsible for safely encoding strings to prevent XML injection and ensure proper
 * XML formatting.
 * 
 * @author <a href="mailto:Jonathan.Hüsing@top-logic.com">Jonathan Hüsing</a>
 */
public class EncodeXML extends GenericMethod {

	/**
	 * Creates a new {@link EncodeXML} expression with the specified name and arguments.
	 */
	protected EncodeXML(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new EncodeXML(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.STRING_TYPE);
	}

	/**
	 * Evaluates the XML encoding operation on the provided arguments. Converts the first argument
	 * to a string and applies XML encoding using TagUtil.
	 *
	 * @param arguments
	 *        The array of arguments to evaluate, where arguments[0] is the string to encode
	 * @param definitions
	 *        The evaluation context
	 * @return The XML-encoded string, with special characters (like < > & etc.) converted to their
	 *         XML entities
	 * @see TagUtil#encodeXML
	 */
	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		return TagUtil.encodeXML(asString(arguments[0]));
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating an {@link EncodeXML} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<EncodeXML> {

		/** Description of parameters for a {@link EncodeXML}. */
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
		public EncodeXML build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new EncodeXML(getConfig().getName(), args);
		}

	}
}
