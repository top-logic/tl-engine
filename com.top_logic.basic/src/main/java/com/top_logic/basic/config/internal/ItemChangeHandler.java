/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.internal;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.PropertyDescriptor;

/**
 * Internal notification interface for configuration items.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public interface ItemChangeHandler {

	/**
	 * Notifies about an updated property.
	 */
	void __notifyUpdate(PropertyDescriptor property, Object oldValue, Object newValue);

	/**
	 * Notifies about an add operation on a list or map property.
	 */
	void __notifyAdd(PropertyDescriptor property, int index, Object element);

	/**
	 * Notifies about an remove operation on a list or map property.
	 */
	void __notifyRemove(PropertyDescriptor property, int index, Object element);

}
