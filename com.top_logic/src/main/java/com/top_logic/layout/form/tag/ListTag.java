/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.layout.Control;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.ListField;
import com.top_logic.layout.list.ListControl;

/**
 * Factory for {@link ListControl}s within forms.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ListTag extends AbstractFormFieldControlTag {

	private Renderer<Object> itemRenderer;
	
	public void setItemRenderer(Renderer<Object> itemRenderer) {
		this.itemRenderer = itemRenderer;
	}

	@Override
	public Control createControl(FormMember member, String displayStyle) {
		ListField listField = (ListField) member;
		
		ListControl control = new ListControl(listField);
		
		if (itemRenderer != null) {
			control.setItemContentRenderer(itemRenderer);
		}
		
		return control;
	}

}
