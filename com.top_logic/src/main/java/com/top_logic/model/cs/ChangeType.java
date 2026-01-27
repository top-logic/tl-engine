/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.cs;

/**
 * Type of a {@link TLObjectChange}.
 */
public enum ChangeType {

	/** Change is a {@link TLObjectCreation}. */
	CREATE,

	/** Change is a {@link TLObjectUpdate}. */
	UPDATE,

	/** Change is a {@link TLObjectDeletion}. */
	DELETE;

}

