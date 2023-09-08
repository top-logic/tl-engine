/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import java.util.Set;

import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;

/**
 * Configuration of a set of types.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TypesConfig extends ConfigurationItem {

	/** Name of {@link #getTypeNames()}. */
	String TYPES = "types";

	/**
	 * Configured type names.
	 */
	@Name(TYPES)
	@Format(CommaSeparatedStringSet.class)
	Set<String> getTypeNames();

	/**
	 * Setter for {@link #getTypeNames()}.
	 */
	void setTypeNames(Set<String> typeNames);

}

