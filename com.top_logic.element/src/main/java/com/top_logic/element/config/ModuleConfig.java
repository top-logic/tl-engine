/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import java.util.Collection;

import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.constraint.annotation.RegexpConstraint;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.config.ModelPartConfig;
import com.top_logic.model.config.NamedPartConfig;
import com.top_logic.model.config.PartNameConstraints;
import com.top_logic.model.config.ScopeConfig;
import com.top_logic.model.config.TLModuleAnnotation;
import com.top_logic.model.config.TypeConfig;

/**
 * Configuration of one module in the {@link ModelConfig}.
 */
@DisplayOrder({
	ModuleConfig.NAME,
	ModuleConfig.ANNOTATIONS,
	ModuleConfig.TYPES,
})
public interface ModuleConfig
		extends ModelPartConfig, ScopeConfig, AnnotatedConfig<TLModuleAnnotation>, NamedPartConfig {

	/**
	 * Default tag for a {@link ModuleConfig} configuration.
	 */
	String TAG_NAME = "module";

	@Override
	@DefaultContainer
	Collection<TypeConfig> getTypes();

	@Override
	@RegexpConstraint(value = PartNameConstraints.MANDATORY_MODULE_NAME_PATTERN, errorKey = PartNameConstraints.MandatoryModuleNameKey.class)
	String getName();

}