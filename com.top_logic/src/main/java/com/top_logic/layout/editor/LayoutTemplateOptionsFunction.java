/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.func.Function0;
import com.top_logic.layout.editor.annotation.AvailableTemplates;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Computes {@link LayoutTemplate} {@link Options}.
 * 
 * <p>
 * A {@link LayoutTemplate} can be chosen, if it
 * {@link DynamicComponentDefinition#getBelongingGroups() belongs to a group} annotated with
 * {@link AvailableTemplates} to the corresponding property.
 * </p>
 * 
 * @see DynamicComponentOptions
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class LayoutTemplateOptionsFunction extends Function0<List<LayoutTemplate>> {

	private DynamicComponentOptions _delegate;

	/**
	 * Creates a {@link LayoutTemplateOptionsFunction}.
	 */
	@CalledByReflection
	public LayoutTemplateOptionsFunction(DeclarativeFormOptions options) {
		_delegate = new DynamicComponentOptions(options);
	}

	@Override
	public List<LayoutTemplate> apply() {
		return _delegate.apply()
			.stream()
			.map(DynamicComponentDefinition::createTemplate)
			.collect(Collectors.toList());
	}

}
