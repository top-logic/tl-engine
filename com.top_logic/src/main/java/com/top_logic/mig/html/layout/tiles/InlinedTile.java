/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link TileLayout} containing the whole configuraton of a {@link LayoutComponent} inlined.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("inlinedTile")
public interface InlinedTile extends PersonalizedTile {

	/**
	 * The configuration of the displayed component.
	 */
	LayoutComponent.Config getComponent();

	/**
	 * Setter for {@link #getComponent()}.
	 */
	void setComponent(LayoutComponent.Config config);

	/**
	 * Application wide unique identifier of this {@link TileLayout}.
	 */
	String getId();

	/**
	 * Settter for {@link #getId()}.
	 */
	void setId(String id);

}

