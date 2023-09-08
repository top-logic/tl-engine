/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts;

import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.editor.config.TypeTemplateParameters;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.model.form.definition.AttributeDefinition;
import com.top_logic.model.form.definition.FormElement;
import com.top_logic.model.form.definition.VisibilityConfig;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelPartRef;

/**
 * {@link AttributeDefinition} for an attribute of an given
 * {@link ForeignAttributeDefinition#getItem()}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("attribute-reference")
@DisplayOrder({
	ForeignAttributeDefinition.TYPE,
	ForeignAttributeDefinition.NAME_ATTRIBUTE,
	ForeignAttributeDefinition.ITEM,
	ForeignAttributeDefinition.VISIBILITY,
})
public interface ForeignAttributeDefinition extends FormElement<ForeignAttributeTemplateProvider>,
		AttributeDefinition, VisibilityConfig, TypeTemplateParameters {

	/**
	 * Configuration option of {@link #getItem()}.
	 */
	String ITEM = "item";

	/**
	 * {@link Expr} for an item of the configured {@link #getType() type}.
	 */
	@Name(ITEM)
	@ItemDisplay(ItemDisplayType.VALUE)
	@Mandatory
	Expr getItem();

	@DerivedRef(TYPE)
	@Override
	TLModelPartRef getOwner();

}

