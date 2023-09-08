/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule.config;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.annotation.EnumDefaultValue;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.element.boundsec.manager.rule.RoleProvider.Type;

/**
 * {@link RoleRulesConfig} special for inheritance rules.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName(InheritanceRule.DEFAULT_TAG_NAME)
public interface InheritanceRule extends RoleRuleConfig {

	/**
	 * {@link EnumDefaultValue} for {@link Type#inheritance}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class InheritanceTypeValue extends EnumDefaultValue {

		@Override
		public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
			return Type.inheritance;
		}

	}

	/** Tag name to create an {@link InheritanceRule} */
	String DEFAULT_TAG_NAME = "inheritance-rule";

	@Override
	@ComplexDefault(InheritanceTypeValue.class)
	Type getType();

}

