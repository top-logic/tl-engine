/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.layout.Attachable;

/**
 * Default implementation of the attachment protocol of an 
 * {@link com.top_logic.layout.Attachable} object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractAttachable implements Attachable {
	
    /** The state managed by this class */
    private boolean attached = false;

    /**
	 * Implemented to manage the {@link #attached} state.
	 * 
	 * <p>
	 * The actual implementation of attaching to the model must be provided by
	 * sub-classes in {@link #internalAttach()}.
	 * </p>
	 * 
	 * @return Whether the attachment operation was actually performed.
	 * 
	 * @see #internalAttach()
	 */
	@Override
	public final boolean attach() {
		if (attached) {
            return false;
        }

		internalAttach();
		
		return attached = true;
	}


	/**
	 * Implemented to manage the {@link #attached} state.
	 * 
	 * <p>
	 * The actual implementation of detaching from the model must be provided by
	 * sub-classes in {@link #internalDetach()}.
	 * </p>
	 * 
	 * @return Whether the detachment operation was actually performed.
	 * 
	 * @see #internalDetach()
	 */
	@Override
	public final boolean detach() {
		if (! attached) {
            return false;
        }
		
		attached = false;
		internalDetach();
		
		return true;
	}

	@Override
	public final boolean isAttached() {
		return attached;
	}
	
	/**
	 * Implementation for attaching this instance to its model (e.g. adding
	 * listeners for observing the model).
	 */
	protected abstract void internalAttach();

	/**
	 * Implementation for detaching this instance from its model (e.g. removing
	 * registered listeners).
	 */
	protected abstract void internalDetach();

	/**
	 * Asserts that this {@link Attachable} is currently attached.
	 * 
	 * @throws IllegalStateException
	 *         if this {@link Attachable} is not attached.
	 */
	protected final void checkAttached() {
		if (! isAttached()) {
			throw new IllegalStateException("Not attached.");
		}
	}
}
