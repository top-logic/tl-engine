/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilteredIterable;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.objects.KnowledgeItem;

/**
 * Describes modifications done to a {@link KnowledgeBase} within a single {@link Revision}.
 * 
 * <p>
 * For each {@link Revision} that touches {@link KnowledgeItem}s, a {@link UpdateEvent} is created
 * that keeps the keys of all created, updated, and deleted objects within that revision.
 * </p>
 * 
 * <p>
 * {@link UpdateEvent}s are either processed synchronously by the
 * {@link KnowledgeBase#addUpdateListener(UpdateListener) registration} of a {@link UpdateListener}
 * at the {@link KnowledgeBase}, or asynchronously by explicitly requesting the next outstanding
 * updates by attaching to the {@link KnowledgeBase#getUpdateChain() update chain}.
 * </p>
 * 
 * <p>
 * Asynchronously, this change event can be processed asynchronously during a UI update to bring UI
 * models in synch with the changed state of the business model. Synchronously, this event can be
 * processed to invalidate global caches.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UpdateEvent {

	private static final Filter<Object> NOT_NULL = new Filter<>() {
		@Override
		public boolean accept(Object anObject) {
			return anObject != null;
		}
	};

	private final KnowledgeBase kb;
	private final boolean remote;

	private final String contextId;

	private Map<ObjectKey, KnowledgeItem> createdObjectKeys;

	private Map<ObjectKey, KnowledgeItem> updatedObjectKeys;

	private Map<ObjectKey, KnowledgeItem> deletedObjectKeys;

	private final long _commitNumber;

	private final ChangeSet _changeSet;
	
	/**
	 * Creates a {@link UpdateEvent}.
	 * 
	 * @param kb
	 *        See {@link #getKnowledgeBase()}.
	 * @param remote
	 *        See {@link #isRemote()}.
	 * @param commitNumber
	 *        See {@link #getCommitNumber()}.
	 * @param createdObjectKeys
	 *        See {@link #getCreatedObjectKeys()}.
	 * @param updatedObjectKeys
	 *        See {@link #getUpdatedObjectKeys()}.
	 * @param deletedObjectKeys
	 *        See {@link #getDeletedObjectKeys()}.
	 * @param changeSet
	 *        The changes represented by this {@link UpdateEvent}.
	 */
	public UpdateEvent(KnowledgeBase kb, boolean remote, long commitNumber,
			Map<ObjectKey, KnowledgeItem> createdObjectKeys,
			Map<ObjectKey, KnowledgeItem> updatedObjectKeys,
			Map<ObjectKey, KnowledgeItem> deletedObjectKeys, ChangeSet changeSet) {
		this.kb = kb;
		this.remote = remote;
		_commitNumber = commitNumber;
		if (!remote) {
			contextId = ThreadContextManager.getSubSession().getContextId();
		} else {
			contextId = null;
		}
		this.createdObjectKeys = createdObjectKeys;
		this.updatedObjectKeys = updatedObjectKeys;
		this.deletedObjectKeys = deletedObjectKeys;
		this._changeSet = changeSet;
	}

	/**
	 * The {@link KnowledgeBase} in which this {@link UpdateEvent} happened.
	 */
	public KnowledgeBase getKnowledgeBase() {
		return kb;
	}
	
	/**
	 * Whether this event describes a revision that was committed on a remote
	 * node.
	 * 
	 * @return <code>true</code> if this changes were committed on a remote
	 *         node, <code>false</code>, if the changes were locally committed.
	 */
	public boolean isRemote() {
		return remote;
	}

	/**
	 * The commit number of the revision, whose changes this event describes.
	 */
	public long getCommitNumber() {
		return _commitNumber;
	}

	/**
	 * The {@link ObjectKey}s of the objects created.
	 */
	public Set<ObjectKey> getCreatedObjectKeys() {
		return getCreatedObjects().keySet();
	}

	/**
	 * The objects created in this event.
	 */
	public Map<ObjectKey, KnowledgeItem> getCreatedObjects() {
		return this.createdObjectKeys;
	}
	
	/**
	 * The {@link ObjectKey}s of the updated objects.
	 */
	public Set<ObjectKey> getUpdatedObjectKeys() {
		return getUpdatedObjects().keySet();
	}

	/**
	 * The objects updated in this event.
	 */
	public Map<ObjectKey, KnowledgeItem> getUpdatedObjects() {
		return this.updatedObjectKeys;
	}

	/**
	 * The {@link ObjectKey}s of the deleted objects.
	 */
	public Set<ObjectKey> getDeletedObjectKeys() {
		return this.deletedObjectKeys.keySet();
	}
	
	/**
	 * The deleted objects that were in the local cache at the time of the event creation.
	 */
	public Iterable<KnowledgeItem> getCachedDeletedObjects() {
		return new FilteredIterable<>(NOT_NULL, deletedObjectKeys.values());
	}

	/**
	 * the {@link SubSessionContext#getContextId() context id} of the session that produces
	 *         the event or <code>null</code> if this is a remote event.
	 */
	public String getContextId() {
		return contextId;
	}

	/**
	 * The changes represent by this {@link UpdateEvent}.
	 */
	public ChangeSet getChanges() {
		return _changeSet;
	}

	/** 
	 * Fetches the touched objects in a remote event to ensure they are not loaded on the fly one by one.
	 */
	public void fetch(BulkIdLoad loader) {
		if (!isRemote()) {
			throw new IllegalStateException("Non remote event must not be fetched. All objects are still in cache.");
		}

		fetch(loader, createdObjectKeys);
		fetch(loader, updatedObjectKeys);

		createdObjectKeys = unmodifiableMap(createdObjectKeys);
		updatedObjectKeys = unmodifiableMap(updatedObjectKeys);
		deletedObjectKeys = unmodifiableMap(deletedObjectKeys);
	}

	private void fetch(BulkIdLoad loader, Map<ObjectKey, KnowledgeItem> keys) {
		if (!keys.isEmpty()) {
			/* ensure that only those items are loaded that are of interest for the event. */
			loader.clear();

			for (Entry<ObjectKey, KnowledgeItem> entry : keys.entrySet()) {
				if (entry.getValue() == null) {
					loader.add(entry.getKey());
				}
			}

			for (KnowledgeItem item : loader.loadUncachedInRevision(_commitNumber)) {
				ObjectKey key = item.tId();
				keys.put(key, item);
			}
		}
	}

	private static <K, V> Map<K, V> unmodifiableMap(Map<K, V> map) {
		return map.isEmpty() ? Collections.<K, V> emptyMap() : Collections.unmodifiableMap(map);
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("knowledge-base", getKnowledgeBase().getName())
			.add("context-id", getContextId())
			.add("commit-number", getCommitNumber())
			.add("remote", isRemote())
			.add("creates", getCreatedObjects().size())
			.add("updates", getUpdatedObjects().size())
			.add("deletes", getDeletedObjectKeys().size())
			.build();
	}

}
