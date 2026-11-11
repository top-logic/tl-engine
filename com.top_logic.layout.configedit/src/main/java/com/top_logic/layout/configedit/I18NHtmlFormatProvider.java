/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.i18n.DefaultHtmlResKey;
import com.top_logic.html.i18n.HtmlResKey;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.control.form.MappedFieldModel;

/**
 * {@link I18NStringFormatProvider} that edits an {@link HtmlResKey} property: an internationalized
 * text whose translations are HTML.
 *
 * <p>
 * The {@code impl} for a {@link ConfigControlService.FormatMapping} entry that claims a property
 * whose value provider produces an {@link HtmlResKey}, e.g.
 * {@link com.top_logic.html.i18n.DefaultHtmlResKey.ValueFormat}. The source of each translation is
 * edited in a multi-line input, and the text is displayed formatted while it cannot be edited - as
 * a {@link ResKey} property annotated with {@link com.top_logic.layout.form.values.edit.annotation.Html}.
 * </p>
 */
public class I18NHtmlFormatProvider extends I18NStringFormatProvider {

	/**
	 * Creates a {@link I18NHtmlFormatProvider}.
	 */
	@CalledByReflection
	public I18NHtmlFormatProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected FieldModel i18nModel(ConfigFieldModel model) {
		return resKeyModel(model);
	}

	/**
	 * The translations of an {@link HtmlResKey} are HTML.
	 */
	@Override
	protected boolean isHtml(ConfigFieldModel model) {
		return true;
	}

	/**
	 * A {@link FieldModel} presenting the {@link HtmlResKey} value of the given model as the
	 * {@link ResKey} of its translations.
	 *
	 * <p>
	 * An editor of an internationalized text edits such a model. A value without translations is
	 * <code>null</code> in both forms.
	 * </p>
	 *
	 * @param model
	 *        The model whose value is an {@link HtmlResKey} or <code>null</code>.
	 * @return A model whose value is a {@link ResKey} or <code>null</code>.
	 */
	public static FieldModel resKeyModel(FieldModel model) {
		return new MappedFieldModel(model, I18NHtmlFormatProvider::toResKey, I18NHtmlFormatProvider::toHtmlResKey);
	}

	private static Object toResKey(Object value) {
		return value instanceof DefaultHtmlResKey htmlKey ? htmlKey.content() : null;
	}

	private static Object toHtmlResKey(Object value) {
		return value instanceof ResKey key ? new DefaultHtmlResKey(key) : null;
	}

}
