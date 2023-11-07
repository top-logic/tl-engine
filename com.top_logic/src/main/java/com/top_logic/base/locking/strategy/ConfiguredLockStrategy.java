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
		 * The identifier the lock aspect.
		 * 
		 * <p>
		 * Locks with different lock aspects for the same object are completely independent of each
		 * other. A lock is always requested on the combination of the object to be locked and the
		 * lock aspect. This means that a lock is not set on an object but on an object aspect (the
		 * combination of object and aspect).
		 * </p>
		 * 
		 * <p>
		 * A lock aspect may be an arbitrary identifier. Locks on different aspects can be acquired
		 * concurrently, while locks on the same aspect may be mutually exclusive depending on the
		 * lock kind.
		 * </p>
		 */
		@Name("aspect")
		@Mandatory
		String getAspect();

		/**
		 * The type of a lock.
		 * 
		 * <p>
		 * Only a single exclusive lock for the same object aspect can be acquired at once. It is
		 * possible to acquire several shared locks for the same object aspect. However, shared and
		 * exclusive locks are mutually exclusive.
		 * </p>
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
