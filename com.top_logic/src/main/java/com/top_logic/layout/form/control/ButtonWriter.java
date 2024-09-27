/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.tag.js.JSObject;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.util.css.CssUtil;

/**
 * Writes a button with a {@link ThemeImage}. The button can be written as a disabled button.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class ButtonWriter {

	private Control _control;

	private String _id;

	private ThemeImage _image;

	private ThemeImage _disabledImage;

	private ControlCommand _command;

	private ResKey _tooltip;

	private ResKey _disabledTooltip;

	private String _cssClass;

	private JSObject _arguments;

	private String _autocomplete;

	/**
	 * Create a new {@link ButtonWriter}.
	 * 
	 * @param control
	 *        The control containing the button.
	 * @param image
	 *        The image to write in enabled state.
	 * @param disabledImage
	 *        The image to write in disabled state.
	 * @param command
	 *        The command of the button.
	 */
	public ButtonWriter(Control control, ThemeImage image, ThemeImage disabledImage, ControlCommand command) {
		_control = control;
		_image = image;
		_disabledImage = disabledImage;
		_command = command;
	}

	/**
	 * Create a new {@link ButtonWriter}. Uses the same image for enabled and disabled.
	 * 
	 * @see #ButtonWriter(Control, ThemeImage, ThemeImage, ControlCommand)
	 */
	public ButtonWriter(Control control, ThemeImage image, ControlCommand command) {
		this(control, image, image, command);
	}

	/**
	 * The client side id.
	 */
	public void setID(String id) {
		_id = id;
	}

	/**
	 * The tooltip to display at the enabled button.
	 */
	public void setTooltip(ResKey tooltip) {
		_tooltip = tooltip;
	}

	/**
	 * The tooltip to display at the disabled button.
	 */
	public void setDisabledTooltip(ResKey disabledTooltip) {
		_disabledTooltip = disabledTooltip;
	}

	/**
	 * The CSS class(es) of the button.
	 */
	public void setCss(String cssClass) {
		_cssClass = cssClass;
	}

	/**
	 * The JS arguments for the command.
	 */
	public void setJSArguments(JSObject arguments) {
		_arguments = arguments;
	}

	/**
	 * The JS arguments for the command.
	 */
	public void setAutocomplete(String autocomplete) {
		_autocomplete = autocomplete;
	}

	/**
	 * Write an enabled button.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        The {@link TagWriter} to write to.
	 */
	public void writeButton(DisplayContext context, TagWriter out) throws IOException {
		XMLTag tag = _image.toButton();
		tag.beginBeginTag(context, out);
		out.writeAttribute(HTMLConstants.ID_ATTR, _id);
		writeCss(out, FormConstants.INPUT_IMAGE_ACTION_CSS_CLASS);

		writeOnClick(out);

		if(_tooltip != null) {
			HTMLUtil.writeImageTooltip(context, out, _tooltip);
		}

		if (!StringServices.isEmpty(_autocomplete)) {
			// Switch off auto completion offered by browser per default. The reason
			// is that this will not fire any event so that the application is not
			// aware of any changes and will therefore ignore the entered values.
			out.writeAttribute("autocomplete", _autocomplete);
		}

		tag.endEmptyTag(context, out);
	}

	private void writeCss(TagWriter out, String inputCss) throws IOException {
		out.beginCssClasses();
		out.append(inputCss);
		if (!StringServices.isEmpty(_cssClass)) {
			out.append(_cssClass);
		}
		out.endCssClasses();
	}

	private void writeOnClick(TagWriter out) throws IOException {
		out.beginAttribute(HTMLConstants.ONCLICK_ATTR);
		out.write("return ");
		if (_arguments != null) {
			_command.writeInvokeExpression(out, _control, _arguments);
		} else {
			_command.writeInvokeExpression(out, _control);
		}
		out.endAttribute();
	}

	/**
	 * Write a disabled button.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        The {@link TagWriter} to write to.
	 */
	public void writeDisabledButton(DisplayContext context, TagWriter out) throws IOException {
		XMLTag tag = _disabledImage.toIcon();
		tag.beginBeginTag(context, out);
		writeCss(out, CssUtil.joinCssClasses(FormConstants.INPUT_IMAGE_CSS_CLASS, FormConstants.DISABLED_CSS_CLASS));
		out.writeAttribute(HTMLConstants.ID_ATTR, _id);

		if (_disabledTooltip != null) {
			HTMLUtil.writeImageTooltip(context, out, _disabledTooltip);
		}

		if (!StringServices.isEmpty(_autocomplete)) {
			// Switch off auto completion offered by browser per default. The reason
			// is that this will not fire any event so that the application is not
			// aware of any changes and will therefore ignore the entered values.
			out.writeAttribute("autocomplete", _autocomplete);
		}

		tag.endEmptyTag(context, out);
	}

}
