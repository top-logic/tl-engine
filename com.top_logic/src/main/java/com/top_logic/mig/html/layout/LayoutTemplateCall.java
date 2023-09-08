/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.io.IOException;
import java.io.OutputStream;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.mig.html.layout.LayoutComponent.Config;
import com.top_logic.util.error.TopLogicException;

/**
 * Layout origins from a template.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class LayoutTemplateCall implements TLLayout {

	private final String _templateName;

	private ConfigurationItem _arguments;

	private LayoutComponent.Config _layoutConfiguration;

	/**
	 * Creates a {@link TLLayout} that origins from a template.
	 */
	public LayoutTemplateCall(String templateName, ConfigurationItem arguments, String layoutKey) {
		_templateName = templateName;
		initArguments(arguments, layoutKey);
		_layoutConfiguration = createLayoutConfiguration(layoutKey);
	}

	/**
	 * Creates the layout configuration by instantiating the template with the given layout scope.
	 */
	protected Config createLayoutConfiguration(String layoutKey) {
		try {
			if (_arguments != null) {
				return LayoutTemplateUtils.getInstantiatedLayoutTemplate(_templateName, _arguments, layoutKey);
			}

			return null;
		} catch (ConfigurationException exception) {
			throw new TopLogicException(I18NConstants.CREATING_COMPONENT_ERROR__LAYOUT.fill(layoutKey), exception);
		}
	}

	/**
	 * {@link ConfigurationItem} representing the typed arguments for a given template.
	 * 
	 * @throws ConfigurationException
	 *         When the arguments are not valid.
	 */
	@Override
	public ConfigurationItem getArguments() throws ConfigurationException {
		return _arguments;
	}

	void initArguments(ConfigurationItem arguments, String scope) {
		LayoutUtils.qualifyComponentNames(scope, arguments);
		_arguments = arguments;
	}

	void initLayoutConfiguration(LayoutComponent.Config configuration) {
		_layoutConfiguration = configuration;
	}

	/**
	 * Relative layout template file path.
	 */
	@Override
	public String getTemplateName() {
		return _templateName;
	}

	@Override
	public Config get() throws ConfigurationException {
		return _layoutConfiguration;
	}

	@Override
	public boolean hasTemplate() {
		return true;
	}

	@Override
	public void writeTo(OutputStream stream, boolean isFinal) throws IOException {
		LayoutTemplateUtils.writeLayoutTemplateCall(stream, _templateName, _arguments, isFinal);
	}
}
