/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import com.top_logic.basic.config.annotation.Binding;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.i18n.I18NConstantsBase;

/**
 * A {@link ResKey} generator with variable number of arguments.
 * 
 * @see ResKey1
 * @see ResKey2
 * @see ResKey3
 * @see ResKey4
 * @see ResKey5
 * @see I18NConstantsBase
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Format(ResKey.ValueFormat.class)
@Binding(ResKey.ValueBinding.class)
public interface ResKeyN {

	/**
	 * Creates a {@link ResKey} using the given objects as dynamic part of the translation.
	 * 
	 * @param arguments
	 *        Dynamic part of the translation.
	 * @return The {@link ResKey} that encapsulates the given arguments.
	 */
	ResKey fill(Object... arguments);

}

