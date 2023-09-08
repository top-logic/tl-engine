/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;

/**
 * Configuration of the name of a component displayed in tile environment.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TileComponentName extends InternationalizedName {

	/**
	 * Name of the new component.
	 * 
	 * <p>
	 * The name is displayed in the navigation bar. It is displayed parameterised with the context
	 * model of the component. The context model is displayed at the position '{0}' in the name.
	 * </p>
	 * 
	 * @see com.top_logic.mig.html.layout.tiles.InternationalizedName#getName()
	 */
	@Override
	@FormattedDefault("com.top_logic.mig.html.layout.tiles.TileComponentName.name.defaultValue")
	ResKey getName();

}

