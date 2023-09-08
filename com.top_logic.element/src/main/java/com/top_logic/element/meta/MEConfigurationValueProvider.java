/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.Set;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.model.TLClass;

/**
 * {@link ConfigurationValueProvider} which serializes {@link TLClass}s by their
 * {@link TLClass#getName()}.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class MEConfigurationValueProvider extends AbstractConfigurationValueProvider<TLClass> {

	/** Singleton {@link MEConfigurationValueProvider} instance */
	public static final MEConfigurationValueProvider INSTANCE = new MEConfigurationValueProvider();

	private MEConfigurationValueProvider() {
		// singleton instance
		super(TLClass.class);
	}

	@Override
	protected TLClass getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		final String desiredMetaElementType = propertyValue.toString();
		final Set allMetaElements = MetaElementFactory.getInstance().getAllMetaElements();
		for (Object metaElement : allMetaElements) {
			if (desiredMetaElementType.equals(((TLClass) metaElement).getName())) {
				return (TLClass) metaElement;
			}
		}
		return null;
	}

	@Override
	protected String getSpecificationNonNull(TLClass configValue) {
		return configValue.getName();
	}
}
