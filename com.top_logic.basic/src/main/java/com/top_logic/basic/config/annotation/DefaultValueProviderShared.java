/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.annotation;

import com.top_logic.basic.config.PropertyDescriptor;

/**
 * {@link DefaultValueProvider} creating a shared default.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DefaultValueProviderShared extends DefaultValueProvider {

	@Override
	public boolean isShared(PropertyDescriptor property) {
		return true;
	}

}
