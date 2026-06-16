/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyUtil;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * A {@link ReactFormFieldControl} for {@code I18NString} attributes (whose value is a multi-locale
 * {@link ResKey}).
 *
 * <p>
 * A single inline text field is shown in the <b>current user's locale</b> (both view and edit
 * mode). When that locale has no value, the value of a fallback locale is shown - as the value in
 * view mode and as the input placeholder in edit mode. In edit mode an icon-button opens an
 * all-languages popup where each supported language can be edited explicitly. All inputs are folded
 * into a {@link ResKey} (a plain {@link String} would be rejected by the I18N storage mapping).
 * </p>
 *
 * @implNote Auto-translation on inline edit is a separate follow-up increment.
 */
public class ReactI18NStringInputControl extends ReactFormFieldControl {

	/** Fallback string (best non-current-locale value) for view display / input placeholder. */
	private static final String FALLBACK = "fallback";

	/** Per-locale descriptors for the all-languages popup: {@code [{language, label, value}]}. */
	private static final String LOCALES = "locales";

	private static final String LANGUAGE = "language";

	private static final String LABEL = "label";

	private static final String VALUES = "values";

	/**
	 * Creates a new {@link ReactI18NStringInputControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model (value is a {@link ResKey} or {@code null}).
	 */
	public ReactI18NStringInputControl(ReactContext context, FieldModel model) {
		super(context, model, "TLI18NStringInput");
		emitState(model.getValue());
	}

	@Override
	protected void handleModelValueChanged(FieldModel source, Object oldValue, Object newValue) {
		emitState(newValue);
	}

	private void emitState(Object value) {
		ResKey resKey = asResKey(value);
		putState(VALUE, localValue(resKey));
		putState(FALLBACK, fallback(resKey));
		putState(LOCALES, locales(resKey));
	}

	/**
	 * Inline edit of the current user's locale - set that locale, preserve the other locales.
	 */
	@Override
	protected Object parseClientValue(Object rawValue) {
		String text = rawValue == null ? null : rawValue.toString();
		Locale userLocale = TLContext.getLocale();
		ResKey current = asResKey(getFieldModel().getValue());

		ResKey.Builder builder = ResKey.builder();
		boolean any = false;
		for (Locale locale : supportedLocales()) {
			String value;
			if (locale.getLanguage().equals(userLocale.getLanguage())) {
				value = text;
			} else {
				value = current == null ? null : ResKeyUtil.translateWithoutFallback(locale, current);
			}
			if (value != null && !value.isEmpty()) {
				builder.add(locale, value);
				any = true;
			}
		}
		return any ? builder.build() : null;
	}

	/**
	 * Explicit per-language edit from the all-languages popup: rebuild the {@link ResKey} from the
	 * full language-&gt;text map.
	 */
	@ReactCommand("applyTranslations")
	void handleApplyTranslations(Map<String, Object> arguments) {
		@SuppressWarnings("unchecked")
		Map<String, Object> values = (Map<String, Object>) arguments.get(VALUES);
		if (values == null) {
			return;
		}
		ResKey.Builder builder = ResKey.builder();
		boolean any = false;
		for (Locale locale : supportedLocales()) {
			Object raw = values.get(locale.getLanguage());
			String value = raw == null ? null : raw.toString();
			if (value != null && !value.isEmpty()) {
				builder.add(locale, value);
				any = true;
			}
		}
		getFieldModel().setValue(any ? builder.build() : null);
	}

	/** The string in the current user's locale (without fallback), or {@code null}. */
	private static Object localValue(ResKey resKey) {
		if (resKey == null) {
			return null;
		}
		return ResKeyUtil.translateWithoutFallback(TLContext.getLocale(), resKey);
	}

	/** Best value from a non-current locale (default locale first), for view display / placeholder. */
	private static Object fallback(ResKey resKey) {
		if (resKey == null) {
			return null;
		}
		Locale userLocale = TLContext.getLocale();
		Locale defaultLocale = ResourcesModule.getInstance().getDefaultLocale();
		String value = ResKeyUtil.translateWithoutFallback(defaultLocale, resKey);
		if (value != null && !value.isEmpty() && !defaultLocale.getLanguage().equals(userLocale.getLanguage())) {
			return value;
		}
		for (Locale locale : supportedLocales()) {
			if (locale.getLanguage().equals(userLocale.getLanguage())) {
				continue;
			}
			value = ResKeyUtil.translateWithoutFallback(locale, resKey);
			if (value != null && !value.isEmpty()) {
				return value;
			}
		}
		return null;
	}

	/** Per-locale descriptors for the popup. */
	private static List<Map<String, Object>> locales(ResKey resKey) {
		Locale displayLocale = TLContext.getLocale();
		List<Map<String, Object>> result = new ArrayList<>();
		for (Locale locale : supportedLocales()) {
			Map<String, Object> entry = new HashMap<>();
			entry.put(LANGUAGE, locale.getLanguage());
			entry.put(LABEL, locale.getDisplayLanguage(displayLocale));
			entry.put(VALUE, resKey == null ? null : ResKeyUtil.translateWithoutFallback(locale, resKey));
			result.add(entry);
		}
		return result;
	}

	private static List<Locale> supportedLocales() {
		return Resources.getInstance().getSupportedLocalesInDisplayOrder();
	}

	private static ResKey asResKey(Object value) {
		return value instanceof ResKey ? (ResKey) value : null;
	}

}
