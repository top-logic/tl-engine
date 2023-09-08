/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import com.top_logic.tool.boundsec.BoundObject;

/**
 * The default {@link GridRowSecurityObjectProvider} returns the row object itself to check the security.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class DefaultGridRowSecurityObjectProvider implements GridRowSecurityObjectProvider {

	/** Singleton {@link DefaultGridRowSecurityObjectProvider} instance. */
	public static final DefaultGridRowSecurityObjectProvider INSTANCE = new DefaultGridRowSecurityObjectProvider();

	/**
	 * Creates a new {@link DefaultGridRowSecurityObjectProvider}.
	 */
	protected DefaultGridRowSecurityObjectProvider() {
		// singleton instance
	}

	@Override
	public BoundObject getSecurityObject(GridComponent component, Object rowObject) {
		return rowObject instanceof BoundObject ? (BoundObject)rowObject : null;
	}

}
