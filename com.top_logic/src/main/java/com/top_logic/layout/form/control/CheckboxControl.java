/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.Map;

import com.top_logic.base.services.simpleajax.JSFunctionCall;
import com.top_logic.base.services.simpleajax.PropertyUpdate;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.tag.CheckboxInputTag;
import com.top_logic.util.Utils;

/**
 * Server-side {@link Control} implementation for checkbox fields.
 * 
 * @see CheckboxInputTag for the mechanism of creating controls of this type on
 *      a form page.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CheckboxControl extends AbstractFormFieldControl {

	protected static final Map COMMANDS = createCommandMap(
		AbstractFormFieldControl.COMMANDS,
		new ControlCommand[] {
		});
	
	public CheckboxControl(FormField model) {
		super(model, COMMANDS);
	}
	
	@Override
	public void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		if (getModel().isVisible()) {
			addDisabledUpdate(newValue.booleanValue());
		}
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		if (field.isVisible()) {
			// Only "true" and "false" are supported by the "checked" property
			// of the client side view, which is a native HTML input element.
			// The event carries the raw value, which also encodes the
			// "null"/"undecided" value.
			addUpdate(new PropertyUpdate(getInputId(), "checked", 
				new ConstantDisplayValue(Boolean.valueOf(Utils.isTrue((Boolean) field.getValue())).toString())));
		}
	}

	@Override
	protected String getTypeCssClass() {
		return "cCheckbox";
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		FormField field = getFieldModel();

		// The surrounding span element is required for compatibility with IE 6,
		// because it cannot directly exchange input elements in its DOM model.
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		
		out.beginBeginTag(INPUT);
		writeInputIdAttr(out);
		writeQualifiedNameAttribute(out);
		out.writeAttribute(CLASS_ATTR, FormConstants.IS_CHECKBOX_CSS_CLASS);
		out.writeAttribute(TYPE_ATTR, CHECKBOX_TYPE_VALUE);
		writeOnClick(out, FormConstants.CHECKBOX_HANDLER_CLASS, this, null);
		out.writeAttribute(STYLE_ATTR, getInputStyle());
		if (hasTabIndex()) {
			out.writeAttribute(TABINDEX_ATTR, getTabIndex());
		}
		
		// TODO BHU: Replace disabled input field with checked or unchecked image (in immutable mode)?
		if (!field.isActive()) {
			out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
		}

		// Content attributes
		Boolean value = (Boolean) field.getValue();
		if (value != null && value) {
			out.writeAttribute(CHECKED_ATTR, CHECKED_CHECKED_VALUE);
		}
		
//		// Tooltip
//		if(field.hasTooltip()) {
//			out.writeContent(OverlibTooltipFragmentGenerator.INSTANCE.generateToolTipFragment(field.getResources().getStringResource("info")));
//		}
		
		// Visual attributes.
		out.endEmptyTag();
		
		out.endTag(SPAN);
	}
	
    @Override
    protected void handleInputStyleChange() {
    	if (getModel().isActive()) {
    		addUpdate(JSFunctionCall.setStyle(getInputId(), getInputStyle()));
    	} else {
    		super.handleInputStyleChange();
    	}
    }
	
	@Override
	protected void writeBlocked(DisplayContext context, TagWriter out) throws IOException {
		writeBlocked(context, out, this, I18NConstants.BLOCKED_CHECKBOX_TEXT);
	}
	
	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		// TODO BHU: Replace disabled input field with checked or unchecked image?
		writeEditable(context, out);
	}

}
