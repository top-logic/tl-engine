/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponent.Config;

/**
 * Configuration of the security for a {@link LayoutComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface SecurityConfiguration extends ConfigurationItem {

	/**
	 * Configuration name of the value {@link #getSecurityId()}.
	 */
	String SECURITY_ID = "securityId";

	/**
	 * The name under which the security for this component is configurable. If not given
	 * {@link Config#getName()} is used.
	 * 
	 * @return May be <code>null</code>.
	 */
	@Name(SECURITY_ID)
	ComponentName getSecurityId();

	/**
	 * Setter for {@link #getSecurityId()}.
	 */
	void setSecurityId(ComponentName id);

}

