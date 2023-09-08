/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;


import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.layout.basic.component.ControlSupport;

/**
 * The class {@link UpdateListener} is used to get updates for the GUI.
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface UpdateListener extends NotifyListener, DisableListener {

	/**
	 * Checks, whether {@link #revalidate(DisplayContext, UpdateQueue) revalidation} is
	 * necessary.
	 */
	public boolean isInvalid();
	
	/**
	 * Add all {@link ClientAction}s that are necessary to update the
	 * client-side view of this {@link UpdateListener} to the given list of
	 * actions.
	 * 
	 * <p>
	 * This method is called from the
	 * {@link ControlSupport#revalidate(DisplayContext, UpdateQueue)}
	 * method on all of its {@link UpdateListener}'s to create an AJAX response
	 * that brings its client-side display in sync.
	 * </p>
	 * 
	 * <p>
	 * After calling this method, {@link #isInvalid()} returns
	 * <code>false</code>.
	 * </p>
	 * 
	 * <p>
	 * An update of the client-side view of a {@link UpdateListener} is
	 * necessary after a change of its model. A {@link UpdateListener} must
	 * observe its model and either
	 * </p>
	 * 
	 * <ul>
	 * <li>cache all resulting update actions upon this method is called, or</li>
	 * 
	 * <li>remember that is is invalid and provide a complete replacement of
	 * its client-side view (as {@link ClientAction}, which it adds to the
	 * given list of actions). </li>
	 * </ul>
	 */
	public void revalidate(DisplayContext context, UpdateQueue/*<ClientAction>*/ actions);
	
}

