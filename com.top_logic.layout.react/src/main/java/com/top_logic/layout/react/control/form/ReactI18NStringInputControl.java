/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.Locale;

import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyUtil;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.tools.resources.translate.Translator;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * A {@link ReactFormFieldControl} for {@code I18NString} attributes (whose value is a multi-locale
 * {@link ResKey}).
 *
 * <p>
 * Reuses the existing {@code TLTextInput} component to render a single inline field in the
 * <b>current user's locale</b>. On input the typed string is folded into a {@link ResKey}, so the
 * model receives a typed {@link ResKey} (a plain {@link String} would be rejected by the I18N
 * storage mapping). When the user-locale value is edited and a {@link TranslationService} is active,
 * the other locales are <b>auto-translated</b> from it when the field is committed (loses focus),
 * overwriting their previous values; otherwise they are preserved. Translation runs on commit
 * rather than per keystroke, since it is a blocking remote call.
 * </p>
 *
 * <p>
 * If the current locale has no value but another locale does, that best-available translation is
 * shown as the displayed value in view mode and as a placeholder in edit mode, so an existing value
 * is never hidden.
 * </p>
 *
 * @implNote An editor for other languages is a separate concern and is composed from existing
 *           controls by the {@link I18NEditorDialog}, not built into this control.
 */
public class ReactI18NStringInputControl extends ReactFormFieldControl {

	/** Command sent by the client when an edited field is committed (loses focus). */
	private static final String COMMIT_COMMAND = "commit";

	/** State key telling the client to send {@link #COMMIT_COMMAND} when the field loses focus. */
	private static final String COMMIT_ON_BLUR = "commitOnBlur";

	private boolean _editable;

	/**
	 * Creates a new {@link ReactI18NStringInputControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model (value is a {@link ResKey} or {@code null}).
	 */
	public ReactI18NStringInputControl(ReactContext context, FieldModel model) {
		super(context, model, "TLTextInput");
		refresh();
		if (TranslationService.isActive()) {
			// Defer auto-translation to field commit (blur); see #handleCommit.
			putState(COMMIT_ON_BLUR, Boolean.TRUE);
		}
	}

	/**
	 * Creates a ready-to-use editor for an {@code I18NString} field: the inline current-locale input
	 * together with the languages button that opens the {@link I18NEditorDialog}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model (value is a {@link ResKey} or {@code null}).
	 * @param rows
	 *        The number of visible rows for a multi-line text area, or {@code 0} to render the
	 *        current-locale input as a single line.
	 * @param fieldLabel
	 *        The label of the edited field, shown in the dialog title, or {@code null} for a
	 *        generic title.
	 * @return The composed editor control.
	 */
	public static ReactControl createEditor(ReactContext context, FieldModel model, int rows, String fieldLabel) {
		ReactI18NStringInputControl inline = new ReactI18NStringInputControl(context, model);
		if (rows > 0) {
			inline.setMultiline(rows);
		}
		return I18NEditorDialog.createEditor(context, model, inline, new I18NStringValueEditor(rows), fieldLabel);
	}

	@Override
	protected void setEditable(boolean editable) {
		super.setEditable(editable);
		_editable = editable;
		refresh();
	}

	@Override
	protected void handleModelValueChanged(FieldModel source, Object oldValue, Object newValue) {
		refresh();
	}

	/**
	 * Recomputes the displayed value and placeholder from the model value and the current edit mode.
	 */
	private void refresh() {
		ResKey resKey = asResKey(getFieldModel().getValue());
		String userText = resKey == null ? null : ResKeyUtil.translateWithoutFallback(TLContext.getLocale(), resKey);
		if (_editable) {
			// Show the user-locale value; if it is empty, offer the best available translation as a
			// placeholder so a value set only in another language is not hidden.
			putState(VALUE, emptyToNull(userText));
			setPlaceholder(isEmpty(userText) ? fallbackValue(resKey) : null);
		} else {
			// View mode: fall back to the best available translation so an existing value shows.
			putState(VALUE, isEmpty(userText) ? fallbackValue(resKey) : userText);
			setPlaceholder(null);
		}
	}

	@Override
	protected Object parseClientValue(Object rawValue) {
		// Fold the typed current-locale text into the ResKey, preserving the other locales. This runs
		// on every keystroke and must stay cheap; translation into the other locales is deferred to
		// the field commit (see #handleCommit), since it is a blocking remote call.
		String text = rawValue == null ? null : rawValue.toString();
		Locale userLocale = TLContext.getLocale();
		ResKey current = asResKey(getFieldModel().getValue());

		ResKey.Builder builder = ResKey.builder();
		boolean any = false;
		for (Locale locale : Resources.getInstance().getSupportedLocalesInDisplayOrder()) {
			String value = locale.getLanguage().equals(userLocale.getLanguage())
				? text
				: (current == null ? null : ResKeyUtil.translateWithoutFallback(locale, current));
			if (!isEmpty(value)) {
				builder.add(locale, value);
				any = true;
			}
		}
		return any ? builder.build() : null;
	}

	/**
	 * Translates the current-locale value into the other locales, overwriting their previous values.
	 *
	 * <p>
	 * Sent by the client when an edited field is committed (loses focus), so the blocking
	 * translation runs once per edit rather than on every keystroke. The client only sends this
	 * after an actual edit, so merely focusing and leaving the field translates nothing.
	 * </p>
	 */
	@ReactCommandHandler(COMMIT_COMMAND)
	void handleCommit() {
		if (!TranslationService.isActive()) {
			return;
		}
		ResKey current = asResKey(getFieldModel().getValue());
		Locale userLocale = TLContext.getLocale();
		String text = current == null ? null : ResKeyUtil.translateWithoutFallback(userLocale, current);
		if (isEmpty(text)) {
			return;
		}
		Translator translator = TranslationService.getInstance();
		if (!translator.isSupported(userLocale)) {
			return;
		}

		ResKey.Builder builder = ResKey.builder();
		boolean any = false;
		for (Locale locale : Resources.getInstance().getSupportedLocalesInDisplayOrder()) {
			String value;
			if (locale.getLanguage().equals(userLocale.getLanguage())) {
				value = text;
			} else if (translator.isSupported(locale)) {
				value = translate(translator, text, userLocale, locale, current);
			} else {
				value = ResKeyUtil.translateWithoutFallback(locale, current);
			}
			if (!isEmpty(value)) {
				builder.add(locale, value);
				any = true;
			}
		}
		if (any) {
			getFieldModel().setValue(builder.build());
		}
	}

	/**
	 * Translates {@code text} from {@code source} to {@code target}; on a service failure keeps the
	 * previous value of {@code target} from {@code current}.
	 */
	private static String translate(Translator translator, String text, Locale source, Locale target, ResKey current) {
		try {
			return translator.translate(text, source, target);
		} catch (I18NRuntimeException ex) {
			return current == null ? null : ResKeyUtil.translateWithoutFallback(target, current);
		}
	}

	/** The first non-empty translation in display order (best available), or {@code null}. */
	private static String fallbackValue(ResKey resKey) {
		if (resKey == null) {
			return null;
		}
		for (Locale locale : Resources.getInstance().getSupportedLocalesInDisplayOrder()) {
			String value = ResKeyUtil.translateWithoutFallback(locale, resKey);
			if (!isEmpty(value)) {
				return value;
			}
		}
		return null;
	}

	private static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

	private static String emptyToNull(String value) {
		return isEmpty(value) ? null : value;
	}

	private static ResKey asResKey(Object value) {
		return value instanceof ResKey ? (ResKey) value : null;
	}

}
