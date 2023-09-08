/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.initializer;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyInitializer;
import com.top_logic.basic.encryption.SecureRandomService;

/**
 * {@link PropertyInitializer} creating a random {@link String} as initial value.
 * 
 * @see UUIDInitializer Initialiser to obtain a unique ID, which is, however, cryptographically
 *      insecure.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SecretInitializer implements PropertyInitializer {

	@Override
	public Object getInitialValue(PropertyDescriptor property) {
		return SecureRandomService.getInstance().getRandomString();
	}

}

