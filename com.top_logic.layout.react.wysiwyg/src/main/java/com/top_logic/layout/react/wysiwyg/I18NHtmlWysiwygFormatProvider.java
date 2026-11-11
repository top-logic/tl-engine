/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.wysiwyg;

import com.top_logic.html.i18n.HtmlResKey;
import com.top_logic.layout.configedit.ConfigControlProvider;
import com.top_logic.layout.configedit.ConfigControlService;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.configedit.I18NHtmlFormatProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * {@link ConfigControlProvider} that edits an {@link HtmlResKey} property in the WYSIWYG editor,
 * per language.
 *
 * <p>
 * The {@code impl} for a {@link ConfigControlService.FormatMapping} entry that claims a property
 * whose value provider produces an {@link HtmlResKey}, e.g.
 * {@link com.top_logic.html.i18n.DefaultHtmlResKey.ValueFormat}. The translation in the user's
 * language is edited inline, the other languages in a dialog, as for an internationalized text
 * annotated with {@link com.top_logic.layout.form.values.edit.annotation.Html}, see
 * {@link HtmlConfigProvider}. The HTML source is not offered for editing.
 * </p>
 */
public class I18NHtmlWysiwygFormatProvider implements ConfigControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, ConfigFieldModel model) {
		return HtmlConfigProvider.createI18NEditor(context, model, I18NHtmlFormatProvider.resKeyModel(model), false);
	}

	/**
	 * The WYSIWYG editor takes the whole row.
	 */
	@Override
	public boolean rendersWholeLine(ConfigFieldModel model) {
		return true;
	}

}
