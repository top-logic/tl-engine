/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.Locale;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyUtil;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * A {@link ReactFormFieldControl} for {@code I18NString} attributes (whose value is a multi-locale
 * {@link ResKey}).
 *
 * <p>
 * A single inline text field is shown in the <b>current user's locale</b>, in both view and edit
 * mode. On input the typed string is folded into a {@link ResKey} that <b>preserves the values of
 * the other locales</b>, so the model receives a typed {@link ResKey} (a plain {@link String} would
 * be rejected by the I18N storage mapping). The all-languages editor and auto-translation are added
 * in follow-up increments.
 * </p>
 */
public class ReactI18NStringInputControl extends ReactFormFieldControl {

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
		// The base constructor seeded the raw ResKey; re-emit the current-locale string.
		putState(VALUE, localValue(model.getValue()));
	}

	@Override
	protected void handleModelValueChanged(FieldModel source, Object oldValue, Object newValue) {
		putState(VALUE, localValue(newValue));
	}

	@Override
	protected Object parseClientValue(Object rawValue) {
		String text = rawValue == null ? null : rawValue.toString();
		Locale userLocale = TLContext.getLocale();
		ResKey current = asResKey(getFieldModel().getValue());

		ResKey.Builder builder = ResKey.builder();
		boolean any = false;
		for (Locale locale : Resources.getInstance().getSupportedLocalesInDisplayOrder()) {
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
		// An I18NString with no value at all is null.
		return any ? builder.build() : null;
	}

	/**
	 * The string in the current user's locale (without fallback), or {@code null}.
	 */
	private static Object localValue(Object value) {
		ResKey resKey = asResKey(value);
		if (resKey == null) {
			return null;
		}
		return ResKeyUtil.translateWithoutFallback(TLContext.getLocale(), resKey);
	}

	private static ResKey asResKey(Object value) {
		return value instanceof ResKey ? (ResKey) value : null;
	}

}
