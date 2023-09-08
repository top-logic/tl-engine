/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.lang.ref.ReferenceQueue;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;

/**
 * {@link UpdateListener} that cane be registered as delegate for another {@link UpdateListener} at
 * some {@link KnowledgeBase}.
 * 
 * <p>
 * The {@link UpdateListener} is deregistered when the represented {@link UpdateListener} is not
 * longer referenced.
 * </p>
 * 
 * <p>
 * This {@link UpdateListener} should be used when there is no lifecycle so that
 * {@link KnowledgeBase#addUpdateListener(UpdateListener)} and
 * {@link KnowledgeBase#removeUpdateListener(UpdateListener)} can be used.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class WeakUpdateListener extends AbstractWeakUpdateListener<UpdateListener> {

	/**
	 * Creates a new {@link WeakUpdateListener}.
	 * 
	 * @param referent
	 *        The {@link UpdateListener} to delegate to.
	 */
	public WeakUpdateListener(UpdateListener referent) {
		super(referent);
	}

	/**
	 * Creates a new {@link WeakUpdateListener}.
	 * 
	 * @param referent
	 *        The {@link UpdateListener} to delegate to.
	 */
	public WeakUpdateListener(UpdateListener referent, ReferenceQueue<? super UpdateListener> q) {
		super(referent, q);
	}

	@Override
	protected void internalUpdate(KnowledgeBase sender, UpdateListener referent, UpdateEvent event) {
		referent.notifyUpdate(sender, event);
	}

}

