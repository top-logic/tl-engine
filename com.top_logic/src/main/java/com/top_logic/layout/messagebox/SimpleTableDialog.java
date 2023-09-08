/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import org.w3c.dom.Document;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.tag.TableTag;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormGroupControl;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplate;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.layoutRenderer.LayoutControlRenderer;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link SimpleFormDialog} that displays a single form table.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class SimpleTableDialog extends SimpleFormDialog {

	private static final Document TEMPLATE = DOMUtil.parseThreadSafe(
				"<div class='layoutControl'"
			+		" style='overflow:auto'"	
            +		" xmlns='" + HTMLConstants.XHTML_NS + "'"
            +		" xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
            +		" xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
            +		LayoutControlRenderer.getLayoutConstraintInformation(100, DisplayUnit.PERCENT)
            +		LayoutControlRenderer.getLayoutInformation(Orientation.VERTICAL, 100)
    		+	">"
            +		"<p:field"
            +			" name='" + SimpleFormDialog.INPUT_FIELD + "' />"
            +	"</div>"
		);
	
	private static final ControlProvider CP = new DefaultFormFieldControlProvider() {

		@Override
		public Control visitTableField(TableField aMember, Void arg) {
			return TableTag.createTableControl(aMember);
		}
	};

	private ResPrefix _resourcePrefix;

	/**
	 * Creates a {@link SimpleTableDialog}.
	 */
	public SimpleTableDialog(ResPrefix resourcePrefix, DisplayDimension width, DisplayDimension height) {
		super(resourcePrefix, width, height);
		_resourcePrefix = resourcePrefix;
	}

    @Override
    protected FormTemplate getTemplate() {
		return defaultTemplate(TEMPLATE, false, _resourcePrefix);
    }
    
    @Override
    protected HTMLFragment createView() {
		return new FormGroupControl(getFormContext(), getTemplate());
    }
	
    @Override
    protected ControlProvider getControlProvider() {
    	return CP;
    }
    
}
