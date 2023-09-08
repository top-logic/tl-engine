/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import com.top_logic.model.TLObject;

/**
 * {@link DBKnowledgeBase} internal implementation of {@link KnowledgeObject}.
 * 
 * @see ImmutableKnowledgeObject The immutable variant.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class DBKnowledgeObject extends WrappedKnowledgeItem implements KnowledgeObjectInternal {

	private Map<NamedConstant, AbstractAssociationCache<?, ?>> associationCaches;

	/**
	 * Construct a new empty {@link DBKnowledgeObject} based on the given {@link MOKnowledgeItem}.
	 */
	public DBKnowledgeObject(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
        super(kb, staticType);
    }

    @Override
	public Iterator<KnowledgeAssociation> getOutgoingAssociations() {
    	return KnowledgeObjectImpl.getOutgoingAssociations(this);
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
	public Iterator<KnowledgeAssociation> getIncomingAssociations() {
    	return KnowledgeObjectImpl.getIncomingAssociations(this);
    }

    @Override
	public Iterator<KnowledgeAssociation> getIncomingAssociations(String anAssociationType) {
    	return KnowledgeObjectImpl.getIncomingAssociations(this, anAssociationType);
    }

	@Override
	public synchronized void dropLocalAssocationCache(DBContext caller) {
		KnowledgeObjectImpl.dropLocalAssociationCaches(this.associationCaches, caller);
	}

	@Override
	public synchronized <T extends TLObject, C> AbstractAssociationCache<T, C> getAssociationCache(
			AbstractAssociationQuery<T, C> query) {
		if (this.associationCaches == null) {
			this.associationCaches = new HashMap<>();
		} else {
			AbstractAssociationCache<?, ?> existingCache = this.associationCaches.get(query.getCacheKey());
			if (existingCache != null) {
				@SuppressWarnings("unchecked")
				AbstractAssociationCache<T, C> result = (AbstractAssociationCache<T, C>) existingCache;
				return result;
			}
		}

		AbstractAssociationCache<T, C> newCache = ((AssociationQueryImpl<T, C>) query).createCache(this);
		this.associationCaches.put(query.getCacheKey(), newCache);
		return newCache;
	}
	
	@Override
	public synchronized void notifyLocalAssociationChange(DBContext context, MOReference reference, KnowledgeItemInternal link, int changeType) {
		KnowledgeObjectImpl.notifyLocalAssociationChange(this.associationCaches, context, reference, link, changeType);
	}

	@Override
	public synchronized void notifyAssociationChange(long revision, MOReference reference, KnowledgeItemInternal link) {
		KnowledgeObjectImpl.notifyAssociationChange(this.associationCaches, revision, reference, link);
	}

}
