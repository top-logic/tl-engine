/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.IdentityHashSet;
import com.top_logic.basic.col.InlineList;
import com.top_logic.basic.col.InlineSet;
import com.top_logic.basic.message.Message;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.filt.DOTypeNameComparator;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.IdentifiedObject;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.journal.JournalLine;
import com.top_logic.knowledge.journal.JournalManager;
import com.top_logic.knowledge.journal.Journallable;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.Committable;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.KnowledgeBaseUIException;
import com.top_logic.knowledge.service.RefetchTimeout;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.service.event.CommitVetoException;
import com.top_logic.knowledge.service.event.Modification;
import com.top_logic.knowledge.service.merge.ChangedDeleted;
import com.top_logic.knowledge.service.merge.ConcurrentChange;
import com.top_logic.knowledge.service.merge.ConcurrentCreation;
import com.top_logic.knowledge.service.merge.DeletedChanged;
import com.top_logic.knowledge.service.merge.DeletingReference;
import com.top_logic.knowledge.service.merge.MergeConflictDescription;
import com.top_logic.knowledge.service.merge.MergeConflictException;
import com.top_logic.knowledge.service.merge.ReferenceToDeleted;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.TLContext;
import com.top_logic.util.message.MessageStoreFormat;

/**
 * For every Transaction in Progress in the DBKB there is a DBContext.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
/*package protected*/
class DefaultDBContext extends DBContext {

	/**
	 * Switch to enable tracing context allocations.
	 * 
	 * <p>
	 * Enabling this switch allows to easily find the context allocator in
	 * autorollback log messages.
	 * </p>
	 * 
	 * <p>
	 * Note: This switch must only be enabled for debugging, since it degrades
	 * system performance.
	 * </p>
	 * 
	 * @see #getAllocationStackTrace()
	 */
	private static final boolean DEBUG_CONTEXT_ALLOCATION = true;

    // LRUValues for static context are configured using Properties.
    
	/** The default number of Prepared Statements we cache. */
    public static final int  LRU_COUNT = 32; 

    /** The intial size for our internal Lists */
    public static final int INIT_SIZE = 16;

    /** The Maximum size for our internal Lists */
    public static final int MAX_SIZE  = 256;

	/**
	 * The connection that is used to process the commit.
	 * 
	 * <p>
	 * The connection is allocated lazily upon the first call to a
	 * {@link CommitContext} method that requires a connection, or at the latest
	 * when the knowledge base commit starts.
	 * </p>
	 */
	private PooledConnection lazyCommitConnection;

	/**
	 * Marker that the commit represented by this context has finished (either
	 * successfully or with a rollback).
	 * 
	 * <p>
	 * A closed {@link DBContext} has put back its connection to the pool and
	 * cannot re-allocate it.
	 * </p>
	 */
	private boolean closed;
	
	/**
	 * @see #lock()
	 * @see #unlock()
	 */
	private boolean locked;

    private TransactionImpl innermostTransaction;

    /** The ID of the Person that changed the object */ 
    private String updater;

    /** A List of objects to be committed. */
	private IdentityHashSet<Committable> committables;

    /** A List of objects to be deleted on commit. */
	private IdentityHashSet<Committable> committablesDeleted;

	/**
	 * Changed persistent {@link DBKnowledgeItem}s indexed by their object key.
	 */
	private HashMap<ObjectKey, DBKnowledgeItem> changedObjectsById;

	/**
	 * Newly created {@link DBKnowledgeItem}s indexed by their object key.
	 */
	private HashMap<ObjectKey, DBKnowledgeItem> newObjectsById;

	/**
	 * Locally deleted persistent {@link DBKnowledgeItem}s indexed by their
	 * object key.
	 */
	private HashMap<ObjectKey, DBKnowledgeItem> removedObjectsById;

	/**
	 * Mapping of {@link AssociationCache} to its context local
	 * {@link Associations} view.
	 * 
	 * @see #getLocalCache(AbstractAssociationCache)
	 */
	private Map<AbstractAssociationCache<?, ?>, Associations<?, ?>> localCaches =
		new HashMap<>();

	/**
	 * All locally touched objects (created, deleted, modified) which have a reference valued
	 * attribute indexed by the key of the referenced object.
	 * 
	 * @see #getAssociationChanges(ObjectKey)
	 */
	private Map<ObjectKey, IdentityHashSet<TLObject>> associationChangesByBaseId =
		new HashMap<>();

	/**
	 * Mapping of an {@link KBCache} to its value within the transaction handled by this context.
	 * 
	 * <p>
	 * Note: Currently all accesses to this variable must be synchronized because this
	 * {@link DBContext} is an {@link UpdateListener} at the {@link KnowledgeBase} and is updated
	 * concurrently.
	 * </p>
	 */
	private HashMap<KBCache<?>, Object> _kbCaches;

	/**
	 * Map containing the local changes for a {@link KnowledgeItem}.
	 * 
	 * <p>
	 * This map is an {@link IdentityHashMap}, because it is important that two item which are
	 * actually the same may have different local changes: During refetch an object may be created
	 * for which a cached copy exists. In such case the values of the fetched items are used to
	 * update values of the cached item. If this map were an ordinary {@link HashMap} and the items
	 * were equal, the local changes of the cached object were used as value for the fetched item.
	 * </p>
	 * 
	 * <p>
	 * This is actually a theoretical construct, because {@link KnowledgeItem}s are equal iff they
	 * are <code>==</code>.
	 * </p>
	 */
	private Map<KnowledgeItem, Object[]> _localChanges = new IdentityHashMap<>();

	/**
	 * Map containing the local changes of dynamic attributes for a {@link KnowledgeItem}.
	 * 
	 * @see #_localChanges
	 */
	private Map<KnowledgeItem, FlexData> _dynamicChanges = new IdentityHashMap<>();

	private RevisionImpl newRevision;

	/*package protected*/ final DBKnowledgeBase kb;

	private final Exception allocationStackTrace;

	private final ReferenceDeletionHelper deletionHelper;

	/**
	 * {@link InlineList} containing {@link MergeConflictDescription} due to update of the session
	 * to a new revision.
	 */
	private Object _mergeConflicts = InlineList.newInlineList();

    /**
	 * Create a new DBContext for the given database connection.
     * @param anUpdater
	 *        a name identifying the upodating Person.
	 */
	public DefaultDBContext(DBKnowledgeBase kb, String anUpdater) {
		super();
		this.kb = kb;
		
		this.updater = anUpdater;
		
		this.committables = new IdentityHashSet<>(INIT_SIZE);
		this.committablesDeleted = new IdentityHashSet<>(INIT_SIZE);
		
		this.changedObjectsById = new HashMap<>(INIT_SIZE);
		this.newObjectsById = new HashMap<>(INIT_SIZE);
		this.removedObjectsById = new HashMap<>(INIT_SIZE);
		
		this.allocationStackTrace = DEBUG_CONTEXT_ALLOCATION ? new Exception("AllocationStackTrace") : null;
		this.deletionHelper = new ReferenceDeletionHelper(kb, this);
	} 
	
	protected final RevisionImpl getNewRevision() {
		return newRevision;
	}
	
	@Override
	public Exception getAllocationStackTrace() {
		return allocationStackTrace;
	}
    
    /** some string Suiteable for debugging */
    @Override
	public String toString() {
        return "DBContext[" + updater  
           +  ']'
           +   " M " + (changedObjectsById != null ? Integer.toString(changedObjectsById.size()) : "-")
           +   " N " + (newObjectsById != null ? Integer.toString(newObjectsById.size())    : "-")
           +   " R " + (removedObjectsById != null ? Integer.toString(removedObjectsById.size())    : "-");
    }
    
	@Override
	void putNewObject(DBKnowledgeItem item) {
		checkLocked();

		// Add item to context
		insertNewItem(item);
    }

	@Override
	void finishPutNewObject(DBKnowledgeItem item) {
		notityCacheAboutCreation(item);
		notifyAssociationChange(item, KnowledgeObjectInternal.TYPE_CREATION);
	}

	@Override
	void putNewObjects(Iterable<? extends DBKnowledgeItem> items) {
		checkLocked();

		// Add items to context
		for (DBKnowledgeItem item : items) {
			insertNewItem(item);
		}
	}

	@Override
	void dropNewItem(DBKnowledgeItem item) {
		DBKnowledgeItem formerlyInserted = newObjectsById.remove(item.tId());
		if (formerlyInserted != item) {
			// revert invalid modification
			newObjectsById.put(formerlyInserted.tId(), formerlyInserted);
			throw new IllegalArgumentException("Different object with same ID exists in context: " + formerlyInserted);
		}
		_localChanges.remove(item);
	}
    
