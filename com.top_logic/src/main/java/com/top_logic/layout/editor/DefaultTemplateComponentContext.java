/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.mig.html.layout.ComponentName;

/**
 * Default {@link TemplateComponentContext} implementation.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class DefaultTemplateComponentContext implements TemplateComponentContext {

	private ComponentName _name;

	private String _template;

	private ConfigurationItem _templateArguments;

	@Override
	public ComponentName getName() {
		return _name;
	}

	@Override
	public void setName(ComponentName name) {
		_name = name;
	}

	@Override
	public String getTemplate() {
		return _template;
	}

	@Override
	public void setTemplate(String template) {
		_template = template;
	}

	@Override
	public ConfigurationItem getTemplateArguments() {
		return _templateArguments;
	}

	@Override
	public void setTemplateArguments(ConfigurationItem arguments) {
		_templateArguments = arguments;
	}

}
