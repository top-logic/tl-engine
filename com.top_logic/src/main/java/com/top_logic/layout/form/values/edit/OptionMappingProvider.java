/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import com.top_logic.basic.func.IGenericFunction;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * {@link OptionMappingProvider} that determines the {@link OptionMapping} for an {@link Options}
 * annotation.
 * 
 * @see Options
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@FunctionalInterface
public interface OptionMappingProvider {

	/**
	 * The {@link OptionMapping} used for an {@link IGenericFunction} of a {@link Options}
	 * annotation.
	 */
	OptionMapping getOptionMapping();

}

