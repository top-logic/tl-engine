/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.util.HashSet;
import java.util.Iterator;

import com.top_logic.layout.basic.AbstractControlBase;

/**
 * {@link ControlScope} that separates child {@link Control}s of composite
 * controls from the global scope.
 * 
 * <p>
 * A {@link LocalScope} must be used, if a composite must prevent child
 * {@link AbstractControlBase controls} from being independently
 * {@link AbstractControlBase#revalidate(DisplayContext, UpdateQueue) revalidated}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LocalScope implements ControlScope {

	/**
	 * Short-cut to wrapped functionality that can be delegated to the
	 * outermost scope.
	 * 
	 * <p>
	 * TODO: The interface {@link ControlScope} should be split to make this
	 * delegation superfluous.
	 * </p>
	 */
	private final FrameScope frame;
	
	/**
	 * The set of currently
	 * {@link #addUpdateListener(UpdateListener) registered} update listeners.
	 */
	private final HashSet<UpdateListener> updateListeners = new HashSet<>();
	
	/**
	 * Optimization to allow {@link #clear() clearing} without copying and
	 * without concurrent modifications of {@link #updateListeners}.
	 */
	private boolean clearing;

	private boolean disabled;

	/**
	 * Creates a new {@link LocalScope} inside a given outer scope. 
	 */
	public LocalScope(FrameScope outerScope, boolean disabled) {
		this.frame = outerScope;
		this.disabled = disabled;
	}
	
	@Override
	public void addUpdateListener(UpdateListener aListener) {
		if (clearing) {
			throw new IllegalStateException("LocalScope is currently clearing its listener.");
		}
		boolean added = updateListeners.add(aListener);
		if (added) {
			aListener.notifyAttachedTo(this);
		}
	}
	
	@Override
	public boolean removeUpdateListener(UpdateListener aListener) {
		boolean wasRemoved;
		if (!clearing) {
			wasRemoved = updateListeners.remove(aListener);
			if (wasRemoved) {
				aListener.notifyDetachedFrom(this);
			}
		} else {
			wasRemoved = updateListeners.contains(aListener);
		}
		
		return wasRemoved;
	}
	
	/**
	 * Returns the {@link FrameScope} of the {@link ControlScope} used to build this
	 * {@link LocalScope}.
	 * 
	 * @see ControlScope#getFrameScope()
	 */
	@Override
	public FrameScope getFrameScope() {
		return frame;
	}

	public void clear() {
		if (clearing) {
			throw new IllegalStateException("LocalScope is currently clearing its listener.");
		}
		clearing = true;
		try {
			for (Iterator<UpdateListener> it = updateListeners.iterator(); it.hasNext(); ) {
				UpdateListener nextControl = it.next();
				nextControl.notifyDetachedFrom(this);
			}
			updateListeners.clear();
		} finally {
			clearing = false;
		}
	}

	/**
	 * Delegates to {@link UpdateListener#isInvalid()} for all
	 * {@link #addUpdateListener(UpdateListener) registered} {@link UpdateListener}s.
	 * 
	 * @see UpdateListener#isInvalid()
	 */
	public boolean hasUpdates() {
		for (Iterator<UpdateListener> it = updateListeners.iterator(); it.hasNext(); ) {
			if (it.next().isInvalid()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Delegates to {@link UpdateListener#revalidate(DisplayContext, UpdateQueue)} for all
	 * {@link #addUpdateListener(UpdateListener) registered} {@link UpdateListener}s.
	 * <p>
	 * Moreover it installs itself as {@link ControlScope} into the {@link DisplayContext} to ensure
	 * that newly rendered controls and repaints of currently added controls gets the correct
	 * {@link ControlScope}.
	 * </p>
	 * 
	 * @see UpdateListener#revalidate(DisplayContext, UpdateQueue)
	 */
	public void revalidate(DisplayContext context, UpdateQueue actions) {
		if (!updateListeners.isEmpty()) {
			context.validateScoped(this, LocalScopeValidator.INSTANCE, actions, this);
		}
	}
	
	@Override
	public void disableScope(boolean disable) {
		if (disabled == disable) {
			return;
		}
		disabled = disable;
		for (UpdateListener updater : updateListeners) {
			updater.notifyDisabled(disable);
		}
	}
	
	@Override
	public boolean isScopeDisabled() {
		return disabled;
	}

	/**
	 * The class {@link LocalScope.LocalScopeValidator} is a {@link Validator} which can be used to validate
	 * some {@link LocalScope}.
	 * <p>
	 * <b>Attention:</b> Be carefully using this {@link Validator}. It must be ensured that the correct
	 * {@link ControlScope} is installed during revalidation. The {@link ControlScope} <b>must</b>
	 * be the {@link ControlScope} used to render the {@link UpdateListener} attached to the
	 * revalidated {@link LocalScope}.
	 * </p>
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static class LocalScopeValidator implements Validator<LocalScope>{

		public static final Validator<LocalScope> INSTANCE = new LocalScopeValidator();
		
		@Override
		public final void validate(DisplayContext context, UpdateQueue queue, LocalScope validationObject) {
			LocalScope self = validationObject;
			for (Iterator<UpdateListener> it = self.updateListeners.iterator(); it.hasNext(); ) {
				UpdateListener updateListener = it.next();
				if (updateListener.isInvalid()) {
					updateListener.revalidate(context, queue);
				}
			}
		}

	}
}