/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking.strategy;

import java.util.Collections;
import java.util.List;

import com.top_logic.base.locking.token.Token;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link LockStrategy} creating global locks.
 * 
 * <p>
 * A global lock is independent of the object for which it was acquired. A global lock refers only
 * to the lock aspect.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
@Label("Global locking")
public class GlobalLockStrategy<C extends GlobalLockStrategy.Config<?>> extends ConfiguredLockStrategy<C, Object> {

	/**
	 * Configuration options for {@link GlobalLockStrategy}.
	 */
	@TagName("global")
	public interface Config<I extends GlobalLockStrategy<?>> extends ConfiguredLockStrategy.Config<I> {
		// Pure marker interface.
	}

	/**
	 * Creates a {@link GlobalLockStrategy} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GlobalLockStrategy(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public List<Token> createTokens(Object model) {
		return Collections.singletonList(Token.newGlobalToken(getConfig().getKind(), getConfig().getAspect()));
	}

}
