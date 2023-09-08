/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.model;

import com.top_logic.basic.annotation.FrameworkInternal;

/**
 * Framework-internal interface that {@link Box} implementations must adhere to.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public interface BoxSPI {

	/**
	 * Access to implementation details.
	 */
	@FrameworkInternal
	BoxInternals internals();

}
