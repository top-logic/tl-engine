/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.List;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ButtonDisplayMode;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.form.I18NStringDialog;
import com.top_logic.layout.react.control.form.ReactI18NStringInputControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackAlign;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackDirection;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link ReactFieldControlProvider} for {@code I18NString} attributes.
 *
 * <p>
 * Composes the inline current-locale input ({@link ReactI18NStringInputControl}, reusing the
 * {@code TLTextInput} component) with a link button that opens the all-languages editor
 * ({@link I18NStringDialog}) - both existing controls, laid out in a {@code TLStack} row.
 * </p>
 */
public class I18NStringControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		ReactControl inline = new ReactI18NStringInputControl(context, model);

		ReactButtonControl editAll = new ReactButtonControl(context,
			Resources.getInstance().getString(I18NConstants.I18N_STRING_ALL_LANGUAGES_BUTTON),
			ctx -> {
				I18NStringDialog.openEditor(ctx, model);
				return HandlerResult.DEFAULT_RESULT;
			});
		editAll.setImage(ThemeImage.icon("css:fa-solid fa-globe"));
		editAll.setDisplayMode(ButtonDisplayMode.ICON_ONLY);

		ReactStackControl row = new ReactStackControl(context, StackDirection.ROW, StackGap.COMPACT,
			StackAlign.CENTER, false, List.of(inline, editAll));
		row.setGrowFirst(true);
		return row;
	}

}
