/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

/**
 * {@link Enum} representing a selection state of a selection in a {@link TreeSelectionModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public enum TriState {

	/**
	 * Representation of an unselected element.
	 */
	NOT_SELECTED,

	/**
	 * Representation of an selected element.
	 */
	SELECTED,

	/**
	 * Representation of an indeterminate element
	 */
	INDETERMINATE;

}