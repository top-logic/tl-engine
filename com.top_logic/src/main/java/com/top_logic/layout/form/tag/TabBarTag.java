/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.DeckField;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.renderers.TabBarRenderer;

/**
 * The class {@link TabBarTag} is used to render the tab bar of an {@link DeckField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TabBarTag extends AbstractFormMemberControlTag {

	private TabBarRenderer renderer;

	/**
	 * Sets the {@link TabBarRenderer} for displaying the rendered {@link DeckField}.
	 */
	@CalledByReflection
	public void setRenderer(TabBarRenderer aRenderer) {
		this.renderer = aRenderer;
	}

	@Override
	protected int endFormMember() throws IOException, JspException {
		return EVAL_PAGE;
	}

	@Override
	protected int startFormMember() throws IOException, JspException {
		ControlTagUtil.writeControl(this, pageContext, getControl());
		return SKIP_BODY;
	}

	@Override
	public Control createControl(FormMember member, String displayStyle) {
		if (renderer != null) {
			member.set(DeckField.CP.TAB_BAR_RENDERER, renderer);
		}
		return DeckField.CP.INSTANCE.createControl(member, FormTemplateConstants.STYLE_DIRECT_VALUE);
	}

}
