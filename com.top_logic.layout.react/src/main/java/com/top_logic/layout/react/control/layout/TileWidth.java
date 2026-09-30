/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Relative width of a dashboard tile as a fraction of the available column count.
 *
 * <p>
 * The actual column span is {@code round(fraction * columns)}, capped to the
 * remaining free columns in the current row. Fractions are targets, not hard
 * constraints — the layout engine stretches the last tile per row to guarantee
 * gap-free rows.
 * </p>
 */
public enum TileWidth implements ExternallyNamed {

	/** Roughly 25% of available columns. */
	QUARTER("quarter", 0.25),

	/** Roughly 33% of available columns. */
	THIRD("third", 1.0 / 3.0),

	/** Roughly 50% of available columns. */
	HALF("half", 0.5),

	/** Roughly 67% of available columns. */
	TWO_THIRDS("two-thirds", 2.0 / 3.0),

	/** Full row. */
	FULL("full", 1.0);

	private final String _externalName;

	private final double _fraction;

	TileWidth(String externalName, double fraction) {
		_externalName = externalName;
		_fraction = fraction;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

	/**
	 * The fraction of available columns (0, 1].
	 */
	public double getFraction() {
		return _fraction;
	}
}
