/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.copy;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Filter function deciding whether to copy a specific part.
 */
public interface CopyFilter {
	/**
	 * Whether to copy the given part.
	 *
	 * @param part
	 *        The {@link TLStructuredTypePart} being copied.
	 * @param value
	 *        The current value being copied.
	 * @param context
	 *        The context object defining the given part and holding the given value.
	 * @return Whether to copy the value.
	 */
	boolean accept(TLStructuredTypePart part, Object value, TLObject context);
}