/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking.handler;

import java.util.List;

import com.top_logic.base.locking.Lock;
import com.top_logic.base.locking.LockService;
import com.top_logic.base.locking.service.ConfiguredLockService;
import com.top_logic.base.locking.service.ConfiguredLockService.Config.TypeConfig.StrategyContainerConfig;
import com.top_logic.base.locking.strategy.LockStrategy;
import com.top_logic.base.locking.token.Token;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.format.MillisFormat;

/**
 * {@link LockHandler} that uses locally configured {@link LockStrategy strategies}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfiguredLockHandler extends AbstractConfiguredLockHandler<ConfiguredLockHandler.Config<?>> {

	/**
	 * Configuration options for {@link ConfiguredLockHandler}.
	 */
	public interface Config<I extends ConfiguredLockHandler>
			extends PolymorphicConfiguration<I>, StrategyContainerConfig {

		/**
		 * Duration in milliseconds an acquired {@link Lock} keeps valid.
		 */
		@Name("lock-timeout")
		@Format(MillisFormat.class)
		Long getLockTimeout();

	}

	private LockStrategy<Object> _strategy;

	/**
	 * Creates a {@link ConfiguredLockHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConfiguredLockHandler(InstantiationContext context, Config<?> config) {
		super(context, config);
		_strategy = ConfiguredLockService.createStrategy(context, config.getStragegies());
	}

	@Override
	protected Lock createLock(Object model) {
		List<Token> tokens = _strategy.createTokens(model);
		return Lock.createLock(lockTimeout(), tokens);
	}

	private long lockTimeout() {
		Long customTimeout = getConfig().getLockTimeout();
		return customTimeout == null ? globalLockTimeout() : customTimeout.longValue();
	}

	private long globalLockTimeout() {
		return LockService.getInstance().getLockTimeout();
	}

}