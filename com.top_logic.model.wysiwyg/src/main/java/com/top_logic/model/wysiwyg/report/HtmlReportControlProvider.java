/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.wysiwyg.report;

import java.util.List;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.persistency.attribute.report.ui.ReportControlProvider;

/**
 * {@link ReportControlProvider} allowing to edit the report template with an HTML editor.
 */
public class HtmlReportControlProvider extends ReportControlProvider {

	private String _editorConfig;

	/**
	 * Creates a {@link HtmlReportControlProvider}.
	 * 
	 * @param editorConfig
	 *        HTML editor configuration, see
	 *        {@link StructuredTextConfigService#getEditorConfig(String, String, List, String)}.
	 */
	public HtmlReportControlProvider(TLObject self, String editorConfig) {
		super(self);
		_editorConfig = editorConfig;
	}

	@Override
	protected Control createReportControl(FormField member) {
		return new HtmlReportControl(getModel(), member, _editorConfig);
	}

}
