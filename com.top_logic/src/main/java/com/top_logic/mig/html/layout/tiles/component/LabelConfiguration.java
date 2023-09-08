/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.LabeledConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;

/**
 * Configuration of the label of a {@link LabeledConfiguration}.
 * 
 * @see LabeledConfiguration#getLabel()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface LabelConfiguration extends ConfigurationItem {

	/** Configuration name for the value of the {@link #getLabel()}. */
	String LABEL_NAME = "label";

	/**
	 * The {@link ResKey#text(String) literal text label} of the {@link LabeledConfiguration}.
	 * 
	 * @see LabeledConfiguration#getLabel()
	 */
	@Name(LABEL_NAME)
	@Mandatory
	String getLabel();

}

