/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.util.UUID;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.util.Utils;

/**
 * Creates a random id from a {@link UUID}.
 * 
 * @see Utils#getRandomID()
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class RandomIDProvider extends DefaultValueProvider {

	@Override
	public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
		return Utils.getRandomID();
	}

}
