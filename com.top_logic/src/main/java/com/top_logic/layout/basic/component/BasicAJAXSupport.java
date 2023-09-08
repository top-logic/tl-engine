/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.component;

import java.io.Serializable;
import java.util.ArrayList;

import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;

/**
 * Mix-in that implements the contract defined by {@link AJAXSupport} by
 * maintaining a list of incremental updates.
 * 
 * TODO: Implements {@link java.io.Serializable} in order to satisfy the layout tests
 *       KHA, BHU, TSA will this result in any helpful state after deserialization?
 * 
 * @see #add(ClientAction) for adding an incremental update that is
 *      transported to the client side.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BasicAJAXSupport implements AJAXSupport, Serializable, UpdateQueue {

    /** Accumulates the outstanding {@link ClientAction updates} for its mix-in master. */
	private ArrayList<ClientAction> updateQueue = new ArrayList<>();

	boolean markedAsInvalid = false;
	
	/**
	 * Adds an incremental update for re-validating the view of this component.
	 */
    @Override
	public void add(ClientAction update) {
    	if (markedAsInvalid) {
    		return;
    	}
    	updateQueue.add(update);
    }
    
	@Override
	public void revalidate(DisplayContext context, UpdateQueue actions) {
		for (int cnt = updateQueue .size(), n = 0; n < cnt; n++) {
			ClientAction update = updateQueue.get(n);
			if (update == null) continue;
			actions.add(update);
		}
		
		dropUpdates();
	}

	/**
     * Cancels all incremental updates when the master is completely rendered.
     */
	@Override
	public void startRendering() {
        dropUpdates();
        markedAsInvalid = false;
	}

	/**
	 * Check, whether incremental updates have been created. 
	 * 
	 * @see AJAXComponent#isRevalidateRequested()
	 */
	@Override
	public boolean isRevalidateRequested() {
		return updateQueue.size() > 0;
	}
	
	/**
	 * Cancel all incremental updates.
	 */
	private void dropUpdates() {
		updateQueue.clear();
	}
	
	@Override
	public void invalidate() {
		markedAsInvalid = true;
		dropUpdates();
	}
	
}
