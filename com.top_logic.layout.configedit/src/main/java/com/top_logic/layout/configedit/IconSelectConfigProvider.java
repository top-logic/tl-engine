/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactIconSelectControl;

/**
 * {@link ConfigControlProvider} that edits a {@link ThemeImage} property the way an
 * <i>icon</i> model attribute is edited: by picking from the icons the theme offers.
 *
 * <p>
 * The {@code impl} for the {@link ConfigControlService.ProviderMapping} entry claiming
 * {@link ThemeImage}. Without that claim such a property reaches the built-in fallback, which can
 * only offer the icon's <em>encoded</em> form as text - a string like {@code css:fas fa-edit} that
 * one has to know by heart to type. The same value in a model is picked from a list; a configured
 * one should be no different.
 * </p>
 *
 * <p>
 * Claiming the property is also what gives this control the value it needs. The claim makes
 * {@link ConfigControlService#createModel(com.top_logic.basic.config.ConfigurationItem,
 * com.top_logic.basic.config.PropertyDescriptor) createModel} hand out the typed
 * {@link ConfigFieldModel} over the {@link ThemeImage} itself rather than the
 * {@link ConfigFormatFieldModel} over its encoded text - and a {@link ThemeImage} is exactly what
 * {@link ReactIconSelectControl} reads and writes, encoding it for the client and decoding what
 * comes back.
 * </p>
 */
public class IconSelectConfigProvider implements ConfigControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, ConfigFieldModel model) {
		return new ReactIconSelectControl(context, model);
	}

}
