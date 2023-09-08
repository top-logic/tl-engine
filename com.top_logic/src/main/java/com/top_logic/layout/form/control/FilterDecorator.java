/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.ContentDecorator;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.renderer.ColumnLabelProvider;
import com.top_logic.layout.table.renderer.TableButtons;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.css.CssUtil;

/**
 * {@link ContentDecorator} of sidebar table filters, which enables display folding.
 * 
 * @author     <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class FilterDecorator implements ContentDecorator {
	
	private static final String SIDEBAR_FILTER_ENTRY_CLASS = "sidebarFilterEntry";
	private static final String COLLAPSIBLE_ENTRY_CLASS = "collapsibleFilterEntry";
	private static final String RESET_SIDEBAR_FILTER_BUTTON_CLASS = "resetSidebarFilterButton";

	private String columnName;

	private TableData table;
	
	/**
	 * Create a new {@link FilterDecorator}.
	 */
	public FilterDecorator(String columnName, TableData table) {
		this.columnName = columnName;
		this.table = table;
	}
	
	@Override
	public void startDecoration(DisplayContext context, TagWriter out, Object value) throws IOException {
		FormGroupControl control = (FormGroupControl) value;
		FormGroup formGroup = control.getFormGroup();
		
		out.beginBeginTag(DIV);
		if(control.isCollapsible()) {
			control.writeOnClickToggle(out);
		}
		writeCssClasses(out, control);
		out.endBeginTag();
		boolean collapsed = formGroup.isCollapsed();
		if (control.isCollapsible()) {
			XMLTag tag = getCollapseImage(collapsed).toButton();
			tag.beginBeginTag(context, out);
			CssUtil.writeCombinedCssClasses(out,
				FormConstants.INPUT_IMAGE_CSS_CLASS,
				FormConstants.TOGGLE_BUTTON_CSS_CLASS);
			out.writeAttribute(ALT_ATTR, getAltText(collapsed, formGroup.getLabel(), context));
			tag.endEmptyTag(context, out);
			out.writeText(HTMLConstants.NBSP);
		}	
		writeColumnLabel(out);
		writeResetFilterButton(context, out);
		out.endTag(DIV);
	}

	private void writeCssClasses(TagWriter out, FormGroupControl control) {
		if (control.isCollapsible()) {
			out.writeAttribute(CLASS_ATTR,
				CssUtil.joinCssClasses(SIDEBAR_FILTER_ENTRY_CLASS, COLLAPSIBLE_ENTRY_CLASS));
		} else {
			out.writeAttribute(CLASS_ATTR, SIDEBAR_FILTER_ENTRY_CLASS);
		}
	}

	private void writeColumnLabel(TagWriter out) {
		LabelProvider columnLabelProvider = ColumnLabelProvider.newInstance(tableData());
		String columnLabel = columnLabelProvider.getLabel(columnName);
		out.writeText(columnLabel);
	}
	
	private void writeResetFilterButton(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(ONCLICK_ATTR, "BAL.eventStopPropagation(event);");
		out.writeAttribute(CLASS_ATTR, RESET_SIDEBAR_FILTER_BUTTON_CLASS);
		out.endBeginTag();
		TableButtons.writeResetFilterForColumn(context, out, tableData(), columnName);
		out.endTag(DIV);
	}

	private TableData tableData() {
		return table;
	}

	@Override
	public void endDecoration(DisplayContext context, TagWriter out, Object value) throws IOException {
		// Nothing to do
	}
	
	private ThemeImage getCollapseImage(boolean collapsed) {
		return collapsed ? Icons.BOX_COLLAPSED : Icons.BOX_EXPANDED;
	}

	private String getAltText(boolean collapsed, String label, DisplayContext context) {
		return context.getResources().getString((collapsed ? I18NConstants.FORM_GROUP_COLLAPSED__LABEL : I18NConstants.FORM_GROUP_EXPANDED__LABEL).fill(label));
	}

}
