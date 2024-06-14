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
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.IdentifiedObject;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;

/**
 * Storage strategy that stores the referenced object instead of its {@link ObjectKey identifier}.
 * This strategy ensures that the referenced object is not removed from JVM cache until the
 * referencing object lives.
 * 
 * <p>
 * This implementation is a little bit tricky. Actually when loading the value the cache just
 * contains the {@link ObjectKey} of the referenced object. At the first access this referenced
 * object is resolved and the referenced object is stored in the cache, i.e. the reference is
 * resolved lazy and then the resolved object is stored.
 * </p>
 * 
 * <p>
 * This is done to be able to load bulk objects in one transaction. If the reference would be
 * resolved directly, then another database access occurs eventually (if reference was not load
 * before).
 * </p>
 * 
 * @see ByIDReferenceStorageImpl
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ByValueReferenceStorageImpl extends KnowledgeReferenceStorageImpl {

	/** Singleton {@link ByValueReferenceStorageImpl} instance. */
	public static final ByValueReferenceStorageImpl INSTANCE = new ByValueReferenceStorageImpl();

	/**
	 * Creates a {@link ByValueReferenceStorageImpl}.
	 */
	protected ByValueReferenceStorageImpl() {
		// singleton instance
	}

	@Override
	public Object fetchValue(DBHelper sqlDialect, ResultSet dbResult, int resultOffset, MOAttribute attribute,
			ObjectContext context) throws SQLException {
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
	public Object fromCacheToApplicationValue(MOAttribute attribute, ObjectContext context, Object cacheValue) {
		if (cacheValue == null) {
			return null;
		}
		long requestedHistoryContext = context.tId().getHistoryContext();
		ObjectKey cachedKey = getObjectKey(cacheValue);
		if (cachedKey.getHistoryContext() == requestedHistoryContext) {
			return getReferencedObject(context, cacheValue);
		}
		return context.resolveObject(adaptToContext(ref(attribute), context.tId(), cachedKey));
	}

	@Override
	public Object fromApplicationToCacheValue(MOAttribute attribute, Object applicationValue) {
		return applicationValue;
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
		Object applicationValue = fromCacheToApplicationValue(attribute, context, cacheValue);
		if (cacheValue instanceof ObjectKey) {
			if (applicationValue == null) {
				if (((MOReference) attribute).getDeletionPolicy() == DeletionPolicy.VETO) {
					// Target has been deleted, veto is not yet checked (only during commit), but
					// somebody tries to access the reference after deleting the referenced object.
					return null;
				}
				throw new KnowledgeBaseRuntimeException("Cache value '" + cacheValue + "' for attribute '" + attribute
					+ "' could not be resolved to an application value for " + item);
			}
			// the reference is currently not resolved. In such case the cache should be updated
			ObjectKey cachedKey = (ObjectKey) cacheValue;
			updateCache(ref(attribute), item, storage, cachedKey, (IdentifiedObject) applicationValue);
		}
		return applicationValue;
	}

	@Override
	protected void stabilizeCachedValue(MOAttribute attribute, KnowledgeItem item, Object[] storage, long historyContext) {
		/* It is not possible to resolve the object in the given history context, because that
		 * revision is currently created. So only the key is adapted and the reference object stored
		 * during next access. */
		ObjectKey referencedKey = getReferencedKey(attribute, item, storage);
		ObjectKey stableObjectKey = newAdaptedKey(historyContext, referencedKey);
		setCacheValue(attribute, item, storage, stableObjectKey);
	}

	/**
	 * Resolves the given cache value (either an {@link ObjectKey} or already an
	 * {@link IdentifiedObject} to the referenced object.
	 * 
	 * @see #getObjectKey(Object)
	 */
	protected final IdentifiedObject getReferencedObject(ObjectContext context, Object cacheValue) {
		if (cacheValue instanceof IdentifiedObject) {
			return (IdentifiedObject) cacheValue;
		} else {
			assert cacheValue instanceof ObjectKey : "Cache contains either '" + ObjectKey.class.getName()
				+ "' or '" + KnowledgeItem.class.getName() + "'.";
			ObjectKey cachedKey = (ObjectKey) cacheValue;
			return context.resolveObject(cachedKey);
		}
	}

	@Override
	protected ObjectKey getReferencedKey(MOAttribute attribute, DataObject item, Object[] storage) {
		Object cacheValue = getCacheValue(attribute, item, storage);
		if (cacheValue == null) {
			return null;
		}
		return getObjectKey(cacheValue);
	}

	/**
	 * Retrieves an {@link ObjectKey} from the given cache value (either already an
	 * {@link ObjectKey}, or an {@link IdentifiedObject}).
	 * 
	 * @see #getReferencedObject(ObjectContext, Object)
	 */
	protected final ObjectKey getObjectKey(Object cacheValue) {
		if (cacheValue instanceof ObjectKey) {
			return (ObjectKey) cacheValue;
		} else {
			assert cacheValue instanceof IdentifiedObject : "Cache contains either '" + ObjectKey.class.getName()
				+ "' or '" + IdentifiedObject.class.getName() + "'.";
			IdentifiedObject cachedReference = (IdentifiedObject) cacheValue;
			return cachedReference.tId();
		}
	}

}
