/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

/**
 * The kind of input control a {@link FilterField} needs, so a UI tier can render the right
 * control without knowing the concrete filter type.
 */
public enum FilterFieldKind {

	/** A free-text input. */
	TEXT,

	/** A numeric input. */
	NUMBER,

	/** A boolean checkbox. */
	CHECKBOX,

	/** A single-select dropdown. */
	SELECT;

}