	private void insertNewItem(DBKnowledgeItem anItem) {
		DBKnowledgeItem clash = newObjectsById.put(anItem.tId(), anItem);
        if (clash != null) {
        	// Revert invalid modification.
        	newObjectsById.put(clash.tId(), clash);
        	
        	throw new IllegalArgumentException("Object with same ID already exists in context: " + anItem);
        }
		_localChanges.put(anItem, anItem.newEmptyStorage());
        beginAuto();
    }

    @Override
	/*package protected*/ KnowledgeItemInternal getNew(ObjectKey key) {
		DBKnowledgeItem dataItem = newObjectsById.get(key);
        return dataItem;
    }

    @Override
	/*package protected*/ void changedObject(DBKnowledgeItem anObject) {
		checkLocked();
		
		ObjectKey identity = anObject.tId();
		if (isChanged(identity)) {
			// already marked as changed
			return;
		}
		
		if (isNew(identity)) {
			// Object new in context. Will be inserted at commit time.
			return;
		}
		
		beginAuto();
		changedObjectsById.put(identity, anObject);
	}

    /**
	 * Called when rollback is calleed for a single Object.
	 */
    /*package protected*/ void internalRollbackObject(DBKnowledgeItem anObject) {
        changedObjectsById.remove(anObject.tId());
    }

	/**
	 * Mark the given item as locally removed.
	 * 
	 * <p>
	 * If the item to remove was not yet persistent, then the item is
	 * immediately invalidated.
	 * </p>
	 * 
	 * @param anObject
	 *        The item to remove.
	 * @return Whether the given item was marked as locally deleted.
	 *         <code>false</code>, if the given item was immediately
	 *         invalidated.
	 */
	@SuppressWarnings("unused")
	boolean removeItem(DBKnowledgeItem anObject) {
		
		beginAuto();
		ObjectKey identity = anObject.tId();

		/* Touching the removed item is currently necessary for technical reasons: For "outdating"
		 * the removed object the attribute REV_MAX is adapted in the local values of the item.
		 * Therefore the copy is needed. The copy is created here, because creating the item marks
		 * the object as changed. If the item is first removed and than changed, the commit process
		 * tries to execute the changes which fails, because the item is deleted. */
		anObject.internalTouch(this);

		boolean result;
		if (newObjectsById.containsKey(identity)) {
			Object[] localValues = getLocalValues(anObject);
			notifyNewAssociationDeleted(anObject, localValues);
			notityCacheAboutDeletion(anObject);
			/* Remove marker that this DBContext is the creation context.(see #isCreationContext()) */
			newObjectsById.remove(identity);
			anObject.invalidateNew();
			/* remove local change as this is the indicator whether the object is alive in this
			 * context. */
			_localChanges.remove(anObject);
			result = false;
		} else {
			notifyAssociationChanged(anObject, false, KnowledgeObjectInternal.TYPE_DELETION);
			notityCacheAboutDeletion(anObject);
			removedObjectsById.put(identity, anObject);
			changedObjectsById.remove(identity);
			if (false) {
				/* Do not remove local change as this is needed for "outdating" this item. see above */
				_localChanges.remove(anObject);
			}
			result = true;
		}
		
		if (anObject instanceof KnowledgeObjectInternal) {
			((KnowledgeObjectInternal) anObject).dropLocalAssocationCache(this);
		}

		return result;
	}

	@Override
	public void removeObject(DBKnowledgeItem object) throws DataObjectException {
		checkLocked();

		ReferenceDeletionHelper helper = deletionHelper;
		helper.collectChangesForDeleted(object);

		beginAuto();

		List<Modification> modifications = helper.executeCollectedChanges();

		for (Modification modification : modifications) {
			modification.execute();
		}
	}

	@Override
	void removeObjects(DBKnowledgeItem[] items, int size) throws DataObjectException {
		checkLocked();

		ReferenceDeletionHelper helper = deletionHelper;
		helper.collectChangesForDeleted(items, size);

		beginAuto();

		List<Modification> modifications = helper.executeCollectedChanges();

		for (Modification modification : modifications) {
			modification.execute();
		}
	}

	private void rollbackDeleted() {
		rollbackLocal(removedObjectsById.values());
	}

	private void rollbackLocal(Iterable<? extends AbstractDBKnowledgeItem> items) {
		for (AbstractDBKnowledgeItem item : items) {
			item.localRollback();
        }
	}

	private void rollbackChanged() {
		rollbackLocal(changedObjectsById.values());
	}

    @Override
	/*package protected*/ final void rollback() {
    	if (this.innermostTransaction == null) {
    		// Transaction has not been started or already rolled back.
    		return;
    	}

    	TransactionImpl canceledTransaction = this.innermostTransaction;
    	
    	TransactionImpl remainingTransaction;
    	if (canceledTransaction.isAnonymous()) {
    		remainingTransaction = canceledTransaction.getOuter();
    	} else {
    		remainingTransaction = canceledTransaction;
    	}
    	
        internalRollback(canceledTransaction, remainingTransaction, null, null);
    }

    /*package protected*/ final void rollbackTransaction(TransactionImpl canceledTransaction, Message message, Throwable cause) {
    	checkActive(canceledTransaction);
    	
    	internalRollback(canceledTransaction, canceledTransaction.getOuter(), message, cause);
    }
    
	private void internalRollback(TransactionImpl canceledTransaction, TransactionImpl remainingTransaction, Message rollbackMessage, Throwable cause) {
		if (remainingTransaction != null) {
			Logger.info("Rollback of pseudo nested transaction '" + canceledTransaction + "': " + MessageStoreFormat.toString(rollbackMessage), cause, DefaultDBContext.class);
			
        	// Mark parent transaction as failed since nested transaction are not supported.
			Message canceledMessage = canceledTransaction.getCommitMessage();
			if (! remainingTransaction.hasError()) {
				remainingTransaction.setError(Messages.NESTED_TRANSACTION_ROLLED_BACK__COMMIT_REASON.fill(canceledMessage, rollbackMessage));
			}
			
        	this.innermostTransaction = remainingTransaction;
        } else {
        	rollbackComplete(true);
        }
	}

    @Override
	/* package protected */boolean rollbackComplete(boolean success) {
    	if (this.innermostTransaction == null) {
    		// Transaction has not been started or already rolled back.
    		return true;
    	}

    	this.innermostTransaction = null;
        
		localRollback();
        
		dropContext(success);
        
        return true;
	}

    /**
	 * Insert the New Objects into the DB.
	 */
	private ArrayList<DBKnowledgeItem> commitNew(PooledConnection commitConnection, long commitNumber,
			Map<MOKnowledgeItem, Object> xrefChanges) throws SQLException {
		ArrayList<DBKnowledgeItem> result = new ArrayList<>(newObjectsById.values());
		initValidity(result);
		
		toPrimaryKeyOrder(result, true);
		
		for (Iterator<List<DBKnowledgeItem>> it = FilterUtil.groupBySorted(DOTypeNameComparator.SINGLETON, result); it
			.hasNext();) {
			List<DBKnowledgeItem> monomorphicObjects = it.next();

			DBKnowledgeItem object = monomorphicObjects.get(0);
			MOKnowledgeItem type = (MOKnowledgeItem) object.tTable();
			
			kb.getDBAccess(type).insertAll(commitConnection, commitNumber, monomorphicObjects);
			kb.getFlexDatamanager(type).addAll(monomorphicObjects, this);

			collectXrefChanges(type, monomorphicObjects, xrefChanges);
		}
		
		return result;
	}

	/**
	 * Sort the given list of objects according to the values of the primary key.
	 * 
	 * <p>
	 * Sorting is crucial for helping MySQL to perform well on large commits (> 30000 objects).
	 * </p>
	 * 
	 * @param result
	 *        The objects to sort.
	 * @param useLocalValues
	 *        whether the local values should be used to compare objects.
	 */
	private static void toPrimaryKeyOrder(ArrayList<? extends AbstractDBKnowledgeItem> result, boolean useLocalValues) {
		Comparator<? super AbstractDBKnowledgeItem > comparator;
		if (useLocalValues) {
			comparator = PrimaryKeyOrder.LOCAL_VALUE_COMPARATOR;
		} else {
			comparator = PrimaryKeyOrder.GLOBAL_VALUE_COMPARATOR;
		}
		Collections.sort(result, comparator);
	}

	private void initValidity(ArrayList<DBKnowledgeItem> objects) {
		for (int n = 0, cnt = objects.size(); n < cnt; n++) {
			DBKnowledgeItem item = objects.get(n);
			
			item.setMaxValidity(this, Revision.CURRENT_REV_WRAPPER);
		}
	}
	
