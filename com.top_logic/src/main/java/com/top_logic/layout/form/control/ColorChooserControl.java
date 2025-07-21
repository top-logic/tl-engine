/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.awt.Color;
import java.io.IOException;
import java.util.Map;

import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractFormFieldControl} rendering {@link Color} value in a color chooser.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ColorChooserControl extends AbstractFormFieldControl {

	private static final Map COMMANDS = createCommandMap(
		AbstractFormFieldControl.COMMANDS,
		new ControlCommand[] {
			new ColorSelectionCommand(),
			new ClearSelection()
		});
	
	/**
	 * Uses the Constructor of {@AbstractFormFieldControl} to build a control.
	 * 
	 * @see AbstractFormFieldControl
	 */
	public ColorChooserControl(FormField model) {
		super(model, COMMANDS);
	}
	
	@Override
	protected String getTypeCssClass() {
		return "cColorChooser";
	}

	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		FormField field = (FormField) getModel();

		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, FormConstants.FLEXIBLE_CSS_CLASS);
			out.endBeginTag();

			out.beginBeginTag(BUTTON);
			writeInputIdAttr(out);

			Color color = getColorOrNull();
			out.beginCssClasses();
			{
				out.append("ColorChooser");

				if (color == null) {
					out.append("noColor");
				}

				if (getModel().isActive()) {
					out.append("active");
				}
			}
			out.endCssClasses();

			if (color != null) {
				out.beginAttribute(STYLE_ATTR);
				String bgColor = toHtmlColor(color);

				out.append("background-color:");
				out.append(bgColor);
				out.append(";");
				out.endAttribute();
			}

			if (field.isDisabled()) {
				out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
			} else {
				writeOnClick(out);
			}
			out.endBeginTag();

			out.writeText(NBSP);

			out.endTag(BUTTON);
			out.endTag(SPAN);

			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, FormConstants.FIXED_RIGHT_CSS_CLASS);
			out.endBeginTag();

			writeClearButton(context, out);

			out.endTag(SPAN);
		}
		out.endTag(SPAN);
	}

	private String getClearButtonID() {
		return getID() + "-clear";
	}

	private void writeClearButton(DisplayContext context, TagWriter out) throws IOException {
		ButtonWriter buttonWriter = new ButtonWriter(this, com.top_logic.layout.form.tag.Icons.DELETE_BUTTON,
			com.top_logic.layout.form.tag.Icons.DELETE_BUTTON_DISABLED, getCommand(ClearSelection.COMMAND));
		buttonWriter.setID(getClearButtonID());
		buttonWriter.setCss(FormConstants.CLEAR_BUTTON_CSS_CLASS);
		buttonWriter.setTooltip(
			com.top_logic.layout.form.I18NConstants.CLEAR_CHOOSER__LABEL.fill(getModel().getLabel()));

		boolean active = activeClearButton();
		if (active) {
			buttonWriter.writeButton(context, out);
		} else {
			buttonWriter.writeDisabledButton(context, out);
		}
	}

	private boolean activeClearButton() {
		FormField model = getFieldModel();
		if (!model.isActive()) {
			return false;
		}
		if (model.hasError()) {
			return true;
		}
		return model.hasValue() && model.getValue() != null;
	}

	private void writeOnClick(TagWriter out) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		out.append("services.form.ColorChooserControl.click(this, ");
		writeIdJsString(out);
		out.append(")");
		out.endAttribute();
	}

	@Override
	public void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		addDisabledUpdate(newValue.booleanValue());
		
		addUpdate(new ElementReplacement(getClearButtonID(), (context, out) -> writeClearButton(context, out)));
	}

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			ColorRenderer.INSTANCE.write(context, out, getColor());
		}
		out.endTag(SPAN);
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		// If the value changed repaint the element
		requestRepaint();
	}

	static class ColorSelectionCommand extends ControlCommand {

		protected static final String COMMAND_ID = "openSelectionDialog";
		
		protected ColorSelectionCommand() {
			super(COMMAND_ID);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control,
				Map<String, Object> arguments) {

			HandlerResult result = HandlerResult.DEFAULT_RESULT;

			// construct the dialog
			ColorChooserControl theControl = (ColorChooserControl) control;
			
			// Create Dialog
			PopupDialogControl dialog = ColorChooserSelectionControl.createDialog(commandContext, theControl);
			
			// Open Dialog
			theControl.getFrameScope().getWindowScope().openPopupDialog(dialog);			
			

			return result;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.OPEN_COLOR_SELECTION;
		}
	}
	
	/**
	 * 
	 * {@link ControlCommand} clearing the selection of a {@link ColorChooserControl}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ClearSelection extends ControlCommand {

		/**
		 * {@link ControlCommand#getID() ID} of the {@link ClearSelection} command.
		 */
		public static final String COMMAND = "clearSelection";

		/**
		 * Singleton constructor.
		 */
		protected ClearSelection() {
			super(COMMAND);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			ColorChooserControl ccc = (ColorChooserControl) control;
			AbstractFormField colorField = (AbstractFormField) ccc.getFieldModel();
			Object newValue = null;
			try {
				FormFieldInternals.setValue(colorField, newValue);
			} catch (VetoException ex) {
				ex.setContinuationCommand(new Command() {

					@Override
					public HandlerResult executeCommand(DisplayContext context) {
						colorField.setValue(newValue);
						return HandlerResult.DEFAULT_RESULT;
					}
				});
				ex.process(ccc.getWindowScope());
			}

			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordFieldInput(colorField, newValue);
			}

			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.CLEAR_COLOR_SELECTION;
		}
	}

	/**
	 * The {@link Color} business object being edited.
	 */
	public Color getColor() {
		Color result = getColorOrNull();
		if (result == null) {
			return Color.black;
		}
		return result;
	}

	private Color getColorOrNull() {
		FormField field = (FormField) getModel();
		Color result = (Color) field.getValue();
		return result;
	}

	/**
	 * Converts the given {@link Color} to HTML hexadecimal representation.
	 */
	public static String toHtmlColor(Color color) {
		if (color == null) {
			return null;
		}
		return "#" + hexByte(color.getRed()) + hexByte(color.getGreen()) + hexByte(color.getBlue());
	}

	private static String hexByte(int byteValue) {
		return (byteValue < 0x10 ? "0" : "") + Integer.toHexString(byteValue);
	}

}
