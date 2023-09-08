/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

/**
 * Default implementation of a {@link ValidityChain}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ValidityChainImpl<T extends ValidityChain<T>> implements ValidityChain<T> {

	/**
	 * Whether this {@link ValidityChain} is frozen, i.e. neither {@link #minValidity()} nor
	 * {@link #maxValidity()} can be modified any more.
	 */
	private volatile boolean _frozen = false;

	/** @see #minValidity() */
	private volatile long _minValidity;

	/** @see #maxValidity() */
	private volatile long _maxValidity;

	/** @see #formerValidity() */
	private volatile T _formerValidity;

	/**
	 * Creates a new {@link ValidityChainImpl}.
	 */
	public ValidityChainImpl(long minValidity, long maxValidity) {
		assert minValidity <= maxValidity;
		_minValidity = minValidity;
		_maxValidity = maxValidity;
	}

	@Override
	public void updateMaxValidity(long validity) {
		if (_frozen) {
			throw new IllegalStateException("Can not set max validity '" + validity + "' to frozen chain: " + this);
		}
		assert validity >= _minValidity;
		_maxValidity = validity;
	}

	@Override
	public void publishLocalValidity(long newMinValidity) {
		if (_frozen) {
			throw new IllegalStateException(
				"Can not publish frozen chain for validity '" + newMinValidity + "': " + this);
		}
		assert newMinValidity <= _maxValidity;
		_minValidity = newMinValidity;
	}

	@Override
	public long minValidity() {
		return _minValidity;
	}

	@Override
	public long maxValidity() {
		return _maxValidity;
	}

	@Override
	public T formerValidity() {
		return _formerValidity;
	}

	@Override
	public void setFormerValidity(T formerValidity) {
		if (formerValidity != null && formerValidity.maxValidity() >= minValidity()) {
			throw failNotFormerValidity(formerValidity);
		}
		if (formerValidity instanceof ValidityChainImpl) {
			((ValidityChainImpl<?>) formerValidity)._frozen = true;
		}
		_formerValidity = formerValidity;

	}

	private RuntimeException failNotFormerValidity(T formerValidity) {
		StringBuilder error = new StringBuilder();
		error.append("Given validity '");
		error.append(formerValidity);
		error.append("' is not a former validity of this '");
		error.append(this);
		error.append("': This: ");
		appendValidityRange(error, this);
		error.append(", other: ");
		appendValidityRange(error, formerValidity);
		throw new IllegalArgumentException(error.toString());
	}

	@Override
	public final void cleanup() {
		setFormerValidity(null);
	}

	/**
	 * Appends {@link #maxValidity()} and {@link #minValidity()} to the given output.
	 */
	public static StringBuilder appendValidityRange(StringBuilder out, ValidityChain<?> chain) {
		return out.append("[max:").append(chain.maxValidity()).append(",min:").append(chain.minValidity()).append(']');
	}

}

