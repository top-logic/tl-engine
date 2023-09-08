/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.config.i18n;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.editor.InternationalizationEditor.WithTooltipConfiguration;

/**
 * Base edit model for an internationalized thing consisting of a name and a description.
 * 
 * @see #getLabel()
 * @see #getDescription()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Internationalized extends ConfigurationItem {

	/**
	 * @see #getLabel()
	 */
	String LABEL = "label";

	/**
	 * @see #getDescription()
	 */
	String DESCRIPTION = "description";

	/**
	 * The internationalized name of the attribute.
	 */
	@Name(LABEL)
	@Nullable
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	@WithTooltipConfiguration(false)
	ResKey getLabel();

	/**
	 * @see #getLabel()
	 */
	void setLabel(ResKey value);

	/**
	 * The internationalized description of the attribute.
	 */
	@Name(DESCRIPTION)
	@Nullable
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	@WithTooltipConfiguration(false)
	@ControlProvider(MultiLineText.class)
	ResKey getDescription();

	/**
	 * @see #getDescription()
	 */
	void setDescription(ResKey value);

}