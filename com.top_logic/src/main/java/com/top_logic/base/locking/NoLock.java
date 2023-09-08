/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking;

import java.util.Collections;
import java.util.List;

import com.top_logic.base.locking.token.Token;
import com.top_logic.util.error.TopLogicException;

/**
 * Dummy {@link Lock} implementation not locking at all.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoLock implements Lock {

	private boolean _locked;

	private long _duration = Long.MAX_VALUE;

	@Override
	public void lock() throws TopLogicException {
		_locked = true;
	}

	@Override
	public void lock(long duration) throws TopLogicException {
		lock();
	}

	@Override
	public boolean renew() {
		return _locked;
	}

	@Override
	public boolean renew(long duration) {
		_duration = duration;
		return _locked;
	}

	@Override
	public boolean check() throws IllegalStateException {
		return _locked;
	}

	@Override
	public boolean isStateAcquired() {
		return _locked;
	}

	@Override
	public void unlock() throws IllegalStateException {
		_locked = false;
	}

	@Override
	public List<Token> getTokens() {
		return Collections.emptyList();
	}

	@Override
	public long getLockTimeout() {
		return _duration;
	}

}
