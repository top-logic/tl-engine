/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.order;

import java.util.List;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.PropertyDescriptor;

/**
 * Strategy that produces a display order of properties of a certain {@link ConfigurationDescriptor}
 * .
 * 
 * @see #getDisplayProperties(ConfigurationDescriptor)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface OrderStrategy {

	/**
	 * The ordered list of properties to display for editing the given
	 * {@link ConfigurationDescriptor}.
	 * 
	 * @param descriptor
	 *        The {@link ConfigurationDescriptor} to edit.
	 * @return The properties and their order to show at the UI.
	 */
	List<PropertyDescriptor> getDisplayProperties(ConfigurationDescriptor descriptor);

}
