/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.definition;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * A configuration with an auto-generated universally unique ID.
 * 
 * @see #getUUID()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface WithUUID extends ConfigurationItem {

	/**
	 * @see #getUUID()
	 */
	String UUID = "uuid";

	/**
	 * Universally unique ID identifying this configuration.
	 */
	@Name(UUID)
	@Nullable
	@Hidden
	String getUUID();

	/**
	 * @see #getUUID()
	 */
	void setUUID(String value);

}
