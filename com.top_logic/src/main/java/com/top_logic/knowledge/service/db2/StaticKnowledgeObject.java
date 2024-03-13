/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.SessionContext;
import com.top_logic.basic.TLID;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.LifecycleAttributes;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.exceptions.InvalidWrapperException;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;

/**
 * {@link DBKnowledgeBase} internal implementation of {@link KnowledgeObject}.
 * 
 * @see StaticImmutableKnowledgeObject The immutable variant.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StaticKnowledgeObject extends StaticKnowledgeItem implements KnowledgeObjectInternal, Wrapper {

	private Map<NamedConstant, AbstractAssociationCache<?, ?>> associationCaches;

	/**
	 * Construct a new empty {@link StaticKnowledgeObject} based on the given MOKnowledgeObject.
	 */
	public StaticKnowledgeObject(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
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
		KnowledgeObjectImpl.dropLocalAssociationCaches(associationCaches, caller);
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
	public synchronized void notifyLocalAssociationChange(DBContext context, MOReference reference, KnowledgeItemInternal link, int changeType) {
		KnowledgeObjectImpl.notifyLocalAssociationChange(this.associationCaches, context, reference, link, changeType);
	}

	@Override
	public synchronized void notifyAssociationChange(long revision, MOReference reference, KnowledgeItemInternal link) {
		KnowledgeObjectImpl.notifyAssociationChange(this.associationCaches, revision, reference, link);
	}

	@Override
	public int compareTo(Wrapper o) {
		return KBUtils.compareKey(tId(), o.tHandle().tId());
	}

	@Override
	public String getName() {
		if (MetaObjectUtils.hasAttribute(tTable(), AbstractWrapper.NAME_ATTRIBUTE)) {
			return (String) getKOValue(AbstractWrapper.NAME_ATTRIBUTE);
		} else {
			return KBUtils.getObjectKeyString(this);
		}
	}

	@Override
	public Long getLastModified() {
		return ((Long) this.getKOValue(LifecycleAttributes.MODIFIED));
	}

	@Override
	public Date getModified() {
		Long modified = (Long) this.getKOValue(LifecycleAttributes.MODIFIED);
		return ((modified != null) ? new Date(modified.longValue()) : null);
	}

	@Override
	public Person getModifier() {
		try {
			KnowledgeObject wrappedObject = this.tHandle();
			String contextId = (String) wrappedObject.getAttributeValue(LifecycleAttributes.MODIFIER);
			return getAuthor(wrappedObject, contextId);
		} catch (NoSuchAttributeException nsax) {
			Logger.error("Missing Liffecycle Excemption, strange", nsax, this);
		}
		return null;
	}

	@Override
	public Person getCreator() {
		try {
			KnowledgeObject wrappedObject = this.tHandle();
			String contextId = (String) wrappedObject.getAttributeValue(LifecycleAttributes.CREATOR);
			return getAuthor(wrappedObject, contextId);
		} catch (NoSuchAttributeException nsax) {
			Logger.error("Missing Liffecycle Excemption, strange", nsax, this);
		}
		return null;
	}

	private Person getAuthor(KnowledgeObject contextObject, String contextId) {
		if (!contextId.startsWith(SessionContext.PERSON_ID_PREFIX)) {
			return null;
		}
		TLID personId = IdentifierUtil.fromExternalForm(contextId.substring(SessionContext.PERSON_ID_PREFIX.length()));
		long historyContext = HistoryUtils.getCreateRevision(contextObject).getCommitNumber();
		return Person.byId(contextObject.getKnowledgeBase(), historyContext,
			personId);
	}

	@Override
	public Date getCreated() {
		Long theVal = (Long) this.getKOValue(LifecycleAttributes.CREATED);

		return ((theVal != null) ? new Date(theVal.longValue()) : null);
	}

	/**
	 * TODO #2829: Delete TL 6 deprecation
	 * 
	 * @deprecated Use {@link #tHandle()} or one of the other short-cuts for accessing underlying
	 *             data, e.g. {@link #tGetData(String)}.
	 * @deprecated Use {@link #tHandle()} instead
	 */
	@Override
	public final KnowledgeObject getWrappedObject() {
		return tHandle();
	}

	@Override
	public KnowledgeObject tHandle() {
		return (KnowledgeObject) super.tHandle();
	}

	private void checkInvalid() throws InvalidWrapperException {
		if (!tValid()) {
			throw new InvalidWrapperException("Wrapper without KnowledgeBase " + this);
		}
	}

	@Override
	public Object getValue(String attributeName) {
		return getKOValue(attributeName);
	}

	@Override
	public void setValue(String attributeName, Object aValue) {
		setKOValue(attributeName, aValue);
	}

	private Object getKOValue(String attributeName) {
		if (!tValid()) {
			return null;
		}

		try {
			return getAttributeValue(attributeName);
		} catch (NoSuchAttributeException ex) {
			Logger.error("Access to unknown attribute '" + attributeName + "' in '" + this + "'.", ex,
				AbstractWrapper.class);
			return null;
		}
	}

	private boolean setKOValue(String attributeName, Object newValue) {
		if (!tValid()) {
			throw new WrapperRuntimeException("Access to invalid object.");
		}

		{
			setAttributeValue(attributeName, newValue);
			return true;
		}
	}

	/**
	 * TODO #2829: Delete TL 6 deprecation.
	 * 
	 * @deprecated Use {@link #tValid()} instead
	 */
	@Deprecated
	@Override
	public final boolean isValid() {
		return tValid();
	}

	@Override
	public final boolean tValid() {
		return isAlive();
	}

}
