/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.basic.annotation.FrameworkInternal;

/**
 * Internal interface for updating dynamically changing executability of commands.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public interface ExecutabilityPolling {

	/**
	 * Updates the executability of this instance.
	 */
	@FrameworkInternal
	void updateExecutabilityState();

}
