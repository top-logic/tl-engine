/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.layout.Control;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.SelectTextControl;
import com.top_logic.layout.form.model.SelectField;

/**
 * JSP tag that renders a {@link SelectTextControl}.
 * 
 * @author <a href=mailto:iwi@top-logic.com>Isabell Wittich</a>
 */
public class PopupTextSelectTag extends AbstractFormFieldControlTag {

	private Renderer<Object> _selectionRenderer;

	@Override
	public Control createControl(FormMember member, String displayStyle) {
		SelectField selectField = (SelectField) member;
		SelectTextControl result = new SelectTextControl(selectField);

		if (_selectionRenderer != null) {
			result.setSelectionRenderer(_selectionRenderer);
		}

		return result;
	}

	/**
	 * Sets the renderer for the selection in view mode.
	 * 
	 * @see SelectTextControl#setSelectionRenderer(Renderer)
	 */
	public void setRenderer(Renderer<?> selectionRenderer) {
		_selectionRenderer = selectionRenderer.generic();
	}
}
