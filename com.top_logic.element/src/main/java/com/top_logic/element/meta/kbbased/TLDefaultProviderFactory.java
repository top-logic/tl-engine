/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.TLDefaultValue;
import com.top_logic.model.provider.DefaultProvider;

/**
 * {@link AnnotationsBasedCacheValueFactory} instantiating an {@link TLDefaultValue annotated}
 * {@link DefaultProvider}.
 * 
 * @see DisplayAnnotations#getDefaultProvider(com.top_logic.model.TLStructuredTypePart)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLDefaultProviderFactory extends AnnotationsBasedCacheValueFactory {

	/**
	 * Singleton {@link TLDefaultProviderFactory} instance.
	 */
	public static final TLDefaultProviderFactory INSTANCE = new TLDefaultProviderFactory();

	private TLDefaultProviderFactory() {
		// Singleton constructor.
	}

	@Override
	public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
		TLDefaultValue tlAnnotation = getAnnotation(item, storage, TLDefaultValue.class);
		if (tlAnnotation == null) {
			return null;
		}
		PolymorphicConfiguration<DefaultProvider> providerConfig = tlAnnotation.getProvider();
		DefaultProvider provider =
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(providerConfig);
		return provider;
	}

}

