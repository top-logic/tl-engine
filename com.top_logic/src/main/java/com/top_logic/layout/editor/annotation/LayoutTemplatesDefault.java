/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.layout.editor.DynamicComponentDefinition;
import com.top_logic.layout.editor.DynamicComponentService;
import com.top_logic.layout.editor.LayoutTemplate;

/**
 * {@link DefaultValueProvider} creating a list of {@link LayoutTemplate}s with default arguments
 * for {@link PropertyDescriptor}s of type
 * {@link com.top_logic.mig.html.layout.LayoutComponent.Config}.
 * 
 * <p>
 * The template names of the templates to instantiate are annotated to the
 * {@link PropertyDescriptor} with the {@link LayoutTemplates} annotation.
 * </p>
 *
 * @see LayoutTemplateDefault
 * @see LayoutTemplates
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LayoutTemplatesDefault extends DefaultValueProvider {

	@Override
	public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
		PropertyDescriptor property = descriptor.getProperty(propertyName);
		LayoutTemplates layoutTemplates = property.getAnnotation(LayoutTemplates.class);
		if (layoutTemplates == null) {
			Logger.error("A " + LayoutTemplatesDefault.class.getSimpleName()
				+ " provider requires arguments annotated through the '" + LayoutTemplates.class.getSimpleName()
				+ "' annotation, ignoring default.", LayoutTemplatesDefault.class);
			return Collections.emptyList();
		}
		
		DynamicComponentService service = DynamicComponentService.getInstance();
		List<LayoutTemplate> result = new ArrayList<>();
		for (String template : layoutTemplates.value()) {
			DynamicComponentDefinition definition = service.getComponentDefinition(template);
			if (definition == null) {
				Logger.error("No such template '" + template + "' annotated to " + property,
					LayoutTemplatesDefault.class);
				continue;
			}
			result.add(definition.createTemplate());
		}
		return result;
	}

}
