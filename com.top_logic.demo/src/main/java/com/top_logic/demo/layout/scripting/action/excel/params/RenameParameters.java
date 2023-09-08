/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.scripting.action.excel.params;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;

/**
 * The parameters beyond the 'context' column, for renaming something.
 * <p>
 * The parameters in or before the 'context' column are stored directly in the action config
 * interface.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public interface RenameParameters extends ConfigurationItem {

	/**
	 * The name of the element.
	 * <p>
	 * The 'name' annotation is not needed here, as the property name would be 'newName' anyway.
	 * It is only here as an example, that and how it can be used.
	 * </p>
	 */
	@Name("neuerName")
	String getNewName();

}
