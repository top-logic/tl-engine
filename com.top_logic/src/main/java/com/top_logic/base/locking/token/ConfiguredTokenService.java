/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking.token;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;

/**
 * Base class for {@link TokenService} implementations.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ConfiguredTokenService<C extends ConfiguredTokenService.Config<?>> extends TokenService
		implements ConfiguredInstance<C> {

	private C _config;

	/**
	 * Configuration options for {@link ConfiguredTokenService}.
	 */
	public interface Config<I extends ConfiguredTokenService<?>> extends ServiceConfiguration<I> {
		/**
		 * Whether to report details about the lock owner in case of a lock conflict.
		 */
		@Name("report-lock-owner-details")
		boolean getReportLockOwnerDetails();
	}

	/**
	 * Creates a {@link ConfiguredTokenService} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConfiguredTokenService(InstantiationContext context, C config) {
		super(context, config);
		_config = config;
	}

	@Override
	public C getConfig() {
		return _config;
	}

	@Override
	protected TopLogicException createDetailedLockConflictError(TLObject owner, long timeout, Long nodeId) {
		if (!getConfig().getReportLockOwnerDetails()) {
			// Ignore details.
			return createGenericLockConflictError();
		}
		return super.createDetailedLockConflictError(owner, timeout, nodeId);
	}

}
