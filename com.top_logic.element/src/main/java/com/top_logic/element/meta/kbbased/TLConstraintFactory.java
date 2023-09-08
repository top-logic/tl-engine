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
import com.top_logic.element.config.annotation.TLConstraint;
import com.top_logic.element.meta.kbbased.filtergen.AttributedValueFilter;

/**
 * {@link AnnotationsBasedCacheValueFactory} creating an {@link AttributedValueFilter} from the
 * {@link TLConstraint} annotation.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLConstraintFactory extends AnnotationsBasedCacheValueFactory {

	/**
	 * Singleton {@link TLConstraintFactory} instance.
	 */
	public static final TLConstraintFactory INSTANCE = new TLConstraintFactory();

	private TLConstraintFactory() {
		// Singleton constructor.
	}

	@Override
	public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
		TLConstraint tlAnnotation = getAnnotation(item, storage, TLConstraint.class);
		if (tlAnnotation == null) {
			return null;
		}
		PolymorphicConfiguration<AttributedValueFilter> filterConfig = tlAnnotation.getFilter();
		AttributedValueFilter filter =
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(filterConfig);
		return filter;
	}

}

