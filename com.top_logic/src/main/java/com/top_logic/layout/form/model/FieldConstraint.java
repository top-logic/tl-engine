/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.basic.util.ResKey;

/**
 * Constraint that validates a field value.
 */
@FunctionalInterface
public interface FieldConstraint {

	/**
	 * Checks the given value.
	 *
	 * @param value
	 *        The value to check. May be {@code null}.
	 * @return {@code null} if valid, an error {@link ResKey} if invalid.
	 */
	ResKey check(Object value);
}
