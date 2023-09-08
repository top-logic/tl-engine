/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.template;

import java.io.StringReader;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.template.parser.ConfigTemplateParser;
import com.top_logic.basic.config.template.parser.ParseException;

/**
 * {@link ConfigurationValueProvider} for {@link TemplateExpression}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TemplateFormat extends AbstractConfigurationValueProvider<TemplateExpression> {

	/**
	 * Creates a {@link TemplateFormat}.
	 */
	public TemplateFormat() {
		super(TemplateExpression.class);
	}

	@Override
	protected TemplateExpression getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		try {
			return new ConfigTemplateParser(new StringReader(propertyValue.toString())).template();
		} catch (ParseException ex) {
			throw new ConfigurationException(I18NConstants.ERROR_PARSING_TEMPLATE, propertyName, propertyValue, ex);
		}
	}

	@Override
	protected String getSpecificationNonNull(TemplateExpression configValue) {
		return configValue.toString();
	}

}
