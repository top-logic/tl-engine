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

import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyUtil;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ButtonDisplayMode;
import com.top_logic.layout.react.control.button.MessageButtons;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackAlign;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.ReactWindowControl;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tools.resources.translate.Translator;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * Opens a dialog to edit an {@code I18NString} value (a multi-locale {@link ResKey}) in all
 * supported languages.
 *
 * <p>
 * Composed entirely from existing controls - a {@code TLWindow} chrome holding a
 * {@link ReactFormBuilder responsive form} of one {@code TLTextInput} per supported language (each
 * wrapped in standard {@code TLFormField} chrome, with the optional translate action as an inline
 * adornment), plus standard {@code TLButton} OK/Cancel actions. The form renders through the same
 * {@code TLFormLayout}/{@code TLFormField} pipeline as a model-bound form, so label wrapping and
 * field anatomy match. No bespoke component is introduced.
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
	 * @param rows
	 *        The number of visible rows for a multi-line text area per language, or {@code 0} to
	 *        render each language as a single line.
	 */
	public static void openEditor(ReactContext context, FieldModel mainModel, int rows) {
		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager == null) {
			return;
		}
		Resources resources = Resources.getInstance();
		Locale displayLocale = TLContext.getLocale();
		ResKey current = mainModel.getValue() instanceof ResKey ? (ResKey) mainModel.getValue() : null;
		boolean multiline = rows > 0;

		// One field per supported language, built through the shared form pipeline so labels,
		// chrome and wrapping match a model-bound form. When a translation service is active, each
		// (supported) field gets an inline "Translate" icon button that fills the other fields from
		// it - the clicked field is the explicit translation source.
		boolean canTranslate = TranslationService.isActive();
		Translator translator = canTranslate ? TranslationService.getInstance() : null;
		Map<Locale, FieldModel> perLocale = new LinkedHashMap<>();
		ReactFormBuilder form = new ReactFormBuilder(context);
		for (Locale locale : resources.getSupportedLocalesInDisplayOrder()) {
			String value = current == null ? null : ResKeyUtil.translateWithoutFallback(locale, current);
			AbstractFieldModel model = new AbstractFieldModel(value);
			perLocale.put(locale, model);

			ReactTextInputControl textInput = new ReactTextInputControl(context, model);
			if (multiline) {
				textInput.setMultiline(rows);
			}
			ReactControl input = textInput;
			if (canTranslate && translator.isSupported(locale)) {
				Locale source = locale;
				ReactButtonControl translateButton = new ReactButtonControl(context,
					resources.getString(I18NConstants.I18N_STRING_TRANSLATE_BUTTON),
					ctx -> {
						translateFrom(perLocale, source);
						return HandlerResult.DEFAULT_RESULT;
					});
				translateButton.setImage(ThemeImage.icon("css:fa-solid fa-language"));
				translateButton.setDisplayMode(ButtonDisplayMode.ICON_ONLY);
				// Top-align the translate button next to a tall multi-line field.
				input = ReactFormBuilder.inputWithAdornment(context, input, translateButton,
					multiline ? StackAlign.START : StackAlign.CENTER);
			}
			form.addField(locale.getDisplayLanguage(displayLocale), input);
		}
		ReactControl body = form.build();

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
		ReactButtonControl okButton = MessageButtons.ok(context, ctx -> {
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
		});
		if (!multiline) {
			// Make OK the Enter-default. Only for single-line fields: in a multi-line text area Enter
			// inserts a newline, so it must not also submit the dialog.
			okButton.markAsDefault();
		}
		actions.add(okButton);
		window.setActions(actions);

		dialogManager.openDialog(false, window, result -> {
			// Nothing to do on close: OK already applied the value, Cancel discards.
		});
	}

	/**
	 * Translates the {@code source} row's text into every other supported row, overwriting their
	 * values. A target left unsupported or a service failure leaves that row unchanged.
	 */
	private static void translateFrom(Map<Locale, FieldModel> perLocale, Locale source) {
		if (!TranslationService.isActive()) {
			return;
		}
		Translator translator = TranslationService.getInstance();
		if (!translator.isSupported(source)) {
			return;
		}
		Object sourceValue = perLocale.get(source).getValue();
		String sourceText = sourceValue == null ? null : sourceValue.toString();
		if (sourceText == null || sourceText.isEmpty()) {
			return;
		}
		for (Map.Entry<Locale, FieldModel> entry : perLocale.entrySet()) {
			Locale target = entry.getKey();
			if (target.getLanguage().equals(source.getLanguage()) || !translator.isSupported(target)) {
				continue;
			}
			try {
				entry.getValue().setValue(translator.translate(sourceText, source, target));
			} catch (I18NRuntimeException ex) {
				// Leave this target unchanged on a service failure.
			}
		}
	}

}
