/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.cs;

import com.top_logic.model.TLObject;

/**
 * Representation of the creation of a {@link TLObject}.
 */
public final class TLObjectCreation extends TLObjectChange {

	/**
	 * Creates a new {@link TLObjectCreation}.
	 */
	public TLObjectCreation(TLObject object) {
		super(object);
	}

	@Override
	public ChangeType changeType() {
		return ChangeType.CREATE;
	}

}

