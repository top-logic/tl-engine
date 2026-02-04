/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.cs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * Change set of a {@link KnowledgeBase} commit.
 */
public class TLObjectChangeSet {

	private List<TLObjectUpdate> _updates = Collections.emptyList();

	private List<TLObjectCreation> _creations = Collections.emptyList();

	private List<TLObjectDeletion> _deletions = Collections.emptyList();

	private final KnowledgeBase _kb;

	private final long _revision;

	/**
	 * Creates a new {@link TLObjectChangeSet}.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} in which the change occurs.
	 * @param revision
	 *        Revision of the commit.
	 */
	public TLObjectChangeSet(KnowledgeBase kb, long revision) {
		_kb = kb;
		_revision = revision;
	}

	/**
	 * Stores the given {@link TLObjectChange} as part of this {@link TLObjectChangeSet}.
	 */
	public void addChange(TLObjectChange change) {
		switch (change.changeType()) {
			case CREATE:
				addCreation((TLObjectCreation) change);
				return;
			case DELETE:
				addDeletion((TLObjectDeletion) change);
				return;
			case UPDATE:
				addUpdate((TLObjectUpdate) change);
				return;
		}
		throw new IllegalArgumentException("Unexpected value: " + change.changeType());
	}

	/**
	 * Stores the {@link TLObjectUpdate} as part of this {@link TLObjectChangeSet}.
	 */
	public void addUpdate(TLObjectUpdate change) {
		if (_updates.isEmpty()) {
			_updates = new ArrayList<>();
		}
		_updates.add(change);
	}

	/**
	 * Stores the {@link TLObjectCreation} as part of this {@link TLObjectChangeSet}.
	 */
	public void addCreation(TLObjectCreation change) {
		if (_creations.isEmpty()) {
			_creations = new ArrayList<>();
		}
		_creations.add(change);
	}

	/**
	 * Stores the {@link TLObjectDeletion} as part of this {@link TLObjectChangeSet}.
	 */
	public void addDeletion(TLObjectDeletion change) {
		if (_deletions.isEmpty()) {
			_deletions = new ArrayList<>();
		}
		_deletions.add(change);
	}

	/**
	 * All {@link TLObjectUpdate} in this change set.
	 */
	public List<TLObjectUpdate> updates() {
		return _updates;
	}

	/**
	 * All {@link TLObjectCreation} in this change set.
	 */
	public List<TLObjectCreation> creations() {
		return _creations;
	}

	/**
	 * All {@link TLObjectDeletion} in this change set.
	 */
	public List<TLObjectDeletion> deletions() {
		return _deletions;
	}

	/**
	 * The {@link KnowledgeBase} in which the change happened.
	 */
	public KnowledgeBase kb() {
		return _kb;
	}

	/**
	 * Commit number of the revision of this {@link TLObjectChangeSet}.
	 */
	public long revision() {
		return _revision;
	}

}

