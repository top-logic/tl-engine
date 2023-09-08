/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking;

import com.top_logic.base.locking.strategy.LockStrategy;
import com.top_logic.base.locking.token.TokenService;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.util.error.TopLogicException;

/**
 * Service for creating {@link Lock}s based on {@link LockStrategy strategies}.
 * 
 * @see #createLock(String, Object...)
 * @see TokenService
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class LockService extends ManagedClass {

	/**
	 * Creates a {@link LockService} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public LockService(InstantiationContext context, ServiceConfiguration<?> config) {
		super(context, config);
	}

	/**
	 * The duration in milliseconds until an acquired lock expires.
	 * 
	 * @see com.top_logic.base.locking.service.ConfiguredLockService.Config#getLockTimeout()
	 */
	public abstract long getLockTimeout();

	/**
	 * Creates a new (not yet acquired) lock for the given models.
	 * 
	 * <p>
	 * The resulting {@link Lock} must be acquired using the {@link Lock#lock()} operation before it
	 * becomes {@link Lock#isStateAcquired() valid}.
	 * </p>
	 *
	 * @param operation
	 *        The abstract operation to perform on the given model.
	 * @param models
	 *        The objects to lock.
	 * @return A not yet acquired {@link Lock} for the given operation on the given model.
	 */
	public abstract Lock createLock(String operation, Object... models);

	/**
	 * Acquires a lock for the given model.
	 * 
	 * <p>
	 * This is a short-cut for {@link #createLock(String, Object...)} and {@link Lock#lock()}.
	 * </p>
	 *
	 * @param operation
	 *        The abstract operation to perform on the given model.
	 * @param model
	 *        The object to lock.
	 * @return A valid {@link Lock} for the given operation on the given model.
	 * 
	 * @throws TopLogicException
	 *         If lock acquisition fails.
	 */
	public Lock acquireLock(String operation, Object... model) throws TopLogicException {
		Lock result = createLock(operation, model);
		result.lock();
		return result;
	}

	/**
	 * The configured {@link LockService}.
	 */
	public static LockService getInstance() {
		return LockService.Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton reference to the configured {@link LockService}.
	 */
	public static final class Module extends TypedRuntimeModule<LockService> {

		/**
		 * Singleton {@link LockService.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<LockService> getImplementation() {
			return LockService.class;
		}

	}

}
