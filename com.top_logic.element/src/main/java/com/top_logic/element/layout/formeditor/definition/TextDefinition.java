/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.definition;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Binding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.model.form.definition.FormElement;

/**
 * Definition of a {@link FormElement} with a {@link ResKey}. {@link TagName} is "text".
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
@Abstract
public interface TextDefinition extends ConfigurationItem {

	/** Configuration name for the value of the {@link #getLabel()}. */
	String LABEL = "label";

	/**
	 * Returns the label (title) of the group.
	 * 
	 * @return The label of the group.
	 */
	@Name(LABEL)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	@Binding(ResKey.ValueBinding.class)
	ResKey getLabel();

	/**
	 * Sets the label (title) of the group.
	 * 
	 * @param id
	 *        The {@link ResKey} for the label of the group.
	 */
	void setLabel(ResKey id);

}
