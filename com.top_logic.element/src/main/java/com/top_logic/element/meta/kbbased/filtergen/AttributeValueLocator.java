/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import com.top_logic.basic.config.annotation.Format;
import com.top_logic.element.config.annotation.LocatorNameFormat;

/**
 * Generic algorithm for finding derived values based on some start object.
 * 
 * <p>
 * <b>Note:</b> Custom implementations must use {@link CustomAttributeValueLocator} as base class.
 * </p>
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
@Format(LocatorNameFormat.class)
public interface AttributeValueLocator {

	/** 
	 * Get the basic data source config
	 * @param anObject	the attributed
	 * 
	 * @return the data source config
	 */
	public Object locateAttributeValue(Object anObject);

}
