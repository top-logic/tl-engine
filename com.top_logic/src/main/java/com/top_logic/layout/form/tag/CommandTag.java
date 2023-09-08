/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.AbstractButtonRenderer;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ButtonRenderer;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * The class {@link CommandTag} renders {@link CommandField}s using a {@link ButtonControl}. Its
 * arguments are the name of an {@link CommandField} and an {@link AbstractButtonRenderer} for
 * rendering the {@link CommandField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CommandTag extends AbstractFormMemberControlTag {

	private AbstractButtonRenderer<?> _renderer = null;

	/**
	 * Define the button renderer to use.
	 */
	public void setRenderer(AbstractButtonRenderer<?> renderer) {
		_renderer = renderer;
	}

	@Override
	protected int endFormMember() throws IOException, JspException {
		ControlTagUtil.writeControl(this, pageContext, getControl());
		return EVAL_PAGE;
	}

	@Override
	protected int startFormMember() throws IOException, JspException {
		return EVAL_BODY_INCLUDE;
	}

	@Override
	protected void teardown() {
		super.teardown();

		_renderer = null;
	}

	@Override
	public Control createControl(FormMember member, String displayStyle) {
		CommandField theCommand = (CommandField) member;
		AbstractButtonRenderer<?> renderer = _renderer;
		if (renderer == null) {
			ControlProvider provider = theCommand.getControlProvider();
			if (provider != null) {
				// Application-defined control.
				return provider.createControl(theCommand);
			} else {
				// Default renderer.
				renderer = ButtonRenderer.INSTANCE;
			}
		}
		return new ButtonControl(theCommand, renderer);
	}

}
