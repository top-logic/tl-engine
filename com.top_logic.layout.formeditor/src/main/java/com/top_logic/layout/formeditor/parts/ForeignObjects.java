/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.model.form.definition.FormElement;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link FormElement} to display a form for each of a list of foreign objects.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	ForeignObjects.TYPE,
	ForeignObjects.ITEMS,
	ForeignObjects.LABEL,
	ForeignObjects.READ_ONLY,
	ForeignObjects.NO_SEPARATE_GROUP,
	ForeignObjects.LAYOUT,
	ForeignObjects.BUTTONS,
})
@TagName("foreign-objects")
public interface ForeignObjects extends FormElement<ForeignObjectsTemplateProvider>, InlineDisplayedObjectsConfig {

	/** Configuration name for {@link #getItems()}. */
	String ITEMS = "items";

	/**
	 * Expression that computes the objects to display. The single input element is the base object.
	 */
	@Name(ITEMS)
	@ItemDisplay(ItemDisplayType.VALUE)
	@Mandatory
	@Label("Objects")
	Expr getItems();

}

