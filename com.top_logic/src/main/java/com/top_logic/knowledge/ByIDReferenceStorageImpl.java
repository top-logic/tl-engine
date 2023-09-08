/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.IdentifiedObject;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeItem;

/**
 * Storage strategy that just stores the {@link ObjectKey identifier} of an referenced object,
 * instead of the object itself. That has the advantage that the JVM is not forced to hold the
 * referenced object in cache.
 * 
 * <p>
 * This is the default {@link MOReference#getStorage() storage strategy} for {@link KIReference}.
 * </p>
 * 
 * @see ByValueReferenceStorageImpl
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ByIDReferenceStorageImpl extends KnowledgeReferenceStorageImpl {

	/** Singleton {@link ByIDReferenceStorageImpl} instance. */
	public static final ByIDReferenceStorageImpl INSTANCE = new ByIDReferenceStorageImpl();

	private ByIDReferenceStorageImpl() {
		// singleton instance
	}

	@Override
	public Object fromCacheToApplicationValue(MOAttribute attribute, ObjectContext context,
			Object cacheValue) {
		assertValidCacheValue(cacheValue);
		ObjectKey adoptedKey = adaptToContext(ref(attribute), context.tId(), (ObjectKey) cacheValue);
		if (adoptedKey != null) {
			return context.resolveObject(adoptedKey);
		} else {
			return null;
		}
	}

	private void assertValidCacheValue(Object cacheValue) {
		if (cacheValue == null || cacheValue instanceof ObjectKey) {
			return;
		}
		StringBuilder invalidCacheValue = new StringBuilder();
		invalidCacheValue.append("Only ");
		invalidCacheValue.append(ObjectKey.class.getName());
		invalidCacheValue.append(" are valid cache values of ");
		invalidCacheValue.append(this);
		invalidCacheValue.append(". ");
		invalidCacheValue.append(cacheValue);
		invalidCacheValue.append(" of type ");
		invalidCacheValue.append(cacheValue.getClass().getName());
		invalidCacheValue.append(" not allowed.");
		throw new IllegalArgumentException(invalidCacheValue.toString());
	}

	@Override
	public Object fromApplicationToCacheValue(MOAttribute attribute, Object applicationValue) {
		if (applicationValue == null) {
			return null;
		} else {
			assert applicationValue instanceof IdentifiedObject : "The application values of " + this + " are "
				+ IdentifiedObject.class.getName();
			return ((IdentifiedObject) applicationValue).tId();
		}
	}

	@Override
	public Object fetchValue(DBHelper sqlDialect, ResultSet dbResult, int resultOffset,
			MOAttribute attribute, ObjectContext context)
			throws SQLException {
		return loadObjectKey(sqlDialect, dbResult, resultOffset, ref(attribute), context);
	}

	@Override
	public void storeValue(ConnectionPool pool, Object[] stmtArgs, int stmtOffset, MOAttribute attribute,
			DataObject item, Object[] storage, long currentCommitNumber) throws SQLException {
		KnowledgeItem ki = (KnowledgeItem) item;
		ObjectKey cachedKey = getReferencedKey(attribute, ki, storage);
		storeObjectKey(stmtArgs, stmtOffset, ref(attribute), ki, storage, currentCommitNumber, cachedKey);
	}

	@Override
	protected ObjectKey getReferencedKey(MOAttribute attribute, DataObject item, Object[] storage) {
		return getCacheValue(attribute, item, storage);
	}

	@Override
	protected void stabilizeCachedValue(MOAttribute attribute, KnowledgeItem item, Object[] storage, long historyContext) {
		ObjectKey oldCacheValue = getCacheValue(attribute, item, storage);
		ObjectKey stableObjectKey = newAdaptedKey(historyContext, oldCacheValue);
		setCacheValue(attribute, item, storage, stableObjectKey);
	}

	/**
	 * The value of an reference attribute is the object key of the referenced object.
	 * 
	 * @see AttributeStorage#getCacheValue(MOAttribute, DataObject, java.lang.Object[])
	 */
	@Override
	public ObjectKey getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
		return (ObjectKey) super.getCacheValue(attribute, item, storage);
	}

	/**
	 * The value of an reference attribute is the object key of the referenced object.
	 * 
	 * @see AttributeStorage#setCacheValue(MOAttribute, DataObject, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public Object setCacheValue(MOAttribute attribute, DataObject item, Object[] storage, Object cacheValue) {
		assertValidCacheValue(cacheValue);
		return super.setCacheValue(attribute, item, storage, cacheValue);
	}

	@Override
	public Object setApplicationValue(MOAttribute attribute, DataObject item, ObjectContext context, Object[] storage,
			Object applicationValue) {
		final Object cacheValue = fromApplicationToCacheValue(attribute, applicationValue);
		final Object oldCacheValue = setCacheValue(attribute, item, storage, cacheValue);
		return fromCacheToApplicationValue(attribute, context, oldCacheValue);
	}

	@Override
	public Object getApplicationValue(MOAttribute attribute, DataObject item, ObjectContext context, Object[] storage) {
		Object cacheValue = getCacheValue(attribute, item, storage);
		Object cachedReference = fromCacheToApplicationValue(attribute, context, cacheValue);
		if (cachedReference != null) {
			updateCache(ref(attribute), item, storage, cacheValue, (IdentifiedObject) cachedReference);
		} else {
			if (cacheValue != null) {
				// This is odd, inconsistent data, broken reference?
				throw new InvalidLinkException(
					"Invalid reference '" + attribute + "' of '" + item + "': " + cacheValue);
			}
		}
		return cachedReference;
	}

}
