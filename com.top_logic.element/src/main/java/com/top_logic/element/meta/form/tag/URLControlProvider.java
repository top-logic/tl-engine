/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.control.DefaultSimpleCompositeControlRenderer;
import com.top_logic.layout.form.control.GotoLinkControl;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * {@link ControlProvider} for {@link FormField}s containing {@link String} values representing an
 * URL.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class URLControlProvider implements ControlProvider {

	private static final String URL_WITH_ICON_CSS_CLASS = "urlWithIcon";

	@Override
	public Control createControl(Object model, String style) {
		BlockControl blockControl = new BlockControl();

		blockControl.addChild(new TextInputControl((FormField) model));
		blockControl.addChild(new GotoLinkControl((FormField) model));

		blockControl.setRenderer(DefaultSimpleCompositeControlRenderer.spanWithCSSClass(URL_WITH_ICON_CSS_CLASS));

		return blockControl;
	}
}
