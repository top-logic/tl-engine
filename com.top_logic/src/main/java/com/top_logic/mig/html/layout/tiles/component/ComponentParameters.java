/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.layout.processor.LayoutModelConstants;
import com.top_logic.tool.boundsec.SecurityConfiguration;

/**
 * {@link ConfigurationItem} representing the informations that are needed to include a layout file.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ComponentParameters extends ConfigurationItem {

	/** Name of the configuration of {@link #getTemplate()}. */
	String TEMPLATE_ATTR = LayoutModelConstants.INCLUDE_NAME;

	/** Configuration name of the value of {@link #getSecurityId()}. */
	String SECURITY_ID = "securityId";

	/**
	 * Name of the template (relative to {@link ModuleLayoutConstants#LAYOUT_RESOURCE_PREFIX})
	 * holding the layout template.
	 */
	@Hidden
	@Mandatory
	@Name(TEMPLATE_ATTR)
	String getTemplate();

	/**
	 * Setter for {@link #getTemplate()}.
	 */
	void setTemplate(String template);

	/**
	 * The {@link SecurityConfiguration#getSecurityId() security id} of the component in the tile.
	 * This is not selected by the user but filled from configuration.
	 */
	@Hidden
	@Name(SECURITY_ID)
	String getSecurityId();

}

