/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.base.services.simpleajax.ClientAction;

/**
 * The class {@link DefaultUpdateQueue} is an implementation of
 * {@link UpdateQueue} which stores all actions in a list.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultUpdateQueue implements UpdateQueue {

	private final List<ClientAction> storage = new ArrayList<>();

	@Override
	public void add(ClientAction update) {
		if (update != null) {
			storage.add(update);
		}
	}

	/**
	 * returns the number of added updates
	 * 
	 * @see List#size()
	 */
	public int size() {
		return storage.size();
	}

	/**
	 * Returns the update at the given position
	 * 
	 * @param index
	 *        must be &gt;= 0 and &lt; {@link #size() size}
	 * 
	 * @see List#get(int)
	 */
	public ClientAction get(int index) {
		return storage.get(index);
	}

	/**
	 * deletes all formerly added updates
	 * 
	 * @see List#clear()
	 */
	public void clear() {
		storage.clear();
	}

	/**
	 * Removes the {@link ClientAction} at the given position. acutally calls
	 * {@link List#set(int, Object)} using <code>index</code> and
	 * <code>null</code>.
	 * 
	 * @param index
	 *        must be &gt;=0 and &lt; {@link #size() size}
	 * 
	 * @see List#set(int, Object)
	 */
	public void deleteUpdate(int index) {
		storage.set(index, null);
	}

	/**
	 * This method returns an unmodifiable view to the list of all formerly
	 * added actions.
	 * 
	 * @return The list containing all actions which were added to this
	 *         {@link UpdateQueue}. Never <code>null</code>.
	 */
	public List<ClientAction> getStorage() {
		return Collections.unmodifiableList(storage);
	}
	
	/**
	 * adds each formerly added update to the given {@link UpdateQueue} and
	 * clears the storage.
	 * 
	 * @param updates
	 *        the queue to add updates to.
	 */
	public void revalidate(UpdateQueue updates) {
		for (int index = 0, size = size(); index < size; index++) {
			updates.add(get(index));
		}
		clear();
	}
}
