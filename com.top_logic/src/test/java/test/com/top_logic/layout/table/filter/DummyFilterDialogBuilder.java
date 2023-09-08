/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.filter;

import java.util.Optional;

import junit.framework.TestCase;

import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.PopupHandler;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.table.filter.FilterDialogBuilder;
import com.top_logic.layout.table.filter.TableFilterModel;

/**
 * Dummy for {@link TestCase}s
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
final class DummyFilterDialogBuilder implements FilterDialogBuilder {

	DummyFilterDialogBuilder() {}

	@Override
	public void openFilterDialog(Control contentControl, DisplayContext context, PopupHandler handler,
			TableFilterModel tableFilterModel, FormGroup filterContext) {
		throw new UnsupportedOperationException("Method is not implemented");
	}

	@Override
	public PopupDialogModel getDialogModel() {
		throw new UnsupportedOperationException("Method is not implemented");
	}

	@Override
	public Control createFilterDialogContent(TableFilterModel filterModel, DisplayContext context,
			FormContext form, Optional<BlockControl> sortControl) {
		throw new UnsupportedOperationException("Method is not implemented");
	}

	@Override
	public void closeFilterDialog() {
		// Do nothing
	}
}