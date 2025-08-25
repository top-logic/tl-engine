/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.definition;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.element.layout.formeditor.implementation.GroupDefinitionTemplateProvider;
import com.top_logic.model.annotate.ModeSelector;
import com.top_logic.model.form.definition.ContainerDefinition;

/**
 * Definition of a form group.
 * 
 * <p>
 * A {@link GroupDefinition} defines visual properties of the group and the {@link #getContent()
 * group contents}.
 * </p>
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
@TagName("group")
@DisplayOrder({
	TextDefinition.LABEL,
	ContainerDefinition.COLUMNS,
	ContainerDefinition.LABEL_PLACEMENT,
	GroupProperties.COLLAPSIBLE,
	GroupProperties.INITIALLY_OPENED,
	GroupProperties.SHOW_BORDER,
	GroupProperties.SHOW_TITLE,
	GroupProperties.WHOLE_LINE,
	GroupDefinition.DYNAMIC_VISIBILITY
})
@DisplayInherited(DisplayStrategy.IGNORE)
public interface GroupDefinition
		extends GroupProperties<GroupDefinitionTemplateProvider>, ContainerDefinition<GroupDefinitionTemplateProvider> {

	/** Configuration name for the value of the {@link #getModeSelector()}. */
	String DYNAMIC_VISIBILITY = "dynamicVisibility";

	/**
	 * Algorithm to compute the visibility of the group.
	 */
	@Name(DYNAMIC_VISIBILITY)
	PolymorphicConfiguration<ModeSelector> getModeSelector();

	/**
	 * @see #getModeSelector()
	 */
	void setModeSelector(PolymorphicConfiguration<ModeSelector> modeSelector);
}