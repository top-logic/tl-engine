/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.misc.EmptyCollection;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.editor.GroupInlineControlProvider;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Reference to a typed layout template.
 * 
 * <p>
 * This configuration is not instantiated, but the {@link LayoutComponent} instantiated by the
 * template with the given arguments is used.
 * </p>
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
@TagName(LayoutTemplate.TAG_NAME)
@DisplayOrder(value = { LayoutTemplate.TEMPLATE_ARGUMENTS })
@DisplayInherited(DisplayStrategy.IGNORE)
public interface LayoutTemplate extends LayoutComponent.Config {

	/**
	 * Property name for {@link #getTemplateLabel()}
	 */
	public static final String TEMPLATE_LABEL = "template-label";

	/**
	 * Property name for {@link #getTemplateArguments()}
	 */
	public static final String TEMPLATE_ARGUMENTS = "template-arguments";

	/**
	 * Property name for {@link #getTemplatePath()}
	 */
	public static final String TEMPLATE_PATH = "template-path";

	/**
	 * Tag to use a {@link LayoutTemplate}.
	 */
	String TAG_NAME = "layout-template";

	/**
	 * Layout template path relative to the root layout directory
	 * {@link ModuleLayoutConstants#LAYOUT_RESOURCE_PREFIX}.
	 */
	@Name(TEMPLATE_PATH)
	@Hidden()
	@StringDefault("com.top_logic/placeholder.template.xml")
	String getTemplatePath();

	/**
	 * @see #getTemplatePath()
	 */
	void setTemplatePath(String path);

	/**
	 * @return Layout template label derived from the {@link #getTemplateLabel()}.
	 * 
	 * @see LayoutTemplateLabelProvider
	 */
	@Name(TEMPLATE_LABEL)
	@Derived(fun = LayoutTemplateLabelProvider.class, args = { @Ref(TEMPLATE_PATH) })
	ResKey getTemplateLabel();

	/**
	 * Typed layout template arguments.
	 */
	@Subtypes(adjust = false, value = {})
	@Name(TEMPLATE_ARGUMENTS)
	@ControlProvider(GroupInlineControlProvider.class)
	@Options(fun = EmptyCollection.class)
	ConfigurationItem getTemplateArguments();

	/**
	 * @see #getTemplateArguments()
	 */
	void setTemplateArguments(ConfigurationItem arguments);
}
