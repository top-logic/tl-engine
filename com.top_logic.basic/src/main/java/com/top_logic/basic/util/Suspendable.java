/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

/**
 * Something that can be {@link #suspend() suspended} and {@link #resume() resumed}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface Suspendable {

	/**
	 * Pause this {@link Suspendable} <b>now</b>.
	 * <p>
	 * If this is already suspended, nothing happens and nothing is thrown.
	 * </p>
	 */
	public void suspend();

	/**
	 * Resume this {@link Suspendable} <b>now</b>.
	 * <p>
	 * If this is not suspended, nothing happens and nothing is thrown.
	 * </p>
	 */
	public void resume();

	/**
	 * Is this {@link Suspendable} suspended?
	 * <p>
	 * Returns true as soon as {@link #suspend()} has been called, even if suspending takes a while
	 * and is not yet completely done. <br/>
	 * Returns false as soon as {@link #resume()} has been called, even if resuming takes a while
	 * and is not yet completely done. <br/>
	 * </p>
	 */
	public boolean isSuspended();

}