	private void limitValidity(Long revMax, ArrayList<DBKnowledgeItem> objects) {
		for (int n = 0, cnt = objects.size(); n < cnt; n++) {
			objects.get(n).setMaxValidity(this, revMax);
		}
	}

	private ArrayList<DBKnowledgeItem> commitChanged(PooledConnection commitConnection, long commitNumber,
			Map<MOKnowledgeItem, Object> xrefChanges) throws SQLException {
		ArrayList<DBKnowledgeItem> result = new ArrayList<>(changedObjectsById.values());
		toPrimaryKeyOrder(result, true);

		for (Iterator<List<DBKnowledgeItem>> it = FilterUtil.groupBySorted(DOTypeNameComparator.SINGLETON, result); it
			.hasNext();) {
			List<DBKnowledgeItem> monomorphicObjects = it.next();

			DBKnowledgeItem object = monomorphicObjects.get(0);
			MOKnowledgeItem type = (MOKnowledgeItem) object.tTable();
			
			kb.getDBAccess(type).updateAll(commitConnection, commitNumber, monomorphicObjects);
			kb.getFlexDatamanager(type).updateAll(monomorphicObjects, this);
			
			collectXrefChanges(type, monomorphicObjects, xrefChanges);
		}
		
		return result;
	}

	private ArrayList<DBKnowledgeItem> commitRemoved(PooledConnection commitConnection, long commitNumber,
			Map<MOKnowledgeItem, Object> xrefChanges) throws SQLException {
		Collection<DBKnowledgeItem> removedObjects = removedObjectsById.values();
		ArrayList<DBKnowledgeItem> result = new ArrayList<>(removedObjects.size());
		for (Iterator<DBKnowledgeItem> it = removedObjects.iterator(); it.hasNext();) {
			DBKnowledgeItem removedObject = it.next();
			result.add(removedObject);
		}
		toPrimaryKeyOrder(result, false);
		
		limitValidity(Long.valueOf(commitNumber - 1), result);

		for (Iterator<List<DBKnowledgeItem>> it = FilterUtil.groupBySorted(DOTypeNameComparator.SINGLETON, result); it.hasNext(); ) {
			List<DBKnowledgeItem> monomorphicObjects = it.next();

			DBKnowledgeItem object = monomorphicObjects.get(0);
			MOKnowledgeItem type = (MOKnowledgeItem) object.tTable();
			
			kb.getDBAccess(type).deleteAll(commitConnection, commitNumber, monomorphicObjects);
			kb.getFlexDatamanager(type).deleteAll(monomorphicObjects, this);
			
			collectXrefChanges(type, monomorphicObjects, xrefChanges);
		}
		
		return result;
    }

	private void collectXrefChanges(MOKnowledgeItem type, List<? extends DBKnowledgeItem> monomorphicObjects,
			Map<MOKnowledgeItem, Object> changes) {
		if (type.isSystem()) {
			// Note: During migration, system object such as revisions might be
			// updated but must not be indexed in the xref table.
			return;
		}
		
		Object currentBranches = changes.get(type);
		Object newBranches = currentBranches;
		if (newBranches == null) {
			newBranches = InlineSet.newInlineSet();
		}

		for (int n = 0, cnt = monomorphicObjects.size(); n < cnt; n++) {
			DBKnowledgeItem item = monomorphicObjects.get(n);
			long branchContext = item.getBranchContext();
			newBranches = InlineSet.add(Long.class, newBranches, Long.valueOf(branchContext));
		}
		if (newBranches != currentBranches) {
			changes.put(type, newBranches);
		}
	}

	/**
	 * Insert the New Object into the cache and send the needed events.
	 */
	private void insertNew(ArrayList<DBKnowledgeItem> newObjects) {
        for (int n = 0, cnt = newObjects.size(); n < cnt; n++) {
			DBKnowledgeItem newObject = newObjects.get(n);
        	
			kb.insertNew(this, newObject);
        }
		insertNewRevision(newRevision);
    }

	/** 
	 * Inserts the new revision into the cache of the {@link KnowledgeBase}. 
	 */
	protected void insertNewRevision(RevisionImpl revision) {
		kb.insertNew(this, revision);
	}

	private void publishChanges(ArrayList<DBKnowledgeItem> updatedObjects) {
    	for (int n = 0, cnt = updatedObjects.size(); n < cnt; n++) {
			DBKnowledgeItem updatedObject = updatedObjects.get(n);
			updatedObject.localCommit(this);
        }
    }

	private void dropDeleted(ArrayList<DBKnowledgeItem> removedObjects, UpdateChainLink updateLink) {
		kb.dropDeleted(updateLink, removedObjects, getCommitNumber());
    }

    /*package protected*/ void beginAuto()  {
        if (innermostTransaction == null) {
        	// Start auto-begin transaction.
			innermostTransaction = initFirstTX(true, true, null);
        }
    }

    @Override
	/*package protected*/ Transaction begin(boolean anonymous, Message commitMessage) {
    	if (innermostTransaction == null) {
			innermostTransaction = initFirstTX(false, anonymous, commitMessage);
    	} else {
    		innermostTransaction = innermostTransaction.nest(anonymous, commitMessage);
    	}
    	return innermostTransaction;
    }

	protected TransactionImpl initFirstTX(boolean autoBegin, boolean anonymous, Message commitMessage) {
		TransactionImpl firstTx;
		if (autoBegin) {
			firstTx = new TransactionImpl(this);
		} else {
			firstTx = new TransactionImpl(this, anonymous, commitMessage);
		}
		return firstTx;
	}

	@Override
    public void commitAnonymous() throws KnowledgeBaseException {
    	if (innermostTransaction == null) {
    		// There are no changes, because the transaction was neither started
			// explicitly nor implicitly.
    		return;
    	}
    	
    	TransactionImpl currentTransaction = innermostTransaction;
    	if (! currentTransaction.isAnonymous()) {
			// Skipping anonymous commit of implicit nested transaction without
			// begin.
    		return;
    	}
    	
    	commitTransaction(currentTransaction);
    }

	/* package protected */Revision commitTransaction(TransactionImpl commitTransaction) throws KnowledgeBaseException {
		checkActive(commitTransaction);
		// ensure that no commit is successful when a merge conflict has occurred.
		checkMergeConflict();

		try {
			if (commitTransaction.hasError()) {
				Message error = commitTransaction.getError();
				String messageString = "msg:" + MessageStoreFormat.toString(error);
				throw new KnowledgeBaseException(messageString, commitTransaction.getFailureStacktrace());
			}
	    	
			TransactionImpl remainingTransaction = commitTransaction.getOuter();
	        if (remainingTransaction != null) {
	        	// Nested transaction.
	        	innermostTransaction = remainingTransaction;
	        	return null;
	        }
	        
			Revision result;
	        if (hasChanges()) {
				executeCallbackModifications();
		        PooledConnection commitConnection = internalGetConnection();
		        
				result = internalStartCommit(commitConnection);
				
				UpdateChainLink link = null;
				long commitNumber = newRevision.getCommitNumber();
				kbBeginCommit(commitNumber);
				Throwable commitError = null;
				try {

					checkVetoReferences();

					JournalLine journalLine = createJournalLine();
					
					handleSecurityUdate();
					
					prepareCommitables();

					// Filter out locally removed objects that have been concurrently deleted. Those
					// conflicts are ignored, but result in a deleted object access during change
					// event production.
					for (Iterator<DBKnowledgeItem> it = removedObjectsById.values().iterator(); it.hasNext();) {
						DBKnowledgeItem removedObject = it.next();
						if (!removedObject.valuesAlive(commitNumber).isAlive()) {
							/* Object was concurrently removed. Drop here to avoid unnecessary
							 * problems with handling deleted objects. */
							it.remove();
						}
					}

					// Note: Model constraints must be checked before starting the DB operation,
					// because DB-level mandatory constraints would otherwise stop the commit before
					// constraint checking with an ugly error message.
					UpdateEvent updateEvent = createUpdateEvent();
					checkVeto(updateEvent);

					Map<MOKnowledgeItem, Object> changes = new HashMap<>();
					ArrayList<DBKnowledgeItem> removedObjects = commitRemoved(commitConnection, commitNumber, changes);
					ArrayList<DBKnowledgeItem> changedObjects = commitChanged(commitConnection, commitNumber, changes);
					ArrayList<DBKnowledgeItem> newObjects = commitNew(commitConnection, commitNumber, changes);
					commitXref(commitConnection, commitNumber, changes);
					
					commitCommitables();
					dbCommit(commitConnection);

					checkLongRunningSuccess();
					// mark commit as successful by sending non null link.
					link = new UpdateChainLink(updateEvent);

					// Make sure that search operations from within commit
					// handlers do no longer consider objects in the local
					// context. See Ticket #940.
					dropContext(true);

					localCommit(removedObjects, changedObjects, newObjects, link);

			        journal(journalLine);

					kb.updateCaches(link.getUpdateEvent());
				} catch (Throwable t) {
					commitError = t;
					throw t;
				} finally {
					try {
						kbEndCommit(link);
					} catch (Throwable t) {
						if (commitError != null) {
							// commitError is currently thrown
							commitError.addSuppressed(t);
						} else {
							throw t;
						}
					}
				}
	        } else {
	        	// Noop commit without changes.
	        	
				// Drop context, to prevent auto-rollback at end of thread
				// scope. Context must not be reused, because it is cleared
				// below.
				dropContext(true);
				
				// A noop commit does not create a revision.
	        	result = null;
	        }
	        
	        // Safety: Call cleanup, even if no changes in context.
	        cleanup();
			
			// The transaction count must only be dropped, if commit returns normally.
			// Otherwise, rollback that is called after a failed commit would deny execution.
	    	innermostTransaction = null;
			return result;
		} catch (Throwable ex) {
			try {
				rollbackComplete(false);
				checkLongRunningFailed();
			} catch (Throwable rollbackFailure) {
				ex.addSuppressed(rollbackFailure);
			}
			if (ex instanceof KnowledgeBaseException) {
				// Do not duplicate exception.
				throw (KnowledgeBaseException) ex;
			} else if (ex instanceof KnowledgeBaseRuntimeException || ex instanceof KnowledgeBaseUIException) {
				// Do not mask KB internal failures.
				throw (RuntimeException) ex;
			} else if (ex instanceof Error) {
				// Do not mask VM internal problems.
				throw (Error) ex;
			}
			throw new KnowledgeBaseException("Database operation failed.", ex);
        }
		
	}

