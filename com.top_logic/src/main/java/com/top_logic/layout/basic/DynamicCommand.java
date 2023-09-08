/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.layout.form.model.ExecutabilityModel;

/**
 * {@link Command} that can dynamically change its executability.
 * 
 * @see #getExecutability()
 * @see #updateExecutabilityState()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DynamicCommand extends Command, ExecutabilityModel {
	// Pure sum interface.
}
