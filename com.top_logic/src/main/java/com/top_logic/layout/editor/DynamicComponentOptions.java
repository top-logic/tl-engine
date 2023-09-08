/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.basic.func.Function0;
import com.top_logic.layout.editor.annotation.AvailableTemplates;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Computes {@link DynamicComponentDefinition} {@link Options}.
 * 
 * <p>
 * A {@link DynamicComponentDefinition} can be chosen, if it
 * {@link DynamicComponentDefinition#getBelongingGroups() belongs to a group} annotated with
 * {@link AvailableTemplates} to the corresponding property.
 * </p>
 *
 * @see DynamicComponentOptionMapping
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DynamicComponentOptions extends Function0<List<DynamicComponentDefinition>> {

	private String[] _templateGroups;

	/**
	 * Creates a {@link DynamicComponentOptions}.
	 */
	@CalledByReflection
	public DynamicComponentOptions(DeclarativeFormOptions options) {
		AnnotationCustomizations customizations = options.getCustomizations();
		AvailableTemplates annotation = customizations.getAnnotation(options.getProperty(), AvailableTemplates.class);
		_templateGroups = annotation == null ? null : annotation.value();
	}

	@Override
	public List<DynamicComponentDefinition> apply() {
		Predicate<? super DynamicComponentDefinition> predicate =
			_templateGroups == null ? x -> true : componentDefinition -> {
				Collection<String> groups = componentDefinition.getBelongingGroups();
				for (Object arg : _templateGroups) {
					if (groups.contains(arg)) {
						return true;
					}
			}
				return false;
			};

		return DynamicComponentService.getInstance().getComponentDefinitions()
			.stream()
			.filter(predicate)
			.collect(Collectors.toList());
	}

}
