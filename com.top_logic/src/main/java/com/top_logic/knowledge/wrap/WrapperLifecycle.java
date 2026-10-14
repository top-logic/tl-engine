/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRefetch;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.event.Modification;
import com.top_logic.knowledge.service.event.ModificationListener;

/**
 * Callbacks the {@link KnowledgeBase} invokes on the application object of a
 * {@link KnowledgeItem} during the life of that item.
 * 
 * <p>
 * The {@link DBKnowledgeBase} calls these hooks on every application object (see
 * {@link KnowledgeItem#getWrapper()}) that implements this interface. All hooks are no-ops by
 * default.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface WrapperLifecycle {

	/**
	 * Hook to initialize due to loading of the corresponding {@link KnowledgeItem}.
	 * 
	 * <p>
	 * Note: This method is called for both, current and historic objects.
	 * </p>
	 * 
	 * @see #handleDelete()
	 */
	default void handleLoad() {
		// Nothing to do by default.
	}

	/**
	 * Callback invoked before an object is deleted.
	 * 
	 * <p>
	 * Note: Implementations must not directly perform any actions, but only collect required
	 * information from this object being deleted and return a {@link Modification} callback that is
	 * called after the initial deletion has completed.
	 * </p>
	 * 
	 * @see ModificationListener#notifyUpcomingDeletion(KnowledgeBase, KnowledgeItem)
	 * @see #handleDelete()
	 * @see Modification#andThen(Modification) Combining modifications from overridden methods with
	 *      own modifications.
	 */
	default Modification notifyUpcomingDeletion() {
		return Modification.NONE;
	}

	/**
	 * Hook to clean up external state after the deletion of this object.
	 * 
	 * <p>
	 * Note: At call time this object has already been deleted and the deletion has been committed.
	 * </p>
	 * 
	 * @see #notifyUpcomingDeletion()
	 * @see #handleLoad()
	 */
	default void handleDelete() {
		// Nothing to do by default.
	}

	/**
	 * Forces this object to refetch its cached values.
	 * 
	 * <p>
	 * This method is called (indirectly) by the {@link KnowledgeBaseRefetch}, which synchronizes
	 * the content between different VMs (in a cluster).
	 * </p>
	 */
	default void refetchWrapper() {
		// Nothing to do by default.
	}

}
