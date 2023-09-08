/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;

/**
 * {@link Control} displaying a {@link FormField} with a {@link Boolean} value as collection of
 * radio buttons.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BooleanChoiceControl extends AbstractFormFieldControl {

	protected static final Map COMMANDS = createCommandMap(
		AbstractFormFieldControl.COMMANDS,
		new ControlCommand[] {
		});

	private boolean falseFirst;
	private boolean labelFirst;

	private ResKey noneLabelKey;

	private ResKey trueLabelKey;

	private ResKey falseLabelKey;

	private boolean _resetable;

	public BooleanChoiceControl(FormField model) {
		super(model, COMMANDS);
	}
	
	public void setFalseFirst(boolean falseFirst) {
		this.falseFirst = falseFirst;
	}
	
	public void setLabelFirst(boolean labelFirst) {
		this.labelFirst = labelFirst;
	}
	
	public void setTrueLabelKey(ResKey trueLabelKey) {
		this.trueLabelKey = trueLabelKey;
	}
	
	public void setFalseLabelKey(ResKey falseLabelKey) {
		this.falseLabelKey = falseLabelKey;
	}
	
	public void setNoneLabelKey(ResKey noneLabelKey) {
		this.noneLabelKey = noneLabelKey;
	}
	
	public void setResetable(boolean value) {
		_resetable = value;
	}

	@Override
	protected String getTypeCssClass() {
		return "cBooleanChoice";
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		Resources res = Resources.getInstance();
		
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
	    
		if (falseFirst) {
			writeOption(out, res, false);
			writeOption(out, res, true);
			if (_resetable) {
				writeOption(out, res, null);
			}
		} else {
			if (_resetable) {
				writeOption(out, res, null);
			}
			writeOption(out, res, true);
			writeOption(out, res, false);
		}
		
		out.endTag(SPAN);
	}

	private void writeOption(TagWriter out, Resources res, Boolean option) throws IOException {
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, "bcc-option");
		out.endBeginTag();

		String optionId = getID() + "-" + (option == null ? "n" : (option ? "t" : "f"));
		
		if (labelFirst) {
			writeLabel(out, res, option, optionId);
			writeOption(out, option, optionId);
		} else {
			writeOption(out, option, optionId);
			writeLabel(out, res, option, optionId);
		}

		out.endTag(SPAN);
	}

	private void writeLabel(TagWriter out, Resources res, Boolean option, String optionId) {
		out.beginBeginTag(LABEL);
		out.writeAttribute(FOR_ATTR, optionId);
		out.endBeginTag();
		out.writeText(res.getString(getLabelKey(option)));
		out.endTag(LABEL);
	}

	private ResKey getLabelKey(Boolean option) {
		return option == null ? 
				(noneLabelKey != null ? noneLabelKey : I18NConstants.NONE_LABEL) : 
				(option.booleanValue() ? 
					(trueLabelKey != null ? trueLabelKey : I18NConstants.TRUE_LABEL) : 
					(falseLabelKey != null ? falseLabelKey : I18NConstants.FALSE_LABEL));
	}

	private void writeOption(TagWriter out, Boolean option, String optionId) throws IOException {
		FormField field = getFieldModel();
		boolean optionChecked =
			Utils.equals(getDisplayedFieldValue(), option);
		
		out.beginBeginTag(INPUT);
		out.writeAttribute(ID_ATTR, optionId);
		out.writeAttribute(TYPE_ATTR,  RADIO_TYPE_VALUE);
		out.writeAttribute(CLASS_ATTR, FormConstants.IS_RADIO_CSS_CLASS);
		writeOnClick(out, FormConstants.BOOLEAN_CHOICE_CONTROL_CLASS, this, "," + option);
		if (optionChecked) {
			out.writeAttribute(CHECKED_ATTR, CHECKED_CHECKED_VALUE);
		}
		if (field.isDisabled()) {
			out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
		}
		out.endEmptyTag();
	}

	private Object getDisplayedFieldValue() {
		FormField field = getFieldModel();
		return _resetable || field.isMandatory() ? field.getValue() : Utils.isTrue((Boolean) field.getValue());
	}

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		out.writeText(Resources.getInstance().getString(getLabelKey((Boolean) getDisplayedFieldValue())));
		out.endTag(SPAN);
	}

	@Override
	public void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		requestRepaint();
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		requestRepaint();
	}
}
