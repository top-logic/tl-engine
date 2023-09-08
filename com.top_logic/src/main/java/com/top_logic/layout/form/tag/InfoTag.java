/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import com.top_logic.basic.CalledFromJSP;
import com.top_logic.layout.Control;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.InfoControl;

/**
 * The class {@link InfoTag} is the tag to construct some {@link InfoControl} for a
 * {@link FormMember}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class InfoTag extends AbstractFormMemberControlTag {
	
	private ThemeImage _icon;

	/**
	 * Use {@link #setIcon(ThemeImage)}.
	 */
	@CalledFromJSP
	@Deprecated
	public void setImage(String src) {
		setIcon(ThemeImage.icon(src));
	}
	
	/**
	 * {@link ThemeImage} to render.
	 */
	@CalledFromJSP
	public void setIcon(ThemeImage icon) {
		_icon = icon;
	}

	@Override
	protected void teardown() {
		_icon = null;
		super.teardown();
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
		return InfoControl.createInfoControl(member, _icon);
	}

}

