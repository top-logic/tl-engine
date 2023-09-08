/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.List;

import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.model.TLObject;

/**
 * Internal interface for all association caches.
 * 
 * @param <T>
 *        The type of the cache entries.
 * @param <C>
 *        The type of the collection managed by the cache.
 * 
 * @see AssociationQueryImpl
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
abstract class AbstractAssociationCache<T extends TLObject, C> {

	/**
	 * @see #getBaseItem()
	 */
	/* package protected */final KnowledgeItemInternal _baseItem;

	private final MOKnowledgeItemImpl _associationType;

	private final MOReference _referenceAttribute;

	/**
	 * Creates a {@link AbstractAssociationCache}.
	 * 
	 * @param baseItem
	 *        See {@link #getBaseItem()}.
	 */
	AbstractAssociationCache(KnowledgeItemInternal baseItem, AssociationQueryImpl<T, C> query) {
		_baseItem = baseItem;

		_associationType = lookupType(query.getAssociationTypeName());
		_referenceAttribute = lookupReference(query.getReferenceAttribute());
	}

	private MOReference lookupReference(String referenceAttribute) {
		try {
			return (MOReference) _associationType.getAttribute(referenceAttribute);
		} catch (NoSuchAttributeException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

	private MOKnowledgeItemImpl lookupType(String typeName) {
		return getKnowledgeBase().lookupType(typeName);
	}

	/**
	 * Returns the {@link KnowledgeBase} of the {@link #getBaseItem()}
	 */
	protected final DBKnowledgeBase getKnowledgeBase() {
		return _baseItem.getKnowledgeBase();
	}

	/**
	 * The object that is the referenced object of all {@link KnowledgeObjectInternal}s cached by
	 * this cache.
	 * 
	 * @see #getReferenceAttribute()
	 */
	public KnowledgeItem getBaseItem() {
		return _baseItem;
	}

	/**
	 * The attribute that is used to cache associations, i.e. the cache contains object which
	 * references via the returned attribute to the {@link #getBaseItem() base item}.
	 */
	public final MOReference getReferenceAttribute() {
		return _referenceAttribute;
	}

	/**
	 * The type of all association links in this cache.
	 */
	public final MOKnowledgeItemImpl getAssociationType() {
		return _associationType;
	}

	/**
	 * Adjusts this cache in reaction to a global association change (after a commit or refetch).
	 * 
	 * @param revision
	 *        The revision number in which the change happened.
	 * @param link
	 *        The association link that was touched (created, deleted, modified).
	 */
	abstract void handleAssociationChange(long revision, KnowledgeItemInternal link);

	/**
	 * Whether this cache has valid values.
	 */
	public abstract boolean isFilled();

	abstract void update(long sessionRevision, List<T> globalItems);

	/**
	 * Adjusts this cache in reaction to an association change in the given context.
	 * 
	 * @param context
	 *        The context in which the change happened.
	 * @param link
	 *        The association link that was touched (created, deleted, modified).
	 * @param changeType
	 *        Type of the association change. Any of {@link KnowledgeObjectInternal#TYPE_CREATION},
	 *        {@link KnowledgeObjectInternal#TYPE_DELETION}, and
	 *        {@link KnowledgeObjectInternal#TYPE_MODIFICATION}.
	 */
	abstract void handleLocalAssociationChange(DBContext context, KnowledgeItemInternal link, int changeType);

	abstract C getLinksDirect();

	/**
	 * Publish changes in the given transaction.
	 * 
	 * @param context
	 *        The transaction successfully committed.
	 * @param localAssociations
	 *        locally changed associations
	 */
	protected abstract void internalCommit(DBContext context, Associations<T, C> localAssociations);

	/**
	 * Whether the given item has a {@link KnowledgeItem#tTable() type} that allows
	 *         it to be part of this cache.
	 */
	public abstract boolean hasCacheType(KnowledgeItem potentialLink);

}
