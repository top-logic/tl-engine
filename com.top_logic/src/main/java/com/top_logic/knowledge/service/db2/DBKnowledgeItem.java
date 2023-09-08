/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.InlineSet;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.MODefaultProvider;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.IdentifiedObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.ChangeInspectable;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.util.Utils;

/**
 * {@link DBKnowledgeBase} internal implementation of {@link KnowledgeItem}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public abstract class DBKnowledgeItem extends AbstractDBKnowledgeItem implements ChangeInspectable {

	/**
	 * Alive state of this {@link KnowledgeItem}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	static enum AliveState {

		/**
		 * The {@link KnowledgeItem} is alive.
		 */
		ALIVE() {
			@Override
			public boolean isAlive() {
				return true;
			}
		},

		/**
		 * The object was persistent (i.e. there is a persistent version of it), but is deleted yet.
		 * 
		 * <p>
		 * Note: This state is always used when the object has no values for the requested version,
		 * e.g. if the user tried to access the object with a revision smaller than the create
		 * revision.
		 * </p>
		 */
		PERSISTENT_OBJECT_DELETED,

		/**
		 * The object is persitent (i.e. there is a persistent version of it), but is locally
		 * deleted, i.e. the object is deleted but the {@link Transaction} is not yet committed.
		 * 
		 */
		PERSISTENT_OBJECT_LOCALLY_DELETED,

		/**
		 * The object is created and deleted, before it was committed, or it is a new object which
		 * was created by another user.
		 * 
		 * @see #NEW_OBJECT_DELETED
		 * @see #NEW_OBJECT_FROM_FOREIGN_CONTEXT
		 * 
		 * @implNote With the given implementation, if a {@link DBContext} is present, it cannot be
		 *           determined whether a new object was directly deleted again or whether it was
		 *           never created.
		 */
		NEW_OBJECT_DELETED_OR_CREATED_IN_FOREIGN_CONTEXT,

		/**
		 * The object is created and deleted, before it was committed.
		 */
		NEW_OBJECT_DELETED,

		/**
		 * The object is new, but created by another user.
		 */
		NEW_OBJECT_FROM_FOREIGN_CONTEXT,
		;

		/**
		 * Whether this {@link AliveState} represents a valid state in which the object can be
		 * accessed.
		 */
		public boolean isAlive() {
			return false;
		}

	}

	private static final Values VALUES_NOT_LOADED_YET = EmptyValues.INSTANCE;

	/**
	 * {@link QueryResult} that includes the common attributes of a
	 * {@link KnowledgeItem}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface KnowledgeItemResult extends ItemResult {

		/**
		 * The name of the {@link KnowledgeItem#tTable() type} of the
		 * item.
		 */
		String getTypeName() throws SQLException;
		
		/**
		 * The value of the {@link BasicTypes#BRANCH_ATTRIBUTE_NAME} attribute.
		 */
		long getBranch() throws SQLException;

		/**
		 * The value of the {@link BasicTypes#IDENTIFIER_ATTRIBUTE_NAME} attribute.
		 */
		TLID getIdentifier() throws SQLException;
		
		/**
		 * The value of the {@link BasicTypes#REV_MAX_ATTRIBUTE_NAME} attribute.
		 */
		long getRevMax() throws SQLException;
		
		/**
		 * The value of the {@link BasicTypes#REV_MIN_ATTRIBUTE_NAME} attribute.
		 */
		long getRevMin() throws SQLException;

	}

	/** Storage object holding global data without local changes. */
	private Values _values;

	/* package protected */DBKnowledgeItem(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
		super(kb, staticType);
    }

	@Override
	public final void touch() {
		internalTouch(createDBContext());
    }

	/**
	 * Registers this item in the given context.
	 */
	final void internalTouch(DBContext context) {
		checkAlive(context);
		/* fetch local variables because it creates the local array in given context. This is
		 * necessary in DynamicKI, because the value of the koAttribute REV_MIN which is stored in
		 * the array is used to detect lost updates. */
		getLocalValues(context, IN_SESSION_REVISION, true);
	}

	@Override
	public State getState() {
		return isPersistent() ? State.PERSISTENT : State.NEW;
	}

	@Override
	public final Object getValue(MOAttribute attribute, long revision) {
		return getAttributeValue(getCurrentDBContext(), attribute, revision, this);
	}

	@Override
	public final Object getAttributeValue(String attributeName, long revision) throws NoSuchAttributeException {
		return getAttributeValue(getCurrentDBContext(), attributeName, revision, this);
	}

	@Override
	public final Object getGlobalAttributeValue(String attribute, long revision) throws NoSuchAttributeException {
		return getAttributeValue(null, attribute, revision, this);
	}

	@Override
	public final ObjectKey getReferencedKey(MOReference reference, long revision) {
		return lookupKey(reference, getLocalValues(getCurrentDBContext(), revision, false));
	}

	@Override
	public ObjectKey getGlobalReferencedKey(MOReference reference) {
		if (noGlobalValues()) {
			/* If this item is new, there are no "global" values. Therefore there is no reference to
			 * another object. */
			return null;
		}
		return super.getGlobalReferencedKey(reference);
	}

	private boolean noGlobalValues() {
		return getValues() == null;
	}

	/**
	 * Implementation of {@link #getAttributeValue(String)}.
	 * 
	 * @param modificationContext
	 *        Current modification context. May be <code>null</code>, in case no local changes are
	 *        made or must be ignored.
	 * @param attributeName
	 *        see {@link #getAttributeValue(String)}
	 * @param revision
	 *        The revision to get value in.
	 * @param objectContext
	 *        a reference to this object.
	 */
	Object getAttributeValue(DBContext modificationContext, String attributeName, long revision,
			ObjectContext objectContext) throws NoSuchAttributeException {
		MOClass metaObject = tTable();
		final MOAttribute attribute = metaObject.getAttribute(attributeName);
		return getAttributeValue(modificationContext, attribute, revision, objectContext);
	}

	Object getAttributeValue(DBContext modificationContext, MOAttribute attribute, long revision,
			ObjectContext objectContext) {
		Object[] localValues;
		checkAlive(modificationContext, revision);
		localValues = getLocalValues(modificationContext, revision, false);
		return lookupValue(attribute, localValues, objectContext);
    }

	@Override
	public Object setAttributeValue(String attributeName, Object newValue)
			throws DataObjectException {
		MOAttribute attribute = tTable().getAttribute(attributeName);
		return setValue(attribute, newValue);
	}

	@Override
	public final Object setValue(MOAttribute attribute, Object newValue) throws DataObjectException {
		DBContext context = this.getCurrentDBContext();
		checkLocallyModifiable(context);

		Object[] values = getLocalValues(context, IN_SESSION_REVISION, false);
		Object oldValue = getApplicationValue(attribute, values);
		if (attribute.getStorage().sameValue(attribute, newValue, oldValue)) {
        	return oldValue;
        }
        
		attribute.getStorage().checkAttributeValue(attribute, this, newValue);
		checkInitialValueNotSet(attribute, oldValue);
        
		Object[] localValues;
		if (context == null) {
			context = createDBContext();
			localValues = getLocalValues(context, IN_SESSION_REVISION, true);
		} else {
			if (hasLocalValues(context)) {
				/* Current values array already contains the local, modifiable values */
				localValues = values;
			} else {
				localValues = getLocalValues(context, IN_SESSION_REVISION, true);
			}
		}
        
		setApplicationValue(attribute, localValues, newValue);
		
		context.notifyAttributeValueChange(this, attribute, oldValue, newValue);
		
		return oldValue;
	}

	/**
	 * Checks that the object can be modified in the given context.
	 */
	protected void checkLocallyModifiable(DBContext context) {
		checkAlive(context);
		if (context != null) {
			context.checkModifiable();
		}
	}

	/**
	 * Invalidates a new item.
	 * 
	 * @see #invalidate(long) Invalidation of a persistent item.
	 */
	void invalidateNew() {
		assert !isPersistent();
		invalidateWrapper();
	}

	/**
	 * Invalidates a persistent item.
	 * 
	 * @param invalidateRevision
	 *        Revision in which this item is invalidated.
	 * 
	 * @see #invalidateNew() Invalidation of a new item.
	 */
	synchronized void invalidate(long invalidateRevision) {
		getValues().updateMaxValidity(invalidateRevision - 1);
		updateValues(deletedValuesFrom(invalidateRevision));
		invalidateWrapper();
	}

	@Override
	public final void onLoad(PooledConnection readConnection) {
		initOnLoad(readConnection);
		initWrapper();
	}

	/**
	 * Create the application object for this item.
	 */
	protected abstract void initWrapper();

	/**
	 * Invalidates the application object for this item.
	 */
	protected abstract void invalidateWrapper();

	@Override
	protected void initIdentifier(DBObjectKey newIdentity) {
		if (newIdentity.getHistoryContext() != Revision.CURRENT_REV) {
			StringBuilder error = new StringBuilder();
			error.append("Instances of ");
			error.append(DBKnowledgeItem.class.getName());
			error.append(" represent current objects. Given key '");
			error.append(newIdentity);
			error.append("' is a historic one.");
			throw new IllegalArgumentException(error.toString());
		}
		super.initIdentifier(newIdentity);
	}

	/**
	 * Initialize state after loading this item from persistent storage.
	 * 
	 * @param readConnection
	 *        See {@link #onLoad(PooledConnection)}
	 */
	protected void initOnLoad(PooledConnection readConnection) {
		// nothing to load here
	}

	@Override
	protected final Object[] getLocalValues() {
		return getLocalValues(getCurrentDBContext(), IN_SESSION_REVISION, false);
	}

	@Override
	protected Object[] getValuesForInitialisation(DBContext context) {
		return getLocalValues(getCurrentDBContext(), IN_SESSION_REVISION, true);
	}

	@Override
	protected final Object[] getGlobalValues(long sessionRevision) {
		Values currentValues = findValues(getRevision(sessionRevision));
		if (!currentValues.isAlive()) {
			throw new DeletedObjectAccess("Requested data of deleted " + this);
		}
		return currentValues.getData();
	}

	/**
	 * Returns the values object that represents the data in the given revision.
	 * 
	 * @param revision
	 *        The revision to get data in. Must <b>not</b> be {@link #IN_SESSION_REVISION}.
	 * 
	 * @see #getValues() Returning of the current values.
	 */
	protected Values findValues(long revision) {
		Values currentValues = findValues(revision, getValues());
		if (currentValues == VALUES_NOT_LOADED_YET) {
			return updateDataForRevision(revision);
		}
		return currentValues;
	}

	private synchronized Values findValues(long revision, Values currentValues) {
		if (revision > currentValues.maxValidity()) {
			/* This session is to new for currently loaded data. */
			return VALUES_NOT_LOADED_YET;
		}
		if (revision >= currentValues.minValidity()) {
			return currentValues;
		}
		while (true) {
			Values olderValues = currentValues.formerValidity();
			if (olderValues == null) {
				/* This revision is to old for the currently loaded data. */
				return VALUES_NOT_LOADED_YET;
			}
			if (revision > olderValues.maxValidity()) {
				/* There is a gap between the current value and next older loaded values. This
				 * session needs data within this gap. */
				return VALUES_NOT_LOADED_YET;
			}
			if (revision < olderValues.minValidity()) {
				/* Data are to new for the requested revision, find older. */
				currentValues = olderValues;
				continue;
			}
			return olderValues;
		}
	}

	private Values updateDataForRevision(long currentRevision) {
		Object[] data = fetchData(currentRevision);
		if (data == null) {
			// Item is not alive at this time. To avoid re-fetching many times for that revision a
			// values object that represents deleted values is installed.
			return updateValues(deletedValuesAt(currentRevision));
		} else {
			Values loadedValues = createValues(data);
			return updateValues(loadedValues);
		}
	}

	/**
	 * Fetches the data for the given revision from the database. Therefore this method should not
	 * be called from within a synchronized block.
	 */
	private Object[] fetchData(long currentRevision) {
		DBKnowledgeBase kb = getKnowledgeBase();
		ConnectionPool pool = kb.getConnectionPool();
		PooledConnection connection = pool.borrowReadConnection();
		try {
			DBAccess dbAccess = kb.getDBAccess(tTable());
			return dbAccess.fetch(connection, this, currentRevision);
		} catch (SQLException ex) {
			String error = "Lookup for data of item " + this + " in revision " + currentRevision + " failed.";
			throw new KnowledgeBaseRuntimeException(error, ex);
		} finally {
			pool.releaseReadConnection(connection);
		}
	}

	/**
	 * Initialises the value of {@link #getGlobalValues()} from the given values.
	 * 
	 * @param validFrom
	 *        The minimum revision in which the data are valid.
	 * @param validTo
	 *        The maximum revision (inclusive) in which the data are valid.
	 */
	protected void initGlobalValues(Object[] globalValues, long validFrom, long validTo) {
		setValues(newValues(validFrom, validTo, globalValues));
	}
	

	@Override
	protected void loadAttributeValues(ResultSet resultSet, int dbOffset) throws SQLException {
		Object[] globalValues = newEmptyStorage();
		loadAttributeValues(resultSet, dbOffset, globalValues);
		long validFrom = longValue(getApplicationValue(revMinAttribute(), globalValues));
		long validTo = longValue(getApplicationValue(revMaxAttribute(), globalValues));
		initGlobalValues(globalValues, validFrom, validTo);
	}

	/**
	 * Retrieves the application value of this item for the the given attribute from the given
	 * storage object.
	 * 
	 * @param attribute
	 *        The attribute to get value for.
	 * @param storage
	 *        The storage holding the values.
	 */
	private Object getApplicationValue(MOAttribute attribute, Object[] storage) {
		return attribute.getStorage().getApplicationValue(attribute, this, this, storage);
	}

	/**
	 * Sets the given application value of this item for the the given attribute into the given
	 * storage object.
	 * 
	 * @param attribute
	 *        The attribute to set value for.
	 * @param storage
	 *        The storage holding the values.
	 * @param value
	 *        The new application value to set.
	 * 
	 * @return The application value formerly contained in the storage.
	 */
	private Object setApplicationValue(MOAttribute attribute, Object[] storage, Object value) {
		Object oldValue = attribute.getStorage().setApplicationValue(attribute, this, this, storage, value);
		for (MOAttribute attr : MetaObjectUtils.getCacheAttributes(tTable())) {
			MetaObjectUtils.resetCacheAttribute(attr, storage, this, attribute);
		}
		return oldValue;
	}

	/**
	 * Returns the values that are valid for the given {@link DBContext}.
	 * 
	 * <p>
	 * The returned array must only be modified if <code>forModification</code> is <code>true</code>
	 * or {@link #hasLocalValues(DBContext)} is <code>true</code>.
	 * </p>
	 * 
	 * @param context
	 *        May be <code>null</code>. In such case the global values in the given revision are
	 *        returned.
	 * @param revision
	 *        The revision for which the values are needed.
	 * @param forModification
	 *        If <code>true</code> and there are no local values stored in the given context, a copy
	 *        of the local values are created, stored and returned.
	 * 
	 * @see #hasLocalValues(DBContext)
	 */
	protected final Object[] getLocalValues(DBContext context, long revision, boolean forModification) {
    	// Note: This is an internal method that must not check the aliveness of
		// the item, because it is used during cache update after deletion.
    	//
    	// checkAlive();
    	
		if (context != null) {
			if (!isPersistent()) {
				if (!context.isCreationContext(this)) {
					throw new IllegalStateException("Access to new object from outside the creating thread.");
				}
				// New objects have no copy for local modifications.
				return context.getLocalValues(this);
			}

			Object[] localValues;
			Object[] knownModifications = context.getLocalValues(this);
			if (knownModifications != null) {
				localValues = knownModifications;
			} else {
				if (forModification) {
					localValues = createLocalValues(context, getGlobalValues(revision));
				} else {
					localValues = getGlobalValues(revision);
				}
			}
			return localValues;
		} else {
			assert forModification == false : "Can not return values for modification without context.";

			// The current thread is not within a transaction.
			if (!isPersistent()) {
				throw new IllegalStateException("Access to new object from outside the creating thread.");
			}
			return getGlobalValues(revision);
		}
	}

	/**
	 * Whether the given {@link DBContext} has changes for this item.
	 */
	private boolean hasLocalValues(DBContext context) {
		return context.getLocalValues(this) != null;
	}

	private Object[] createLocalValues(DBContext context, Object[] globalValues) {
		Object[] localValues = ArrayUtil.copy(globalValues);
		context.initLocalValues(this, localValues);

		// Register object in the commit context.
		context.changedObject(this);
		return localValues;
	}

	/**
	 * Commit the Object locally.
	 * 
	 * Will be called <em>after</em> the sucesfull DBContext commit
	 */
	@Override
	protected void localCommit(DBContext commitContext) {
		if (!this.isPersistent()) {
			this.setValues(this.newValues(commitContext, State.NEW));
		} else {
			this.updateValues(this.newValues(commitContext, State.PERSISTENT));
		}
	}

	/**
	 * Rollback the Object locally.
	 */
	@Override
	protected void localRollback() {
		if (!isPersistent()) {
			invalidateNew();
		}
	}

	/**
	 * Determines whether the {@link AliveState} of this item in the given session revision.
	 * 
	 * @param context
	 *        the {@link DBContext} of the current session, or <code>null</code> when no context
	 *        should be regarded.
	 * @param sessionRevision
	 *        This is the revision of the session to check.
	 */
	private AliveState aliveStateInSession(DBContext context, long sessionRevision) {
		boolean locallyDeleted;
		if (context == null) {
			// item not modified in current session.
			locallyDeleted = false;
		} else {
			locallyDeleted = context.isRemovedKey(tId());
		}
		if (locallyDeleted) {
			// deleted in current session.
			return AliveState.PERSISTENT_OBJECT_LOCALLY_DELETED;
		}

		return valuesAlive(getRevision(sessionRevision));
	}

	/**
	 * Checks whether the {@link Values} at the given revision represents {@link Values#isAlive()
	 * alive} data.
	 * 
	 * @param dataRevision
	 *        The revision to fetch values in.
	 * @return {@link AliveState} representing the alive state of the {@link Values} for the given
	 *         revision.
	 */
	AliveState valuesAlive(long dataRevision) {
		Values sessionValues = findValues(dataRevision);
		return sessionValues.isAlive() ? AliveState.ALIVE : AliveState.PERSISTENT_OBJECT_DELETED;
	}
	 		        
	/**
	 * Unconditionally update the attribute value at the given index.
	 * 
	 * <p>
	 * Can be called directly form within the framework to update attributes even on deleted
	 * objects. User updates must go through {@link #setAttributeValue(String, Object)}.
	 * </p>
	 * 
	 * @param modificationContext
	 *        Context in which setting the attribute occurred
	 */
	protected final void internalSetAttribute(DBContext modificationContext, MOAttribute attribute, Object value) {
		Object[] localChanges = getLocalValues(modificationContext, IN_SESSION_REVISION, true);
		setApplicationValue(attribute, localChanges, value);
	}

	/**
	 * Allocates a context if it does not yet exist.
	 * 
	 * @return The existing or newly allocated context.
	 */
	protected final DBContext createDBContext() {
		return getKnowledgeBase().createDBContext();
	}

	/**
	 * Allocates a context if it does not yet exist.
	 * 
	 * @return The existing or newly allocated context.
	 */
	protected final DBContext getCurrentDBContext() {
		return getKnowledgeBase().getCurrentDBContext();
	}
	
	private void checkInitialValueNotSet(MOAttribute attribute, Object value) throws DataObjectException {
		if (isPersistent()) {
			// Initial attributes are just relevant in new state.
			return;
		}
		if (!attribute.isInitial()) {
			// Non initial attributes are not relevant.
			return;
		}
		if (value != null) {
			throw new DataObjectException("Value for initial attribute '" + attribute + "' can not be set twice");
		}
	}

	@Override
	public long getCreateCommitNumber() {
		return getCreateCommitNumber(this);
	}

    @Override
	public long getLastUpdate() {
    	// Note: The revision of the last update (that is used to ensure that
		// the local modification are still the current version at the time of
		// the commit) must be used from the thread local state of this object. 
		// Otherwise, a concurrent refetch (which updates the global state)
		// would hide the lost update. This also requires that global value for
		// the last update property must be adjusted after the commit succeeded.
    	//
    	//    	checkAlive();
    	//    	// Make sure to return the original value in case this method is called
    	//		// from within the process of outdating and re-inserting an updated
    	//		// item.
    	//    	return (Long) attrValueContainer.getAttributeValue(REV_MIN_IDX);
    	//
		return getLastUpdate(this);
	}

	/**
	 * Returns the revision in which the given data are changed.
	 */
	static long getLastUpdate(AbstractDBKnowledgeItem item) {
		MOAttribute attribute = item.revMinAttribute();
		return longValue(attribute.getStorage().getApplicationValue(attribute, item, item, item.getLocalValues()));
	}
    
    /*package protected*/ final void setLastUpdateLocal(Long revMin) {
    	// Note: Since this method is only called from within the storing
		// process, no deletion checks must happen, otherwise, the commit fails
		// with an internal error by throwing a runtime exception (instead of
		// returning false). The only situation, in which this object can be
		// already deleted, is a concurrent delete on another node. This will
		// cause the commit to fail anyway. Therefore, the update of the local
		// values is not necessary.
    	// 
    	// checkAlive();
		internalSetAttribute(getCurrentDBContext(), revMinAttribute(), revMin);
    }
    
    /*package protected*/ final void setMaxValidity(DBContext modificationContext, Long revMax) {
		internalSetAttribute(modificationContext, revMaxAttribute(), revMax);
    }

	void refetch(ResultSet resultSet, int dbOffset) throws SQLException {
		Object[] refetchedValues = newEmptyStorage();
		ConnectionPool pool = getKnowledgeBase().getConnectionPool();
		loadAttribute(pool, resultSet, dbOffset, refetchedValues, revMinAttribute());
		loadAttribute(pool, resultSet, dbOffset, refetchedValues, revMaxAttribute());
		Values newValues = createValues(refetchedValues);
		Values valuesToUse = updateValues(newValues);
		if (valuesToUse == newValues) {
			loadAttributeValues(resultSet, dbOffset, refetchedValues);
		}
	}

	private Values createValues(Object[] data) {
		long validFrom = longValue(getApplicationValue(revMinAttribute(), data));
		long validTo = longValue(getApplicationValue(revMaxAttribute(), data));

		return newValues(validFrom, validTo, data);
	}
	
	 
	private synchronized Values updateValues(Values updatedValues) {
		Values currentValues = getValues();
		long updatedMinValidity = updatedValues.minValidity();
		long updatedMaxValidity = updatedValues.maxValidity();
		// Determine whether the values to update are newer or older than the current.
		boolean updatedValuesAreNewer;
		if (updatedMaxValidity == Revision.CURRENT_REV) {
			if (updatedMinValidity > currentValues.maxValidity()) {
				updatedValuesAreNewer = true;
			} else if (updatedMinValidity > currentValues.minValidity()) {
				assert currentValues.maxValidity() == Revision.CURRENT_REV : "Overlapping values: " + updatedValues
						+ " " + currentValues;
				updatedValuesAreNewer = true;
			} else {
				/* Update with older values which are not up-to-date: At the time the updated values
				 * were fetched from the database the values were the "current" value. In the
				 * meanwhile a change happened such that the given updatedValues is an out-dated
				 * object. Ignore that data! */
				return currentValues;
			}
		} else {
			if (updatedMinValidity > currentValues.minValidity()) {
				updatedValuesAreNewer = true;
			} else {
				updatedValuesAreNewer = false;
			}
		}
		if (updatedValuesAreNewer) {
			// updated values contains current data or historic data but newer than the known
			return updateWithNewerValues(updatedValues, updatedMinValidity, currentValues);
		} else {
			return updateWithOlderValues(updatedValues, updatedMaxValidity, updatedMinValidity, currentValues);
		}
	}

	private Values updateWithNewerValues(Values updatedValues, long updatedMinValidity, Values currentValues) {
		if (currentValues.minValidity() == updatedMinValidity) {
			/* Re-fetch of same range. Return the old current values. */
			long updatedMaxValidity = updatedValues.maxValidity();
			long currentMaxValidity = currentValues.maxValidity();
			if (updatedMaxValidity < currentMaxValidity) {
				/* Concurrent fetch of "current" data: Between the different fetches a commit could
				 * have been occurred. Therefore the maximum validity must be adapted. */
				assert currentMaxValidity == Revision.CURRENT_REV : "Different maximum validity for the same data range";
				currentValues.updateMaxValidity(updatedMaxValidity);
			}
			return currentValues;
		}
		if (currentValues.maxValidity() == Revision.CURRENT_REV) {
			assert updatedMinValidity > currentValues.minValidity() : "Overlapping values: " + updatedValues
				+ " " + currentValues;
			// The stored values suppose to contain current data. This is no longer correct
			currentValues.updateMaxValidity(updatedMinValidity - 1);
		}
		updatedValues.setFormerValidity(currentValues);
		getKnowledgeBase().registerForCleanup(currentValues, updatedValues);
		setValues(updatedValues);
		return updatedValues;
	}

	private Values updateWithOlderValues(Values updatedValues, long updatedMaxValidity, long updatedMinValidity,
			Values currentValues) {
		while (true) {
			if (updatedMinValidity == currentValues.minValidity()) {
				assert updatedMaxValidity <= currentValues.maxValidity() : "Overlapping values: " + updatedValues
					+ " " + currentValues;
				// same data are refetched
				return currentValues;
			}
			assert updatedMaxValidity < currentValues.minValidity() : "Overlapping values: " + updatedValues + " "
				+ currentValues;
			Values nextValues = currentValues.formerValidity();
			if (nextValues == null) {
				// updated values are older than all known
				currentValues.setFormerValidity(updatedValues);
				getKnowledgeBase().registerForCleanup(updatedValues, currentValues);
				break;
			}
			if (updatedMinValidity > nextValues.maxValidity()) {
				// there is a gap between the known values and the updated values are correct
				// for that gap
				updatedValues.setFormerValidity(nextValues);
				currentValues.setFormerValidity(updatedValues);
				getKnowledgeBase().registerForCleanup(updatedValues, currentValues);
				break;
			}
			currentValues = nextValues;
		}
		return updatedValues;
	}

	@Override
	public final boolean isAlive() {
		return this.isAlive(getCurrentDBContext());
	}

	final boolean isAlive(DBContext context) {
		return aliveState(context, IN_SESSION_REVISION).isAlive();
	}

	AliveState aliveState(DBContext context, long sessionRevision) {
		if (!isPersistent()) {
			if (context == null) {
				// item created in foreign context.
				return AliveState.NEW_OBJECT_FROM_FOREIGN_CONTEXT;
			}
			if (!context.isCreationContext(this)) {
				// item is accessed from a different context or already deleted.
				return AliveState.NEW_OBJECT_DELETED_OR_CREATED_IN_FOREIGN_CONTEXT;
			}
			return hasLocalValues(context) ? AliveState.ALIVE : AliveState.NEW_OBJECT_DELETED;
		} else {
			return aliveStateInSession(context, sessionRevision);
		}
	}

	/**
	 * Whether this item was successfully committed to the database, i.e. whether the object is not
	 * currently created.
	 */
	protected boolean isPersistent() {
		return !noGlobalValues();
	}

	@Override
	public final void checkAlive(DBContext context) throws DeletedObjectAccess {
		checkAlive(context, IN_SESSION_REVISION);
	}

	/**
	 * Checks whether this item is alive in the given context in the given revision.
	 * 
	 * @param context
	 *        May be <code>null</code>.
	 * @param revision
	 *        May be {@link AbstractDBKnowledgeItem#IN_SESSION_REVISION}.
	 */
	void checkAlive(DBContext context, long revision) {
		switch(aliveState(context, revision)) {
			case ALIVE:
				// Object alive.
				return;
			case NEW_OBJECT_FROM_FOREIGN_CONTEXT: {
				StringBuilder error = new StringBuilder();
				error.append("New object ");
				appendImplClassAndID(error);
				error.append(" is not visible in this session. It was created in a different context.");
				throw new DeletedObjectAccess(error.toString());
			}
			case NEW_OBJECT_DELETED: {
				StringBuilder error = new StringBuilder();
				error.append("New object ");
				appendImplClassAndID(error);
				error.append(" was deleted.");
				throw new DeletedObjectAccess(error.toString());
			}
			case NEW_OBJECT_DELETED_OR_CREATED_IN_FOREIGN_CONTEXT: {
				StringBuilder error = new StringBuilder();
				error.append("New object ");
				appendImplClassAndID(error);
				error.append(" was deleted or created in another context.");
				throw new DeletedObjectAccess(error.toString());
			}
			case PERSISTENT_OBJECT_LOCALLY_DELETED: {
				StringBuilder error = new StringBuilder();
				error.append("Object ");
				appendLocallyDeleted(error);
				error.append(" locally deleted.");
				throw new DeletedObjectAccess(error.toString());
			}
			case PERSISTENT_OBJECT_DELETED: {
				long dataRevision = getRevision(revision);
				Values values = findValues(dataRevision);
				Values formerValidity = values.formerValidity();
				if (formerValidity != null) {
					KnowledgeItem historicItem = getKnowledgeBase()
						.resolveObjectKey(KBUtils.ensureHistoryContext(tId(), formerValidity.maxValidity()));
					if (historicItem != null) {
						StringBuilder error = new StringBuilder();
						error.append("This object, the current version of ");
						error.append(historicItem);
						error.append(", was already deleted in revision ");
						error.append(values.minValidity());
						error.append(". Access revision: ");
						error.append(dataRevision);
						throw new DeletedObjectAccess(error.toString());
					}
				}
				StringBuilder error = new StringBuilder();
				error.append("Object ");
				appendImplClassAndID(error);
				error.append(" was deleted before or created after access revision ");
				error.append(dataRevision);
				error.append(".");
				throw new DeletedObjectAccess(error.toString());
			}
		}
	}

	private void appendImplClassAndID(StringBuilder out) {
		out.append("(");
		out.append("ID: ");
		out.append(tId());
		out.append("; ");
		out.append("Wrapper class: ");
		out.append(getWrapper().getClass().getName());
		out.append(")");
	}

	private void appendLocallyDeleted(StringBuilder out) {
		DBKnowledgeItem deletedItem = this;
		try {
			out.append(getKnowledgeBase().withoutModifications(deletedItem::toString));
		} catch (RuntimeException ex) {
			// Panic mode: Ensure that creating exception message does not fail.
			out.append("<serialization failed> ");
			appendImplClassAndID(out);
		}
	}

	void appendInvalidToString(StringBuilder out, AliveState aliveState, long revision) {
		switch (aliveState) {
			case ALIVE:
				break;
			case NEW_OBJECT_DELETED_OR_CREATED_IN_FOREIGN_CONTEXT:
			case NEW_OBJECT_DELETED:
			case NEW_OBJECT_FROM_FOREIGN_CONTEXT:
				appendImplClassAndID(out);
				break;
			case PERSISTENT_OBJECT_LOCALLY_DELETED:
				appendLocallyDeleted(out);
				break;
			case PERSISTENT_OBJECT_DELETED:
				long dataRevision = getRevision(revision);
				Values values = findValues(dataRevision);
				Values formerValidity = values.formerValidity();
				if (formerValidity != null) {
					KnowledgeItem historicItem = getKnowledgeBase()
						.resolveObjectKey(KBUtils.ensureHistoryContext(tId(), formerValidity.maxValidity()));
					if (historicItem != null) {
						out.append(historicItem);
						break;
					}
				}
				appendImplClassAndID(out);
				break;
		}
	}


	/**
	 * Whether this object was changed (modified or deleted) at a remote node (and re-fetched) while
	 * it is currently being committed on the local node.
	 * 
	 * @param dataRevision
	 *        The revision to fetch global values and compare against local modifications.
	 */
	/*package protected*/ boolean wasConcurrentlyChanged(long dataRevision) {
		if (!valuesAlive(dataRevision).isAlive()) {
			// refetch of deletion of this item.
			return true;
		}
		
		DBContext dbContext = getCurrentDBContext();
		assert dbContext != null : "Lost updates are only possible if there is current context.";

		Object[] localValues = dbContext.getLocalValues(this);
		assert localValues != null : "Local values are copied during touch.";

		MOAttribute attribute = revMinAttribute();
		long revMinLocal = longValue(getApplicationValue(attribute, localValues));
		long revMinGlobal = longValue(getApplicationValue(attribute, getGlobalValues(dataRevision)));
		
		if (revMinLocal < revMinGlobal) {
			// refetch of modification of this item.
			return true;
		} else {
			return false;
		}
	}

	private static long longValue(Object v) {
		return ((Long) v).longValue();
	}
	
	@Override
	public ItemChange getChange() {
		ObjectBranchId objectId = ObjectBranchId.toObjectBranchId(tId());
		Object[] oldValues;
		Object[] newValues;
		ItemChange event;
		DBContext modificationContext = getCurrentDBContext();
		if (!isPersistent()) {
			oldValues = null;
			Object[] localChanges = modificationContext.getLocalValues(this);
			if (localChanges == null) {
				// No changes have been occurred in the given context
				throw new IllegalStateException("Object " + this + " was created in different context.");
			}
			newValues = localChanges;
			event = new ObjectCreation(ChangeInspectable.NO_REVISION, objectId);
		} else {
			if (modificationContext == null) {
				// this item was not changed.
				return null;
			}
			if (!isAlive(modificationContext)) {
				// item is removed in current context.
				oldValues = getGlobalValues();
				newValues = null;
				event = new ItemDeletion(ChangeInspectable.NO_REVISION, objectId);
			} else {
				oldValues = getGlobalValues();
				Object[] localChanges = modificationContext.getLocalValues(this);
				if (localChanges == null) {
					// No changes have been occurred in the given context
					return null;
				}
				newValues = localChanges;
				event = new ItemUpdate(ChangeInspectable.NO_REVISION, objectId, true);
			}
		}
		boolean actuallyUnchanged = true;
		DBKnowledgeBase kb = getKnowledgeBase();
		Set<String> keyAttributes = kb.getKeyAttributes(tTable());
		for (MOAttribute attribute : tTable().getAttributes()) {
			if (attribute.isSystem()) {
				continue;
			}

			Object oldValue = oldValues == null ? null : getOldValue(kb, attribute, oldValues);
			Object newValue =
				newValues == null ? null : attribute.getStorage().getCacheValue(attribute, this, newValues);
			String attributeName = attribute.getName();
			if (keyAttributes.contains(attributeName) || !Utils.equals(oldValue, newValue)) {
				event.setValue(attributeName, getEventValue(oldValue), getEventValue(newValue), false);
				actuallyUnchanged = false;
			}
		}
		if (event instanceof ItemUpdate && actuallyUnchanged) {
			/* There is actually no change; the local changes are reverted in the transaction to the
			 * former values. */
			return null;
		}

		return event;
	}

	private Object getOldValue(DBKnowledgeBase kb, MOAttribute attribute, Object[] oldValues) {
		DBContext before = kb.installContext(ImmutableDBContext.INSTANCE);
		try {
			return attribute.getStorage().getCacheValue(attribute, this, oldValues);
		} finally {
			kb.installContext(before);
		}
	}

	/**
	 * Transforms the given value to a value usable in an ItemEvent.
	 */
	private Object getEventValue(Object value) {
		Object eventValue;
		if (value instanceof IdentifiedObject) {
			// May occur in case ReferenceByValue
			eventValue = ((IdentifiedObject) value).tId();
		} else {
			eventValue = value;
		}
		return eventValue;
	}

	/**
	 * Initialises the item with the given values.
	 * 
	 * @param context
	 *        The {@link DBContext} of the current thread.
	 * @param initialValues
	 *        A sequence of attribute names and the corresponding initial values.
	 */
	void init(DBContext context, Iterable<Entry<String, Object>> initialValues) throws DataObjectException {
		assert !isPersistent() : "Intialisation only in new objects.";
		Object[] localValues = getValuesForInitialisation(context);
		Object initialSetAttributes = InlineSet.newInlineSet();
		for (Entry<String, Object> entry : initialValues) {
			String attributeName = entry.getKey();
			initAttribute(attributeName, localValues, entry.getValue());
			initialSetAttributes = InlineSet.add(String.class, initialSetAttributes, attributeName);
		}
		for (MOAttribute attr : tTable().getAttributes()) {
			MODefaultProvider defaultProvider = attr.getDefaultProvider();
			if (defaultProvider != null && !InlineSet.contains(initialSetAttributes, attr.getName())) {
				initAttribute(attr, localValues, defaultProvider.createDefault(attr));
			}
			if (attr.isInitial()) {
				Object initialAttributeValue = getApplicationValue(attr, localValues);
				if (initialAttributeValue == null) {
					throw new DataObjectException("No non null value for initial attribute '" + attr + "' set.");
				}
			}
		}
	}

	/**
	 * Sets the given attribute to the given value initial. This method is used to initialise this
	 * new object. Therefore this methods must not have effects outside this object (e.g.
	 * registration at current context or update of some outer caches).
	 * 
	 * @param name
	 *        The name of the attribute to initialise.
	 * @param localValues
	 *        The local values to initialise attribute.
	 * @param value
	 *        The value of the attribute.
	 */
	protected void initAttribute(String name, Object[] localValues, Object value) throws DataObjectException {
		initAttribute(tTable().getAttribute(name), localValues, value);
	}

	/**
	 * Returns the locally changed dynamic values, or <code>null</code> when no dynamic values has
	 * been changed.
	 * 
	 * @param commitContext
	 *        The context in which commit occurs.
	 */
	abstract FlexData getLocalDynamicValues(CommitContext commitContext);

	@Override
	public void delete() throws DataObjectException {
		KnowledgeObjectImpl.delete(this);
	}
	
	/**
	 * Creates a new value holder object for this item.
	 * 
	 * @param revMin
	 *        The minimum revision in which the given values are valid.
	 * @param revMax
	 *        The maximum revision (inclusive) in which the given values are valid.
	 * @param storage
	 *        The actual data.
	 */
	protected Values newValues(long revMin, long revMax, Object[] storage) {
		return new ValuesImpl(revMin, revMax, storage);
	}

	/**
	 * Creates a new value holder object for this item with local values from given commit context.
	 * 
	 * @param commitContext
	 *        The context in which commit currently occur.
	 * @param currentState
	 *        The object of the state during modification, i.e. before the actual commit.
	 */
	protected Values newValues(DBContext commitContext, State currentState) {
		Object[] localChanges = commitContext.getLocalValues(this);
		assert localChanges != null : "Only object which are touched in context may be published. These objects have local values.";

		return newValues(commitContext.getCommitNumber(), Revision.CURRENT_REV, localChanges);
	}

	/**
	 * Returns the value holder object of this item.
	 * 
	 * <p>
	 * In a new object this is <code>null</code> until the object is committed.
	 * </p>
	 * 
	 * @see #findValues(long) The values for a given revision.
	 */
	protected synchronized Values getValues() {
		return _values;
	}

	/**
	 * Sets value of {@link #getValues()}
	 */
	private synchronized void setValues(Values values) {
		_values = values;
	}		 

	/**
	 * Creates a {@link Values} that represents deleted data at the given revision.
	 * 
	 * @param revision
	 *        The revision in which no data are available.
	 */
	protected Values deletedValuesAt(long revision) {
		return new DeletedValues(revision, revision);
	}

	/**
	 * Creates a {@link Values} that represents data deleted in the given revision.
	 * 
	 * @param revision
	 *        The first revision in which no data are available.
	 */
	protected Values deletedValuesFrom(long revision) {
		return new DeletedValues(revision, Revision.CURRENT_REV);
	}

}
