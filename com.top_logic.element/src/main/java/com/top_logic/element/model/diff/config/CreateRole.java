/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.security.RoleConfig;

/**
 * {@link DiffElement} requesting the creation of a new role.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("create-role")
public interface CreateRole extends Create, ModulePartDiff {

	/**
	 * Configuration of the new role to create within {@link #getModule()}.
	 */
	RoleConfig getRole();

	/**
	 * @see #getRole()
	 */
	void setRole(RoleConfig value);

}
