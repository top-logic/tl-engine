/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.Arrays;
import java.util.Queue;

import com.top_logic.mig.html.layout.Action;

/**
 * Queue containing {@link Action}s which consolidates these {@link Action Actions}, i.e. if an
 * {@link Action} is about to offer and there is already an {@link Action}, such that the new
 * {@link Action} is an {@link Action#isUpdate(Action) update} of the old {@link Action}, the old
 * {@link Action} is removed from the queue.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class ConsolidatingQueue {

	private static final int DEFAULT_CAPACITY = 16;

	/** storage containing the {@link Action Actions} in the {@link Queue} */
	private Action[] storage = new Action[DEFAULT_CAPACITY];

	/** Pointer to slot containing the next {@link Action} for {@link #poll()} */
	private int nextIndex = 0;

	/** Pointer to the slot to insert the next {@link Action} */
	private int nextInsertIndex = 0;

	boolean offer(Action action) {
		removeOutdatedAction(action);
		add(action);
		return true;
	}

	private void add(Action action) {
		updateLastIndex();
		ensureCapacity();
		set(nextInsertIndex, action);
		nextInsertIndex++;
	}

	private void ensureCapacity() {
		if (nextInsertIndex >= storage.length) {
			if (nextIndex > 0) {
				int numberElements = nextInsertIndex - nextIndex;
				System.arraycopy(storage, nextIndex, storage, 0, numberElements);
				Arrays.fill(storage, numberElements, nextInsertIndex, null);
				nextIndex = 0;
				nextInsertIndex = numberElements;
			} else {
				Action[] newStorage = new Action[(storage.length * 3) / 2 + 1];
				System.arraycopy(storage, 0, newStorage, 0, storage.length);
				storage = newStorage;
			}
		}
	}

	private void removeOutdatedAction(Action newAction) {
		for (int i = nextIndex; i < nextInsertIndex; i++) {
			Action existingAction = get(i);
			if (existingAction == null) {
				continue;
			}
			if (existingAction.isUpdate(newAction)) {
				if (i == nextIndex) {
					freeNextSlot();
				} else {
					set(i, null);
				}
			}
		}
	}

	private void updateLastIndex() {
		while (nextIndex < nextInsertIndex) {
			Action existingAction = get(nextInsertIndex - 1);
			if (existingAction == null) {
				nextInsertIndex--;
				continue;
			}
			break;
		}
		consolidateIndexes();
	}

	private void consolidateIndexes() {
		if (nextIndex == nextInsertIndex) {
			nextIndex = nextInsertIndex = 0;
		}
	}

	Action poll() {
		Action nextAction = peek();
		if (nextAction != null) {
			freeNextSlot();
		} else {
			consolidateIndexes();
		}
		return nextAction;
	}

	private void freeNextSlot() {
		set(nextIndex, null);
		nextIndex++;
	}

	Action peek() {
		while (nextIndex < nextInsertIndex) {
			Action nextAction = get(nextIndex);
			if (nextAction == null) {
				nextIndex++;
				continue;
			} else {
				return nextAction;
			}
		}
		return null;
	}

	private Action get(int index) {
		return storage[index];
	}

	private void set(int index, Action object) {
		storage[index] = object;
	}

	public void clear() {
		Arrays.fill(storage, nextIndex, nextInsertIndex, null);
		nextIndex = nextInsertIndex = 0;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public int size() {
		return nextInsertIndex - nextIndex;
	}

}
