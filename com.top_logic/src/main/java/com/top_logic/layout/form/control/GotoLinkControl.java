/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractConstantControl;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;

/**
 * Control to render an icon for an url.
 * 
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class GotoLinkControl extends AbstractConstantControl implements ValueListener {

	private final ThemeImage _icon;

	private final FormField _field;

	/**
	 * @param field
	 *        {@link FormField} which contains the URL as value.
	 */
	public GotoLinkControl(FormField field) {
		this(field, Icons.URL_LINK);
	}

	/**
	 * @param field
	 *        {@link FormField} which contains the URL as value.
	 * @param icon
	 *        {@link ThemeImage} which contains the theme icon.
	 */
	public GotoLinkControl(FormField field, ThemeImage icon) {
		_icon = icon;
		_field = field;
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		requestRepaint();
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();

		_field.addValueListener(this);
	}

	@Override
	protected void internalDetach() {
		_field.removeValueListener(this);

		super.internalDetach();
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		if (hasLink()) {
			out.beginTag(SPAN);
			out.beginBeginTag(ANCHOR);
			writeControlAttributes(context, out);
			out.writeAttribute(HREF_ATTR, (String) _field.getValue());
			out.writeAttribute(TARGET_ATTR, HTMLConstants.BLANK_VALUE);
			out.endBeginTag();

			_icon.writeWithTooltip(context, out, createUrlText());

			out.endTag(ANCHOR);
			out.endTag(SPAN);
		}
	}

	private boolean hasLink() {
		return _field.hasValue() && !StringServices.isEmpty(_field.getValue()) && !_field.hasError();
	}

	private String createUrlText() {
		return "URL: " + "<b>" + _field.getValue() + "</b>";
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);

		HTMLUtil.appendCSSClass(out, FormConstants.INPUT_IMAGE_ACTION_CSS_CLASS);
		HTMLUtil.appendCSSClass(out, GotoHandler.GOTO_CLASS);
	}
}
