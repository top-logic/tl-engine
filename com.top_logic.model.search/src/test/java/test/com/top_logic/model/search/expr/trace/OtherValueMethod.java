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
 * The second argument is the name of the attribute to read. Each read is done by
 * {@link #read(EvalContext, TLObject, TLStructuredTypePart)}.
 * </p>
 *
 * @see TestScriptTracerJavaFunctions
 */
public abstract class OtherValueMethod extends GenericMethodWithSecurity {

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
		TLObject other = (TLObject) read(definitions, self, self.tType().getPartOrFail(AbstractScriptTracerTest.OTHER));
		if (other == null) {
			return null;
		}
		return read(definitions, other, other.tType().getPartOrFail(attribute));
	}

	/**
	 * Reads the given attribute of the given object.
	 */
	protected abstract Object read(EvalContext definitions, TLObject self, TLStructuredTypePart part);

	/**
	 * {@link OtherValueMethod} reading through {@link EvalContext#access(TLObject, TLStructuredTypePart, boolean)}.
	 */
	public static class Traced extends OtherValueMethod {

		/**
		 * Creates a {@link Traced}.
		 */
		protected Traced(String name, SearchExpression[] arguments, boolean usesSecurity) {
			super(name, arguments, usesSecurity);
		}

		@Override
		public GenericMethod copy(SearchExpression[] arguments) {
			return new Traced(getName(), arguments, usesSecurity());
		}

		@Override
		protected Object read(EvalContext definitions, TLObject self, TLStructuredTypePart part) {
			return definitions.access(self, part, usesSecurity());
		}

		/**
		 * Builder for {@link Traced}.
		 */
		public static class Builder extends AbstractSimpleMethodBuilder<Traced> {

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
			public Traced build(Expr expr, SearchExpression[] args) throws ConfigurationException {
				return new Traced(getName(), args, true);
			}
		}
	}

	/**
	 * {@link OtherValueMethod} reading through {@link TLObject#tValue(TLStructuredTypePart)}.
	 */
	public static class Plain extends OtherValueMethod {

		/**
		 * Creates a {@link Plain}.
		 */
		protected Plain(String name, SearchExpression[] arguments, boolean usesSecurity) {
			super(name, arguments, usesSecurity);
		}

		@Override
		public GenericMethod copy(SearchExpression[] arguments) {
			return new Plain(getName(), arguments, usesSecurity());
		}

		@Override
		protected Object read(EvalContext definitions, TLObject self, TLStructuredTypePart part) {
			return self.tValue(part);
		}

		/**
		 * Builder for {@link Plain}.
		 */
		public static class Builder extends AbstractSimpleMethodBuilder<Plain> {

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
			public Plain build(Expr expr, SearchExpression[] args) throws ConfigurationException {
				return new Plain(getName(), args, true);
			}
		}
	}

}
