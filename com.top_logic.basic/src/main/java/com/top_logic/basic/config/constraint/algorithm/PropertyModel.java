/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.algorithm;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.util.ResKey;

/**
 * The location of the configuration value that is part of a constraint check.
 */
public interface PropertyModel<T> {

	/**
	 * The label of the referenced property.
	 */
	ResKey getLabel();

	/**
	 * Whether an explicit value has been set.
	 */
	boolean isValueSet();

	/**
	 * The value of the referenced property.
	 */
	T getValue();

	/**
	 * The {@link PropertyDescriptor} describing the referenced property.
	 */
	PropertyDescriptor getProperty();

	/**
	 * Callback that reports an error message that describes a potential constraint violation
	 * out of the sight of the referenced property.
	 * 
	 * @param message
	 *        The constraint violation error to report for the referenced property.
	 */
	void setProblemDescription(ResKey message);

}