/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.element.config.annotation.TLValidityCheck;
import com.top_logic.element.meta.ValidityCheck;

/**
 * {@link AnnotationsBasedCacheValueFactory} creating a {@link ValidityCheck} from the
 * {@link TLValidityCheck} annotation.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLValidityCheckFactory extends AnnotationsBasedCacheValueFactory {

	/**
	 * Singleton {@link TLValidityCheckFactory} instance.
	 */
	public static final TLValidityCheckFactory INSTANCE = new TLValidityCheckFactory();

	private TLValidityCheckFactory() {
		// Singleton constructor.
	}

	@Override
	public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
		TLValidityCheck tlAnnotation = getAnnotation(item, storage, TLValidityCheck.class);
		String validityPattern;
		if (tlAnnotation == null) {
			validityPattern = null;
		} else {
			validityPattern = tlAnnotation.getValue();
		}
		ValidityCheck validityCheck = ValidityCheck.getValidityCheck(validityPattern);
		return validityCheck;
	}

}

