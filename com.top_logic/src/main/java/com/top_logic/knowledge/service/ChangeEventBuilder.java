/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.objects.KnowledgeItem;


/**
 * Builder for a sequence of {@link UpdateEvent}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ChangeEventBuilder {

	private final KnowledgeBase kb;
	
	private long _commitNumber;

	private Map<ObjectKey, KnowledgeItem> createdObjects;

	private Map<ObjectKey, KnowledgeItem> updatedObjects;

	private Map<ObjectKey, KnowledgeItem> deletedObjects;

	public ChangeEventBuilder(KnowledgeBase kb) {
		this.kb = kb;
		init();
	}
	
	/**
	 * See {@link UpdateEvent#getCommitNumber()}.
	 */
	public void setCommitNumber(long commitNumber) {
		_commitNumber = commitNumber;
	}

	/**
	 * Adds the given {@link ObjectKey} to the set of created objects.
	 */
	public void addCreatedObjectKey(ObjectKey objectIdentifier, KnowledgeItem object) {
		if (createdObjects == null) {
			createdObjects = new HashMap<>();
		}
		createdObjects.put(objectIdentifier, object);
	}

	/**
	 * Adds the given {@link ObjectKey} to the set of updated objects.
	 */
	public void addUpdatedObjectKey(ObjectKey key, KnowledgeItem object) {
		if (updatedObjects == null) {
			updatedObjects = new HashMap<>();
		}
		updatedObjects.put(key, object);
	}
	
	/**
	 * Adds the given {@link ObjectKey} to the set of deleted objects.
	 */
	public void addDeletedObjectKey(ObjectKey key, KnowledgeItem object) {
		if (deletedObjects == null) {
			deletedObjects = new HashMap<>();
		}
		deletedObjects.put(key, object);
	}

	/**
	 * Creates a {@link UpdateEvent} from the {@link ObjectKey}s added so far and {@link #clear()}s
	 * this builder.
	 * 
	 * @return the new {@link UpdateEvent}.
	 */
	public UpdateEvent createEvent(boolean remote, ChangeSet cs) {
		if (_commitNumber < 0) {
			throw new IllegalStateException("Commit number not initialized");
		}

		UpdateEvent result = new UpdateEvent(
			kb,
			remote,
			_commitNumber,
			notNull(createdObjects),
			notNull(updatedObjects),
			notNull(deletedObjects), cs);
		clear();
		return result;
	}

	private Map<ObjectKey, KnowledgeItem> notNull(Map<ObjectKey, KnowledgeItem> map) {
		if (map == null) {
			return Collections.emptyMap();
		}
		return map;
	}

	/**
	 * Resets this builder by clearing all added keys.
	 */
	public void clear() {
		init();
	}

	private void init() {
		_commitNumber = -1;
		createdObjects = null;
		updatedObjects = null;
		deletedObjects = null;
	}

}
