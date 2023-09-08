/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

/**
 * Base class for implementations that can me made immutable some time after instantiation.
 * 
 * 
 * <p>
 * This pattern can be used, if a class is its own builder implementation. After construction, the
 * instance is the builder that can be modified, until its final state is reached. By
 * {@link #freeze() freezing} the instance before publishing its reference, it is guaranteed that
 * customers can no longer use its builder API and modify its state.
 * </p>
 * 
 * <p>
 * Note: Implementations must call {@link #checkFrozen()} at the beginning of each method that
 * modifies the internal state of this instance.
 * </p>
 * 
 * <p>
 * In general, its preferable to split implementations in a builder and an implementation that is
 * immutable directly after construction.
 * </p>
 * 
 * <p>
 * This class is declared <code>abstract</code>, because it must be sub-classed before it is usable.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFreezable {

	/**
	 * @see #checkFrozen()
	 */
	private boolean frozen;

	/**
	 * Check, that this object can be modified, throw an {@link IllegalStateException} otherwise.
	 * 
	 * @throws IllegalStateException
	 *         If this object is {@link #freeze() frozen}.
	 * 
	 * @see #isFrozen() Test without throwing an exception.
	 */
	protected final void checkFrozen() throws IllegalStateException {
		if (frozen) {
			throw new IllegalStateException("Immutable object '" + toString() + "' cannot be modified.");
		}
	}

	/**
	 * Whether this object can still be modified.
	 * 
	 * @see #checkFrozen() Assert that this object can be modified.
	 */
	public final boolean isFrozen() {
		return frozen;
	}

	/**
	 * Makes this object immutable.
	 */
	public void freeze() {
		this.frozen = true;
	}

}
