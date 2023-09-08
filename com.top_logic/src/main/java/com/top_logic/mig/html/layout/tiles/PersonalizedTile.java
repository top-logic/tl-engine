/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import com.top_logic.basic.config.LabeledConfiguration;
import com.top_logic.mig.html.layout.tiles.component.PreviewedTile;

/**
 * A {@link TileLayout} that is created by the user.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface PersonalizedTile extends TileLayout, LabeledConfiguration, PreviewedTile {

	/**
	 * Whether the {@link TileLayout} is currently hidden.
	 */
	boolean isHidden();

	/**
	 * Setter for {@link #isHidden()}.
	 * 
	 * @see #isHidden()
	 */
	void setHidden(boolean hidden);

}

