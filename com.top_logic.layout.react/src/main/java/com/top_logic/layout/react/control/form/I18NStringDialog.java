/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyUtil;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.MessageButtons;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackAlign;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackDirection;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.ReactWindowControl;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * Opens a dialog to edit an {@code I18NString} value (a multi-locale {@link ResKey}) in all
 * supported languages.
 *
 * <p>
 * Composed entirely from existing controls - a {@code TLWindow} chrome holding a {@code TLStack} of
 * one {@code TLFormField}-wrapped {@code TLTextInput} per supported language, with standard
 * {@code TLButton} OK/Cancel actions. No bespoke component is introduced.
 * </p>
 */
public class I18NStringDialog {

	/**
	 * Opens the all-languages editor for the given field.
	 *
	 * @param context
	 *        The current React context (must provide a {@link DialogManager}).
	 * @param mainModel
	 *        The field model whose value is the edited {@link ResKey}.
	 */
	public static void openEditor(ReactContext context, FieldModel mainModel) {
		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager == null) {
			return;
		}
		Resources resources = Resources.getInstance();
		Locale displayLocale = TLContext.getLocale();
		ResKey current = mainModel.getValue() instanceof ResKey ? (ResKey) mainModel.getValue() : null;

		// One field model + text-input row per supported language.
		Map<Locale, FieldModel> perLocale = new LinkedHashMap<>();
		List<ReactControl> rows = new ArrayList<>();
		for (Locale locale : resources.getSupportedLocalesInDisplayOrder()) {
			String value = current == null ? null : ResKeyUtil.translateWithoutFallback(locale, current);
			AbstractFieldModel model = new AbstractFieldModel(value);
			perLocale.put(locale, model);

			ReactControl input = new ReactTextInputControl(context, model);
			rows.add(new ReactFormFieldChromeControl(context, locale.getDisplayLanguage(displayLocale), input));
		}
		ReactStackControl body =
			new ReactStackControl(context, StackDirection.COLUMN, StackGap.DEFAULT, StackAlign.STRETCH, false, rows);

		ReactWindowControl window = new ReactWindowControl(context,
			resources.getString(I18NConstants.I18N_STRING_ALL_LANGUAGES_TITLE),
			DisplayDimension.px(480),
			() -> dialogManager.closeTopDialog(DialogResult.cancelled()));
		window.setChild(body);

		List<ReactControl> actions = new ArrayList<>();
		actions.add(MessageButtons.cancel(context, ctx -> {
			dialogManager.closeTopDialog(DialogResult.cancelled());
			return HandlerResult.DEFAULT_RESULT;
		}));
		actions.add(MessageButtons.ok(context, ctx -> {
			ResKey.Builder builder = ResKey.builder();
			boolean any = false;
			for (Map.Entry<Locale, FieldModel> entry : perLocale.entrySet()) {
				Object value = entry.getValue().getValue();
				String text = value == null ? null : value.toString();
				if (text != null && !text.isEmpty()) {
					builder.add(entry.getKey(), text);
					any = true;
				}
			}
			// Setting the main field's value re-renders the inline control via its model listener.
			mainModel.setValue(any ? builder.build() : null);
			dialogManager.closeTopDialog(DialogResult.ok(null));
			return HandlerResult.DEFAULT_RESULT;
		}));
		window.setActions(actions);

		dialogManager.openDialog(false, window, result -> {
			// Nothing to do on close: OK already applied the value, Cancel discards.
		});
	}

}
