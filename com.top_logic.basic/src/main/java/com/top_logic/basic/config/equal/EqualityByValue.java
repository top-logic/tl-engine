/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.equal;

import com.top_logic.basic.config.ConfigurationItem;

/**
 * Super interface for {@link ConfigurationItem} which should be equal by value.
 * 
 * @see ConfigEquality
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface EqualityByValue extends ConfigurationItem {

	/**
	 * A {@link EqualityByValue} is equal to a different {@link ConfigurationItem} if the property
	 * values as equal.
	 * 
	 * @see ConfigEquality
	 */
	@Override
	boolean equals(Object obj);

	/**
	 * Hash code base on the hash codes of the property values.
	 * 
	 * @see ConfigEquality
	 */
	@Override
	int hashCode();

}

