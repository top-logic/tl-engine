/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.layout.Control;
import com.top_logic.layout.basic.VerticalSizableControl;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.decorator.DecorateService;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;

/**
 * Factory for {@link TableControl}s within forms.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TableTag extends AbstractFormFieldControlTag implements BodyTag, ControlBodyTag {

	private ITableRenderer tableRenderer;
	private BodyContent		bodyContent;

	private Boolean useFullFooter;

	public void setRenderer(ITableRenderer tableRenderer) {
		this.tableRenderer = tableRenderer;
	}

	public ITableRenderer getRenderer() {
		return tableRenderer;
	}

	public void setFullFooter(String useFullFooter) {
		this.useFullFooter = StringServices.parseBoolean(useFullFooter);
	}

	@Override
	public Control createControl(FormMember member, String displayStyle) {
		TableField tableField = (TableField) member;
		TableData  tableData  = DecorateService.prepareTableData(tableField);

		ITableRenderer theTableRenderer = getConfiguredTableRenderer(tableField);
		if (tableRenderer == null) {
			if (theTableRenderer == null) {
				if (useFullFooter == null) {
					// Create default table renderer using global table configuration
					theTableRenderer = DefaultTableRenderer.newInstance();
				} else {
					// Create default table renderer using tag configuration
					theTableRenderer = DefaultTableRenderer.newInstance(useFullFooter);
				}
			} else {
				if (useFullFooter != null && (theTableRenderer instanceof DefaultTableRenderer)) {
					((DefaultTableRenderer) theTableRenderer).setUseFullFooter(useFullFooter);
				}
			}
		} else {
			theTableRenderer = tableRenderer;
		}

		TableControl tableControl = createTableControl(tableField, tableData, theTableRenderer, true);

		return new VerticalSizableControl(tableControl, tableControl.getViewModel().getConfigKey());
	}
	
	private ITableRenderer getConfiguredTableRenderer(TableField tableField) {
		return tableField.getTableModel().getTableConfiguration().getTableRenderer();
	}

	public static TableControl createTableControl(TableField tableField, ITableRenderer theTableRenderer,
			boolean replaceable) {
		return createTableControl(tableField, tableField, theTableRenderer, replaceable);
	}

	public static TableControl createTableControl(TableField baseField, TableData data, ITableRenderer theTableRenderer,
			boolean replaceable) {
		TableControl control = new TableControl(data, theTableRenderer);
		control.addVisibilityListenerFor(baseField);
		control.addFocusListener(baseField);
		control.setSelectable(baseField.isSelectable());
		return control;
	}

	public static TableControl createTableControl(TableField tableField) {
		ITableRenderer tableRenderer = tableField.getTableModel().getTableConfiguration().getTableRenderer();
		if (tableRenderer == null) {
			tableRenderer = DefaultTableRenderer.newInstance();
		}
		return createTableControl(tableField, tableRenderer, true);
	}

	@Override
	public String addControl(HTMLFragment childControl) {
		((TableControl) getControl()).addTitleBarControl(childControl);

		// Additional controls are displayed in a flow layout in the table title
		// bar without any other user-defined markup.
		return null;
	}

	@Override
	protected void teardown() {
		super.teardown();
		this.tableRenderer     = null;
		this.bodyContent       = null;
	}

	@Override
	public void doInitBody() throws JspException {
		ControlBodyTagSupport.doInitBody();
		installCorrectTagWriter();
	}
	
	@Override
	protected int endFormMember() throws IOException, JspException {
		installCorrectTagWriter();
		return super.endFormMember();
	}

	@Override
	public void setBodyContent(BodyContent bodyContent) {
		this.bodyContent = bodyContent;
	}

	@Override
	public int doAfterBody() throws JspException {
		return ControlBodyTagSupport.doAfterBody(bodyContent);
	}
}
