/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.DeferredMetaObject;

/**
 * {@link ConfigurationValueProvider} for {@link MetaObject}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MetaObjectFormat extends AbstractConfigurationValueProvider<MetaObject> {

	/**
	 * Singleton {@link MetaObjectFormat} instance.
	 */
	public static final MetaObjectFormat INSTANCE = new MetaObjectFormat();

	private MetaObjectFormat() {
		super(MetaObject.class);
	}

	@Override
	public MetaObject getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		String moName = propertyValue.toString();
		MOPrimitive result = MOPrimitive.getPrimitive(moName);
		if (result != null) {
			return result;
		}
		return new DeferredMetaObject(moName);
	}

	@Override
	public String getSpecificationNonNull(MetaObject configValue) {
		return configValue.getName();
	}

}
