/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.persistency.attribute.report.ui;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.model.TLObject;

/**
 * {@link ControlProvider} creating a {@link ReportControl}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ReportControlProvider extends DefaultFormFieldControlProvider {

	private final TLObject _model;

	/**
	 * Creates a {@link ReportControlProvider}.
	 */
	public ReportControlProvider(TLObject self) {
		_model = self;
	}

	/**
	 * The current model being displayed.
	 */
	protected TLObject getModel() {
		return _model;
	}

	@Override
	public Control visitComplexField(ComplexField member, Void arg) {
		return createReportControl(member);
	}

	@Override
	public Control visitHiddenField(HiddenField member, Void arg) {
		return createReportControl(member);
	}

	/**
	 * Creates the control for displaying a report field.
	 */
	protected Control createReportControl(FormField member) {
		return new ReportControl(getModel(), member);
	}

}