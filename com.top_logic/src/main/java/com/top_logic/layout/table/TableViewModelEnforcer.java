/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractControl;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.util.ToBeValidated;
import com.top_logic.util.ValidationQueue;

/**
 * A {@link ToBeValidated} that enforced the existence of the {@link TableViewModel} in the
 * {@link DefaultTableData}.
 * <p>
 * Creating the {@link TableViewModel} must not happen during the rendering, as it can cause
 * updates. And updates are not allowed during rendering:
 * {@link AbstractControl#addUpdate(ClientAction)}, {@link AbstractControlBase#checkCommandThread()}
 * </p>
 * <p>
 * The {@link TableViewModel} creation must not happen immediately, as the {@link ConfigKey} might
 * not be usable, yet. A {@link TableField} needs to be added to its {@link FormContext}, for
 * example, before the {@link ConfigKey} can be used.
 * </p>
 * <p>
 * Using a {@link ToBeValidated} for enforcing the creation of the {@link TableViewModel} solves
 * that problem: The {@link TableViewModel} is created before the rendering phase and the
 * {@link ConfigKey} should be usable when {@link ToBeValidated}s are processed.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
final class TableViewModelEnforcer implements ToBeValidated {

	private final TableData _tableData;

	TableViewModelEnforcer(TableData tableData) {
		_tableData = tableData;
	}

	@Override
	public void validate(DisplayContext context) {
		enforceTableViewModel();
	}

	private void enforceTableViewModel() {
		_tableData.getViewModel();
	}

	void register() {
		getValidationQueue().notifyInvalid(this);
	}

	private ValidationQueue getValidationQueue() {
		return DefaultDisplayContext.getDisplayContext().getLayoutContext();
	}

	@Override
	public String toString() {
		return new NameBuilder(this).addUnnamed(_tableData).build();
	}

}
