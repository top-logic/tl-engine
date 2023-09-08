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
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLObject;

/**
 * {@link LockStrategy} creating a single token identifying the given {@link TLObject}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LocalLockStrategy<C extends LocalLockStrategy.Config<?>> extends ConfiguredLockStrategy<C, TLObject> {

	/**
	 * Configuration options for {@link LocalLockStrategy}.
	 */
	@TagName("local")
	public interface Config<I extends LocalLockStrategy<?>> extends ConfiguredLockStrategy.Config<I> {
		// Pure marker interface.
	}

	/**
	 * Creates a {@link LocalLockStrategy} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public LocalLockStrategy(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public List<Token> createTokens(TLObject model) {
		return Collections
			.singletonList(Token.newToken(getConfig().getKind(), model, getConfig().getAspect()));
	}

}
