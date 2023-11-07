/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.call.request;

import org.apache.hc.core5.http.ContentType;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * {@link ConfigurationValueProvider} formatting and parsing a {@link ContentType}.
 */
public class ContentTypeFormat extends AbstractConfigurationValueProvider<ContentType> {

	/**
	 * Creates a {@link ContentTypeFormat}.
	 */
	public ContentTypeFormat() {
		super(ContentType.class);
	}

	@Override
	protected ContentType getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		return ContentType.parseLenient(propertyValue);
	}

	@Override
	protected String getSpecificationNonNull(ContentType configValue) {
		return configValue.toString();
	}

}
