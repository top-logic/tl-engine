/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import com.top_logic.basic.config.annotation.TagName;

/**
 * Marker interface to have configuration name for the root of a {@link TileLayout} hierarchy.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("root")
public interface TileRoot extends CompositeTile {

	// Root of a tile hierarchy

}

