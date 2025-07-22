/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts;

import java.util.List;

import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.ValueInitializer;
import com.top_logic.basic.config.format.JavaIdentifier;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.meta.HideActiveIf;
import com.top_logic.layout.editor.config.ButtonTemplateParameters;
import com.top_logic.layout.editor.config.TypeTemplateParameters;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.initializer.UUIDInitializer;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.definition.FormElement;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandler.ConfigBase;

/**
 * {@link FormElement} to display a form for each of a list of foreign objects.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	ForeignObjects.TITLE,
	ForeignObjects.TYPE,
	ForeignObjects.ITEMS,
	ForeignObjects.LABEL,
	ForeignObjects.READ_ONLY,
	ForeignObjects.NO_SEPARATE_GROUP,
	ForeignObjects.INITIALLY_COLLAPSED,
	ForeignObjects.LAYOUT,
	ForeignObjects.BUTTONS,
})
@TagName("foreign-objects")
public interface ForeignObjects
		extends FormElement<ForeignObjectsTemplateProvider>, TypeTemplateParameters, ButtonTemplateParameters {

	/** Configuration name for {@link #getId()}. */
	String ID_PROPERTY = "id";

	/** Configuration name for {@link #getItems()}. */
	String ITEMS = "items";

	/** Configuration name for {@link #getLayout()}. */
	String LAYOUT = "layout";

	/** Configuration name for {@link #getLabel()}. */
	String LABEL = "label";

	/** Configuration name for {@link #getTitle()}. */
	String TITLE = "title";

	/** Configuration name for {@link #getReadOnly()}. */
	String READ_ONLY = "read-only";

	/** Configuration name for {@link #isNoSeparateGroup()}. */
	String NO_SEPARATE_GROUP = "no-separate-group";

	/**
	 * @see #initiallyCollapsed()
	 */
	String INITIALLY_COLLAPSED = "initially-collapsed";

	/**
	 * ID to make form context contents stable.
	 */
	@Name(ID_PROPERTY)
	@Format(JavaIdentifier.class)
	@ValueInitializer(UUIDInitializer.class)
	@Hidden
	@Nullable // For legacy compatibility.
	String getId();

	/**
	 * The items to display forms for.
	 */
	@Name(ITEMS)
	@ItemDisplay(ItemDisplayType.VALUE)
	@Mandatory
	Expr getItems();

	/**
	 * Title of group surrounding all object groups.
	 * 
	 * <p>
	 * If not given, content groups are not surrounded by an extra group.
	 * </p>
	 */
	@DynamicMode(fun = HideActiveIf.class, args = @Ref(NO_SEPARATE_GROUP))
	@Name(TITLE)
	ResKey getTitle();

	/**
	 * Expression that is used to compute the label of the single form groups, from the base object
	 * of the form group.
	 */
	@Name(LABEL)
	@DynamicMode(fun = HideActiveIf.class, args = @Ref(NO_SEPARATE_GROUP))
	Expr getLabel();

	/**
	 * Definition of the layout that is used to display the single objects.
	 */
	@Name(LAYOUT)
	@ItemDisplay(ItemDisplayType.VALUE)
	FormDefinition getLayout();

	@Override
	@DerivedRef(TYPE)
	TLModelPartRef getFormContextType();

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
	 * Whether fields of objects should be directly embedded into the rest of the form without any
	 * visual grouping.
	 * 
	 * <p>
	 * This option is most likely only appropriate, if at most a single object is being displayed.
	 * This option then gives the impression that the fields of that object are fields of the
	 * surrounding object defining the rest of the form.
	 * </p>
	 */
	@Label("Embedd contents")
	@Name(NO_SEPARATE_GROUP)
	boolean isNoSeparateGroup();

	/**
	 * Whether to collapse all object groups during initial display.
	 * 
	 * <p>
	 * Has no meaning, if {@link #isNoSeparateGroup()} is active.
	 * </p>
	 */
	@DynamicMode(fun = HideActiveIf.class, args = @Ref(NO_SEPARATE_GROUP))
	@Name(INITIALLY_COLLAPSED)
	boolean initiallyCollapsed();
}

