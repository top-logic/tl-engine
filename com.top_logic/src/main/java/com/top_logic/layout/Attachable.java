/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

/**
 * An {@link Attachable} object temporarily observes a second object by means of
 * listening to the second object's events.
 * 
 * <p>
 * The {@link Attachable} interface controls the time, when listening starts ({@link #attach()})
 * and stops ({@link #detach()}).
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Attachable {

    /**
	 * Attaches this {@link Attachable} to its model.
	 * 
	 * <p>
	 * An {@link Attachable} must not perform any actions upon changes in its model
	 * before it has been attached.
	 * </p>
	 * 
	 * @return Whether the attachment operation was actually performed.
	 * 
	 * @see #detach()
	 */
	public boolean attach();
	
	/**
	 * Detaches this {@link Attachable} from its model.
	 * 
	 * <p>
	 * Detaching an {@link Attachable} stops it from listening for changes to its model. 
	 * </p>
	 * 
	 * @return Whether the detachment operation was actually performed.
	 * 
	 * @see #attach()
	 */
	public boolean detach();
	
	/**
	 * Whether this {@link Attachable} is currently {@link #attach() attached}.
	 */
	public boolean isAttached();
	
}
