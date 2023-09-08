/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking.token;

import java.util.Collection;
import java.util.Date;

import com.top_logic.base.locking.Lock;
import com.top_logic.base.locking.LockService;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;

/**
 * Service for handling the actual locking aspect of {@link Lock}s.
 * 
 * @see LockService
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TokenService extends ManagedClass {

	/**
	 * Creates a {@link TokenService} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TokenService(InstantiationContext context, ServiceConfiguration<?> config) {
		super(context, config);
	}

	/**
	 * Locks the given set of {@link Token}s.
	 *
	 * @param expireDate
	 *        The timeout after which the given {@link Token}s will become invalid automatically.
	 * @param tokens
	 *        The {@link Token}s to acquire.
	 * @throws TopLogicException
	 *         If a conflict occurrs.
	 */
	public abstract void acquire(Date expireDate, Collection<Token> tokens) throws TopLogicException;

	/**
	 * Checks, whether the given set of {@link Token} are still valid.
	 *
	 * @param tokens
	 *        The {@link Token}s to check.
	 * @return Whether all given {@link Token}s are still acquired.
	 */
	public abstract boolean allValid(Collection<Token> tokens);

	/**
	 * Updates the timeout of the given set of {@link Token}s to the given date.
	 *
	 * @param expireDate
	 *        The new expire date.
	 * @param tokens
	 *        The {@link Token}s to upate.
	 * @return Whether the update succeeded.
	 */
	public abstract boolean renew(Date expireDate, Collection<Token> tokens);

	/**
	 * Releases all given {@link Token}s.
	 *
	 * @param tokens
	 *        The {@link Token}s to release.
	 */
	public abstract void release(Collection<Token> tokens);

	/**
	 * Retrieves information about all currently acquired locks.
	 */
	public abstract Collection<LockInfo> getAllLocks();

	/**
	 * Creates a lock conflict error with lock owner details.
	 */
	protected TopLogicException createDetailedLockConflictError(TLObject owner, long timeout, Long nodeId) {
		return new TopLogicException(
			I18NConstants.ERROR_LOCK_CONFLICT__OWNER_TIMEOUT_NODE.fill(owner, new Date(timeout), nodeId));
	}

	/**
	 * Creates a lock conflict error without lock owner details.
	 */
	protected TopLogicException createGenericLockConflictError() {
		return new TopLogicException(I18NConstants.ERROR_LOCK_CONFLICT);
	}

	/**
	 * The {@link TokenService} singleton.
	 */
	public static TokenService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton reference to the configured {@link TokenService}.
	 */
	public static final class Module extends TypedRuntimeModule<TokenService> {

		/**
		 * Singleton {@link TokenService.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<TokenService> getImplementation() {
			return TokenService.class;
		}

	}

}
