/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Factory creating {@link RequestLock} instances during user logins / subsession creations.
 */
public abstract class RequestLockFactory extends ManagedClass {

	/**
	 * Creates a {@link RequestLockFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RequestLockFactory(InstantiationContext context, ServiceConfiguration<RequestLockFactory> config) {
		super(context, config);
	}

	/**
	 * Creates a new {@link RequestLock}.
	 */
	public abstract RequestLock createLock();

	/**
	 * Access to the global {@link RequestLockFactory} instance.
	 */
	public static RequestLockFactory getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton reference to {@link RequestLockFactory}.
	 */
	public static final class Module extends TypedRuntimeModule<RequestLockFactory> {

		/**
		 * Singleton {@link RequestLockFactory.Module} instance.
		 */
		public static final RequestLockFactory.Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<RequestLockFactory> getImplementation() {
			return RequestLockFactory.class;
		}

	}

}