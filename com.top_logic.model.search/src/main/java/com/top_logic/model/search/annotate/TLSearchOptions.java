/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.annotate;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.func.misc.AlwaysNull;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.ui.model.ValueContext;
import com.top_logic.model.search.ui.model.operator.Operator;
import com.top_logic.model.search.ui.model.structure.RightHandSide;

/**
 * Search-customization for {@link TLPrimitive} types.
 * 
 * @see #getOperators()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("search-options")
@TargetType({ TLTypeKind.BINARY, TLTypeKind.BOOLEAN, TLTypeKind.DATE, TLTypeKind.FLOAT, TLTypeKind.INT,
	TLTypeKind.STRING })
public interface TLSearchOptions extends TLTypeAnnotation {

	/**
	 * Factory for {@link Operator} options in the search UI for the annotated {@link TLPrimitive}
	 * type.
	 */
	@Mandatory
	@InstanceFormat
	@ImplementationClassDefault(ConfiguredOperators.class)
	Provider<List<Operator<?>>> getOperators();

	/**
	 * Factory for {@link RightHandSide value} options in the search UI for the annotated
	 * {@link TLPrimitive} type.
	 */
	@Mandatory
	@InstanceFormat
	@ImplementationClassDefault(ConfiguredLiterals.class)
	Provider<List<RightHandSide>> getLiterals();

	/**
	 * Default {@link TLSearchOptions#getOperators()} implementation.
	 */
	class ConfiguredOperators extends AbstractConfiguredInstance<ConfiguredOperators.Config>
			implements Provider<List<Operator<?>>> {
		public interface Config extends PolymorphicConfiguration<ConfiguredOperators>, DummyValueContext {
			@DefaultContainer
			List<Operator<?>> getOperators();
		}

		/**
		 * Creates a {@link ConfiguredOperators}.
		 */
		public ConfiguredOperators(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public List<Operator<?>> get() {
			ArrayList<Operator<?>> result = new ArrayList<>();
			for (Operator<?> x : getConfig().getOperators()) {
				result.add(TypedConfiguration.copy(x));
			}
			return result;
		}
	}

	/**
	 * Default {@link TLSearchOptions#getLiterals()} implementation.
	 */
	class ConfiguredLiterals extends AbstractConfiguredInstance<ConfiguredLiterals.Config>
			implements Provider<List<RightHandSide>> {

		public interface Config extends PolymorphicConfiguration<ConfiguredLiterals>, DummyValueContext {
			@DefaultContainer
			List<RightHandSide> getLiterals();
		}

		/**
		 * Creates a {@link ConfiguredLiterals}.
		 */
		public ConfiguredLiterals(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public List<RightHandSide> get() {
			ArrayList<RightHandSide> result = new ArrayList<>();
			for (RightHandSide item : getConfig().getLiterals()) {
				RightHandSide copy = TypedConfiguration.copy(item);
				result.add(copy);
			}
			return result;
		}
	}

	/**
	 * {@link ValueContext} implementation that allows configuring {@link SearchExpression}s inside
	 * model annotations.
	 */
	interface DummyValueContext extends ValueContext {
		@Override
		@BooleanDefault(false)
		boolean getValueMultiplicity();

		@Override
		@Derived(fun = AlwaysNull.class, args = {})
		public TLType getValueType();

	}
}
