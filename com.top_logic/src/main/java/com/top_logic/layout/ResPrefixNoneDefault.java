/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.annotation.DefaultValueProviderShared;

/**
 * {@link DefaultValueProviderShared} constantly returning {@link ResPrefix#NONE}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ResPrefixNoneDefault extends DefaultValueProviderShared {

	@Override
	public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
		return ResPrefix.NONE;
	}

}
