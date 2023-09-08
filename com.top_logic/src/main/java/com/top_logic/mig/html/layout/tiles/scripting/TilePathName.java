/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.scripting;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.scripting.recorder.ref.ui.BreadcrumbStrings;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.component.TileContainerComponent;

/**
 * {@link ConfigurationItem} representing a path in a {@link TileLayout}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TilePathName extends ConfigurationItem {

	/** Configuration name for the value of {@link #getTilePath()}. */
	String PATH_NAME = "tile-path";

	/**
	 * The path representing the {@link TileLayout}.
	 * 
	 * @see TileContainerComponent#getTilePath(TileLayout)
	 */
	@Format(BreadcrumbStrings.class)
	@Name(PATH_NAME)
	List<String> getTilePath();

	/**
	 * Setter for {@link #getTilePath()}.
	 */
	void setTilePath(List<String> value);

}

