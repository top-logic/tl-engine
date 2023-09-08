/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.annotation;

import java.util.Arrays;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.layout.editor.DynamicComponentDefinition;
import com.top_logic.layout.editor.DynamicComponentService;
import com.top_logic.layout.editor.LayoutTemplate;

/**
 * {@link DefaultValueProvider} creating one {@link LayoutTemplate}s with default arguments for
 * {@link PropertyDescriptor}s of type {@link com.top_logic.mig.html.layout.LayoutComponent.Config}.
 * 
 * <p>
 * The template name of the template to instantiate is annotated to the {@link PropertyDescriptor}
 * with the {@link LayoutTemplates} annotation.
 * </p>
 *
 * @see LayoutTemplatesDefault
 * @see LayoutTemplates
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LayoutTemplateDefault extends DefaultValueProvider {

	@Override
	public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
		PropertyDescriptor property = descriptor.getProperty(propertyName);
		LayoutTemplates layoutTemplates = property.getAnnotation(LayoutTemplates.class);
		if (layoutTemplates == null) {
			Logger.error("A " + LayoutTemplateDefault.class.getSimpleName()
				+ " provider requires arguments annotated through the '" + LayoutTemplates.class.getSimpleName()
				+ "' annotation, ignoring default.", LayoutTemplateDefault.class);
			return null;
		}
		
		switch (layoutTemplates.value().length) {
			case 0: {
				Logger.error("A " + LayoutTemplateDefault.class.getSimpleName()
					+ " provider requires exactly one argument annotated through the '"
					+ LayoutTemplates.class.getSimpleName()
					+ "' annotation: Nothing configured.", LayoutTemplateDefault.class);
				return null;
			}
			case 1: {
				return createTemplate(property, layoutTemplates.value()[0]);
			}
			default: {
				Logger.error("A " + LayoutTemplateDefault.class.getSimpleName()
					+ " provider requires exactly one argument annotated through the '"
					+ LayoutTemplates.class.getSimpleName()
					+ "' annotation. Using first of configured " + Arrays.toString(layoutTemplates.value()) + ".",
					LayoutTemplateDefault.class);
				return createTemplate(property, layoutTemplates.value()[0]);
			}

		}
	}

	private LayoutTemplate createTemplate(PropertyDescriptor property, String templateName) {
		DynamicComponentService service = DynamicComponentService.getInstance();
		DynamicComponentDefinition definition = service.getComponentDefinition(templateName);
		if (definition == null) {
			Logger.error("No such template '" + templateName + "' annotated to " + property,
				LayoutTemplateDefault.class);
			return null;
		}
		return definition.createTemplate();
	}

}
