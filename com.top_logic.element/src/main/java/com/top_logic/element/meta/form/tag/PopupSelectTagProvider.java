/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.element.meta.form.EditContext;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.Control;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.SelectTextControl;
import com.top_logic.layout.form.control.SelectionControl;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.tag.PopupSelectTag;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.model.annotate.DisplayAnnotations;

/**
 * {@link DisplayProvider} that creates a {@link PopupSelectTag} for arbitrary business objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class PopupSelectTagProvider implements DisplayProvider {

	/**
	 * Singleton {@link PopupSelectTagProvider} instance.
	 */
	public static final PopupSelectTagProvider INSTANCE = new PopupSelectTagProvider();

	private PopupSelectTagProvider() {
		// Singleton constructor.
	}

	@Override
	public Control createDisplay(EditContext editContext, FormMember member) {
		Renderer<Object> optionRenderer = MetaResourceProvider.DEFAULT_RENDERER;

		if (ThemeFactory.getTheme().getValue(com.top_logic.layout.Icons.POPUP_TEXT_SELECT_VIEW)) {
			SelectTextControl result = new SelectTextControl((FormField) member);
			result.setSelectionRenderer(optionRenderer);
			return result;
		} else {
			SelectionControl result = new SelectionControl((SelectField) member);
			result.setColumns(DisplayAnnotations.inputSize(editContext, PopupSelectTag.NO_COLUMNS));
			result.setClearButton(!editContext.isMandatory());
			result.setOptionRenderer(optionRenderer);
			return result;
		}
	}

}
