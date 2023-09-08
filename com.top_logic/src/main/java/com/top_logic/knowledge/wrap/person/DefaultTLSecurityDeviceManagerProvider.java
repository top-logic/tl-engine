/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.col.Provider;

/**
 * {@link Provider} of the default {@link TLSecurityDeviceManager}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DefaultTLSecurityDeviceManagerProvider implements Provider<TLSecurityDeviceManager> {

	public static final DefaultTLSecurityDeviceManagerProvider INSTANCE = new DefaultTLSecurityDeviceManagerProvider();

	private DefaultTLSecurityDeviceManagerProvider() {
		// Singleton instance
	}

	@Override
	public TLSecurityDeviceManager get() {
		return TLSecurityDeviceManager.getInstance();
	}

}