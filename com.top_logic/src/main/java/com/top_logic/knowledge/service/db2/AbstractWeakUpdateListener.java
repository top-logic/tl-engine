/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;

/**
 * {@link UpdateListener} for a {@link KnowledgeBase} that bases on a {@link WeakReference}.
 * 
 * <p>
 * This {@link UpdateListener} deregistes itself from the calling {@link KnowledgeBase}, if the
 * reference is not longer valid, i.e. if the {@link #get() referent} was removed by GC.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractWeakUpdateListener<T> extends WeakReference<T> implements UpdateListener {

	/**
	 * Creates a new {@link AbstractWeakUpdateListener}.
	 * 
	 * @see WeakReference#WeakReference(Object)
	 */
	public AbstractWeakUpdateListener(T referent) {
		super(referent);
	}

	/**
	 * Creates a new {@link AbstractWeakUpdateListener}.
	 * 
	 * @see WeakReference#WeakReference(Object, ReferenceQueue)
	 */
	public AbstractWeakUpdateListener(T referent, ReferenceQueue<? super T> q) {
		super(referent, q);
	}

	@Override
	public final void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
		T referent = get();
		if (referent == null) {
			/* Reference was removed. Remove this listener. */
			sender.removeUpdateListener(this);
			return;
		}
		internalUpdate(sender, referent, event);
	}

	/**
	 * Handles update event. Actual implementation of
	 * {@link AbstractWeakUpdateListener#notifyUpdate(KnowledgeBase, UpdateEvent)}.
	 * 
	 * @param sender
	 *        The {@link KnowledgeBase} in which given {@link UpdateEvent} occurred.
	 * @param referent
	 *        The referent to update.
	 * @param event
	 *        a {@link KnowledgeBase} change event.
	 * 
	 * @see UpdateListener#notifyUpdate(KnowledgeBase, UpdateEvent)
	 */
	protected abstract void internalUpdate(KnowledgeBase sender, T referent, UpdateEvent event);

}

