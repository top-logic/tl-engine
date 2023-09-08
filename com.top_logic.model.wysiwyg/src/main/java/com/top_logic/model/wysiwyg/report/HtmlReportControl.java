/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.wysiwyg.report;

import java.io.IOException;
import java.util.List;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService;
import com.top_logic.layout.wysiwyg.ui.StructuredTextControl;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.persistency.attribute.report.ui.ReportControl;

/**
 * {@link ReportControl} allowing to edit a report with WYSIWYG editor.
 */
public class HtmlReportControl extends ReportControl {

	private final String _editorConfig;

	/**
	 * Creates a {@link HtmlReportControl}.
	 * 
	 * @param editorConfig
	 *        HTML editor configuration, see
	 *        {@link StructuredTextConfigService#getEditorConfig(String, String, List, String)}.
	 */
	public HtmlReportControl(TLObject self, FormField member, String editorConfig) {
		super(self, member);
		_editorConfig = editorConfig;
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		StructuredTextControl result = new StructuredTextControl(getFieldModel(), _editorConfig);
		result.write(context, out);
	}

}
