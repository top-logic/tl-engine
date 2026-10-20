/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.present;

/**
 * How a {@link Presentation} shows its objects.
 *
 * <p>
 * The constant names are what the model writes: the {@code present} tool's schema lists them.
 * </p>
 */
public enum PresentAs {

	/** One row per object, with the columns of the objects' type - or one per label of data. */
	TABLE,

	/** One bar, point or slice per object, its label and value computed from the object - or data. */
	CHART,

	/** The fields of exactly one object. */
	FORM;

}
