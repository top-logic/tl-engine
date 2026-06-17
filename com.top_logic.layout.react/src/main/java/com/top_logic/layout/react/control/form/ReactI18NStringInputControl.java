/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.Locale;
import java.util.Objects;

import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyUtil;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
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
 * the other locales are <b>auto-translated</b> from it (overwriting their previous values);
 * otherwise they are preserved.
 * </p>
 *
 * <p>
 * If the current locale has no value but another locale does, that best-available translation is
 * shown as the displayed value in view mode and as a placeholder in edit mode, so an existing value
 * is never hidden. An optional adornment (e.g. the all-languages button) is shown only in edit mode.
 * </p>
 *
 * @implNote An editor for all languages is a separate concern and should be composed from existing
 *           controls (dialog / form / fields / buttons), not built into this control.
 */
public class ReactI18NStringInputControl extends ReactFormFieldControl {

	private boolean _editable;

	private ReactControl _adornment;

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
	}

	/**
	 * Registers an adornment control (e.g. the all-languages button) that is only shown while the
	 * field is editable.
	 */
	public void setAdornment(ReactControl adornment) {
		_adornment = adornment;
		adornment.setHidden(!_editable);
	}

	@Override
	protected void setEditable(boolean editable) {
		super.setEditable(editable);
		_editable = editable;
		refresh();
		if (_adornment != null) {
			_adornment.setHidden(!editable);
		}
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
		String text = rawValue == null ? null : rawValue.toString();
		Locale userLocale = TLContext.getLocale();
		ResKey current = asResKey(getFieldModel().getValue());
		String previousText = current == null ? null : ResKeyUtil.translateWithoutFallback(userLocale, current);

		// On a real edit of the user-locale value, auto-translate it into the other locales,
		// overwriting their previous (now potentially stale) values. A stale auto-translation is
		// worse than a fresh one. When the translation service is inactive (or a target language is
		// unsupported), the other locales are left unchanged. Explicit, source-language-aware
		// translation is offered by the all-languages editor ({@link I18NStringDialog}).
		boolean edited = !Objects.equals(emptyToNull(text), emptyToNull(previousText));
		boolean autoTranslate = edited && !isEmpty(text) && TranslationService.isActive();
		Translator translator = autoTranslate ? TranslationService.getInstance() : null;
		boolean sourceSupported = translator != null && translator.isSupported(userLocale);

		ResKey.Builder builder = ResKey.builder();
		boolean any = false;
		for (Locale locale : Resources.getInstance().getSupportedLocalesInDisplayOrder()) {
			String value;
			if (locale.getLanguage().equals(userLocale.getLanguage())) {
				value = text;
			} else if (sourceSupported && translator.isSupported(locale)) {
				value = translate(translator, text, userLocale, locale, current);
			} else {
				value = current == null ? null : ResKeyUtil.translateWithoutFallback(locale, current);
			}
			if (!isEmpty(value)) {
				builder.add(locale, value);
				any = true;
			}
		}
		return any ? builder.build() : null;
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
