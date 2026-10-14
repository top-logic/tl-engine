/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.trace;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.GenericMethodWithSecurity;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;

/**
 * Test {@link GenericMethod} reading an attribute of the object referenced by the
 * {@link AbstractScriptTracerTest#OTHER} reference of its first argument.
 *
 * <p>
 * The second argument is the name of the attribute to read. Each read is done through
 * {@link TLObject#tValue(TLStructuredTypePart)}.
 * </p>
 *
 * @see TestScriptTracerJavaFunctions
 */
public class OtherValueMethod extends GenericMethodWithSecurity {

	/**
	 * The {@link ArgumentDescriptor} of an {@link OtherValueMethod}.
	 */
	static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
		.mandatory("self")
		.mandatory("attribute")
		.build();

	/**
	 * Creates an {@link OtherValueMethod}.
	 */
	protected OtherValueMethod(String name, SearchExpression[] arguments, boolean usesSecurity) {
		super(name, arguments, usesSecurity);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new OtherValueMethod(getName(), arguments, usesSecurity());
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		TLObject self = asTLObject(arguments[0]);
		if (self == null) {
			return null;
		}
		String attribute = asString(arguments[1]);
		TLObject other = (TLObject) self.tValue(self.tType().getPartOrFail(AbstractScriptTracerTest.OTHER));
		if (other == null) {
			return null;
		}
		return other.tValue(other.tType().getPartOrFail(attribute));
	}

	/**
	 * Builder for {@link OtherValueMethod}.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<OtherValueMethod> {

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
		public OtherValueMethod build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new OtherValueMethod(getName(), args, true);
		}
	}

}
