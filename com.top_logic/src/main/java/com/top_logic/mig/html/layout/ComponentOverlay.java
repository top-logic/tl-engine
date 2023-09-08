/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;

/**
 * Configuration of the overlays for the components in a given layout tree.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ComponentOverlay extends ConfigurationItem {

	/**
	 * Default tag name for using this {@link ConfigurationItem} as top level configuration in a
	 * layout file.
	 */
	String DEFAULT_TAG_NAME = "components";

	/**
	 * Configuration of the overlay components, indexed by its name.
	 */
	@DefaultContainer
	@Key(LayoutComponent.Config.NAME)
	Map<ComponentName, LayoutComponent.Config> getComponents();

}

