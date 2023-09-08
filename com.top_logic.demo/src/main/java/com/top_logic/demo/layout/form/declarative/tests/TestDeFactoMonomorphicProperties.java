/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.tests;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;

/**
 * Test case for editing properties that are defined polymorphic but are de-facto monomorphic, since
 * there is only a single implementation option.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TestDeFactoMonomorphicProperties extends ConfigurationItem {

	/**
	 * Some abstract base configuration.
	 */
	@Abstract
	interface A extends ConfigurationItem {
		int getX();
	}

	/**
	 * The single concrete (instantiable) configuration that is {@link A}-like.
	 */
	interface B extends A {
		String getY();
	}

	/**
	 * Optional {@link A}-like configuration.
	 */
	A getAOptional();

	/**
	 * Mandatory {@link A}-like configuration.
	 */
	@Mandatory
	A getAMandatory();

	/**
	 * List of {@link A}-like configurations.
	 */
	List<A> getAs();

	/**
	 * Functional interface of some configured instance.
	 */
	interface F {
		String foo();
	}

	/**
	 * Single implementation of {@link F}.
	 */
	class Impl<C extends Impl.Config<?>> extends AbstractConfiguredInstance<C> implements F {

		/**
		 * Configuration options for {@link TestDeFactoMonomorphicProperties.Impl}.
		 */
		public interface Config<I extends TestDeFactoMonomorphicProperties.Impl<?>>
				extends PolymorphicConfiguration<I> {
			String getMessage();
		}

		/**
		 * Creates a {@link TestDeFactoMonomorphicProperties.Impl} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public Impl(InstantiationContext context, C config) {
			super(context, config);
		}

		@Override
		public String foo() {
			return getConfig().getMessage();
		}

	}

	/**
	 * Optional configuration of implementation of {@link F}.
	 */
	PolymorphicConfiguration<F> getIOptional();

	/**
	 * Mandatory configuration of implementation of {@link F}.
	 */
	@Mandatory
	PolymorphicConfiguration<F> getIMandatory();

	/**
	 * List of implementations of {@link F}.
	 */
	List<PolymorphicConfiguration<F>> getIs();

	/**
	 * Optional direct instance of {@link F}.
	 */
	@InstanceFormat
	F getIInstanceOptional();

	/**
	 * Mandatory direct instance of {@link F}.
	 */
	@InstanceFormat
	@Mandatory
	F getIInstanceMandatory();

	/**
	 * List of direct instances of {@link F}.
	 */
	@InstanceFormat
	List<F> getIInstances();
}
