/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link DiffElement} requesting the deletion of a role.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("delete-role")
public interface DeleteRole extends ModulePartDiff {

	/**
	 * Configuration of the role to delete within {@link #getModule()}.
	 */
	String getRole();

	/**
	 * @see #getRole()
	 */
	void setRole(String value);

}
