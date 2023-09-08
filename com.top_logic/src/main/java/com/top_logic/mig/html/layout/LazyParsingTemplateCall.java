/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.util.ComputationEx;
import com.top_logic.layout.processor.TemplateLayout;
import com.top_logic.mig.html.layout.LayoutComponent.Config;

/**
 * {@link LayoutTemplateCall} that fetches {@link #getArguments()} on demand.
 * 
 * <p>
 * It is possible that the arguments of a {@link TemplateLayout} are not valid at creation time. The
 * {@link LazyParsingTemplateCall} fetches the arguments on demand and stores the successful
 * result locally.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LazyParsingTemplateCall extends LayoutTemplateCall {

	private ComputationEx<ConfigurationItem, ConfigurationException> _arguments;

	private String _layoutKey;

	/**
	 * Creates a new {@link LazyParsingTemplateCall}.
	 * 
	 * @param templateName
	 *        Name of the {@link #getTemplateName() template}.
	 * @param arguments
	 *        Computation algorithm to create the value of {@link #getArguments()}. When creating
	 *        the arguments fail, a {@link ConfigurationException} must be thrown. The returned
	 *        {@link ConfigurationItem} must not be <code>null</code>.
	 */
	public LazyParsingTemplateCall(String templateName,
			ComputationEx<ConfigurationItem, ConfigurationException> arguments, String layoutKey) {
		super(templateName, null, layoutKey);
		_arguments = arguments;
		_layoutKey = layoutKey;
	}

	@Override
	public ConfigurationItem getArguments() throws ConfigurationException {
		ConfigurationItem currentArguments = super.getArguments();
		if (currentArguments != null) {
			return currentArguments;
		}

		ConfigurationItem parsedArguments = _arguments.run();
		initArguments(parsedArguments, _layoutKey);
		return parsedArguments;
	}

	@Override
	public Config get() throws ConfigurationException {
		Config layoutConfiguration = super.get();
		if (layoutConfiguration != null) {
			return layoutConfiguration;
		}

		getArguments();
		Config parsedLayoutConfiguration = createLayoutConfiguration(_layoutKey);
		initLayoutConfiguration(parsedLayoutConfiguration);

		return parsedLayoutConfiguration;
	}

}
