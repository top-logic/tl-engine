/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.base.locking.handler.LockHandler;
import com.top_logic.base.locking.token.Token;
import com.top_logic.base.locking.token.Token.Kind;
import com.top_logic.basic.Logger;
import com.top_logic.util.error.TopLogicException;

/**
 * A {@link #check() valid} {@link Lock} prevents concurrent access to the guarded object.
 * 
 * @see LockHandler#acquireLock(Object)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Lock {

	/**
	 * Acquires a {@link #isStateReleased() released} {@link Lock} for the default duration.
	 * 
	 * <p>
	 * If the operation succeeds, {@link #isStateAcquired()} becomes <code>true</code>.
	 * </p>
	 * 
	 * <p>
	 * The lock keeps valid for the duration {@link #getLockTimeout()}.
	 * </p>
	 *
	 * @throws TopLogicException
	 *         If the lock acquisition fails.
	 */
	void lock() throws TopLogicException;

	/**
	 * Acquires a {@link #isStateReleased() released} {@link Lock}.
	 * 
	 * @param duration
	 *        The validity duration in ms. This updates the {@link #getLockTimeout()} value.
	 *
	 * @throws TopLogicException
	 *         If the lock acquisition fails.
	 */
	void lock(long duration) throws TopLogicException;

	/**
	 * Tries to {@link #lock()} but does not throw a {@link TopLogicException}.
	 * 
	 * <p>
	 * Note: Better use {@link #lock()} to prevent discarding the details of a potential conflict.
	 * </p>
	 *
	 * @return Whether locking was successful.
	 */
	default boolean tryLock() {
		try {
			lock();
			return true;
		} catch (TopLogicException ex) {
			Logger.info("Lock request denied for: " + this, ex, Lock.class);
			return false;
		}
	}

	/**
	 * Tries to {@link #lock(long)} but does not throw a {@link TopLogicException}.
	 * 
	 * <p>
	 * Note: Better use {@link #lock(long)} to prevent discarding the details of a potential
	 * conflict.
	 * </p>
	 *
	 * @return Whether locking was successful.
	 */
	default boolean tryLock(long duration) {
		try {
			lock(duration);
			return true;
		} catch (TopLogicException ex) {
			return false;
		}
	}

	/**
	 * Renews this {@link Lock}.
	 * 
	 * <p>
	 * Renewing a lock updates the expiration date so that the lock does not time out before the
	 * default validity duration has elapsed, if the lock is still valid. An expired {@link Lock}
	 * cannot be renewed.
	 * </p>
	 * 
	 * @return Whether the renewal succeeded.
	 * 
	 * @see #renew(long)
	 */
	boolean renew();

	/**
	 * Renews this {@link Lock}.
	 * 
	 * <p>
	 * Renewing a lock updates the expiration date so that the lock does not time out before the
	 * given validity duration has elapsed, if the lock is still valid. An expired {@link Lock}
	 * cannot be renewed.
	 * </p>
	 * 
	 * @param duration
	 *        The new validity duration in ms. If the renewal succeeds, the expiration date of the
	 *        {@link Lock} is adjusted to now plus the given value.
	 * @return Whether the renewal succeeded.
	 */
	boolean renew(long duration);

	/**
	 * Check if the {@link Lock} is still valid.
	 * 
	 * <p>
	 * Can only be called in ACQUIRED state.
	 * </p>
	 * 
	 * @return <code>true</code> if it is locked, <code>false</code> otherwise.
	 * @throws IllegalStateException
	 *         If the context is not in ACQUIRED state.
	 */
	boolean check() throws IllegalStateException;

	/**
	 * Whether this context is in state ACQUIRED.
	 */
	boolean isStateAcquired();

	/**
	 * Whether not {@link #isStateAcquired()}.
	 */
	default boolean isStateReleased() {
		return !isStateAcquired();
	}

	/**
	 * @deprecated There is no difference in meaning from {@link #isStateReleased()}.
	 */
	@Deprecated
	default boolean isStateFailed() {
		return isStateReleased();
	}

	/**
	 * Releases this {@link Lock}.
	 * 
	 * <p>
	 * Can only be called in state ACQUIRED or FAILED. The state is changed to RELEASED.
	 * </p>
	 * 
	 * @throws IllegalStateException
	 *         If the state is not ACQUIRED or FAILED.
	 */
	void unlock() throws IllegalStateException;

	/**
	 * The {@link Token}s this {@link Lock} is composed of.
	 */
	List<Token> getTokens();

	/**
	 * Creates a custom {@link Lock} for a single custom exclusive token..
	 * 
	 * @param duration
	 *        See {@link #createLock(long, List)}.
	 * @param tokenName
	 *        See {@link Token#getName()}.
	 */
	static Lock createLock(long duration, String tokenName) {
		return createLock(duration, Collections.singletonList(Token.newGlobalToken(Kind.EXCLUSIVE, tokenName)));
	}

	/**
	 * Creates a custom {@link Lock}.
	 *
	 * @param duration
	 *        The initial lock duration, when using {@link Lock#lock()}.
	 * @param tokens
	 *        The {@link Token} to acquire when locking.
	 * @return The new {@link Lock}.
	 */
	static Lock createLock(long duration, List<Token> tokens) {
		return new LockImpl(duration, Collections.unmodifiableList(new ArrayList<>(tokens)));
	}

	/**
	 * Default duration in milliseconds this {@link Lock} keeps valid when being {@link #lock()
	 * locked}.
	 * 
	 * <p>
	 * The lock duration can be specified explicitly using {@link #lock(long)}.
	 * </p>
	 */
	long getLockTimeout();

}
