/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.TLID;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.util.TLContext;

/**
 * {@link EventWriter} that applies {@link KnowledgeEvent}s to a given
 * {@link KnowledgeBase} through regular {@link KnowledgeBase} operations.
 * 
 * @see KnowledgeBase#getReplayWriter() Writer to apply events literally, e.g.
 *      during a migration step.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultEventWriter implements EventWriter {
	
	/** {@link Protocol} to write messages to. */
	protected final Protocol _protocol;

	/** The {@link KnowledgeBase} to migrate to. */
	protected final KnowledgeBase _kb;

	/** Value of {@link KnowledgeBase#getHistoryManager()} of {@link #_kb}. */
	protected final HistoryManager _kbHistory;
	
	/**
	 * States of a {@link DefaultEventWriter}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	protected static enum State {
		/**
		 * All information has been committed to the underlying {@link KnowledgeBase}.
		 */
		clean, 
		
		/**
		 * The writer has opened a transaction that is not yet committed.
		 */
		transaction,

		/**
		 * A branch has been created, but the corresponding commit event has not
		 * yet been seen.
		 */
		branchCreated;
	}
	
	private DefaultEventWriter.State state = State.clean;

	private Transaction _tx;

	/**
	 * Creates a {@link DefaultEventWriter} with {@link KnowledgeBaseProtocol}.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to apply events to.
	 */
	public DefaultEventWriter(KnowledgeBase kb) {
		this(kb, new KnowledgeBaseProtocol());
	}

	/**
	 * Creates a {@link DefaultEventWriter}.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to apply events to.
	 * @param protocol
	 *        must not be <code>null</code>. failures are reported to that protocol
	 */
	public DefaultEventWriter(KnowledgeBase kb, Protocol protocol) {
		this._kb = kb;
		this._protocol = protocol;
		this._kbHistory = kb.getHistoryManager();
	}
	
	/**
	 * The current {@link State} of this writer.
	 */
	protected final State getState() {
		return state;
	}
	
	@Override
	public void flush() {
		// Ignored, commit events are written to the DB immediately.
	}

	@Override
	public void close() {
		flush();
		/* Do not commit changes. This EventWriter is also closed when an error occurred before
		 * which leads to inconsistent commit. */
		rollbackCurrentChanges();
	}
	
	@Override
	public void write(ChangeSet cs) {
		checkChangeSetValid(cs);
		processBranches(cs.getBranchEvents());
		processCreations(cs.getCreations());
		processUpdates(cs.getUpdates());
		processDeletions(cs.getDeletions());
		CommitEvent commit = cs.getCommit();
		if (commit != null) {
			processCommit(commit);
		}
	}

	/**
	 * Checks that the given {@link ChangeSet} is either a branch event or a modification of the
	 * data, but not both.
	 * 
	 * @throws IllegalArgumentException
	 *         iff {@link ChangeSet} contains {@link BranchEvent} and other events.
	 */
	private void checkChangeSetValid(ChangeSet cs) {
		if (cs == null) {
			throw new IllegalArgumentException("Can not process null change set.");
		}
		if (!cs.getBranchEvents().isEmpty()) {
			if (cs.getBranchEvents().size() > 1) {
				throw new IllegalArgumentException(ChangeSet.class.getName()
					+ " must not contain more than one branch event.");
			}
			if (!cs.getCreations().isEmpty()) {
				throw new IllegalArgumentException(ChangeSet.class.getName()
					+ " must not contain branch events and creations.");
			}
			if (!cs.getUpdates().isEmpty()) {
				throw new IllegalArgumentException(ChangeSet.class.getName()
					+ " must not contain branch events and updates.");
			}
			if (!cs.getDeletions().isEmpty()) {
				throw new IllegalArgumentException(ChangeSet.class.getName()
					+ " must not contain branch events and deletions.");
			}
		}
	}

	/**
	 * Returns the {@link Protocol} used to protocol messages.
	 */
	public final Protocol getProtocol() {
		return _protocol;
	}

	/**
	 * Process the {@link BranchEvent} of the {@link ChangeSet}.
	 * 
	 * @param branchEvents
	 *        Value of {@link ChangeSet#getBranchEvents()}.
	 */
	protected void processBranches(List<BranchEvent> branchEvents) {
		for (BranchEvent event : branchEvents) {
			processBranch(event);
		}
	}

	/**
	 * Processes a single {@link BranchEvent}.
	 * 
	 * @param branchEvt
	 *        A {@link BranchEvent} to replay.
	 */
	protected void processBranch(BranchEvent branchEvt) {
		if (branchEvt.getBranchId() == TLContext.TRUNK_ID) {
			// Trunk is created during system setup. The initial branch creation
			// cannot be replayed.
		} else {
			if (state == State.transaction) {
				try {
					internalCommit(false);
				} catch (KnowledgeBaseException ex) {
					throw _protocol.fatal(enhanceErrorMessage(branchEvt, "Commit for branch event failed."), ex);
				}
			}
			doCreateBranch(branchEvt);
		}
		state = State.branchCreated;
	}

	/**
	 * Creates the actual branch described in the given {@link BranchEvent}.
	 * 
	 * @param event
	 *        The event to get information for the branch creation from.
	 */
	protected void doCreateBranch(BranchEvent event) {
		Set<MetaObject> branchedTypes = resolveBranchedTypes(event.getBranchedTypeNames());
		
		Branch baseBranch = _kbHistory.getBranch(event.getBaseBranchId());
		Revision baseRevision = _kbHistory.getRevision(event.getBaseRevisionNumber());
		_kbHistory.createBranch(baseBranch, baseRevision, branchedTypes);
	}

	/**
	 * Resolves the given branched type names in the target repository and returns them
	 * 
	 * @param branchedTypeNames
	 *        the type names to resolve
	 * 
	 * @return a set of {@link MetaObject} containing the resolved types
	 */
	protected Set<MetaObject> resolveBranchedTypes(Collection<String> branchedTypeNames) {
		Set<MetaObject> branchedTypes = CollectionUtil.newSet(branchedTypeNames.size());
		try {
			MORepository repository = _kb.getMORepository();
			for (String typeName : branchedTypeNames) {
				branchedTypes.add(repository.getMetaObject(typeName));
			}
		} catch (UnknownTypeException ex) {
			throw _protocol.fatal("Branch replay failed.", ex);
		}
		return branchedTypes;
	}

	private void processCommit(CommitEvent event) {
		this.state = doCommit(event, state);
	}

	/**
	 * Processes the {@link CommitEvent} of the {@link ChangeSet}.
	 * 
	 * @param currentState
	 *        Current state of the {@link EventWriter}.
	 * @return The new {@link State} of the writer.
	 */
	protected State doCommit(CommitEvent event, State currentState) {
		if (currentState == State.branchCreated) {
			// Skip, since branch creation is atomic.
		} else {
			try {
				internalCommit(false);
			} catch (KnowledgeBaseException ex) {
				throw _protocol.fatal(enhanceErrorMessage(event, "Replay of commit  failed."), ex);
			}
		}
		return State.clean;
	}

	/**
	 * Process the {@link ObjectCreation} of the {@link ChangeSet}.
	 * 
	 * @param creations
	 *        Value of {@link ChangeSet#getCreations()}.
	 */
	protected void processCreations(List<ObjectCreation> creations) {
		if (creations.isEmpty()) {
			return;
		}

		internalBeginCommit();

		try {
			_kb.createObjects(creations, KnowledgeItem.class);
		} catch (DataObjectException ex) {
			_protocol.error(enhanceErrorMessage(creations, null), ex);
		}
		state = State.transaction;
	}

	/**
	 * Begins a transaction if state is clean
	 */
	protected void internalBeginCommit() {
		if (state == State.clean) {
			beginCommit();
		}
	}

	/**
	 * Begins a transaction. Finished in {@link #internalCommit(boolean)}
	 */
	protected void beginCommit() {
		_tx = _kb.beginTransaction();
	}

	/**
	 * Processes the {@link ItemDeletion}s in the written {@link ChangeSet}.
	 */
	protected void processDeletions(Collection<ItemDeletion> deletions) {
		for (ItemDeletion deletion : deletions) {
			processDeletion(deletion);
		}
	}

	/**
	 * Processes the {@link ItemDeletion} in the written {@link ChangeSet}.
	 * 
	 * @param event
	 *        The event to process.
	 */
	protected void processDeletion(ItemDeletion event) {
		try {
			internalBeginCommit();
			ObjectBranchId objectId = event.getObjectId();
			Branch branch = _kbHistory.getBranch(objectId.getBranchId());
			KnowledgeItem item =   
				_kbHistory
					.getKnowledgeItem(branch, Revision.CURRENT, objectId.getObjectType(), objectId.getObjectName());
			
			if (item == null) {
				_protocol.info(enhanceErrorMessage(event, "Item not found: " + objectId
					+ ". This may happen because it may be removed, as reaction on a different deletion."));
				return;
			}

			item.delete();
			
			state = State.transaction;
		} catch (DataObjectException ex) {
			_protocol.error(enhanceErrorMessage(event, null), ex);
		}
	}

	/**
	 * Processes the {@link ItemUpdate}s in the written {@link ChangeSet}.
	 */
	protected void processUpdates(Collection<ItemUpdate> updates) {
		for (ItemUpdate update : updates) {
			processUpdate(update);
		}
	}

	/**
	 * Processes the {@link ItemUpdate} in the written {@link ChangeSet}.
	 * 
	 * @param event
	 *        The event to process.
	 */
	protected void processUpdate(ItemUpdate event) {
		try {
			internalBeginCommit();
			ObjectBranchId objectId = event.getObjectId();
			Branch branch = _kbHistory.getBranch(objectId.getBranchId());
			
			KnowledgeItem item = 
				_kbHistory
					.getKnowledgeItem(branch, Revision.CURRENT, objectId.getObjectType(), objectId.getObjectName());
			
			if (item == null) {
				_protocol.error(enhanceErrorMessage(event, "Item not found: " + objectId));
				return;
			}
			
			internalSetValues(event, item, event.getValues().entrySet());

			state = State.transaction;
		} catch (DataObjectException ex) {
			_protocol.error(enhanceErrorMessage(event, null), ex);
		}
	}

	private void internalSetValues(ItemChange event, KnowledgeItem item, Iterable<Entry<String, Object>> values)
			throws NoSuchAttributeException, DataObjectException {
		long eventRevision = event.getRevision();
		MOClass itemType = (MOClass) item.tTable();
		
		for (Entry<String, Object> entry : values) {
			String attributeName = entry.getKey();
			Object attributeValue = entry.getValue();
			transformValueAndSet(item, itemType, attributeName, attributeValue, eventRevision);
		}
	}

	/**
	 * transforms the cache value from a value of the source KB to a value of the destination KB and
	 * sets it to the {@link KnowledgeItem}.
	 * 
	 * @param data
	 *        the object to update
	 * @param itemType
	 *        the type of the given <code>data</code>
	 * @param attributeName
	 *        the name of the attribute for update
	 * @param cacheValue
	 *        the value of the attribute build in the source {@link KnowledgeBase}
	 * @param eventRevision
	 *        the revision in which the event occurred
	 * @see MetaObjectUtils#getAttribute(MetaObject, String)
	 * @see AttributeStorage#getCacheValue(MOAttribute, DataObject, Object[])
	 */
	private void transformValueAndSet(KnowledgeItem data, MOClass itemType, String attributeName,
			Object cacheValue, long eventRevision) throws NoSuchAttributeException, DataObjectException {
		MOAttribute attribute = itemType.getAttributeOrNull(attributeName);
		if (attribute == null) {
			// attribute is flex attribute. Therefore value is "primitive" and must not be
			// transformed.
			data.setAttributeValue(attributeName, cacheValue);
		} else {
			Object applicationValue = toApplicationValue(data, attribute, cacheValue, eventRevision);
			data.setValue(attribute, applicationValue);
		}
	}

	static Object toApplicationValue(ObjectContext context, MOAttribute attribute, Object cacheValue,
			long eventRevision) {
		if (cacheValue instanceof ObjectKey) {
			ObjectKey cachedKey = (ObjectKey) cacheValue;
			long cachedHistoryContext = cachedKey.getHistoryContext();
			if (cachedHistoryContext == eventRevision) {
				// transform objects with the current event revision to current objects
				final long historyContext = Revision.CURRENT_REV;

				final long branchContext = cachedKey.getBranchContext();
				final MetaObject objectType = cachedKey.getObjectType();
				final TLID objectName = cachedKey.getObjectName();
				cacheValue = new DefaultObjectKey(branchContext, historyContext, objectType, objectName);
			}
		}
		return attribute.getStorage().fromCacheToApplicationValue(attribute, context, cacheValue);
	}

	/**
	 * commits the transaction began in {@link #beginCommit()}
	 * 
	 * @param finalized
	 *        whether the call is synthesized by closing this {@link EventWriter}
	 * @throws KnowledgeBaseException
	 *         when commit fails
	 */
	protected void internalCommit(boolean finalized) throws KnowledgeBaseException {
		if (_tx == null) {
			if (!finalized) {
				_protocol.info("Replay of commit without changes.");
			}
			return;
		}
		_tx.commit();
		_tx = null;
	}

	/**
	 * Enhances the given message by adding information about the given {@link KnowledgeEvent}
	 * 
	 * @param event
	 *        the occurred event or list of events. must not be <code>null</code>
	 * @param msg
	 *        may be <code>null</code>. In that case just informations about the event are reported
	 */
	protected static String enhanceErrorMessage(Object event, String msg) {
		StringBuilder message = new StringBuilder("Unable to process ").append(event);
		if (msg != null) {
			message.append(": ").append(msg);
		}
		return message.toString();
	}

	/**
	 * Drops all changes since the last commit event.
	 */
	public void rollbackCurrentChanges() {
		if (_tx == null) {
			return;
		}
		_tx.rollback(null);
		_tx = null;

	}

}
