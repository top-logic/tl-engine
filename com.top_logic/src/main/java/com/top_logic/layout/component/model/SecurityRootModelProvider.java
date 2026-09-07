/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.securityObjectProvider.SecurityRootObjectProvider;

/**
 * {@link ModelProvider} that constantly returns security root.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SecurityRootModelProvider implements ModelProvider {

	/** Singleton {@link SecurityRootModelProvider} instance. */
	public static final SecurityRootModelProvider INSTANCE = new SecurityRootModelProvider();

	private SecurityRootModelProvider() {
		// singleton instance
	}

	@Override
	public Object getBusinessModel(LayoutComponent businessComponent) {
		return SecurityRootObjectProvider.INSTANCE.getSecurityRoot();
	}

}

