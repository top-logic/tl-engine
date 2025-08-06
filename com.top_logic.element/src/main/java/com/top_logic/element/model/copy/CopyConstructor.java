/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.copy;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;

/**
 * Constructor function allocating a copy of a specific original object.
 */
public interface CopyConstructor {
	/**
	 * Allocates a copy of the given original.
	 *
	 * @param orig
	 *        The object to copy.
	 * @param reference
	 *        The reference in which the original is stored. <code>null</code> for a top-level
	 *        copy operation.
	 * @param context
	 *        The context object holding the original. <code>null</code> for a top-level copy
	 *        operation.
	 * @return A newly allocated uninitialized copy of the given original.
	 */
	TLObject allocate(TLObject orig, TLReference reference, TLObject context);
}