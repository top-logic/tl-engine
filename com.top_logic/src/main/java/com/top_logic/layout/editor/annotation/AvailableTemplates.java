/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.top_logic.basic.config.CommaSeparatedStringArray;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.editor.DynamicComponentDefinition;
import com.top_logic.layout.editor.DynamicComponentOptions;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Annotates {@link DynamicComponentDefinition#getBelongingGroups() group names} of layout templates
 * that can be chosen for a property that has {@link Options} computed by
 * {@link DynamicComponentOptions}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@TagName("available-templates")
public @interface AvailableTemplates {

	/**
	 * Group names of layout templates that can be chosen.
	 */
	@Format(CommaSeparatedStringArray.class)
	String[] value();
}