	private void checkVetoReferences() {
		switch (removedObjectsById.size()) {
			case 0: return;
			case 1: {
				DBKnowledgeItem deletedObject = removedObjectsById.values().iterator().next();
				checkVetoReferences(deletedObject.tTable(), Collections.singleton(deletedObject));
				return;
			}
			default: {
				Map<MetaObject, List<DBKnowledgeItem>> deletionsByType = new HashMap<>();
				for (Entry<ObjectKey, DBKnowledgeItem> deletedObject : removedObjectsById.entrySet()) {
					MetaObject type = deletedObject.getKey().getObjectType();
					List<DBKnowledgeItem> list = deletionsByType.get(type);
					if (list == null) {
						list = new ArrayList<>();
						deletionsByType.put(type, list);
					}
					list.add(deletedObject.getValue());
				}
				for (Entry<MetaObject, List<DBKnowledgeItem>> deletion : deletionsByType.entrySet()) {
					checkVetoReferences(deletion.getKey(), deletion.getValue());
				}
				return;
			}
		}
	}

	private void checkVetoReferences(MetaObject type, Collection<DBKnowledgeItem> items) {
		List<DBKnowledgeItem> vetoReferences =
			kb.getAnyReferer(Revision.CURRENT, type, items, DeletionPolicy.VETO, DBKnowledgeItem.class);
		if (!vetoReferences.isEmpty()) {
			throw new KnowledgeBaseUIException(
				I18NConstants.DELETE_FAILED_REFERER_HAS_VETO__ITEMS_REFERERS.fill(
					WrapperFactory.getWrappersForKOsGeneric(items),
					WrapperFactory.getWrappersForKOsGeneric(vetoReferences)));
		}
	}



	/**
	 * @param changes
	 *        Mapping of the touched types to the (inline) set of branches on which the changes
	 *        occured.
	 */
	private void commitXref(Connection commitConnection, long commitNumber, Map<MOKnowledgeItem, Object> changes)
			throws SQLException {
		// Create revision xref entries.
		if (changes.isEmpty()) {
			return;
		}
		try (RevisionXref.InsertXref insertXref = new RevisionXref.InsertXref(kb, commitConnection)) {
			for (Entry<MOKnowledgeItem, Object> entry : changes.entrySet()) {
				String typeName = entry.getKey().getName();
				Object inlineSet = entry.getValue();
				switch (InlineSet.size(inlineSet)) {
					case 0: {
						continue;
					}
					case 1: {
						/* Use internal knowledge that one element sets are represented by their
						 * element. */
						insertXref.addBatch(commitNumber, typeName, ((Long) inlineSet).longValue());
						break;
					}
					default: {
						for (Long branch : InlineSet.toSet(Long.class, inlineSet)) {
							insertXref.addBatch(commitNumber, typeName, branch.longValue());
						}
					}
				}
			}
			insertXref.executeBatch();
		}
	}

	private void executeCallbackModifications() throws KnowledgeBaseException {
		Map<ObjectKey, DBKnowledgeItem> createdObjects = unmodifyableCopy(newObjectsById);
		Map<ObjectKey, DBKnowledgeItem> updatedObjects = unmodifyableCopy(changedObjectsById);
		Map<ObjectKey, DBKnowledgeItem> removedObjects = Collections.unmodifiableMap(removedObjectsById);
		List<Modification> modifications = kb.fireKBChangeEvent(this, createdObjects, updatedObjects, removedObjects);
		if (modifications.isEmpty()) {
			return;
		}
		for (Modification modification : modifications) {
			modification.execute();
		}
	}

	protected long getCommitStartTime() {
		return newRevision.getDate();
	}

	private void checkLongRunningSuccess() {
		long commitTime = System.currentTimeMillis() - getCommitStartTime();
		if (commitTime > kb.getCommitWarnTime()) {
			long commitNumber = newRevision.getCommitNumber();
			String duration = DebugHelper.toDuration(commitTime);
			Logger.warn(
				"Long running commit (revision " + commitNumber + " lasting " + duration + ")",
				new Exception("Stacktrace"), DefaultDBContext.class);
		}
	}

	private void checkLongRunningFailed() {
		if (newRevision != null) {
			long commitTime = System.currentTimeMillis() - getCommitStartTime();
			if (commitTime > kb.getCommitWarnTime()) {
				long commitNumber = newRevision.getCommitNumber();
				String duration = DebugHelper.toDuration(commitTime);
				Logger.warn(
					"Long running failed commit (revision " + commitNumber + " lasting " + duration + ")",
					new Exception("Stacktrace"), DefaultDBContext.class);
			}
		}
	}

	/**
	 * Whether this context has potentially any local modifications that require
	 * a database commit.
	 */
	@Override
	protected final boolean hasChanges() {
		if (hasCommitConnection()) {
			// Database transaction is already open.
			return true;
		}

	    if (! changedObjectsById.isEmpty()) {
	    	return true;
	    }

	    if (! newObjectsById.isEmpty()) {
	    	return true;
	    }

	    if (! removedObjectsById.isEmpty()) {
	    	return true;
	    }
		
	    if (! committables.isEmpty()) {
	    	return true;
	    }

	    if (! committablesDeleted.isEmpty()) {
	    	return true;
	    }

	    return false;
	}

	/**
	 * Check, whether the given {@link TransactionImpl} is the active
	 * transaction on which commit and rollback operations are allowed.
	 * 
	 * @param currentTransaction
	 *        The {@link TransactionImpl} to check.
	 * 
	 * @throws IllegalStateException
	 *         If the given transaction is not the active transaction.
	 */
	private void checkActive(TransactionImpl currentTransaction) {
		if (innermostTransaction != currentTransaction) {
			// Detected invalid transaction nesting.
			throw new IllegalStateException("Invalid nesting of transactions, currentTransaction='" + currentTransaction + "', expected='" + innermostTransaction + "'.");
		}
	}

	protected void kbBeginCommit(long commitNumberValue) {
		kb.beginCommit(commitNumberValue);
	}
	
	protected UpdateEvent createUpdateEvent() {
		long commitNumber = newRevision.getCommitNumber();
		ChangeSet changeSet = new ChangeSet(commitNumber);
		changeSet.setCommit(CommitEvent.newCommitEvent(newRevision));
		addChangeEvents(changeSet, newObjectsById.values());
		addChangeEvents(changeSet, changedObjectsById.values());
		addChangeEvents(changeSet, removedObjectsById.values());

		return
			new UpdateEvent(
				kb,
				false, 
			commitNumber,
			unmodifyableCopy(newObjectsById),
			unmodifyableCopy(changedObjectsById),
			unmodifyableCopy(removedObjectsById), 
			changeSet);
	}

	private void addChangeEvents(ChangeSet changeSet, Collection<DBKnowledgeItem> items) {
		for (DBKnowledgeItem changedItem : items) {
			ItemChange change = changedItem.getChange();
			if (change == null) {
				/* This may happen when the done changes are reverted in the same transaction. */
				continue;
			}
			change.setRevision(changeSet.getRevision());
			changeSet.add(change);
		}
	}

