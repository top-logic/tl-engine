/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.initializer;

import java.util.regex.Pattern;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyInitializer;
import com.top_logic.util.Utils;

/**
 * {@link PropertyInitializer} that assigns unique IDs to string-valued properties.
 * 
 * @see SecretInitializer Initialiser to get a random {@link String} that can be used as secret.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UUIDInitializer implements PropertyInitializer {

	/**
	 * Singleton {@link UUIDInitializer} instance.
	 */
	public static final UUIDInitializer INSTANCE = new UUIDInitializer();

	/**
	 * {@link Pattern} detecting an ID generated with the {@link UUIDInitializer}.
	 * 
	 * <p>
	 * For example, an ID looks like <code>ID_f42ea885_2c81_4aa6_807b_d82956439d79</code>.
	 * </p>
	 */
	public static final Pattern ID_PATTERN =
		Pattern.compile("ID_[0-9a-f]{8}_[0-9a-f]{4}_[0-9a-f]{4}_[0-9a-f]{4}_[0-9a-f]{12}");

	private UUIDInitializer() {
		// Singleton constructor.
	}

	@Override
	public Object getInitialValue(PropertyDescriptor property) {
		return Utils.getRandomID();
	}

}
