/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.config;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.HTMLTemplateUtils;

/**
 * {@link AbstractConfigurationValueProvider} for {@link HTMLTemplateFragment}s.
 */
public class HTMLTemplateFragmentFormat extends AbstractConfigurationValueProvider<HTMLTemplateFragment> {

	/**
	 * Creates a {@link HTMLTemplateFragmentFormat}.
	 */
	public HTMLTemplateFragmentFormat() {
		super(HTMLTemplateFragment.class);
	}

	@Override
	protected HTMLTemplateFragment getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		String html = propertyValue.toString();
		HTMLTemplateFragment template = HTMLTemplateUtils.parse(propertyName, html);
		return new ConfiguredTemplate(template, html);
	}

	@Override
	protected String getSpecificationNonNull(HTMLTemplateFragment configValue) {
		if (configValue instanceof ConfiguredTemplate) {
			return ((ConfiguredTemplate) configValue).getHtml();
		}
		throw new IllegalArgumentException("Only ConfiguredTemplate instances can be used as configuration values.");
	}

}
