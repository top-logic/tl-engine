/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;


import com.top_logic.dob.meta.MOReference;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.model.TLObject;


/**
 * {@link DBKnowledgeBase} internal functionality of a {@link KnowledgeObject}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
/*package protected*/
interface KnowledgeObjectInternal extends KnowledgeItemInternal, KnowledgeObject {
	
	/**
	 * Association change type that marks a link creation.
	 */
	int TYPE_CREATION = 0;
	
	/**
	 * Association change type that marks a link deletion.
	 */
	int TYPE_DELETION = 1;
	
	/**
	 * Association change type that marks a link attribute modification.
	 */
	int TYPE_MODIFICATION = 2;

	/**
	 * Get or create an association cache for the specified associations and
	 * associate the potentially created cache with this object using the given
	 * key.
	 * @param query The query to resolve.
	 * @return The requested cache.
	 */
	<T extends TLObject, C> AbstractAssociationCache<T, C> getAssociationCache(AbstractAssociationQuery<T, C> query);

	/**
	 * Notifies this base object about a context local change of the given
	 * association link.
	 * 
	 * @param context
	 *        The context in which the change happened.
	 * @param reference
	 *        Whether this object is the source or destination object of the
	 *        given association link.
	 * @param link
	 *        The touched association link (created, deleted, or modified).
	 * @param changeType
	 *        Type of the change. Any of {@link #TYPE_CREATION},
	 *        {@link #TYPE_DELETION}, and {@link #TYPE_MODIFICATION}.
	 */
	void notifyLocalAssociationChange(DBContext context, MOReference reference, KnowledgeItemInternal link, int changeType);

	/**
	 * Notifies this base object about a global change of the given association
	 * link (commit or refetch).
	 * 
	 * @param revision
	 *        The revision number in which the change happened.
	 * @param reference
	 *        Whether this object is the source or destination object of the
	 *        given association link.
	 * @param link
	 *        The touched association link (created, deleted, or modified).
	 */
	void notifyAssociationChange(long revision, MOReference reference, KnowledgeItemInternal link);

	/**
	 * Drops the local association caches in the given {@link DBContext}.
	 */
	void dropLocalAssocationCache(DBContext caller);
}
