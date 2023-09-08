/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.config;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.editor.DynamicComponentOptionMapping;
import com.top_logic.layout.editor.DynamicComponentOptions;
import com.top_logic.layout.editor.annotation.AvailableTemplates;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Template configuration parameter to provide in app edit of dialogs.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@Abstract
public interface DialogTemplateParameters extends ConfigurationItem {

	/**
	 * Group name used in templates to mark it as a template to create dialogs.
	 */
	public static final String DIALOG_TEMPLATE_GROUP = "dialogs";
	/**
	 * Configuration option for {@link #getDialogs()}.
	 */
	public static final String DIALOGS = "dialogs";

	/**
	 * All dialogs on this component.
	 */
	@Name(DIALOGS)
	@AvailableTemplates(DIALOG_TEMPLATE_GROUP)
	@Options(fun = DynamicComponentOptions.class, mapping = DynamicComponentOptionMapping.class)
	@Hidden // Managed by a separate command for adding dialogs.
	List<LayoutComponent.Config> getDialogs();

}
