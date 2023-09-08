/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.Map;

import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.basic.Logger;
import com.top_logic.layout.Control;
import com.top_logic.layout.DefaultUpdateQueue;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;

/**
 * Generic implementation of incremental revalidation of a {@link Control}.
 * 
 * <p>
 * The generic revalidation management provided by this implementation supports
 * two states of a {@link com.top_logic.layout.Control}:
 * </p>
 * 
 * <dl>
 * <dt>Incrementally revalidatable</dt>
 * <dd>A {@link com.top_logic.layout.Control} that is incrementally
 * revalidatable can provide incremental updates that bring its client-side view
 * in sync with potential changes to its model. Those updates can be reported as
 * direct responses to model changes by calling {@link #addUpdate(ClientAction)}.
 * </dd>
 * 
 * <dt>Repaint requested</dt>
 * <dd>A {@link com.top_logic.layout.Control} can mark itself invalid ({@link #requestRepaint()},
 * if the changes to its model are to extensive and render an incremental update
 * either inefficient or impossible. An invalid
 * {@link com.top_logic.layout.Control} must provide actions that completely
 * refresh its client-side view. This is handled in {@link AbstractControlBase}. 
 * Concrete subclasses can bring its internal up to date in {@link #handleRepaintRequested(UpdateQueue)}. 
 * </dd>
 * </dl>
 * 
 * <p>
 * A concrete sub-class must provide a mechanism to attach and detach the
 * listener, which observes its model (see {@link #attachRevalidated()} and
 * {@link #detachInvalidated()}). This allows this generic revalidation
 * implementation to automatically detach an invalid
 * {@link com.top_logic.layout.Control} to prevent unnecessary overhead for
 * observing its model (in a state, where it can no longer generate incremental
 * updates).
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractControl extends AbstractControlBase {
    
    /**
	 * List of {@link ClientAction}s that represent an incremental update to the
	 * client-side view of this control.
	 * 
	 * <p>
	 * As long as no {@link #requestRepaint() repaint is requested} for this
	 * {@link Control}, the update list represents the difference that brings
	 * the client-side view in sync with the server-side model.
	 * </p>
	 */
    private DefaultUpdateQueue updateQueue = new DefaultUpdateQueue();

    public AbstractControl(Map<String, ControlCommand> commandsByName) {
    	super(commandsByName);
    }
    
	public AbstractControl() {
		super();
	}

    @Override
	protected void detachInvalidated() {
    	super.detachInvalidated();
    	
    	clearUpdateQueue();
    }
    
	/**
	 * Adds the given update to the update queue of this {@link com.top_logic.layout.Control}.
	 * 
	 * <p>
	 * <b>Note</b>: This method is declared public only to enable
	 * {@link com.top_logic.layout.Control}s being rendered and updated by external control
	 * renderers. This method <b>must not</b> be called outside the layout framework.
	 * </p>
	 * 
	 * <p>
	 * <b>Note</b>: This method must only be called if this {@link com.top_logic.layout.Control} has
	 * not yet {@link #requestRepaint() requested repaint}.
	 * </p>
	 * 
	 * <p>
	 * The given update incrementally updates the client-side view of this
	 * {@link com.top_logic.layout.Control} after a change to its model. All provided updates are
	 * applied to the client-side document in exactly the same order in which they are reported to
	 * this method.
	 * </p>
	 * 
	 * @return An identifier for the enqueued update. This identifier can be passed to
	 *         {@link #dropUpdate(int)}, if this update becomes obsoleted before
	 *         {@link #revalidate(DisplayContext, UpdateQueue)} is called.
	 */
	protected int addUpdate(ClientAction update) {
		if (isRepaintRequested()) {
			Logger.warn(
				"Only a valid control can be updated incrementally. Control has been de-registered from its model?",
				AbstractControl.class);
			return -1;
		}
		assert isAttached() : "Only a control that is attached to its model may produce incremental updates.";
		assert update != null : "An update must not be null.";
		assert checkCommandThread();
		
		int updateIndex = updateQueue.size();
		updateQueue.add(update);
		return updateIndex;
	}
	
	/**
	 * Returns the original update queue where the updates are added to.
	 */
	protected UpdateQueue getUpdates() {
		return updateQueue;
	}
	
	/**
	 * @see #addUpdate(ClientAction)
	 */
	public ClientAction dropUpdate(int updateIndex) {
		if (updateIndex == -1) {
			return null;
		}
		ClientAction result = updateQueue.get(updateIndex);
		
		assert result != null : "An update may be dropped only once.";
		
		// Note: Do not remove the index to keep all references intact that where
		// generated by calls to addUpdate().
		//
		// Note: Also do not insert any new update at the old index to keep the 
		// events in order.
		updateQueue.deleteUpdate(updateIndex);
		
		return result;
	}
	
	@Override
	protected boolean hasUpdates() {
		return updateQueue.size() > 0;
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		for (int cnt = updateQueue.size(), n = 0; n < cnt; n++) {
			ClientAction update = updateQueue.get(n);
			
			if (update == null) {
				// The update was dropped.
				continue;
			}
			
			actions.add(update);
		}
		clearUpdateQueue();
	}
	
	/**
	 * Schedules a {@link ClientAction} that updates the control's CSS class to the value currently
	 * created by {@link #writeControlClassesContent(Appendable)}.
	 */
	protected final void updateCss() {
		addUpdate(createCssUpdate());
	}

	/**
	 * Invalidating a control drops all
	 * {@link #addUpdate(ClientAction) incremental updates} it has collected so
	 * far.
	 * 
	 * @see com.top_logic.layout.basic.AbstractControlBase#internalRequestRepaint()
	 */
	@Override
	protected void internalRequestRepaint() {
		super.internalRequestRepaint();
		
		// All updates collected so far can be dropped, because the view is now
		// completely out of sync.
		clearUpdateQueue();
	}
	
	/**
	 * Resets the {@link #isInvalid()} state of this {@link com.top_logic.layout.Control}.
	 */
	@Override
	protected void reset() {
		super.reset();
		
		this.clearUpdateQueue();
	}

	protected void clearUpdateQueue() {
		this.updateQueue.clear();
	}

}