	/**
	 * Commits the database.
	 * 
	 * @param commitConnection
	 *        The connection for the commit.
	 */
	protected void dbCommit(PooledConnection commitConnection) throws SQLException {
		commitConnection.commit();
	}

	/**
	 * Removes this context from the {@link #kb}.
	 */
	protected void dropContext(boolean sucess) {
		kb.dropDBContext();
		close(sucess);
	}

	protected void kbEndCommit(UpdateChainLink link) {
		kb.endCommit(link);
	}

	private void localCommit(ArrayList<DBKnowledgeItem> removedObjects, ArrayList<DBKnowledgeItem> changedObjects,
			ArrayList<DBKnowledgeItem> newObjects, UpdateChainLink updateLink) {
		
		insertNew(newObjects);
		publishChanges(changedObjects);
		dropDeleted(removedObjects, updateLink);

		/* When publishing caches, the caches may access the values of new objects. For this case
		 * the session must be updated to ensure that the caches see the new values. */
		TLContext currentContext = TLContext.getContext();
		UpdateChainLink currentRev = HistoryUtils.updateSessionAndInteractionRevision(kb, currentContext, updateLink);
		try {
			publishKBCaches();
			publishAssociationCaches();
			completeCommitable();
		} finally {
			HistoryUtils.updateSessionAndInteractionRevision(kb, currentContext, currentRev);
		}
	}
	
	private void publishAssociationCaches() {
		for (Entry<AbstractAssociationCache<?, ?>, Associations<?, ?>> cache : localCaches.entrySet()) {
			Associations<?, ?> localCache = cache.getValue();
			((AbstractAssociationCache) cache.getKey()).internalCommit(this, localCache);
		}
	}

    /**
     * Undo of local modifications.
     */
	/*package protected*/ final void localRollback() {
		rollbackNew();
        rollbackCommitables();
        rollbackChanged();
		rollbackDeleted();
		
        cleanup();
	}
    
	@Override
	protected void startCommit() {
    	try {
			internalStartCommit(internalGetConnection());
		} catch (SQLException ex) {
			throw new KnowledgeBaseRuntimeException("Start of commit failed.", ex);
		} catch (MergeConflictException ex) {
			String message = "Commit aborted, because local changes come in conflict with former changes.";
			throw new KnowledgeBaseRuntimeException(message, ex);
		}
    }

	private Revision internalStartCommit(PooledConnection connection) throws SQLException, MergeConflictException {
		if (newRevision != null) {
			// Commit has already started.
			return newRevision;
		}
		
		try {
			newRevision = createRevision(connection);
		} catch (RefetchTimeout ex) {
			String message = "Commit aborted in response to long running concurrent refetch.";
			throw new KnowledgeBaseRuntimeException(message, ex);
		}
        
        // Refetch upon commit has completed during revision number creation.
		// The cache is up to date. 
        //
		// Acquire the refetch lock by announcing a (pseudo) refetch of the
		// newly created revision to prevent external automatic refetch of the
		// new revision immediately after the database commit has completed.
		beginCommit(newRevision.getCommitNumber());
		
		return newRevision;
	}

	/**
	 * Allocates the revision for the current commit.
	 * 
	 * <p>
	 * After this method returns, the created {@link Revision} is already
	 * inserted into the database revision table.
	 * </p>
	 * 
	 * @param commitConnection
	 *        the connection for the commit.
	 * @return The created {@link Revision}.
	 */
	protected RevisionImpl createRevision(PooledConnection commitConnection) throws SQLException,
			MergeConflictException, RefetchTimeout {
		TransactionImpl tx = getOutermostTransaction();
		return kb.createRevision(commitConnection, tx.getCommitMessage());
	}

	private TransactionImpl getOutermostTransaction() {
		TransactionImpl outermost = this.innermostTransaction;
		while (true) {
			TransactionImpl outer = outermost.getOuter();
			if (outer == null) {
				return outermost;
			}
			outermost = outer;
		}
	}
	
    protected void handleSecurityUdate() {
    	if (AccessManager.Module.INSTANCE.isActive()) {
    		AccessManager.getInstance().handleSecurityUpdate(
				kb,
					Collections.<TLID, Object> emptyMap(),
					this.<Object> toNameMap(this.newObjectsById),
					this.<Object> toNameMap(this.removedObjectsById),
    				kb);
    	} else {
			Logger.info("Commit without access manager, security is not updated.", DefaultDBContext.class);
    	}
    }

	private <T> Map<TLID, T> toNameMap(HashMap<? extends ObjectKey, ? extends T> objectsByKey) {
		Map<TLID, T> result = new HashMap<>();
		for (Entry<? extends ObjectKey, ? extends T> entry : objectsByKey.entrySet()) {
            
            ObjectKey key = entry.getKey();
			// TODO #1960: Workaround for missing branch support in SecurityStorage: Only objects in
			// trunk have object-based security.
			if (TLContext.TRUNK_ID != key.getBranchContext()) {
            	continue;
            }
            
			Object clash = result.put(key.getObjectName(), entry.getValue());
            assert clash == null;
        }
        return result;
    }

	private static <K, V> Map<K, V> unmodifyableCopy(Map<? extends K, ? extends V> map) {
		if (map.isEmpty()) {
			return Collections.emptyMap();
		}
		
		return Collections.unmodifiableMap(new HashMap<>(map));
	}

	private void checkVeto(UpdateEvent event) throws CommitVetoException {
		kb.fireEventVeto(this, event);
	}

	/** 
     * Trigger complete() method on all commitables
     */
    private void completeCommitable() {
		for (Committable committable : committables) {
			committable.complete(this);
        }
	}

    /**
     * Prepare all the Commitables so that the later commit() should succeed.
     */
    private void prepareCommitables() throws SQLException {
		for (Committable committable : committablesDeleted) {
            if (! committable.prepareDelete(this)) {
                throw new SQLException("Unable to prepare deletion of committable: " + committable);
            }
        }

		for (Committable committable : committables) {
			if (!committable.prepare(this)) {
				throw new SQLException("Preparation of committable failed: " + committable);
            }
        }
    }
    
    /**
     * Commit all the {@link Committable}s (should not actually fail).
     */
    private void commitCommitables() throws SQLException {
		for (Committable committable : committables) {
            if (! committable.commit(this)) {
                throw new SQLException("Commit of committable failed: " + committable);
            }
        }
    }
    
    /**
     * Nullify the Knowledgebase for New object that did not make it.
     */
    private void rollbackNew() {
		rollbackLocal(newObjectsById.values());
		if (newRevision != null) {
			rollbackLocal(Collections.singletonList(newRevision));
		}
    }

    /**
     * Roll back all the Commitables (should not actually fail).
     */
    private void rollbackCommitables() {
        // Care about Comittables to delete
		for (Committable committable : committablesDeleted) {
            try {
				if (!committable.rollback(this)) {
					Logger.warn("Unable to rollback " + committable, this);
	            }
            }
            catch (Throwable t) {
				Logger.error("Unable to rollback " + committable, t, this);
            }
        }

        // Care about other Comittables
		for (Committable committable : committables) {
            try {
				if (!committable.rollback(this)) {
					Logger.warn("Unable to rollback " + committable, this);
	            }
            }
            catch (Throwable t) {
				Logger.error("Unable to rollback " + committable, t, this);
            }
        }
    }

	@Override
	protected void cleanup() {
    	super.cleanup();
    	
		this.newRevision = null;
		
		this.committables = cleanupCommittables(this.committables);
		this.committablesDeleted = cleanupCommittablesDeleted(this.committablesDeleted);
		this.changedObjectsById = cleanupChangedObjectsById(this.changedObjectsById);
		this.newObjectsById = cleanupNewObjectsById(this.newObjectsById);
		this.removedObjectsById = cleanupRemovedObjectsById(this.removedObjectsById);
		this._localChanges = cleanupLocalChanges(this._localChanges);
		this._dynamicChanges = cleanupDynamicChanges(this._dynamicChanges);
		this.localCaches = cleanupLocalCaches(this.localCaches);
		this.associationChangesByBaseId = cleanupAssociationChangesByBaseId(this.associationChangesByBaseId);
		unlock();
    }
    
	protected <K, V> Map<K, V> cleanupAssociationChangesByBaseId(Map<K, V> associationChangesByBaseId) {
		return null;
	}

	protected <K, V> Map<K, V> cleanupLocalCaches(Map<K, V> localCaches) {
		return null;
	}

	protected <K, V> Map<K, V> cleanupDynamicChanges(Map<K, V> dynamicChanges) {
		return null;
	}

