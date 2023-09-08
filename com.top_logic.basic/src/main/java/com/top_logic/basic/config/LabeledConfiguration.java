/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;

/**
 * The internationalised label of a configuration item.
 * 
 * <p>
 * The value of {@link #getLabel()} is the label that is displayed on the GUI to identify this
 * {@link ConfigurationItem}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface LabeledConfiguration extends ConfigurationItem {

	/** Configuration name for the property {@link #getLabel()}. */
	String LABEL_NAME = "label";

	/**
	 * The label of this {@link ConfigurationItem}.
	 */
	@Mandatory
	@Name(LABEL_NAME)
	ResKey getLabel();

	/**
	 * Setter for {@link #getLabel()}
	 */
	void setLabel(ResKey label);

}