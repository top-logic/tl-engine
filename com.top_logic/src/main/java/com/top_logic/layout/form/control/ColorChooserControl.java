/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.awt.Color;
import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
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
			ColorSelectionCommand.INSTANCE
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
		String bgColor = getHtmlColor();

		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			out.beginBeginTag(BUTTON);
			writeInputIdAttr(out);
			out.writeAttribute(CLASS_ATTR, "ColorChooser");
			writeEditableStyle(out, bgColor);
			if (field.isDisabled()) {
				out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
			} else {
				writeOnClick(out);
			}
			out.endBeginTag();

			out.writeText(NBSP);

			out.endTag(BUTTON);
		}
		out.endTag(SPAN);
	}

	private void writeEditableStyle(TagWriter out, String bgColor) throws IOException {
		out.beginAttribute(STYLE_ATTR);
		out.append("background-color:");
		out.append(bgColor);
		out.append(";");
		if (!getModel().isDisabled()) {
			out.append("cursor:pointer;");
		}
		out.endAttribute();
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
	}

	@Override
	protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
		String bgColor = getHtmlColor();

		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			// The inner span is necessary since the IE won't show the style
			// attribute correctly
			out.beginBeginTag(SPAN);
			writeImmutableStyle(out, bgColor);
			out.endBeginTag();

			out.writeText(NBSP);
			out.writeText(NBSP);
			out.writeText(NBSP);
			out.writeText(NBSP);
			out.writeText(NBSP);

			out.endTag(SPAN);
		}
		out.endTag(SPAN);
	}

	private void writeImmutableStyle(TagWriter out, String bgColor) throws IOException {
		out.beginAttribute(STYLE_ATTR);
		out.append("background-color:");
		out.append(bgColor);
		out.append("; cursor:default;");
		out.endAttribute();
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		// If the value changed repaint the element
		requestRepaint();
	}

	static class ColorSelectionCommand extends ControlCommand {

		public static final ColorSelectionCommand INSTANCE = new ColorSelectionCommand();
		
		protected static final String COMMAND_ID = "openSelectionDialog";
		
		protected ColorSelectionCommand() {
			super(COMMAND_ID);
		}

		/**
		 * @see com.top_logic.layout.basic.ControlCommand#execute(com.top_logic.layout.DisplayContext,
		 *      com.top_logic.layout.Control,
		 *      Map)
		 */
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
	 * The {@link Color} business object being edited.
	 */
	public Color getColor() {
		FormField field = (FormField) getModel();
		Color result = (Color) field.getValue();
		if (result == null) {
			return Color.black;
		}
		return result;
	}

	/**
	 * The {@link #getColor()} in HTML hexadecimal representation.
	 */
	public String getHtmlColor() {
		return toHtmlColor(getColor());
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
