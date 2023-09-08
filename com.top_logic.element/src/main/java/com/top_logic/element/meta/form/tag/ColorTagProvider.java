/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.form.tag.ColorChooserTag;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * {@link DisplayProvider} creating {@link ColorChooserTag}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ColorTagProvider extends IndirectDisplayProvider {

	/**
	 * Singleton {@link ColorTagProvider} instance.
	 */
	public static final ColorTagProvider INSTANCE = new ColorTagProvider();

	private ColorTagProvider() {
		// Singleton constructor.
	}

	@Override
	public ControlProvider getControlProvider(EditContext editContext) {
		return new ColorChooserTag();
	}

}
