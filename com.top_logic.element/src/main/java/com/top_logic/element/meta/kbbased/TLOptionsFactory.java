/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.element.config.annotation.TLOptions;
import com.top_logic.element.meta.kbbased.filtergen.Generator;

/**
 * {@link AnnotationsBasedCacheValueFactory} creating a {@link Generator} from the {@link TLOptions}
 * annotation.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLOptionsFactory extends AnnotationsBasedCacheValueFactory {

	/**
	 * Singleton {@link TLOptionsFactory} instance.
	 */
	public static final TLOptionsFactory INSTANCE = new TLOptionsFactory();

	private TLOptionsFactory() {
		// Singleton constructor.
	}

	@Override
	public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
		TLOptions tlAnnotation = getAnnotation(item, storage, TLOptions.class);
		if (tlAnnotation == null) {
			return null;
		}
		PolymorphicConfiguration<Generator> generatorConfig = tlAnnotation.getGenerator();
		Generator generator = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(generatorConfig);
		return generator;
	}

}

