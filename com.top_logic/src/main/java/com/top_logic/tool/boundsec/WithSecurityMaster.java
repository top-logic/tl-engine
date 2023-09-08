/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;

/**
 * Mix-in interface for configurations with a "security master" property.
 */
@Abstract
public interface WithSecurityMaster extends ConfigurationItem {

	/** @see #getIsSecurityMaster() */
	String IS_SECURITY_MASTER = "isSecurityMaster";

	/**
	 * Whether this component controls the access to its whole view.
	 * 
	 * <p>
	 * If a user has no access to a security-master component, he is prohibited to show the whole
	 * view (tab, tile) the component resides in.
	 * </p>
	 */
	@Name(IS_SECURITY_MASTER)
	boolean getIsSecurityMaster();

}
