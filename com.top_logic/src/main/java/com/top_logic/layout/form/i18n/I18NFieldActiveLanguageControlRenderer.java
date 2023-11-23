/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.i18n;

import java.io.IOException;
import java.util.Locale;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.CompositeControl;
import com.top_logic.layout.DefaultControlRenderer;
import com.top_logic.layout.DisplayContext;

/**
 * Control renderer for {@link I18NField} fields that renders only the value of the current
 * {@link Locale}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class I18NFieldActiveLanguageControlRenderer extends DefaultControlRenderer<CompositeControl> {

	@Override
	protected String getControlTag(CompositeControl control) {
		return SPAN;
	}

	@Override
	protected void writeControlContents(DisplayContext context, TagWriter out, CompositeControl control)
			throws IOException {
		I18NField<?, ?, ?> i18nField = (I18NField<?, ?, ?>) control.getModel();
		writeFieldValue(context, out, i18nField);
	}

	/**
	 * Writes the value of the given {@link I18NField}.
	 */
	protected abstract void writeFieldValue(DisplayContext context, TagWriter out, I18NField<?, ?, ?> i18nField)
			throws IOException;

}
