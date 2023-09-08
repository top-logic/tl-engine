/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.util;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.model.annotate.TLTypeKind;

/**
 * {@link StaticDefaultProviderOptions} for enumeration types.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EnumDefaultProviderOptions extends StaticDefaultProviderOptions {

	/**
	 * Creates a {@link EnumDefaultProviderOptions}.
	 */
	@CalledByReflection
	public EnumDefaultProviderOptions(DeclarativeFormOptions options) {
		super(options, TLTypeKind.ENUMERATION);
	}

}
