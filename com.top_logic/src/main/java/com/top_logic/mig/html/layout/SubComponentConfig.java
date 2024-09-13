/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.editor.DynamicComponentDefinition;
import com.top_logic.layout.editor.DynamicComponentOptionMapping;
import com.top_logic.layout.editor.DynamicComponentOptions;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Base configuration for an aggregation of component configs.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface SubComponentConfig extends ConfigurationItem {

	/**
	 * @see #getComponents()
	 */
	String COMPONENTS = "components";

	/**
	 * Sub-component configurations.
	 * 
	 * @see SubComponentConfig#getComponents()
	 * @see LayoutComponent.Config#getDialogs()
	 * @see MainLayout#COMPONENT_DESCRIPTORS
	 */
	@Name(COMPONENTS)
	@EntryTag(LayoutComponent.Config.COMPONENT)
	@Key(LayoutComponent.Config.NAME)
	@Options(fun = DynamicComponentOptions.class, mapping = DynamicComponentOptionMapping.class)
	@OptionLabels(DynamicComponentDefinition.Labels.class)

	@DefaultContainer
	List<LayoutComponent.Config> getComponents();

}
