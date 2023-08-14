/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.element.layout.formeditor.FormTypeProperty;
import com.top_logic.element.layout.formeditor.builder.TypedFormDefinition;
import com.top_logic.element.layout.meta.HideActiveIf;
import com.top_logic.layout.editor.config.ButtonTemplateParameters;
import com.top_logic.layout.editor.config.TypeTemplateParameters;
import com.top_logic.layout.form.values.edit.annotation.CollapseEntries;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandler.ConfigBase;

/**
 * Configuration to display a collection of objects inline within another form.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface InlineDisplayedObjectsConfig extends TypeTemplateParameters, ButtonTemplateParameters {

	/** Configuration name for {@link #getLayout()}. */
	String LAYOUT = "layout";

	/** Configuration name for {@link #getLayoutSpecializations()}. */
	String LAYOUT_SPECIALIZATIONS = "layout-specializations";

	/** Configuration name for {@link #getLabel()}. */
	String LABEL = "label";

	/** Configuration name for {@link #getReadOnly()}. */
	String READ_ONLY = "read-only";

	/** Configuration name for {@link #isNoSeparateGroup()}. */
	String NO_SEPARATE_GROUP = "no-separate-group";

	/**
	 * Expression that computes the label from the base object. It may also compute the object which
	 * is used to display in the group title.
	 */
	@Name(LABEL)
	@DynamicMode(fun = HideActiveIf.class, args = @Ref(NO_SEPARATE_GROUP))
	Expr getLabel();

	/**
	 * Definition of the layout that is used to display the single objects.
	 */
	@Name(LAYOUT)
	@FormTypeProperty(TYPE)
	@ItemDisplay(ItemDisplayType.VALUE)
	FormDefinition getLayout();

	/**
	 * Specialized form definitions.
	 * 
	 * <p>
	 * {@link #getLayoutSpecializations()} contains specialized forms for more concrete types. For a
	 * displayed object, the form that best fits the type of the object is selected.
	 * </p>
	 * 
	 * <p>
	 * If no specialized forms are configured, {@link #getLayout()} is used.
	 * </p>
	 */
	@Name(LAYOUT_SPECIALIZATIONS)
	@Key(TypedFormDefinition.TYPE)
	@CollapseEntries
	Map<TLModelPartRef, TypedFormDefinition> getLayoutSpecializations();

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

	/**
	 * Defines that no separate groups should be displayed for the individual objects. In this case
	 * the elements are displayed inline in the outer form.
	 */
	@Name(NO_SEPARATE_GROUP)
	boolean isNoSeparateGroup();

}
