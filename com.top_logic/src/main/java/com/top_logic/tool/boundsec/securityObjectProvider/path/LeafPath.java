/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.securityObjectProvider.path;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link SecurityPath} to use at the end of a path chain.
 * 
 * <p>
 * This {@link SecurityPath} delegates to no other component.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class LeafPath extends SecurityPath {

	@Override
	public LayoutComponent nextComponent(LayoutComponent component, int pathIndex, int size) {
		return null;
	}

}
