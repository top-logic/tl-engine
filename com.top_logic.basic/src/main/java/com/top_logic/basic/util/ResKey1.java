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
 * A {@link ResKey} generator accepting exactly one argument.
 * 
 * @see ResKey2
 * @see ResKey3
 * @see ResKey4
 * @see ResKey5
 * @see ResKeyN
 * @see I18NConstantsBase
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Format(ResKey.ValueFormat.class)
@Binding(ResKey.ValueBinding.class)
public interface ResKey1 {

	/**
	 * Creates a {@link ResKey} using the given object as dynamic part of the translation.
	 * 
	 * @param arg
	 *        Dynamic part of the translation. May be <code>null</code>.
	 * @return The {@link ResKey} that encapsulates the given argument.
	 */
	ResKey fill(Object arg);

}