	protected <K, V> Map<K, V> cleanupLocalChanges(Map<K, V> localChanges) {
		return null;
	}

	protected <E> IdentityHashSet<E> cleanupCommittables(IdentityHashSet<E> committables) {
		return null;
	}

	protected <E> IdentityHashSet<E> cleanupCommittablesDeleted(IdentityHashSet<E> committablesDeleted) {
		return null;
	}

	protected <K, V> HashMap<K, V> cleanupChangedObjectsById(HashMap<K, V> changedObjectsById) {
		return null;
	}

	protected <K, V> HashMap<K, V> cleanupNewObjectsById(HashMap<K, V> newObjectsById) {
		return null;
	}

	protected <K, V> HashMap<K, V> cleanupRemovedObjectsById(HashMap<K, V> newObjectsById) {
		return null;
	}

	@Override
	/*package protected*/ boolean addCommittable(Committable aCommittable) {
		checkLocked();
        if (this.committables.contains(aCommittable)) {
        	return false;
        }

        if (this.committablesDeleted.contains(aCommittable)) {
        	Logger.warn("addCommitable() called for deleted committable object, ignored " + aCommittable, this);
        	return false;
        }
        
        beginAuto();
            
        return (this.committables.add(aCommittable));
    }

    @Override
	/*package protected*/ boolean addCommittableDelete(Committable aCommitable) {
		checkLocked();
        if (this.committablesDeleted.contains(aCommitable)) {
        	return false;
        }
        
        beginAuto();
        this.committables.remove(aCommitable);
        return this.committablesDeleted.add(aCommitable);
    }

    @Override
	/*package protected*/ boolean removeCommittable(Committable aCommittable) {
		checkLocked();
        return (this.committables.remove(aCommittable));
    }

    /**
     * The ID of that identifies the owner of this context.
     */
	public final String getUpdater() {
		return updater;
	}

	@Override
	final <T extends TLObject, C> Associations<T, C> getLocalCache(AbstractAssociationCache<T, C> globalCache) {
		@SuppressWarnings("unchecked")
		Associations<T, C> result = (Associations<T, C>) localCaches.get(globalCache);
		return result;
	}

	@Override
	final <T extends TLObject, C> Associations<T, C> dropLocalCache(AbstractAssociationCache<T, C> globalCache) {
		@SuppressWarnings("unchecked")
		Associations<T, C> result = (Associations<T, C>) localCaches.remove(globalCache);
		return result;
	}
	
	@Override
	final <T extends TLObject, C> void setLocalCache(AbstractAssociationCache<T, C> globalCache,
			Associations<T, C> localCache) {
		localCaches.put(globalCache, localCache);
	}

	@Override
	public final Collection<DBKnowledgeItem> getChangedObjects() {
		return changedObjectsById.values();
	}

	@Override
	public final Collection<DBKnowledgeItem> getNewObjects() {
		return newObjectsById.values();
	}

	@Override
	public final Collection<DBKnowledgeItem> getDroppedObjects() {
		return removedObjectsById.values();
	}

	@Override
	public final boolean isRemovedKey(ObjectKey key) {
		return removedObjectsById.containsKey(key);
	}

	/**
	 * Index the given link as being touched in this context.
	 * @param link
	 *        The touched link.
	 * @param changeType
	 *        Type of the association change. Any of
	 *        {@link KnowledgeObjectInternal#TYPE_CREATION},
	 *        {@link KnowledgeObjectInternal#TYPE_DELETION}, and
	 *        {@link KnowledgeObjectInternal#TYPE_MODIFICATION}.
	 */
	/* package protected */void notifyAssociationChange(KnowledgeItemInternal link, int changeType) {
		boolean isNew = isNew(link.tId());
		notifyAssociationChanged(link, isNew, changeType);
	}

	private void notifyAssociationChanged(KnowledgeItemInternal link, boolean isNew, int changeType) {
		List<? extends MOReference> references = MetaObjectUtils.getReferences(link.tTable());
		if (references.isEmpty()) {
			return;
		}
		Object[] localValues = getLocalValues(link);
		Object[] globalValues = globalValues(link, isNew);
		for (MOReference reference : references) {
			notifyAssociationChange(link, reference, localValues, globalValues, isNew, changeType);
		}
	}

	private void notifyAssociationChange(KnowledgeItemInternal link, MOReference reference, Object[] localValues,
			Object[] globalValues, boolean isNew, int changeType) {
		ObjectKey localReferencedKey = link.lookupKey(reference, localValues);
		if (localReferencedKey != null) {
			addChangeToBaseObject(localReferencedKey, link, reference, changeType);
		}
		ObjectKey globalReferencedKey = globalReferencedKey(link, reference, globalValues, isNew);
		if (globalReferencedKey != null && !globalReferencedKey.equals(localReferencedKey)) {
			addChangeToBaseObject(globalReferencedKey, link, reference, changeType);
		}
	}

	/**
	 * Return the global values of the given item if the item is already persistent or the empty
	 * array instead.
	 * 
	 * @param item
	 *        The item to get global values from.
	 * @param isNew
	 *        Whether the given item is new in this context.
	 */
	private Object[] globalValues(KnowledgeItemInternal item, boolean isNew) {
		Object[] globalValues;
		if (isNew) {
			globalValues = ArrayUtil.EMPTY_OBJECT_ARRAY;
		} else {
			globalValues = item.getGlobalValues();
		}
		return globalValues;
	}

	private ObjectKey globalReferencedKey(KnowledgeItemInternal item, MOReference reference, Object[] globalValues,
			boolean isNew) {
		ObjectKey globalReferencedKey;
		if (isNew) {
			globalReferencedKey = null;
		} else {
			globalReferencedKey = item.lookupKey(reference, globalValues);
		}
		return globalReferencedKey;
	}

	private void addChangeToBaseObject(ObjectKey baseId, KnowledgeItemInternal link, MOReference attribute,
			int changeType) {
		addChange(associationChangesByBaseId, baseId, link);
		notifyChangeToBaseObject(baseId, attribute, link, changeType);
	}

	/* package protected */void notifyNewAssociationDeleted(KnowledgeItemInternal link, Object[] localValues) {
		for (MOReference reference : MetaObjectUtils.getReferences(link.tTable())) {
			ObjectKey localReferencedKey = link.lookupKey(reference, localValues);
			if (localReferencedKey != null) {
				removeChange(this.associationChangesByBaseId, localReferencedKey, link);
				notifyChangeToBaseObject(localReferencedKey, reference, link, KnowledgeObjectInternal.TYPE_DELETION);
			}
			// link is new so there is no global known referenced object
		}
	}

	@Override
	void notifyDynamicAttributeValueChange(KnowledgeItemInternal item, String dynamicAttribute, Object oldValue,
			Object newValue) {
		notifyCacheAboutDynamicChange(item, dynamicAttribute, oldValue, newValue);
	}

	@Override
	void notifyAttributeValueChange(KnowledgeItemInternal link, MOAttribute attribute, Object oldValue,
			Object newValue) {
		notifyCacheAboutChange(link, attribute, oldValue, newValue);

		int typeModification = KnowledgeObjectInternal.TYPE_MODIFICATION;
		if (!(attribute instanceof MOReference)) {
			notifyAssociationChange(link, typeModification);
		} else {
			List<? extends MOReference> references = MetaObjectUtils.getReferences(link.tTable());
			if (references.isEmpty()) {
				return;
			}
			boolean isNew = isNew(link.tId());
			Object[] localValues = getLocalValues(link);
			Object[] globalValues = globalValues(link, isNew);
			for (MOReference reference : references) {
				if (reference.equals(attribute)) {
					/* For the changed attribute a special check must be done. It is possible that
					 * in one transaction the value is first switched from x (global referenced
					 * value) to y and then from y to z. In this case the standard mechanism does
					 * not remove the link in the association cache of y. */
					ObjectKey oldKey;
					if (oldValue != null) {
						oldKey = ((IdentifiedObject) oldValue).tId();
						addChangeToBaseObject(oldKey, link, reference, typeModification);
					} else {
						oldKey = null;
					}
					ObjectKey newKey;
					if (newValue != null) {
						newKey = ((IdentifiedObject) newValue).tId();
						addChangeToBaseObject(newKey, link, reference, typeModification);
					} else {
						newKey = null;
					}
					ObjectKey globalReferencedKey = globalReferencedKey(link, reference, globalValues, isNew);
					if (globalReferencedKey != null && !globalReferencedKey.equals(newKey)
						&& !globalReferencedKey.equals(oldKey)) {
						addChangeToBaseObject(globalReferencedKey, link, reference, typeModification);
					}
				} else {
					notifyAssociationChange(link, reference, localValues, globalValues, isNew, typeModification);
				}
			}

		}
	}

