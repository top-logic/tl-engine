/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.top_logic.basic.NamedConstant;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.model.TLObject;

/**
 * Superclass for immutable {@link KnowledgeObjectInternal} implementations.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
abstract class AbstractImmutableKnowledgeObject extends ImmutableKnowledgeItem implements KnowledgeObjectInternal {

	private Map<NamedConstant, AbstractAssociationCache<?, ?>> associationCaches;

	private final Object[] _values;

	protected AbstractImmutableKnowledgeObject(DBKnowledgeBase kb, MOKnowledgeItem type) {
		super(kb, type);
		_values = newEmptyStorage();
	}

	@Override
	protected Object[] getGlobalValues(long sessionRevision) {
		// There is only one storage for all sessions.
		return _values;
	}

	@Override
	public long getLastUpdate() {
		return DBKnowledgeItem.getLastUpdate(this);
	}

	@Override
	public long getCreateCommitNumber() {
		return getCreateCommitNumber(this);
	}

	@Override
	public Iterator<KnowledgeAssociation> getOutgoingAssociations() {
		return KnowledgeObjectImpl.getOutgoingAssociations(this);
	}

	@Override
	public Iterator<KnowledgeAssociation> getIncomingAssociations() {
		return KnowledgeObjectImpl.getIncomingAssociations(this);
	}

	@Override
	public synchronized void dropLocalAssocationCache(DBContext caller) {
		KnowledgeObjectImpl.dropLocalAssociationCaches(associationCaches, caller);
	}

	@Override
	public Iterator<KnowledgeAssociation> getIncomingAssociations(String anAssociationType) {
		return KnowledgeObjectImpl.getIncomingAssociations(this, anAssociationType);
	}

	@Override
	public Iterator<KnowledgeAssociation> getOutgoingAssociations(String anAssociationType) {
		return KnowledgeObjectImpl.getOutgoingAssociations(this, anAssociationType);
	}

	@Override
	public Iterator<KnowledgeAssociation> getOutgoingAssociations(String anAssociationType,
			KnowledgeObject aKnowledgeObject) {
		return KnowledgeObjectImpl.getOutgoingAssociations(this, anAssociationType, aKnowledgeObject);
	}

	@Override
	public synchronized <T extends TLObject, C> AbstractAssociationCache<T, C> getAssociationCache(
			AbstractAssociationQuery<T, C> query) {
		if (this.associationCaches == null) {
			this.associationCaches = new HashMap<>();
		} else {
			@SuppressWarnings("unchecked")
			AbstractAssociationCache<T, C> existingCache =
				(AbstractAssociationCache<T, C>) this.associationCaches.get(query.getCacheKey());
			if (existingCache != null) {
				return existingCache;
			}
		}

		AbstractAssociationCache<T, C> newCache = ((AssociationQueryImpl<T, C>) query).createCache(this);
		this.associationCaches.put(query.getCacheKey(), newCache);
		return newCache;
	}

	@Override
	public synchronized void notifyLocalAssociationChange(DBContext context, MOReference reference,
			KnowledgeItemInternal link, int changeType) {
		KnowledgeObjectImpl.notifyLocalAssociationChange(this.associationCaches, context, reference, link, changeType);
	}

	@Override
	public synchronized void notifyAssociationChange(long revision, MOReference reference, KnowledgeItemInternal link) {
		KnowledgeObjectImpl.notifyAssociationChange(this.associationCaches, revision, reference, link);
	}

	@Override
	public FlexDataManager getFlexDataManager() {
		return super.getFlexDataManager();
	}

}

