/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations;

import com.top_logic.react.flow.data.LODVariant;
import com.top_logic.react.flow.svg.SvgWriter;

/**
 * Operations for {@link LODVariant}.
 *
 * <p>
 * Variants are not drawn directly: the enclosing {@link com.top_logic.react.flow.data.LOD}
 * picks one variant and writes only that variant's content. {@link #draw(SvgWriter)} is
 * therefore a no-op.
 * </p>
 */
public interface LODVariantOperations extends WidgetOperations {

	@Override
	LODVariant self();

	@Override
	default void draw(SvgWriter out) {
		// Variants are rendered through their enclosing LOD box.
	}

}
