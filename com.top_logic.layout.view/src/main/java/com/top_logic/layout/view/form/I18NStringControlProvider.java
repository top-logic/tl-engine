/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ButtonDisplayMode;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.form.I18NStringDialog;
import com.top_logic.layout.react.control.form.ReactFormBuilder;
import com.top_logic.layout.react.control.form.ReactI18NStringInputControl;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link ReactFieldControlProvider} for {@code I18NString} attributes.
 *
 * <p>
 * Composes the inline current-locale input ({@link ReactI18NStringInputControl}, reusing the
 * {@code TLTextInput} component) with an icon button that opens the all-languages editor
 * ({@link I18NStringDialog}) as an inline adornment so the input fills the field width.
 * </p>
 */
public class I18NStringControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		ReactI18NStringInputControl inline = new ReactI18NStringInputControl(context, model);

		ReactButtonControl editAll = new ReactButtonControl(context,
			Resources.getInstance().getString(I18NConstants.I18N_STRING_ALL_LANGUAGES_BUTTON),
			ctx -> {
				I18NStringDialog.openEditor(ctx, model);
				return HandlerResult.DEFAULT_RESULT;
			});
		editAll.setImage(ThemeImage.icon("css:fa-solid fa-globe"));
		editAll.setDisplayMode(ButtonDisplayMode.ICON_ONLY);

		// The all-languages button is an edit affordance - shown only while the field is editable.
		inline.setAdornment(editAll);

		return ReactFormBuilder.inputWithAdornment(context, inline, editAll);
	}

}
