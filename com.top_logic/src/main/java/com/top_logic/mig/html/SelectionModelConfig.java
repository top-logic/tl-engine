/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Configuration for the selection model of a {@link LayoutComponent}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public interface SelectionModelConfig extends ConfigurationItem {

	/**
	 * The configuration parameter for {@link #getSelectionModelFactory()}.
	 */
	String SELECTION_MODEL_FACTORY = "selectionModelFactory";

	/**
	 * Factory to create the {@link SelectionModel}.
	 */
	@InstanceFormat
	@InstanceDefault(DefaultSelectionModelFactory.class)
	@ImplementationClassDefault(DefaultSelectionModelFactory.class)
	@Name(SELECTION_MODEL_FACTORY)
	SelectionModelFactory getSelectionModelFactory();

}
