/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * {@link ConfigurationValueProvider} for {@link TLID}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IDFormat extends AbstractConfigurationValueProvider<TLID> {

	/**
	 * Singleton {@link IDFormat} instance.
	 */
	public static final IDFormat INSTANCE = new IDFormat();

	private IDFormat() {
		super(TLID.class);
	}

	@Override
	public TLID getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		return IdentifierUtil.fromExternalForm(propertyValue.toString());
	}

	@Override
	public String getSpecificationNonNull(TLID configValue) {
		return IdentifierUtil.toExternalForm(configValue);
	}

}
