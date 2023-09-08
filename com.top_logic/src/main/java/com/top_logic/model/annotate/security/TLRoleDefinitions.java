/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.security;

import java.util.Collection;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLModule;
import com.top_logic.model.config.TLModuleAnnotation;

/**
 * {@link TLModuleAnnotation} for defining roles in the context of a {@link TLModule}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
@TagName("roles")
public interface TLRoleDefinitions extends TLModuleAnnotation {

	/** Property name of {@link #getRoles()}. */
	String ROLES = "roles";

	/**
	 * Roles defined by the annotated module.
	 */
	@Name(ROLES)
	@Key(RoleConfig.NAME_ATTRIBUTE)
	@DefaultContainer
	Collection<RoleConfig> getRoles();

}
