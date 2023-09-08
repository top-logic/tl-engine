/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * {@link ConfigurationValueProvider} for {@link URL}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class URLFormat extends AbstractConfigurationValueProvider<URL> {

	/**
	 * Singleton {@link URLFormat} instance.
	 */
	public static final URLFormat INSTANCE = new URLFormat();

	private URLFormat() {
		super(URL.class);
	}

	@Override
	public URL getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		try {
			return new URL(propertyValue.toString());
		} catch (MalformedURLException ex) {
			throw new ConfigurationException("Invalid URL specification.", ex);
		}
	}

	@Override
	public String getSpecificationNonNull(URL configValue) {
		return configValue.toExternalForm();
	}

}
