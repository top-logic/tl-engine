/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.cs;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Representation of the update of a {@link TLObject}.
 * 
 * <p>
 * The {@link TLObjectUpdate} contains information about the changed values in the change.
 * </p>
 */
public final class TLObjectUpdate extends TLObjectChange {

	private Map<TLStructuredTypePart, Object> _oldValues = new HashMap<>();

	private Map<TLStructuredTypePart, Object> _newValues = new HashMap<>();

	/**
	 * Creates a new {@link TLObjectUpdate}.
	 */
	public TLObjectUpdate(TLObject object) {
		super(object);
	}

	@Override
	public ChangeType changeType() {
		return ChangeType.UPDATE;
	}

	/**
	 * Delivers the values before the change.
	 * 
	 * <p>
	 * The keys of the map are the {@link TLStructuredTypePart} and the values the corresponding
	 * value before the change. Both, keys and values may contain actually deleted objects.
	 * </p>
	 * 
	 * <p>
	 * The current value for changed {@link TLStructuredTypePart} is stored in {@link #newValues()}.
	 * When a {@link TLStructuredTypePart} is deleted in the same commit, then {@link #oldValues()}
	 * contain an entry with the old value, but {@link #newValues()} does not contain an entry for
	 * the part.
	 * </p>
	 * 
	 * <p>
	 * When the value for a {@link TLStructuredTypePart} was not changed, there is no entry either
	 * in {@link #oldValues()} or {@link #newValues()}.
	 * </p>
	 * 
	 * @return The changed values before the change.
	 * 
	 * @see #newValues()
	 * @see KnowledgeBase#withoutModifications(com.top_logic.basic.util.ComputationEx2)
	 */
	public Map<TLStructuredTypePart, Object> oldValues() {
		return _oldValues;
	}

	/**
	 * Delivers the values after the change.
	 * 
	 * <p>
	 * The keys of the map are the {@link TLStructuredTypePart} and the values the corresponding
	 * value before the change.
	 * </p>
	 * 
	 * <p>
	 * The old value for changed {@link TLStructuredTypePart} is stored in {@link #oldValues()}.
	 * When a {@link TLStructuredTypePart} is created in the same commit, then {@link #newValues()}
	 * contain an entry with the new value, but {@link #oldValues()} does not contain an entry for
	 * the part.
	 * </p>
	 * 
	 * <p>
	 * When the value for a {@link TLStructuredTypePart} was not changed, there is no entry either
	 * in {@link #oldValues()} or {@link #newValues()}.
	 * </p>
	 * 
	 * @return The changed values after the change.
	 * 
	 * @see #oldValues()
	 * @see KnowledgeBase#withoutModifications(com.top_logic.basic.util.ComputationEx2)
	 */
	public Map<TLStructuredTypePart, Object> newValues() {
		return _newValues;
	}

}

