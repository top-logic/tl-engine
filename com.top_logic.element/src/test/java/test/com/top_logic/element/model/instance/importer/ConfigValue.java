/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.instance.importer;

import com.top_logic.basic.config.ConfigurationItem;

/**
 * A {@link ConfigurationItem} definition for Ticket #22779.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ConfigValue extends ConfigurationItem {
	/**
	 * A string value.
	 */
	String getString();

	/**
	 * A boolean value.
	 */
	boolean getBoolean();

	/**
	 * A long value.
	 */
	long getLong();

	/**
	 * A double value.
	 */
	double getDouble();
}