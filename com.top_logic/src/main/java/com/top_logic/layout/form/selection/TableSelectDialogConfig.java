/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;

/**
 * Configuration options for {@link SelectDialogBase} for Tables.
 * @author <a href="mailto:Diana.Pankratz@top-logic.com">Diana Pankratz</a>
 */
public interface TableSelectDialogConfig extends SelectDialogConfig {

	@Override
	@ClassDefault(TableSelectDialogProvider.class)
	Class<? extends SelectDialogProvider> getImplementationClass();

	/**
	 * Returns the number of options to show maximal on one page.
	 */
	@Override
	@Name(OPTIONS_PER_PAGE)
	@IntDefault(10000)
	int getOptionsPerPage();
}
