/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.cs;

import com.top_logic.model.TLObject;

/**
 * Change of a {@link TLObject} in a {@link TLObjectChangeSet}.
 */
public abstract sealed class TLObjectChange permits TLObjectUpdate, TLObjectCreation, TLObjectDeletion {

	private final TLObject _object;

	/**
	 * Creates a new {@link TLObjectChange}.
	 * 
	 */
	public TLObjectChange(TLObject object) {
		_object = object;
	}

	/**
	 * Delivers the object which was changed.
	 */
	public TLObject object() {
		return _object;
	}

	/**
	 * Type of this {@link TLObjectChange}.
	 */
	public abstract ChangeType changeType();

}

