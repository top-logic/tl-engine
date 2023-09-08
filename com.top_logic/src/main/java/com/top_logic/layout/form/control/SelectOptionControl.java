/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import static com.top_logic.layout.form.tag.LabelTag.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.JSFunctionCall;
import com.top_logic.base.services.simpleajax.PropertyUpdate;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMember.IDUsage;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link Control} implementation representing a single option of a
 * {@link SelectField}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SelectOptionControl extends AbstractSelectControl {

	private static final Map<String, ControlCommand> SELECT_OPTION_CONTROL_COMMANDS =
		createCommandMap(
			AbstractFormFieldControl.COMMANDS, 
			new ControlCommand[] {
				ValueChanged.INSTANCE
			});

	/**
	 * @see #getOption()
	 */
	private final Object option;

	/**
	 * Creates a new {@link SelectOptionControl}.
	 * 
	 * @param model
	 *        The {@link SelectField} to display option from.
	 * @param option
	 *        The represented option. An option from the given {@link SelectField}.
	 */
	public SelectOptionControl(SelectField model, Object option) {
		super(model, SELECT_OPTION_CONTROL_COMMANDS);
		
		this.option= option;
	}
	
	/**
	 * The select option represented by this control.
	 */
	public Object getOption() {
		return option;
	}

	@Override
	protected String getTypeCssClass() {
		return "cSelectOption";
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		SelectField selectField = getSelectField();
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			out.beginBeginTag(INPUT);
			writeInputIdAttr(out);

			// Name is required to achieve mutual exclusion of radio
			// buttons.
			out.writeAttribute(NAME_ATTR, selectField.getQualifiedName());

			if (selectField.isMultiple()) {
				out.writeAttribute(TYPE_ATTR, CHECKBOX_TYPE_VALUE);
			} else {
				out.writeAttribute(TYPE_ATTR, RADIO_TYPE_VALUE);
			}
			if (isChecked()) {
				out.writeAttribute(CHECKED_ATTR, CHECKED_CHECKED_VALUE);
			}

			// Note: Display immutable fields as disabled fields, since
			// immutable rendering is redirected to editable rendering (see
			// below).
			if (selectField.isDisabled() || selectField.isImmutable()) {
				out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
			}

			if (selectField.isMultiple()) {
				out.writeAttribute(CLASS_ATTR, FormConstants.IS_CHECKBOX_CSS_CLASS);
			} else {
				out.writeAttribute(CLASS_ATTR, FormConstants.IS_RADIO_CSS_CLASS);
			}
			out.writeAttribute(STYLE_ATTR, getInputStyle());
			if (hasTabIndex()) {
				out.writeAttribute(TABINDEX_ATTR, getTabIndex());
			}

			writeOnChange(context, out);

			out.endEmptyTag();
		}
		out.endTag(SPAN);
	}

	private void writeOnChange(DisplayContext context, TagWriter out) throws IOException {
		if (context.getUserAgent().is_ie()) {
			// Workaround for IE bug: onchange events for select input
			// are fired too late. Updates are almost always lost. Therefore,
			// the onclick event is used to grab the input value. Operation
			// must always continue, because the browser otherwise stops processing
			// and the new value is not displayed in the client.
			out.beginAttribute(ONCLICK_ATTR);
			out.append(FormConstants.SELECT_OPTION_CONTROL_CLASS);
			out.append(".handleOnChange(this");
			if (showWait(this)) {
				out.append(",true");
			}
			out.append("); return true;");
			out.endAttribute();
		} else {
			out.beginAttribute(ONCHANGE_ATTR);
			writeHandleOnChangeAction(out, FormConstants.SELECT_OPTION_CONTROL_CLASS, this, null);
			out.endAttribute();
		}
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
	protected String buildInputId() {
		return inputIdFor(getSelectField(), getFrameScope(), IDUsage.INPUT, false, 0, option);
	}
	
	private boolean isChecked() {
		SelectField selectField = getSelectField();
		return 
			selectField.getSelectionSet().contains(option);
	}

	@Override
	protected void writeBlocked(DisplayContext context, TagWriter out) throws IOException {
		writeBlocked(context, out, this, I18NConstants.BLOCKED_CHECKBOX_TEXT);
	}
	
	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		// TODO Should be better displayed as image?
		writeEditable(context, out);
	}
	
	@Override
	public void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		addDisabledUpdate(newValue.booleanValue());
	}

	/**
	 * Transforms programatically changed field values into an update for the
	 * UI.
	 * 
	 * @see ValueChanged for transforming changes at the UI into model updates.
	 */
	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		SelectField selectField = getSelectField();
		String optionId = selectField.getOptionID(option);
		boolean wasChecked = oldValue != null && ((List<?>) oldValue).contains(optionId);
		boolean isChecked = newValue != null && ((List<?>) newValue).contains(optionId);
		
		if (wasChecked ^ isChecked) {
			// The checked status changed.
			addUpdate(new PropertyUpdate(getInputId(), CHECKED_ATTR, new ConstantDisplayValue(Boolean.valueOf(isChecked).toString())));
		}
	}

	/**
	 * Command that updates the field value upon changes done to the view.
	 */
	private static class ValueChanged extends ControlCommand {

		public static final ControlCommand INSTANCE = new ValueChanged();
		
		public static final String COMMAND_ID = "valueChanged";
		private static final String VALUE_PARAM = "value";

		public ValueChanged() {
			super(COMMAND_ID);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			Boolean value = (Boolean) arguments.get(VALUE_PARAM);
			
			SelectOptionControl optionControl = (SelectOptionControl) control;
			SelectField selectField = optionControl.getSelectField();

			String rawIndex = 
				selectField.getOptionID(optionControl.getOption());
			
			if (selectField.isMultiple()) {
				// Just set or remove the referenced option.
				
				@SuppressWarnings("unchecked")
				List<String> rawValue = (List<String>) selectField.getRawValue();
				if (rawValue == null) {
					rawValue = Collections.emptyList();
				}
				List<String> newRawValue;
				if (value.booleanValue()) {
					// The option was selected.
					newRawValue = new ArrayList<>(rawValue.size() + 1);
					newRawValue.addAll(rawValue);
					newRawValue.add(rawIndex);
				} else {
					// The option was deselected.
					newRawValue = new ArrayList<>(rawValue.size());
					for (int cnt = rawValue.size(), n = 0; n < cnt; n++) {
						String option = rawValue.get(n);
						if (option.equals(rawIndex)) continue;
						newRawValue.add(option);
					}
				}
				update(optionControl, selectField, newRawValue);
			} else {
				// Two events are received, one that sets the new option and one
				// that removes the old selection. Only the event that sets the
				// new selection has to be processed.
				if (value.booleanValue()) {
					List<String> newRawValue = Collections.singletonList(rawIndex);
					update(optionControl, selectField, newRawValue);
				}
			}
			
			return HandlerResult.DEFAULT_RESULT;
		}

		private void update(SelectOptionControl optionControl, FormField field, Object newRawValue) {
			try {
				field.update(newRawValue);
				
				if (field.isCheckRequired(false)) {
					field.checkWithAllDependencies();
				}
			} catch (VetoException ex) {
				ex.setContinuationCommand(newContinuation(field, newRawValue));
				ex.process(optionControl.getWindowScope());
			}
		}

		private Command newContinuation(final FormField field, final Object newRawValue) {
			return new Command() {

				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					try {
						Object parsedValue = FormFieldInternals.parseRawValue((AbstractFormField) field, newRawValue);
						field.setValue(parsedValue);
					} catch (CheckException ex) {
						// Ignore
					}

					return HandlerResult.DEFAULT_RESULT;
				}
			};
		}

		@Override
		public ResKey getI18NKey() {
			return com.top_logic.layout.form.control.I18NConstants.VALUE_CHANGED;
		}
		
	}

}
