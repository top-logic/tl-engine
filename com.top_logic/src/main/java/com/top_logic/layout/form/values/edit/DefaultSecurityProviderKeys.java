/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.func.Function0;
import com.top_logic.layout.security.SecurityProvider;

/**
 * Provides a selection of default {@link SecurityProvider}s for editing.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class DefaultSecurityProviderKeys extends Function0<List<String>> {

	@Override
	public List<String> apply() {
		return Arrays.asList("securityRoot", "master", "slave", "model");
	}

}
