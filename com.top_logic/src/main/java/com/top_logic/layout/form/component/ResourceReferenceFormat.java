/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * {@link ConfigurationValueProvider} for web application resource references.
 * 
 * <p>
 * The resource name is checked during parsing for being resolvable through the {@link FileManager}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ResourceReferenceFormat extends AbstractConfigurationValueProvider<String> {

	/**
	 * Singleton {@link ResourceReferenceFormat} instance.
	 */
	public static final ResourceReferenceFormat INSTANCE = new ResourceReferenceFormat();

	private ResourceReferenceFormat() {
		super(String.class);
	}

	@Override
	protected String getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		String reference = propertyValue.toString();
		{
			if (!FileManager.getInstance().exists(reference)) {
				throw new ConfigurationException("Missing resource '" + reference + "'.", propertyName,
					propertyValue);
			}
		}
		return reference;
	}

	@Override
	protected String getSpecificationNonNull(String configValue) {
		return configValue;
	}

}