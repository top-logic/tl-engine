/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.definition;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.func.And;
import com.top_logic.element.layout.meta.ActiveIf;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.model.form.definition.ContainerProperties;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;

/**
 * Configuration options of a {@link GroupDefinition}.
 *
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
@Abstract
public interface GroupProperties<T extends FormElementTemplateProvider> extends TextDefinition, ContainerProperties<T> {

	/** Configuration name for the value of the {@link #getCssClass()}. */
	String CSS_CLASS = "cssClass";

	/** Configuration name for the value of the {@link #getStyle()}. */
	String STYLE = "style";

	/** Configuration name for the value of the {@link #getWidth()}. */
	String WIDTH = "width";

	/** Configuration name for the value of the {@link #getCollapsible()}. */
	String COLLAPSIBLE = "collapsible";

	/** Configuration name for the value of the {@link #getCollapsibleValue()}. */
	String COLLAPSIBLE_VALUE = "collapsibleValue";

	/** Configuration name for the value of the {@link #getShowBorder()}. */
	String SHOW_BORDER = "showBorder";

	/** Configuration name for the value of the {@link #getShowTitle()}. */
	String SHOW_TITLE = "showTitle";

	/** Configuration name for the value of the {@link #getWholeLine()}. */
	String WHOLE_LINE = "wholeLine";

	/** Configuration name for the value of the {@link #getInitiallyOpened()}. */
	String INITIALLY_OPENED = "initiallyOpened";

	/**
	 * The CSS class(es) of the group.
	 */
	@Hidden
	@Name(CSS_CLASS)
	String getCssClass();

	/**
	 * @see #getCssClass()
	 */
	void setCssClass(String cssClass);

	/**
	 * The CSS style of the group.
	 */
	@Hidden
	@Name(STYLE)
	String getStyle();

	/**
	 * @see #getStyle()
	 */
	void setStyle(String style);

	/**
	 * The width of the group in CSS units.
	 */
	@Hidden
	@Name(WIDTH)
	String getWidth();

	/**
	 * @see #getWidth()
	 */
	void setWidth(String width);

	/**
	 * Whether the group can be collapsed using a toggle button.
	 * 
	 * @see #getCollapsibleValue() To access the actual value, a derived property is implemented
	 *      that also considers the value of {@link #getShowTitle()}. A group without title cannot
	 *      be collapsed.
	 */
	@Name(COLLAPSIBLE)
	@BooleanDefault(true)
	@DynamicMode(fun = ActiveIf.class, args = @Ref(SHOW_TITLE))
	boolean getCollapsible();

	/**
	 * @see #getCollapsible()
	 */
	void setCollapsible(boolean collapsible);

	/**
	 * Whether this group can finally be collapsed.
	 */
	@Name(COLLAPSIBLE_VALUE)
	@BooleanDefault(true)
	@Hidden
	@Derived(fun = And.class, args = { @Ref(SHOW_TITLE), @Ref(COLLAPSIBLE) })
	boolean getCollapsibleValue();

	/**
	 * Whether the groups border is visible.
	 */
	@Name(SHOW_BORDER)
	@BooleanDefault(true)
	@DynamicMode(fun = ActiveIf.class, args = @Ref(SHOW_TITLE))
	boolean getShowBorder();

	/**
	 * @see #getShowBorder()
	 */
	void setShowBorder(boolean showBorder);

	/**
	 * Whether the groups title is visible.
	 */
	@Name(SHOW_TITLE)
	@BooleanDefault(true)
	@DynamicMode(fun = ActiveIf.class, args = @Ref(SHOW_BORDER))
	boolean getShowTitle();

	/**
	 * @see #getShowTitle()
	 */
	void setShowTitle(boolean showTitle);

	/**
	 * Whether the group is rendered over the entire page width.
	 */
	@Name(WHOLE_LINE)
	@BooleanDefault(true)
	boolean getWholeLine();

	/**
	 * @see #getWholeLine()
	 */
	void setWholeLine(boolean wholeLine);

	/**
	 * Whether the group is opened during first rendering.
	 * 
	 * @see #getCollapsible()
	 */
	@Name(INITIALLY_OPENED)
	@BooleanDefault(true)
	@DynamicMode(fun = ActiveIf.class, args = @Ref(COLLAPSIBLE))
	boolean getInitiallyOpened();

	/**
	 * @see #getInitiallyOpened()
	 */
	void setInitiallyOpened(boolean initiallyOpened);

}
