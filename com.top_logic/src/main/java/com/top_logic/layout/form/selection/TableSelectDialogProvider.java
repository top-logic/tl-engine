/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.model.SelectField;

/**
 * {@link SelectDialogProvider} of table based select dialogs.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TableSelectDialogProvider extends SelectDialogProvider {

	/**
	 * Create a new {@link TableSelectDialogProvider} with a given {@link SelectDialogConfig}.
	 */
	public TableSelectDialogProvider(InstantiationContext context, SelectDialogConfig config) {
		super(context, config);
	}

	@Override
	public AbstractSelectDialog createSelectDialog(SelectField selectField) {
		return new TableSelectDialog(selectField, getConfig());
	}
}
