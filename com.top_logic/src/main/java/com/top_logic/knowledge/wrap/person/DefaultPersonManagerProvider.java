/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import com.top_logic.basic.col.Provider;

/**
 * {@link Provider} of the default {@link PersonManager}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DefaultPersonManagerProvider implements Provider<PersonManager> {
	
	public static final DefaultPersonManagerProvider INSTANCE = new DefaultPersonManagerProvider();

	private DefaultPersonManagerProvider() {
		// Singleton instance
	}

	@Override
	public PersonManager get() {
		return PersonManager.getManager();
	}

}