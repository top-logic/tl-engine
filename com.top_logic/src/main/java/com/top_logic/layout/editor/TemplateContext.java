/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Subtypes;

/**
 * General context information using typed templates.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public interface TemplateContext {

	/**
	 * Template name.
	 */
	public String getTemplate();

	/**
	 * @see #getTemplate()
	 */
	public void setTemplate(String template);

	/**
	 * Typed template properties.
	 */
	@Subtypes(adjust = false, value = {})
	public ConfigurationItem getTemplateArguments();

	/**
	 * @see #getTemplateArguments()
	 */
	void setTemplateArguments(ConfigurationItem arguments);

}
