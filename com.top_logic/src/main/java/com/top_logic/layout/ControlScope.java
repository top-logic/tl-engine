/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

/**
 * Scope that manages {@link UpdateListener} and {@link CommandListener}
 * 
 * <p>
 * Only {@link UpdateListener}s that have
 * {@link #addUpdateListener(UpdateListener) registered} to a
 * {@link ControlScope} are
 * {@link UpdateListener#revalidate(DisplayContext, UpdateQueue) revalidated}.
 * </p>
 * 
 * @author <a href=mailto:CBR@top-logic.com>CBR</a>
 */
public interface ControlScope {

	/**
	 * Registers the given {@link UpdateListener} for updates in this
	 * {@link ControlScope}.
	 */
	public void addUpdateListener(UpdateListener aListener);

	/**
	 * Unregisters the given {@link UpdateListener} for this
	 * {@link ControlScope}.
	 * 
	 * @see #addUpdateListener(UpdateListener)
	 * 
	 * @return <code>true</code>, if the given {@link UpdateListener} was
	 *         formerly registered, <code>false</code> otherwise.
	 */
	public boolean removeUpdateListener(UpdateListener aListener);

	/**
	 * Returns the {@link FrameScope} for this {@link ControlScope}.
	 */
	public FrameScope getFrameScope();

	/**
	 * Forces this {@link ControlScope} to disable each added
	 * {@link UpdateListener} which which is also a {@link View}.
	 * 
	 * @param disable
	 *        whether the view shall be disabled or not.
	 */
	public void disableScope(boolean disable);
	
	/**
	 * Whether someone had {@link #disableScope(boolean) disabled} this scope.
	 */
	public boolean isScopeDisabled();
	
}
