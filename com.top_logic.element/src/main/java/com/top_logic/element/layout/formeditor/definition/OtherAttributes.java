/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.definition;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.formeditor.implementation.OtherAttributesTemplateProvider;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.definition.FormElement;

/**
 * {@link FormElement} showing attributes of sub-types of the declaring typ.
 * 
 * <p>
 * A {@link FormDefinition} is used for all sub-types of the declaring type that has no own
 * {@link FormDefinition}. Attributes of such sub-types must be integrated into the UI in a generic
 * way. The {@link OtherAttributes} form element defines a placeholder where and how such
 * generically displayed attributes are placed.
 * </p>
 * 
 * @see OtherAttributesTemplateProvider
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("other-attributes")
@DisplayOrder({
	GroupProperties.LABEL,
	GroupProperties.LABEL_PLACEMENT,
	GroupProperties.COLUMNS,
	GroupProperties.COLLAPSIBLE,
	GroupProperties.INITIALLY_OPENED,
	GroupProperties.SHOW_BORDER,
	GroupProperties.SHOW_TITLE,
	GroupProperties.WHOLE_LINE,
})
@DisplayInherited(DisplayStrategy.IGNORE)
public interface OtherAttributes extends GroupProperties<OtherAttributesTemplateProvider> {

	@Override
	@ComplexDefault(DefaultLabel.class)
	ResKey getLabel();

	/**
	 * {@link DefaultValueProvider} for {@link OtherAttributes#getLabel()}.
	 */
	class DefaultLabel extends DefaultValueProvider {
		@Override
		public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
			return I18NConstants.OTHER_ATTRIBUTES;
		}
	}
}
