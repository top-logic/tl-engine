/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts;

import java.util.List;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.element.layout.formeditor.FormTypeProperty;
import com.top_logic.layout.editor.config.ButtonTemplateParameters;
import com.top_logic.layout.editor.config.TypeTemplateParameters;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.definition.FormElement;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandler.ConfigBase;

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
	ForeignObjects.LAYOUT,
	ForeignObjects.BUTTONS,
})
@TagName("foreign-objects")
public interface ForeignObjects
		extends FormElement<ForeignObjectsTemplateProvider>, TypeTemplateParameters, ButtonTemplateParameters {

	/** Configuration name for {@link #getItems()}. */
	String ITEMS = "items";

	/** Configuration name for {@link #getLayout()}. */
	String LAYOUT = "layout";

	/** Configuration name for {@link #getLayout()}. */
	String LABEL = "label";

	/** Configuration name for {@link #getLayout()}. */
	String READ_ONLY = "read-only";

	/**
	 * The items to display forms for.
	 */
	@Name(ITEMS)
	@ItemDisplay(ItemDisplayType.VALUE)
	@Mandatory
	Expr getItems();

	/**
	 * Expression that is used to compute the label of the single form groups, from the base object
	 * of the form group.
	 */
	@Name(LABEL)
	Expr getLabel();

	/**
	 * Definition of the layout that is used to display the single objects.
	 */
	@Name(LAYOUT)
	@FormTypeProperty(TYPE)
	@ItemDisplay(ItemDisplayType.VALUE)
	FormDefinition getLayout();

	/**
	 * Expression that is used for each of the items to compute, whether the form elements must be
	 * displayed read-only instead of editable.
	 * 
	 * <p>
	 * The first argument for the evaluation is the displayed item, the second is the base object,
	 * e.g. the expression 'true' displays all attributes read-only.
	 * </p>
	 * 
	 * <p>
	 * When nothing is configured, no attribute is explicitly set to read-only.
	 * </p>
	 */
	@Name(READ_ONLY)
	@Label("Display item attributes read-only")
	Expr getReadOnly();

	/**
	 * Commands that can be executed on the individual objects.
	 * 
	 * @see com.top_logic.layout.editor.config.ButtonTemplateParameters#getButtons()
	 */
	@Override
	List<ConfigBase<? extends CommandHandler>> getButtons();
}