	/**
	 * Propagate change information to the base object of the given link.
	 * 
	 * @param baseKey
	 *        The key of the base object to inform about the association link change.
	 * @param reference
	 *        the reference attribute of the association whose value
	 * @param link
	 *        The changed link.
	 * @param changeType
	 *        Type of the association change. Any of {@link KnowledgeObjectInternal#TYPE_CREATION},
	 *        {@link KnowledgeObjectInternal#TYPE_DELETION}, and
	 *        {@link KnowledgeObjectInternal#TYPE_MODIFICATION}.
	 */
	private void notifyChangeToBaseObject(ObjectKey baseKey, MOReference reference, KnowledgeItemInternal link,
			int changeType) {
		if (! isRemovedKey(baseKey)) {
			KnowledgeObjectInternal cachedObject =
				(KnowledgeObjectInternal) kb.resolveIdentifierFromGlobalCache(baseKey, kb.getSessionRevision());
			if (cachedObject == null) {
				cachedObject = (KnowledgeObjectInternal) getNew(baseKey);
			}
			if (cachedObject != null) {
				cachedObject.notifyLocalAssociationChange(this, reference, link, changeType);
			}
		}
	}

	private static void addChange(Map<ObjectKey, IdentityHashSet<TLObject>> associationChangesByBaseId,
			ObjectKey baseId, KnowledgeItemInternal link) {
		IdentityHashSet<TLObject> changes = associationChangesByBaseId.get(baseId);
		if (changes == null) {
			changes = new IdentityHashSet<>();
			associationChangesByBaseId.put(baseId, changes);
		}
		changes.add(link.getWrapper());
	}

	private static void removeChange(Map<ObjectKey, IdentityHashSet<TLObject>> associationChangesByBaseId,
			ObjectKey baseId, KnowledgeItemInternal link) {
		Set<TLObject> changes = associationChangesByBaseId.get(baseId);
		if (changes != null) {
			changes.remove(link.getWrapper());
			if (changes.isEmpty()) {
				// remove changed associations, as null indicates no association changes are known.
				associationChangesByBaseId.remove(baseId);
			}
		}
	}

	@Override
	public final Set<TLObject> getAssociationChanges(ObjectKey baseKey) {
		return associationChangesByBaseId.get(baseKey);
	}

	/**
	 * Initializes the {@link #lazyCommitConnection}.
	 */
	@Override
	protected final PooledConnection internalGetConnection() {
		if (! hasCommitConnection()) {
			if (this.closed) {
				throw new IllegalStateException("Cannot re-allocate connection for a closed commit context.");
			}
			this.lazyCommitConnection = this.kb.getConnectionPool().borrowWriteConnection();
			
			beginAuto();
		}
		return this.lazyCommitConnection;
	}
	
	/*package protected*/ final void close(boolean success) {
    	this.closed = true;
		if (hasCommitConnection()) {
			if (success) {
				this.kb.getConnectionPool().releaseWriteConnection(this.lazyCommitConnection);
			} else {
				// For safety reasons, drop the connection. This is a workaround
				// for driver bugs. E.g. in Oracle, a failed batch insert
				// (caused by a binding problem with a double value) causes
				// following inserts using the same statement to fail with an
				// internal null pointer exception.
				this.kb.getConnectionPool().invalidateWriteConnection(this.lazyCommitConnection);
			}
			this.lazyCommitConnection = null;
		}
	}
	
	/*package protected*/ boolean isClosed() {
		return closed;
	}

	@Override
	public final boolean transactionStarted() {
		return hasCommitConnection();
	}

	protected final boolean hasCommitConnection() {
		return this.lazyCommitConnection != null;
	}

	/** 
     * Create a journal line according to the committable objects registered.
     */
	private JournalLine createJournalLine() {
        if (! JournalManager.isActive()) {
			return null;
		}
		
		Set<Journallable> journallables = new HashSet<>();
		// The current way to register an additional Journallable is to register it as Committable.
		addJournallableCommittables(journallables, committables);
		addJournallables(journallables, changedObjectsById.values());
		addJournallables(journallables, newObjectsById.values());
		// TSA: deletions are not journalled
//		addJournallables(journallables, removedObjectsById.values());
		Map theMod = Collections.unmodifiableMap(this.changedObjectsById);
		Map theNew = Collections.unmodifiableMap(this.newObjectsById);
		Map theRem = Collections.unmodifiableMap(this.removedObjectsById);
		
		return JournalManager.getInstance().createJournalLine(journallables, theMod, theNew, theRem);
    }

	private void addJournallables(Collection<? super Journallable> journallables,
			Iterable<? extends DBKnowledgeItem> objects) {
		for (DBKnowledgeItem changed : objects) {
			TLObject wrapper = changed.getWrapper();
			if (wrapper instanceof Journallable) {
				journallables.add((Journallable) wrapper);
			}
		}
	}

	private void addJournallableCommittables(Collection<? super Journallable> journallables,
			Iterable<? extends Committable> objects) {
		for (Committable committable : objects) {
			if (committable instanceof Journallable) {
				journallables.add((Journallable) committable);
			}
		}
	}

	private void journal(JournalLine journalLine) {
		if (journalLine != null) {
			JournalManager.getInstance().journal(journalLine);
		}
	}
	
	@Override
	void lock() {
		locked = true;
	}

	@Override
	void unlock() {
		locked = false;
	}

	@Override
	void checkModifiable() {
		checkLocked();
	}

	private void checkLocked() {
		if (locked) {
			throw new IllegalStateException("Context is currently locked. No modification allowed.");
		}
	}
    
	@Override
	public TLID createID() {
		return kb.createID();
	}

	@Override
	Object[] getLocalValues(KnowledgeItem item) {
		return _localChanges.get(item);
	}

	@Override
	void initLocalValues(KnowledgeItem item, Object[] modifiedValues) {
		Object[] clash = _localChanges.put(item, modifiedValues);
		assert clash == null : "For item '" + item + "' there were local changes added before: Former '"
			+ Arrays.toString(clash) + "', Current: '" + Arrays.toString(modifiedValues) + "'";
	}

	@Override
	FlexData getLocalDynamicValues(DynamicKnowledgeItem ki) {
		return _dynamicChanges.get(ki);
	}

	@Override
	void initLocalDynamicValues(DynamicKnowledgeItem ki, FlexData modifiedValues) {
		FlexData clash = _dynamicChanges.put(ki, modifiedValues);
		assert clash == null : "For item '" + ki + "' there were dynamic changes added before: Former '"
			+ clash + "', Current: '" + modifiedValues + "'";
	}

	@Override
	boolean isPersistentItemModified(KnowledgeItem item) {
		ObjectKey key = item.tId();
		return isChanged(key) || isRemovedKey(key);
	}

	private boolean isNew(ObjectKey key) {
		return newObjectsById.containsKey(key);
	}

	private boolean isChanged(ObjectKey key) {
		return changedObjectsById.containsKey(key);
	}

	@Override
	boolean isCreationContext(DBKnowledgeItem item) {
		/* Note: This method does not just checks that there is any new object with the same key,
		 * but that this is exactly the given one. This is done because it could be that two
		 * sessions have created objects with the same key. */
		return newObjectsById.get(item.tId()) == item;
	}

	@Override
	void checkMergeConflict() throws MergeConflictException {
		if (!InlineList.isEmpty(_mergeConflicts)) {
			List<MergeConflictDescription> conflicts =
				InlineList.toList(MergeConflictDescription.class, _mergeConflicts);
			throw new MergeConflictException(kb, conflicts);
		}
	}

	@Override
	void merge(UpdateEvent evt) {
		checkNoTouchOfDeletedItems(evt);
		checkNoConcurrentCreations(evt);
		checkNoConcurrentChanges(evt);
		checkNoDeletionOfTouchedItem(evt);
		updateKBCaches(evt);
	}

	/**
	 * Checks that the given {@link UpdateEvent} does not contain changes on objects which is also
	 * changed in this {@link DBContext}.
	 * 
	 * <p>
	 * A found conflict is added to {@link #_mergeConflicts}.
	 * </p>
	 */
	private void checkNoConcurrentChanges(UpdateEvent evt) {
		Set<ObjectKey> concurrentChanged = evt.getUpdatedObjectKeys();
		Set<ObjectKey> changed = changedObjectsById.keySet();
		if (!CollectionUtil.containsAny(concurrentChanged, changed)) {
			/* This additional question may actually not be done, but in real life there is in most
			 * cases no match and in service method optimizations are used. */
			return;
		}
		for (KnowledgeItem changedItem : getChangedObjects()) {
			if (concurrentChanged.contains(changedItem.tId())) {
				addConflict(new ConcurrentChange(evt, changedItem));
			}
		}
	}

