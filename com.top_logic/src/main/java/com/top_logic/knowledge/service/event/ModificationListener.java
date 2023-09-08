/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.event;

import java.util.EventListener;
import java.util.Map;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.PersistentObject;

/**
 * {@link ModificationListener} reacts on changes within a transaction and creates a
 * {@link Modification} to execute as reaction on the given event.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ModificationListener extends EventListener {

	/**
	 * Reacts on the the given changes within the transaction.
	 * 
	 * <p>
	 * The implementation must not change state of any objects, but collect changes to do (e.g.
	 * select additional objects for deletion) and create a {@link Runnable} which executes the
	 * actual action (e.g. deleting the selected objects).
	 * </p>
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} sending this event.
	 * @param createdObjects
	 *        The objects created in currently committed transaction.
	 * @param updatedObjects
	 *        The objects updated in currently committed transaction.
	 * @param removedObjects
	 *        The objects deleted in currently committed transaction. As the items are already
	 *        deleted no attribute access must happen.
	 * 
	 * @return Not <code>null</code> (see {@link Modification#NONE}).
	 */
	Modification createModification(KnowledgeBase kb,
			Map<ObjectKey, ? extends KnowledgeItem> createdObjects,
			Map<ObjectKey, ? extends KnowledgeItem> updatedObjects,
			Map<ObjectKey, ? extends KnowledgeItem> removedObjects);

	/**
	 * Reacts on the upcoming deletion of the given item.
	 * 
	 * <p>
	 * The given item is <b>not</b> deleted yet.
	 * </p>
	 * 
	 * <p>
	 * The implementation must not change state of any objects, but collect changes to do (e.g.
	 * select additional objects for deletion) and create a {@link Runnable} which executes the
	 * actual action (e.g. deleting the selected objects).
	 * </p>
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} sending this event.
	 * @param item
	 *        The item scheduled for deletion.
	 * 
	 * @return Not <code>null</code> (see {@link Modification#NONE}).
	 * 
	 * @see PersistentObject#notifyUpcomingDeletion()
	 */
	Modification notifyUpcomingDeletion(KnowledgeBase kb, KnowledgeItem item);

}

