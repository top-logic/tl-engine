/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * Configuration of a column naming the cell value its scripts receive: the value an edit enters,
 * the value a scripted filter matches.
 *
 * @implNote The scripts declare the value with
 *           {@link com.top_logic.layout.view.channel.Supplies} renamed by {@link #VALUE_AS}.
 */
public interface ValueAsConfig extends ConfigurationItem {

	/** Configuration name for {@link #getValueAs()}. */
	String VALUE_AS = "value-as";

	/** Name of the variable holding the cell value when no other name is given. */
	String DEFAULT_VALUE_AS = "value";

	/**
	 * The name of the variable holding a cell value in the scripts of this column: the value the
	 * user entered in the update of an edited column, the cell value a scripted filter of the
	 * column matches.
	 *
	 * <p>
	 * Unset, the variable is named {@value #DEFAULT_VALUE_AS}. An input of the same name is a
	 * configuration error and is renamed with <code>as</code>.
	 * </p>
	 */
	@Name(VALUE_AS)
	@Nullable
	String getValueAs();

}
