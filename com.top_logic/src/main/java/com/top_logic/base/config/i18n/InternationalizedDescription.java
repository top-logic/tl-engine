/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.config.i18n;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.i18n.I18NMultiline;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.editor.InternationalizationEditor.WithTooltipConfiguration;

/**
 * Internationalized description.
 */
public interface InternationalizedDescription extends ConfigurationItem {

	/**
	 * @see #getDescription()
	 */
	String DESCRIPTION = "description";

	/**
	 * The internationalized description.
	 */
	@Name(DESCRIPTION)
	@Nullable
	@WithTooltipConfiguration(false)
	@ControlProvider(I18NMultiline.class)
	ResKey getDescription();

	/**
	 * @see #getDescription()
	 */
	void setDescription(ResKey value);

}
