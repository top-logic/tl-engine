/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.base.accesscontrol;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Service module tracking failed login attempts to enforce exponential back-off delays.
 *
 * <p>
 * For each lookup key (typically a combination of user name and client IP address), this module
 * keeps track of consecutive failed login attempts. Using a per-IP key ensures that a
 * denial-of-service attack from one IP address cannot lock out a legitimate user connecting from a
 * different IP. After a configurable number of direct retries, subsequent attempts are delayed
 * exponentially up to a maximum wait time. Outdated failure entries are cleaned up automatically to
 * prevent memory exhaustion caused by flooding with random user names.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LoginFailuresModule<C extends LoginFailuresModule.Config<?>> extends ConfiguredManagedClass<C> {

	/**
	 * Configuration for {@link LoginFailuresModule}.
	 */
	public interface Config<I extends LoginFailuresModule<?>> extends ConfiguredManagedClass.Config<I> {
		// configuration interface definition
	}

	private ConcurrentMap<String, LoginFailure> _failures = new ConcurrentHashMap<>();

	/**
	 * Creates a new {@link LoginFailuresModule}.
	 */
	public LoginFailuresModule(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected void shutDown() {
		_failures.clear();
		super.shutDown();
	}

	/**
	 * Returns the current {@link LoginFailure} record for the given key, or {@code null} if no
	 * failure has been recorded.
	 *
	 * @param key
	 *        The lookup key, typically a combination of user name and client IP address.
	 * @return The failure record, or {@code null}.
	 */
	public LoginFailure getFailureFor(String key) {
		return _failures.get(key);
	}

	/**
	 * Records a failed login attempt for the given key and returns the updated failure record.
	 *
	 * <p>
	 * If an existing failure record has timed out, it is removed and a fresh one is created. After
	 * incrementing the failure counter, outdated entries for other keys are purged to prevent
	 * denial-of-service attacks that flood the server with random user names.
	 * </p>
	 *
	 * @param userKey
	 *        The lookup key, typically a combination of user name and client IP address, that failed to log in.
	 * @return The updated {@link LoginFailure} record for the given key.
	 */
	public LoginFailure notifyLoginFailed(String userKey) {
		LoginFailure failure = _failures.get(userKey);
		if (failure != null && failure.timedOut()) {
			_failures.remove(userKey, failure);
			failure = null;
		}
		if (failure == null) {
			failure = new LoginFailure(userKey);
			failure = MapUtil.putIfAbsent(_failures, userKey, failure);
		}
		failure.incFailures();

		// Clean up outdated entries to prevent denial of service attacks by flooding
		// the server with nonsense authentication failures for random user names.
		for (Iterator<LoginFailure> it = _failures.values().iterator(); it.hasNext();) {
			if (it.next().timedOut()) {
				it.remove();
			}
		}
		return failure;
	}

	/**
	 * Clears the failure record for the given key after a successful login.
	 *
	 * @param userKey
	 *        The lookup key, typically a combination of user name and client IP address, that logged in successfully.
	 */
	public void notifyLoginSuccessed(String userKey) {
		_failures.remove(userKey);
	}

	/**
	 * {@link TypedRuntimeModule} for {@link LoginFailuresModule}.
	 */
	public static final class Module extends TypedRuntimeModule<LoginFailuresModule<?>> {

		/** Singleton {@link Module}. */
		public static final Module INSTANCE = new Module();

		@SuppressWarnings("unchecked")
		@Override
		public Class<LoginFailuresModule<?>> getImplementation() {
			@SuppressWarnings("rawtypes")
			Class class1 = LoginFailuresModule.class;
			return class1;
		}
	}

}

