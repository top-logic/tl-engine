/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.definition;

import java.util.List;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.layout.formeditor.implementation.TabDefinitionTemplateProvider;
import com.top_logic.element.layout.formeditor.implementation.TabbarDefinitionTemplateProvider;
import com.top_logic.model.form.definition.ContainerDefinition;
import com.top_logic.model.form.definition.FormElement;

/**
 * Configuration of a {@link FormElement} displaying a tabbar.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("tabbar")
public interface TabbarDefinition extends FormElement<TabbarDefinitionTemplateProvider>, WithUUID {

	/**
	 * @see #getTabs()
	 */
	String TABS = "tabs";

	/**
	 * The tabs grouping other {@link FormElement}s.
	 * 
	 * <p>
	 * Only the {@link FormElement}s of a single tab are visible together.
	 * </p>
	 */
	@Name(TABS)
	List<TabDefinition> getTabs();

	/**
	 * Definition of a single tab within a {@link TabbarDefinition}.
	 */
	@TagName("tab")
	public interface TabDefinition extends ContainerDefinition<TabDefinitionTemplateProvider>, TextDefinition {
		// Pure sum interface.
	}

}
