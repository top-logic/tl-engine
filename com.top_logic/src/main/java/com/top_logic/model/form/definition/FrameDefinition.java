/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.definition;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.model.form.implementation.FrameDefinitionTemplateProvider;

/**
 * A definition of a border with custom style and several elements inside. {@link TagName} is
 * "frame".
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
@TagName("frame")
@DisplayOrder(value = { FrameDefinition.BORDER, FrameDefinition.WHOLE_LINE, FrameDefinition.CSS_CLASS })
public interface FrameDefinition extends ContainerDefinition<FrameDefinitionTemplateProvider> {

	/** Configuration name for the value of the {@link #getShowBorder()}. */
	String BORDER = "showBorder";

	/** Configuration name for the value of the {@link #getCssClass()}. */
	String CSS_CLASS = "cssClass";

	/** Configuration name for the value of the {@link #getWholeLine()}. */
	String WHOLE_LINE = "wholeLine";

	/**
	 * Sets whether the border of this element is visible.
	 */
	void setShowBorder(Boolean visible);

	/**
	 * Returns whether the border of this element is visible.
	 */
	@Name(BORDER)
	@BooleanDefault(true)
	Boolean getShowBorder();

	/**
	 * Sets the custom CSS class(es).
	 * 
	 * @param cssClass
	 *        An arbitrary number of CSS classes.
	 */
	void setCssClass(String cssClass);

	/**
	 * Returns the custom CSS classes.
	 */
	@Name(CSS_CLASS)
	String getCssClass();

	/**
	 * Sets whether the container element is rendered over the entire line.
	 */
	void setWholeLine(Boolean wholeLine);

	/**
	 * Returns whether the container element is rendered over the entire line.
	 */
	@Name(WHOLE_LINE)
	@BooleanDefault(true)
	Boolean getWholeLine();
}
