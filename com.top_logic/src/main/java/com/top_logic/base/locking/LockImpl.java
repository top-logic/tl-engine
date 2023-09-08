/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking;

import java.util.Date;
import java.util.List;

import com.top_logic.base.context.SubSessionListener;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.base.locking.token.Token;
import com.top_logic.base.locking.token.TokenService;
import com.top_logic.basic.DebugHelper;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * Default {@link Lock} implementation base on {@link Token}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LockImpl implements Lock, SubSessionListener {

	private final List<Token> _tokens;

	private boolean _acquired;

	private long _duration;

	/**
	 * Creates a {@link LockImpl}.
	 *
	 * @param tokens
	 *        See {@link #getTokens()}.
	 * 
	 * @see Lock#createLock(long, List)
	 */
	LockImpl(long duration, List<Token> tokens) {
		_duration = duration;
		_tokens = tokens;
	}

	@Override
	public long getLockTimeout() {
		return _duration;
	}

	@Override
	public List<Token> getTokens() {
		return _tokens;
	}

	@Override
	public void lock() throws TopLogicException {
		addSubSessionListener();
		try {
			tokenService().acquire(getExpireDate(), getTokens());
			markAcquired();
		} finally {
			if (!isStateAcquired()) {
				removeSubSessionListener();
			}
		}
	}

	@Override
	public void lock(long duration) throws TopLogicException {
		setDuration(duration);
		lock();
	}

	@Override
	public boolean renew() {
		return internalRenew();
	}

	@Override
	public boolean renew(long duration) {
		setDuration(duration);
		return internalRenew();
	}

	private void setDuration(long duration) {
		_duration = duration;
	}

	private boolean internalRenew() {
		if (!isStateAcquired()) {
			return false;
		}
	
		return tokenService().renew(getExpireDate(), getTokens());
	}

	@Override
	public boolean check() throws IllegalStateException {
		if (!isStateAcquired()) {
			return false;
		}
		return tokenService().allValid(getTokens());
	}

	@Override
	public boolean isStateAcquired() {
		return _acquired;
	}

	/**
	 * Sets {@link #isStateAcquired()} to <code>true</code>.
	 */
	private void markAcquired() {
		_acquired = true;
	}

	/**
	 * Sets {@link #isStateAcquired()} to <code>false</code>.
	 */
	private void markReleased() {
		_acquired = false;
	}

	@Override
	public void unlock() throws IllegalStateException {
		if (!isStateAcquired()) {
			return;
		}
		tokenService().release(getTokens());
		markReleased();
		removeSubSessionListener();
	}

	private void addSubSessionListener() {
		TLContext.getContext().addUnboundListener(this);
	}

	private void removeSubSessionListener() {
		TLContext.getContext().removeUnboundListener(this);
	}

	@Override
	public void notifySubSessionUnbound(TLSubSessionContext context) {
		unlock();
	}

	/**
	 * Utility to compute the timeout for a {@link Lock} that is acquired now.
	 */
	private final Date getExpireDate() {
		return new Date(System.currentTimeMillis() + _duration);
	}

	private TokenService tokenService() {
		return TokenService.getInstance();
	}

	@Override
	public String toString() {
		return "Lock(" + DebugHelper.formatTime(_duration) + ", " + getTokens() + ")";
	}
}