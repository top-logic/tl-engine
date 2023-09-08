/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking.strategy;

import com.top_logic.base.locking.token.Token.Kind;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * Base class for {@link LockStrategy} implementations.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ConfiguredLockStrategy<C extends ConfiguredLockStrategy.Config<?>, M>
		extends AbstractConfiguredInstance<C> implements LockStrategy<M> {

	/**
	 * Configuration options for {@link ConfiguredLockStrategy}.
	 */
	public interface Config<I extends ConfiguredLockStrategy<?, ?>> extends LockStrategy.Config<I> {

		/**
		 * The identifier the lock aspect the token represents.
		 */
		@Name("aspect")
		@Mandatory
		String getAspect();

		/**
		 * The token {@link Kind} to produce.
		 */
		@Name("kind")
		Kind getKind();

	}

	/**
	 * Creates a {@link ConfiguredLockStrategy} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConfiguredLockStrategy(InstantiationContext context, C config) {
		super(context, config);
	}

}
