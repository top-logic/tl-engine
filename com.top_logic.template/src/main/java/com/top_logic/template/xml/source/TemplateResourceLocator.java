/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.xml.source;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * Factory for {@link TemplateResourceSource}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TemplateResourceLocator implements TemplateLocator {

	/**
	 * Configuration options for {@link TemplateResourceLocator}.
	 */
	public interface Config extends PolymorphicConfiguration<TemplateResourceLocator> {

		/**
		 * The optional application root-folder where templates are stored in the application.
		 * 
		 * <p>
		 * If no path is given, references are resolved relative to the web application root.
		 * </p>
		 */
		@Nullable
		String getBasePath();

	}

	private final Config _config;

	/**
	 * Creates a {@link TemplateResourceLocator} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TemplateResourceLocator(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public TemplateSource resolve(TemplateSource context, String templateReference) {
		return new TemplateResourceSource(this, resourceName(templateReference));
	}

	private String resourceName(String templateReference) {
		StringBuilder result = new StringBuilder();
		appendBasePath(result);
		appendPath(result, templateReference);
		return result.toString();
	}

	private void appendBasePath(StringBuilder result) {
		String basePath = _config.getBasePath();
		if (basePath != null) {
			appendPath(result, basePath);
		}
	}

	private void appendPath(StringBuilder result, String path) {
		if ((!path.startsWith("/")) && path.indexOf(':') < 0) {
			result.append("/");
		}
		result.append(path);
	}

}