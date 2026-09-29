/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.ViewContext;

/**
 * Strategy producing the breadcrumb label of a {@link TileFrame} at push time.
 *
 * <p>
 * The result is captured into {@link TileFrame#getLabel()} once when the frame is created and
 * thereafter does not re-evaluate. To compute a label that depends on a value already on a
 * channel of the caller's scope (e.g. the name of the selected business object), pick the
 * {@link ScriptedTileLabel} implementation; for fixed text use {@link StaticTileLabel}.
 * </p>
 */
public interface TileLabelProvider {

	/**
	 * Computes the breadcrumb label.
	 *
	 * @param context
	 *        The {@link ViewContext} of the call site (the frame from which the push originates).
	 *        Use it to resolve channels referenced by the provider.
	 * @return The label, or {@code null} for an unlabeled frame.
	 */
	ResKey compute(ViewContext context);

	/**
	 * Configuration for {@link TileLabelProvider}.
	 */
	interface Config<I extends TileLabelProvider> extends PolymorphicConfiguration<I> {
		// No common properties.
	}
}
