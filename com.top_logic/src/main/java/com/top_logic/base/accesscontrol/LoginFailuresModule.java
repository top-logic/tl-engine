/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.base.accesscontrol;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.ServletRequest;

import com.top_logic.base.security.util.Password;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.category.AuthenticationCategory;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.util.ResKey1;

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
 * @implNote A check guarded by this module runs through
 *           {@link #attempt(String, ResKey1, Attempt)} (or its two steps
 *           {@link #checkRetry(String, ResKey1)} and {@link #recordAttempt(String, boolean)}), which
 *           refuses the attempt while its key is backing off and records its outcome otherwise. The
 *           keys of the checks are built by {@link #accountKey(String, ServletRequest)} (a password
 *           of an account) and {@link #secretKey(Password)} (a one-time code of a second-factor
 *           secret), so that all checks of the same credential count together.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceDependencies(SchedulerService.Module.class)
@ServiceCategory.Of(AuthenticationCategory.class)
public class LoginFailuresModule<C extends LoginFailuresModule.Config<?>> extends ConfiguredManagedClass<C> {

	/**
	 * Configuration for {@link LoginFailuresModule}.
	 */
	public interface Config<I extends LoginFailuresModule<?>> extends ConfiguredManagedClass.Config<I> {
		// configuration interface definition
	}

	/**
	 * Separates the account name from the client address in an {@link #accountKey(String, ServletRequest)}.
	 */
	private static final char ADDRESS_SEPARATOR = '@';

	/**
	 * Suffix marking a {@link #secretKey(Password)}, keeping it apart from every
	 * {@link #accountKey(String, ServletRequest)}.
	 */
	private static final String SECRET_KEY_SUFFIX = "@OTP";

	/**
	 * A check of a credential guarded by {@link LoginFailuresModule#attempt(String, ResKey1, Attempt)}.
	 *
	 * @param <E>
	 *        The exception the check may throw. A throwing check counts neither as a failure nor as
	 *        a success.
	 */
	@FunctionalInterface
	public interface Attempt<E extends Throwable> {

		/**
		 * Checks the credential.
		 *
		 * @return Whether the credential was accepted.
		 */
		boolean run() throws E;
	}

	private final ConcurrentMap<String, LoginFailure> _failures = new ConcurrentHashMap<>();

	private final SchedulerService _scheduler;

	private ScheduledFuture<?> _cleanupTask;

	/**
	 * Creates a new {@link LoginFailuresModule}.
	 */
	public LoginFailuresModule(InstantiationContext context, C config) {
		super(context, config);
		_scheduler = SchedulerService.getInstance();
	}

	/**
	 * @see com.top_logic.basic.module.ManagedClass#startUp()
	 */
	@Override
	protected void startUp() {
		super.startUp();
		_cleanupTask = _scheduler.scheduleAtFixedRate(this::cleanupTimedOut, 1, 1, TimeUnit.HOURS);
	}

	@Override
	protected void shutDown() {
		_cleanupTask.cancel(false);
		_cleanupTask = null;
		_failures.clear();
		super.shutDown();
	}

	/**
	 * The running {@link LoginFailuresModule}.
	 */
	public static LoginFailuresModule<?> getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * The key under which the password checks of an account are counted.
	 *
	 * <p>
	 * The failures are counted per account and client address, so that guessing from one address
	 * does not lock out the account holder working from another one. Where no request is at hand,
	 * the key is the account name alone.
	 * </p>
	 *
	 * @param accountName
	 *        The name of the account whose password is checked. Letter case is ignored.
	 * @param request
	 *        The request bringing the password, or {@code null} if there is none.
	 */
	public static String accountKey(String accountName, ServletRequest request) {
		String name = accountName.toLowerCase();
		String address = request == null ? null : request.getRemoteAddr();
		if (address == null) {
			return name;
		}
		return name + ADDRESS_SEPARATOR + address;
	}

	/**
	 * The key under which the one-time codes of a second-factor secret are counted.
	 *
	 * @param secret
	 *        The secret the codes are checked against.
	 */
	public static String secretKey(Password secret) {
		return secret.getCryptedValue() + SECRET_KEY_SUFFIX;
	}

	/**
	 * Runs a check of a credential under the back-off of the given key.
	 *
	 * <p>
	 * While the key is backing off, the check is not run and the attempt is refused. Otherwise, the
	 * outcome of the check is {@link #recordAttempt(String, boolean) recorded}. A check that throws
	 * records nothing.
	 * </p>
	 *
	 * <p>
	 * A caller whose check throws several checked exceptions it handles one by one makes the two
	 * steps itself: {@link #checkRetry(String, ResKey1)} before the check,
	 * {@link #recordAttempt(String, boolean)} after it.
	 * </p>
	 *
	 * @param key
	 *        The key the attempt is counted under, see {@link #accountKey(String, ServletRequest)}
	 *        and {@link #secretKey(Password)}.
	 * @param tooManyAttempts
	 *        The message of the refusal, filled with the seconds to wait before the next attempt.
	 * @param attempt
	 *        The check of the credential.
	 * @return Whether the check accepted the credential.
	 * @throws RetryDeniedException
	 *         If the key is backing off and the check was not run.
	 * @throws E
	 *         What the check throws.
	 */
	public <E extends Throwable> boolean attempt(String key, ResKey1 tooManyAttempts, Attempt<E> attempt)
			throws E, RetryDeniedException {
		checkRetry(key, tooManyAttempts);
		return recordAttempt(key, attempt.run());
	}

	/**
	 * Refuses an attempt under the given key while the key is backing off.
	 *
	 * @param key
	 *        The key the attempt is counted under.
	 * @param tooManyAttempts
	 *        The message of the refusal, filled with the seconds to wait before the next attempt.
	 * @throws RetryDeniedException
	 *         If the key is backing off; the credential must not be checked then.
	 */
	public void checkRetry(String key, ResKey1 tooManyAttempts) throws RetryDeniedException {
		LoginFailure failure = getFailureFor(key);
		if (failure != null && !failure.allowRetry()) {
			long retryTimeout = failure.retryTimeout();
			throw new RetryDeniedException(tooManyAttempts.fill(TimeUnit.MILLISECONDS.toSeconds(retryTimeout)),
				retryTimeout);
		}
	}

	/**
	 * Records the outcome of a check of a credential: a rejected credential is
	 * {@link #notifyLoginFailed(String) recorded as failure}, an accepted one
	 * {@link #notifyLoginSuccessed(String) clears} the failures of the key.
	 *
	 * @param key
	 *        The key the attempt is counted under.
	 * @param success
	 *        Whether the credential was accepted.
	 * @return The given success.
	 */
	public boolean recordAttempt(String key, boolean success) {
		if (success) {
			notifyLoginSuccessed(key);
		} else {
			notifyLoginFailed(key);
		}
		return success;
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

		cleanupTimedOut();
		return failure;
	}

	private void cleanupTimedOut() {
		// Clean up outdated entries to prevent denial of service attacks by flooding
		// the server with nonsense authentication failures for random user names.
		for (Iterator<LoginFailure> it = _failures.values().iterator(); it.hasNext();) {
			if (it.next().timedOut()) {
				it.remove();
			}
		}
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

