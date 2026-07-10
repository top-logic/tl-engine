/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.AbstractFormFieldControl;
import com.top_logic.layout.form.control.ButtonWriter;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.tag.Icons;
import com.top_logic.layout.form.template.AbstractFormFieldControlProvider;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Control rendering the model allowlist of an {@link LLMProviderConfig.Config} as a free-text input
 * that looks like the standard TopLogic popup-select field.
 *
 * <p>
 * The control writes a single value box with two trailing icons attached inline on the right: an
 * "expand" icon opening the discover-and-choose dialog (see {@link ModelsEditor#openModelChooser})
 * and a "clear" icon emptying the value. Unlike a real popup-select, the value box stays free-text
 * editable: model names can be typed directly (comma-separated) and persist through the field's
 * {@link com.top_logic.basic.config.CommaSeparatedStrings} format via the inherited
 * {@code valueChanged} command.
 * </p>
 */
public class ModelsControl extends AbstractFormFieldControl {

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		AbstractFormFieldControl.COMMANDS,
		new ControlCommand[] {
			OpenModels.INSTANCE,
			ClearModels.INSTANCE,
		});

	/**
	 * The provider configuration being edited, required to discover available models.
	 */
	private final ConfigurationItem _config;

	/**
	 * Creates a {@link ModelsControl}.
	 *
	 * @param model
	 *        The field holding the comma-separated model list.
	 * @param config
	 *        The provider configuration being edited.
	 */
	public ModelsControl(FormField model, ConfigurationItem config) {
		super(model, COMMANDS);
		_config = config;
	}

	@Override
	protected String getTypeCssClass() {
		return "cPopupSelect";
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		// The value box mirrors the field's formatted text and the clear button toggles with
		// emptiness; a full repaint keeps both in sync after a programmatic value change (e.g. the
		// chooser dialog or the clear command).
		requestRepaint();
	}

	@Override
	protected void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		// The input's disabled state and the buttons' active state both depend on field activeness.
		requestRepaint();
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		ComplexField field = (ComplexField) getFieldModel();

		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, FormConstants.FLEXIBLE_CSS_CLASS);
			out.endBeginTag();
			{
				out.beginBeginTag(INPUT);
				writeInputIdAttr(out);
				out.writeAttribute(TYPE_ATTR, TEXT_TYPE_VALUE);
				out.writeAttribute(NAME_ATTR, getInputId());
				out.writeAttribute(CLASS_ATTR, FormConstants.IS_POPUP_CSS_CLASS);
				out.writeAttribute(VALUE_ATTR, field.getRawString());
				if (!field.isActive()) {
					out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
				}
				writeOnChange(out);
				out.endBeginTag();
				out.endTag(INPUT);
			}
			out.endTag(SPAN);

			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, FormConstants.FIXED_RIGHT_CSS_CLASS);
			out.endBeginTag();
			{
				writeOpenButton(context, out, field);
				writeClearButton(context, out, field);
			}
			out.endTag(SPAN);
		}
		out.endTag(SPAN);
	}

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		ComplexField field = (ComplexField) getFieldModel();

		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		out.writeText(field.getRawString());
		out.endTag(SPAN);
	}

	/**
	 * Writes the {@code onchange} handler that posts the typed text back to the server. The handler
	 * reuses the generic {@code TextInputControl} client function, which dispatches the input value
	 * under the {@code valueChanged} command resolved from the enclosing control's id; that command
	 * is inherited from {@link AbstractFormFieldControl} and parses the value through the field
	 * format.
	 */
	private void writeOnChange(TagWriter out) throws IOException {
		out.beginAttribute(ONCHANGE_ATTR);
		out.append("return ");
		out.append(FormConstants.TEXT_INPUT_CONTROL_CLASS);
		out.append(".handleOnChange(this, ");
		out.writeJsString(getID());
		out.append(");");
		out.endAttribute();
	}

	private void writeOpenButton(DisplayContext context, TagWriter out, FormField field) throws IOException {
		ButtonWriter buttonWriter = new ButtonWriter(this, Icons.OPEN_CHOOSER, OpenModels.INSTANCE);
		buttonWriter.setCss(FormConstants.CHOOSE_BUTTON);
		buttonWriter.setID(getID() + "-open");
		if (field.isActive()) {
			buttonWriter.writeButton(context, out);
		} else {
			buttonWriter.writeDisabledButton(context, out);
		}
	}

	private void writeClearButton(DisplayContext context, TagWriter out, FormField field) throws IOException {
		ButtonWriter buttonWriter =
			new ButtonWriter(this, Icons.DELETE_BUTTON, Icons.DELETE_BUTTON_DISABLED, ClearModels.INSTANCE);
		buttonWriter.setCss(FormConstants.CLEAR_BUTTON_CSS_CLASS);
		buttonWriter.setID(getID() + "-clear");
		if (field.isActive()) {
			buttonWriter.writeButton(context, out);
		} else {
			buttonWriter.writeDisabledButton(context, out);
		}
	}

	/**
	 * Opens the discover-and-choose dialog for the model allowlist.
	 */
	public static class OpenModels extends ControlCommand {

		/** Singleton {@link OpenModels} instance. */
		public static final OpenModels INSTANCE = new OpenModels();

		private static final String COMMAND_ID = "openModels";

		private OpenModels() {
			super(COMMAND_ID);
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ModelsControl modelsControl = (ModelsControl) control;
			FormField field = modelsControl.getFieldModel();
			if (!field.isActive()) {
				return HandlerResult.DEFAULT_RESULT;
			}
			return ModelsEditor.openModelChooser(context, (ComplexField) field, modelsControl._config);
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.LOAD_MODELS;
		}

	}

	/**
	 * Clears the model allowlist.
	 */
	public static class ClearModels extends ControlCommand {

		/** Singleton {@link ClearModels} instance. */
		public static final ClearModels INSTANCE = new ClearModels();

		private static final String COMMAND_ID = "clearModels";

		private ClearModels() {
			super(COMMAND_ID);
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ModelsControl modelsControl = (ModelsControl) control;
			FormField field = modelsControl.getFieldModel();
			if (!field.isActive()) {
				return HandlerResult.DEFAULT_RESULT;
			}
			try {
				FormFieldInternals.setValue((AbstractFormField) field, Collections.emptyList());
			} catch (VetoException ex) {
				ex.process(context.getWindowScope());
			}
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.CLEAR_MODELS;
		}

	}

	/**
	 * {@link AbstractFormFieldControlProvider} creating a {@link ModelsControl} bound to the provider
	 * configuration being edited.
	 */
	public static class Provider extends AbstractFormFieldControlProvider {

		private final ConfigurationItem _config;

		/**
		 * Creates a {@link Provider}.
		 *
		 * @param config
		 *        The provider configuration being edited.
		 */
		public Provider(ConfigurationItem config) {
			_config = config;
		}

		@Override
		protected Control createInput(FormMember member) {
			return new ModelsControl((FormField) member, _config);
		}

	}

}
