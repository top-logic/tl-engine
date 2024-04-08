/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.SelectionControl.OpenSelector;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.form.tag.Icons;
import com.top_logic.layout.form.tag.js.JSObject;
import com.top_logic.layout.form.tag.js.JSString;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Separated options selection list view of a {@link FormField} model.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class SelectTextControl extends AbstractSelectControl {

	/**
	 * Commands registered at this control.
	 * 
	 * @see #getCommand(String)
	 */
	private static final Map<String, ControlCommand> SELECT_TEXT_COMMANDS = createCommandMap(
		AbstractFormFieldControlBase.COMMANDS,
		new ControlCommand[] {
			ClearSelection.INSTANCE,
			OpenSelector.INSTANCE,
		});

	private static final String CSS_CLASS_OPTION = "option";

	private static final String SELECTED_VALUE = "valueDisplay";

	private Renderer<Object> _selectionRenderer;

	/**
	 * Creates a {@link SelectTextControl} and registers its commands.
	 * 
	 * @param model
	 *        The {@link FormField} to display.
	 */
	public SelectTextControl(FormField model) {
		super(model, SELECT_TEXT_COMMANDS);
	}

	@Override
	protected String getTypeCssClass() {
		return "cTextSelect";
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		SelectField field = getSelectField();
		boolean isDisabled = field.isDisabled();

		if (isDisabled) {
			writeImmutable(context, out);
		} else {
			List<?> selection = field.getSelection();
			LabelProvider optionLabelProvider = SelectFieldUtils.getOptionLabelProvider(field);

			out.beginBeginTag(SPAN);
			writeControlAttributes(context, out);
			out.endBeginTag();
			{
				for (int i = 0; i < selection.size(); i++) {
					Object option = selection.get(i);
					out.beginBeginTag(SPAN);
					out.writeAttribute(ID_ATTR, getOptionSpanId(option));
					out.writeAttribute(CLASS_ATTR, CSS_CLASS_OPTION);
					out.endBeginTag();
					
					if (_selectionRenderer != null) {
						_selectionRenderer.write(context, out, option);
					} else {
						out.writeText(optionLabelProvider.getLabel(option));
					}
					writeClearButton(context, out, getOptionID(option));
					out.endTag(SPAN);
				}

				SelectionControl.writePopupButton(context, out, this, isDisabled, getID() + "-button",
					getOpenSelectorCommand(),
					field.getLabel());
			}
			out.endTag(SPAN);
		}
	}

	private String getOptionSpanId(Object option) {
		return getID() + "-" + getOptionID(option);
	}

	/**
	 * {@link Renderer} that displays a single option in edit mode, or the whole selection list in
	 * immutable mode.
	 */
	public void setSelectionRenderer(Renderer<Object> selectionRenderer) {
		_selectionRenderer = selectionRenderer;
	}

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		SelectField field = getSelectField();

		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, SELECTED_VALUE);
			out.endBeginTag();
			{
				if (_selectionRenderer != null) {
					List<?> selectionList = field.getSelection();
					_selectionRenderer.write(context, out, selectionList);
				} else {
					try {
						SelectFieldUtils.writeSelectionImmutable(context, out, field);
					} catch (Throwable throwable) {
						try {
							produceErrorOutput(context, out, throwable);
						} catch (Throwable inner) {
							// In the rare case of catastrophe better throw the original.
							throw throwable;
						}
					}
				}
			}
			out.endTag(SPAN);
		}
		out.endTag(SPAN);
	}

	final ControlCommand getOpenSelectorCommand() {
		return getCommand(OpenSelector.COMMAND);
	}

	@Override
	public void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		repaintOnEvent(sender);
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		SelectField selectField = getSelectField();

		List<?> oldValueList = (List<?>) oldValue;

		if (!CollectionUtil.isEmptyOrNull(oldValueList)) {
			List<?> newValueList = (List<?>) newValue;
			Set<?> removedElements = new HashSet<>(oldValueList);
			removedElements.removeAll(newValueList);

			if (moreThanRemove(oldValueList, newValueList, removedElements)) {
				// repaint
				repaintOnEvent(field);
			} else {
				// incremental update
				for (Object optionID : removedElements) {
					Object option = selectField.getOptionByID((String) optionID);
					String clientID = getOptionSpanId(option);
					addUpdate(new ElementReplacement(clientID, Fragments.empty()));
				}
			}
		} else {
			repaintOnEvent(field);
		}
	}

	/**
	 * Checks if the new list has more changes than only elements which should be removed.
	 * 
	 * @param oldValue
	 *        List of old values.
	 * @param newValue
	 *        List of new value.
	 * @return Indicates if there are more changes done between both lists than removes.
	 */
	private boolean moreThanRemove(List<?> oldValue, List<?> newValue, Set<?> removedElements) {
		// true - values are added
		boolean valueAdded = !oldValue.containsAll(newValue);
		boolean valuesReplaced = (oldValue.size() == newValue.size());

		if (valueAdded || valuesReplaced) {
			return true;
		} else {
			// true - positions are changed or a new element is in the new list
			int iNew = 0;
			for (int iOld = 0; iOld < oldValue.size(); iOld++) {
				if (newValue.size() > iNew) {
					if (!(oldValue.get(iOld).equals(newValue.get(iNew)))) {
						// different elements on the same position
						if (removedElements.contains(oldValue.get(iOld))) {
							// different elements because it is a removed element of the old list
							iNew--;
						} else {
							// positions have changed
							return true;
						}
					}

					iNew++;
				} else {
					return false;
				}
			}
		}

		return false;
	}

	void writeClearButton(DisplayContext context, TagWriter out, String optionID) throws IOException {
		boolean clearButtonActive = isClearButtonActive(optionID);

		out.beginBeginTag(SPAN);
		out.writeAttribute(ID_ATTR, getClearButtonSpanID(optionID));

		out.endBeginTag();

		ButtonWriter buttonWriter = new ButtonWriter(this, Icons.DELETE_BUTTON,
			Icons.DELETE_BUTTON_DISABLED, getClearSelectionCommand());
		JSObject arguments = new JSObject(ClearSelection.OPTION_KEY, new JSString(optionID));

		buttonWriter.setJSArguments(arguments);
		buttonWriter.setID(getClearButtonID(optionID));
		buttonWriter.setCss(FormConstants.CLEAR_BUTTON_CSS_CLASS);
		buttonWriter.setTooltip(
			com.top_logic.layout.form.I18NConstants.CLEAR_CHOSEN__LABEL.fill(getSelectionModel().getLabel()));
		buttonWriter.setDisabledTooltip(
			com.top_logic.layout.form.I18NConstants.CLEAR_FIXED__LABEL.fill(getSelectionModel().getLabel()));

		if (clearButtonActive) {
			buttonWriter.writeButton(context, out);
		} else {
			buttonWriter.writeDisabledButton(context, out);
		}

		out.endTag(SPAN);
	}

	private boolean isClearButtonActive(String optionID) {
		SelectField selectField = getSelectField();

		boolean isDisabled = selectField.isDisabled();

		Filter fixedOptions = selectField.getFixedOptions();

		if (fixedOptions != null) {
			if (fixedOptions.accept(selectField.getOptionByID(optionID))) {
				isDisabled = true;
			}
		}

		return (!isDisabled) || selectField.hasError();
	}

	private String getClearButtonID(String optionID) {
		return (getID() + "-" + optionID + "-clear");
	}

	private String getClearButtonSpanID(String label) {
		return getClearButtonID(label) + "-span";
	}

	SelectField getSelectionModel() {
		return (SelectField) super.getModel();
	}

	private void writeOnClick(TagWriter out, String optionID) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		out.append("return ");
		JSObject arguments = new JSObject(ClearSelection.OPTION_KEY, new JSString(optionID));
		getClearSelectionCommand().writeInvokeExpression(out, this, arguments);
		out.append(';');
		out.endAttribute();
	}

	private String getOptionID(Object option) {
		return getSelectField().getOptionID(option);
	}

	final ControlCommand getClearSelectionCommand() {
		return getCommand(ClearSelection.CLEAR_COMMAND);
	}

	private static class ClearSelection extends ControlCommand {

		static final String OPTION_KEY = CSS_CLASS_OPTION;

		private static final String CLEAR_COMMAND = "clearSelection";

		public static final SelectTextControl.ClearSelection INSTANCE = new ClearSelection();

		/**
		 * Singleton constructor.
		 */
		protected ClearSelection() {
			super(CLEAR_COMMAND);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			final SelectField selectField = (SelectField) control.getModel();

			Filter fixedOptions = selectField.getFixedOptions();
			final List<?> newValue = new ArrayList<Object>(getSelection(selectField));

			boolean isRemovable = true;

			// Element of the arguments is not a fixed option, clear the selection by this element
			Object optionToRemove = selectField.getOptionByID((String) arguments.get(OPTION_KEY));
			if (fixedOptions != null) {
				if (fixedOptions.accept(optionToRemove)) {
					isRemovable = false;
				}
			}

			if (isRemovable) {
				newValue.remove(optionToRemove);
			}

			try {
				FormFieldInternals.setValue(selectField, newValue);
			} catch (VetoException ex) {
				ex.setContinuationCommand(new Command() {

					@Override
					public HandlerResult executeCommand(DisplayContext context) {
						selectField.setValue(newValue);
						return HandlerResult.DEFAULT_RESULT;
					}
				});
				ex.process(commandContext.getWindowScope());
			}

			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordFieldInput(selectField, newValue);
			}

			return HandlerResult.DEFAULT_RESULT;
		}

		private List<?> getSelection(SelectField targetSelectField) {
			Object storedValue = FormFieldInternals.getStoredValue(targetSelectField);
			if (storedValue != null) {
				return (List<?>) storedValue;
			} else {
				return Collections.emptyList();
			}
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.CLEAR_SELECTION;
		}
	}
}
