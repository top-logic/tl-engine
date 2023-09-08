/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking.handler;

import com.top_logic.base.locking.Lock;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link LockHandler} that uses the {@link LockHandler} of another referenced component.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SharedLockHandler extends AbstractConfiguredInstance<SharedLockHandler.Config<?>> implements LockHandler {

	/**
	 * Configuration options for {@link SharedLockHandler}.
	 */
	public interface Config<I extends SharedLockHandler> extends PolymorphicConfiguration<I> {
		/**
		 * Name of the component whose {@link LockHandler} should be used.
		 * 
		 * <p>
		 * This component is required to be an {@link EditComponent}, since only those have
		 * {@link LockHandler}s.
		 * </p>
		 */
		@Name("lockComponent")
		@Mandatory
		ComponentName getLockComponent();

		/**
		 * The {@link LockHandler} to use, if no lock was acquired in {@link #getLockComponent()}.
		 * 
		 * <p>
		 * If not given, the {@link #getLockComponent()}'s handler is used even for acquiring locks.
		 * </p>
		 */
		@Name("defaultHandler")
		@ImplementationClassDefault(DefaultLockHandler.class)
		PolymorphicConfiguration<LockHandler> getDefaultHandler();
	}

	private EditComponent _lockComponent;

	private LockHandler _defaultHandler;

	/**
	 * Creates a {@link SharedLockHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SharedLockHandler(InstantiationContext context, Config<?> config) {
		super(context, config);

		_defaultHandler = context.getInstance(config.getDefaultHandler());

		context.resolveReference(config.getLockComponent(), LayoutComponent.class,
			c -> _lockComponent = (EditComponent) c);
	}

	@Override
	public boolean hasLock() {
		return proxy().hasLock() || defaultHandler().hasLock();
	}

	@Override
	public Lock getLock() {
		return proxy().hasLock() ? proxy().getLock() : defaultHandler().getLock();
	}

	@Override
	public void acquireLock(Object model) throws IllegalStateException, TopLogicException {
		if (!proxy().hasLock()) {
			defaultHandler().getLock();
		}
	}

	@Override
	public void releaseLock() {
		if (!proxy().hasLock()) {
			defaultHandler().releaseLock();
		}
	}

	@Override
	public void updateLock() throws TopLogicException {
		if (proxy().hasLock()) {
			proxy().updateLock();
		} else {
			defaultHandler().updateLock();
		}
	}

	private LockHandler defaultHandler() {
		return _defaultHandler == null ? proxy() : _defaultHandler;
	}

	private LockHandler proxy() {
		return _lockComponent.getLockHandler();
	}

}