	/**
	 * Checks that the given {@link UpdateEvent} does not contain changes on objects that are
	 * deleted in this session. Moreover it checks that there are no
	 * {@link ChangeSet#getCreations()} or {@link ChangeSet#getUpdates()} in which a reference was
	 * changed to an item deleted in this {@link DBContext}.
	 * 
	 * <p>
	 * A found conflict is added to {@link #_mergeConflicts}.
	 * </p>
	 */
	private void checkNoDeletionOfTouchedItem(UpdateEvent evt) {
		if (removedObjectsById.isEmpty()) {
			return;
		}
		Set<ObjectKey> deletedKeys = removedObjectsById.keySet();
		Collection<ObjectKey> changedObjectKeys = evt.getUpdatedObjectKeys();
		for (ObjectKey changedObjectKey : changedObjectKeys) {
			if (deletedKeys.contains(changedObjectKey)) {
				addConflict(new DeletedChanged(evt, removedObjectsById.get(changedObjectKey)));
			}
		}
		ChangeSet cs = evt.getChanges();
		checkNoDeletionOfReference(evt, cs.getCreations(), deletedKeys);
		checkNoDeletionOfReference(evt, cs.getUpdates(), deletedKeys);
	}

	private void checkNoDeletionOfReference(UpdateEvent evt, List<? extends ItemChange> changes,
			Set<ObjectKey> deletedKeys) {
		for (ItemChange change : changes) {
			MetaObject type = change.getObjectType();
			for (Entry<String, Object> changeValue : change.getValues().entrySet()) {
				Object value = changeValue.getValue();
				ObjectKey referencedKey;
				if (value instanceof KnowledgeItem) {
					referencedKey = ((KnowledgeItem) value).tId();
				} else if (value instanceof ObjectKey) {
					referencedKey = (ObjectKey) value;
				} else {
					referencedKey = null;
				}
				if (referencedKey == null) {
					// no reference value.
					continue;
				}
				
				if (!deletedKeys.contains(referencedKey)) {
					// referred object is not deleted.
					continue;
				}
				MOAttribute reference =  ((MOStructure) type).getAttributeOrNull(changeValue.getKey());
				if (!(reference instanceof MOReference)) {
					// changed value is not a reference. It may be that this is an ordinary
					// attribute which has an object key as value.
					continue;
				}
				ObjectKey referrerKey = change.getObjectId().toObjectKey(Revision.CURRENT);
				DBKnowledgeItem deletedItem = removedObjectsById.get(referencedKey);
				KnowledgeItem cachedItem = kb.resolveCachedObjectKey(referrerKey);
				addConflict(new DeletingReference(evt, deletedItem, cachedItem, referrerKey, (MOReference) reference));
			}
		}
	}

	/**
	 * Checks that the given {@link UpdateEvent} does not creation of an object which is also
	 * created in this {@link DBContext}.
	 * 
	 * <p>
	 * A found conflict is added to {@link #_mergeConflicts}.
	 * </p>
	 */
	private void checkNoConcurrentCreations(UpdateEvent evt) {
		Set<ObjectKey> concurrentCreated = evt.getCreatedObjectKeys();
		Set<ObjectKey> created = newObjectsById.keySet();
		if (!CollectionUtil.containsAny(concurrentCreated, created)) {
			/* This additional question may actually not be done, but in real life there is in most
			 * cases no match and in service method optimizations are used. */
			return;
		}
		for (KnowledgeItem newItem : getNewObjects()) {
			if (concurrentCreated.contains(newItem.tId())) {
				addConflict(new ConcurrentCreation(evt, newItem));
			}
		}
	}

	/**
	 * Checks that in this {@link DBContext} no objects are touched that are deleted in the given
	 * {@link UpdateEvent}. Moreover it is checked that no objects refers to a deleted object.
	 * 
	 * <p>
	 * A found conflict is added to {@link #_mergeConflicts}.
	 * </p>
	 */
	private void checkNoTouchOfDeletedItems(UpdateEvent evt) {
		Set<ObjectKey> deletedKeys = evt.getDeletedObjectKeys();
		if (deletedKeys.isEmpty()) {
			return;
		}
		Collection<DBKnowledgeItem> changedObjects = getChangedObjects();
		for (DBKnowledgeItem changedObject : changedObjects) {
			if (deletedKeys.contains(changedObject.tId())) {
				addConflict(new ChangedDeleted(evt, changedObject));
			}
		}
		checkNoReferenceToDeleted(changedObjects, deletedKeys, evt);
		checkNoReferenceToDeleted(getNewObjects(), deletedKeys, evt);
	}

	private void checkNoReferenceToDeleted(Collection<? extends KnowledgeItemInternal> touchedObjects,
			Set<ObjectKey> deletedKeys, UpdateEvent evt) {
		long eventRevision = evt.getCommitNumber();
		for (KnowledgeItemInternal changedObject : touchedObjects) {
			MOStructure type = changedObject.tTable();
			// Prevent resolving an object that has no references at all.
			List<? extends MOReference> references = MetaObjectUtils.getReferences(type);
			if (references.isEmpty()) {
				continue;
			}
			for (MOReference reference : references) {
				ObjectKey referencedKey = changedObject.getReferencedKey(reference, eventRevision);
				if (!deletedKeys.contains(referencedKey)) {
					// item was not deleted in given event.
					continue;
				}
				addConflict(new ReferenceToDeleted(evt, changedObject, reference));
			}
		}
	}

	private void addConflict(MergeConflictDescription newConflict) {
		_mergeConflicts = InlineList.add(MergeConflictDescription.class, _mergeConflicts, newConflict);
	}

	@Override
	Object getLocalValue(KBCache<?> cache) {
		if (_kbCaches == null) {
			return KBCache.noLocalCacheValue();
		}
		Object value = _kbCaches.get(cache);
		if (value == null && !_kbCaches.containsKey(cache)) {
			return KBCache.noLocalCacheValue();
		}
		return value;
	}

	@Override
	void initLocalValue(KBCache<?> cache, Object localValue) {
		if (_kbCaches == null) {
			_kbCaches = new HashMap<>();
		}
		Object clash = _kbCaches.put(cache, localValue);
		if (clash != null) {
			throw new IllegalStateException();
		}
	}

	private void publishKBCaches() {
		if (_kbCaches == null) {
			return;
		}
		for (Entry<KBCache<?>, Object> entry : _kbCaches.entrySet()) {
			entry.getKey().notifyCommit(entry.getValue());
		}
	}

	private void notifyCacheAboutDynamicChange(KnowledgeItem item, String attributeName, Object oldValue,
			Object newValue) {
		if (_kbCaches == null) {
			return;
		}
		for (Entry<KBCache<?>, Object> entry : _kbCaches.entrySet()) {
			KBCache<?> cache = entry.getKey();
			Object newCacheValue =
				cache.dynamicAttributeValueChanged(entry.getValue(), item, attributeName, oldValue, newValue);
			entry.setValue(newCacheValue);
		}
	}

	private void notifyCacheAboutChange(KnowledgeItemInternal link, MOAttribute attribute,
			Object oldValue, Object newValue) {
		if (_kbCaches == null) {
			return;
		}
		for (Entry<KBCache<?>, Object> entry : _kbCaches.entrySet()) {
			KBCache<?> cache = entry.getKey();
			Object newCacheValue =
				cache.attributeValueChanged(entry.getValue(), link, attribute, oldValue, newValue);
			entry.setValue(newCacheValue);
		}
	}

	private void updateKBCaches(UpdateEvent event) {
		if (_kbCaches == null) {
			return;
		}
		for (Entry<KBCache<?>, Object> entry : _kbCaches.entrySet()) {
			KBCache<?> cache = entry.getKey();
			Object newCacheValue = cache.updateLocalCache(event, entry.getValue());
			entry.setValue(newCacheValue);
		}
	}

	private void notityCacheAboutDeletion(DBKnowledgeItem item) {
		if (_kbCaches == null) {
			return;
		}
		for (Entry<KBCache<?>, Object> entry : _kbCaches.entrySet()) {
			KBCache<?> cache = entry.getKey();
			Object newCacheValue = cache.itemDeleted(entry.getValue(), item);
			entry.setValue(newCacheValue);
		}
	}

	private void notityCacheAboutCreation(DBKnowledgeItem item) {
		if (_kbCaches == null) {
			return;
		}
		for (Entry<KBCache<?>, Object> entry : _kbCaches.entrySet()) {
			KBCache<?> cache = entry.getKey();
			Object newCacheValue = cache.itemCreated(entry.getValue(), item);
			entry.setValue(newCacheValue);
		}
	}

}
