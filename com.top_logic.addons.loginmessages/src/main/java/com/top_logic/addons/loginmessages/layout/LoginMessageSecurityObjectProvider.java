/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.addons.loginmessages.layout;

import com.top_logic.element.layout.grid.GridComponent;
import com.top_logic.element.layout.grid.GridRowSecurityObjectProvider;
import com.top_logic.tool.boundsec.BoundObject;

/**
 * A {@link GridRowSecurityObjectProvider} proposing to check security on anything but the row
 * object.
 *
 * @author <a href=mailto:Dmitry.Ivanizki@top-logic.com>Dmitry Ivanizki</a>
 */
public class LoginMessageSecurityObjectProvider implements GridRowSecurityObjectProvider {

	/** Singleton {@link LoginMessageSecurityObjectProvider} instance. */
	public static final LoginMessageSecurityObjectProvider INSTANCE = new LoginMessageSecurityObjectProvider();

	/**
	 * Creates a new {@link LoginMessageSecurityObjectProvider}.
	 */
	protected LoginMessageSecurityObjectProvider() {
		// singleton instance
	}

	@Override
	public BoundObject getSecurityObject(GridComponent component, Object rowObject) {
		return null;
	}
}
