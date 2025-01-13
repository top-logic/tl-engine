/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import com.top_logic.layout.form.Constraint;

/**
 * Common interface for {@link Constraint}s that limit values to ranges.
 */
public interface IRangeConstraint {

	/**
	 * The lower bound.
	 */
	Comparable<?> getLower();

	/**
	 * Whether {@link #getLower()} is inclusive.
	 */
	boolean isLowerInclusive();

	/**
	 * The upper bound.
	 */
	Comparable<?> getUpper();

	/**
	 * Whether {@link #getUpper()} is inclusive.
	 */
	boolean isUpperInclusive();

}
