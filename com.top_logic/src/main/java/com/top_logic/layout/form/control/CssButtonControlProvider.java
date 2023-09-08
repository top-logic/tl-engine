/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import com.top_logic.layout.Control;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * {@link ControlProvider} creating {@link ButtonControl}s with a certain CSS class.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class CssButtonControlProvider implements ControlProvider {

	private final AbstractButtonRenderer<?> _renderer;

	/**
	 * Creates a {@link CssButtonControlProvider}.
	 * 
	 * @param cssClass
	 *        CSS class to use for the created buttons.
	 */
	public CssButtonControlProvider(String cssClass) {
		_renderer = ImageButtonRenderer.newSystemButtonRenderer(cssClass);
	}

	@Override
	public Control createControl(Object model, String style) {
		if (model == null) {
			return null;
		}
		return new ButtonControl((CommandModel) model, _renderer);
	}
}