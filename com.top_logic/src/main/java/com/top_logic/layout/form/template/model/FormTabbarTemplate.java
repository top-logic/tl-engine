/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.DeckPaneControl;
import com.top_logic.layout.form.model.DeckField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.model.internal.TemplateRenderer;
import com.top_logic.layout.template.WithProperties;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.Card;

/**
 * Template displaying a tabbar.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormTabbarTemplate implements Template {

	private String _name;

	/**
	 * Creates a {@link FormTabbarTemplate}.
	 *
	 * @param name
	 *        See {@link #getName()}.
	 */
	public FormTabbarTemplate(String name) {
		_name = name;
	}

	/**
	 * Name of the rendered {@link DeckField}.
	 */
	public String getName() {
		return _name;
	}

	@Override
	public void write(DisplayContext displayContext, TagWriter out, WithProperties properties) throws IOException {
		DeckField deckField = (DeckField) TemplateRenderer.resolveMember(properties, getName());

		out.beginBeginTag(HTMLConstants.DIV);
		out.writeAttribute(HTMLConstants.CLASS_ATTR, "locked");
		out.endBeginTag();
		TemplateRenderer.renderControl(displayContext, out, DeckField.CP.INSTANCE.createControl(deckField));
		out.endTag(HTMLConstants.DIV);

		ControlProvider cp = new ControlProvider() {
			@Override
			public Control createControl(Object aModel, String aStyle) {
				Card card = (Card) aModel;

				FormMember tabGroup = (FormMember) card.getContent();
				return DefaultFormFieldControlProvider.INSTANCE.createControl(tabGroup, aStyle);
			}
		};
		TemplateRenderer.renderControl(displayContext, out, new DeckPaneControl(deckField.getModel(), cp));
	}
}
