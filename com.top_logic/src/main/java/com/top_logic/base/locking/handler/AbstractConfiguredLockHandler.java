/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking.handler;

import com.top_logic.base.locking.I18NConstants;
import com.top_logic.base.locking.Lock;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.util.error.TopLogicException;

/**
 * Base class for configurable {@link LockHandler}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractConfiguredLockHandler<C extends PolymorphicConfiguration<?>>
		extends AbstractConfiguredInstance<C> implements LockHandler {

	private Lock _lock;

	/**
	 * Creates a {@link AbstractConfiguredLockHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractConfiguredLockHandler(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public boolean hasLock() {
		return _lock != null;
	}

	@Override
	public Lock getLock() {
		return _lock;
	}

	/**
	 * For overriding see {@link #createLock(Object)}.
	 */
	@Override
	public final void acquireLock(Object model) throws IllegalStateException, TopLogicException {
		if (hasLock() && _lock.isStateAcquired()) {
			throw new TopLogicException(I18NConstants.ERROR_ALREADY_LOCKED);
		}
		_lock = createLock(model);
		_lock.lock();
	}

	/** 
	 * Builds a {@link Lock} for the given model.
	 */
	protected abstract Lock createLock(Object model);

	@Override
	public void releaseLock() {
		if (hasLock()) {
			_lock.unlock();
			_lock = null;
		}
	}

	@Override
	public void updateLock() throws TopLogicException {
		if (!hasLock()) {
			// No lock, no update.
			return;
		}
		ensureAcquired();
		if (!_lock.renew()) {
			throw new TopLogicException(I18NConstants.ERROR_LOCK_NOT_ACQUIRED);
		}
	}

	private void ensureAcquired() {
		if (!_lock.isStateAcquired()) {
			throw new TopLogicException(I18NConstants.ERROR_LOCK_NOT_ACQUIRED);
		}
	}

}
