/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.model.check;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;

/**
 * Algorithm validating a single object during commit.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface InstanceCheck {
	/**
	 * Checks the given object for consistency.
	 * 
	 * <p>
	 * Potential problems are reported to the given {@link Sink}.
	 * </p>
	 *
	 * @param problems
	 *        {@link Sink} of problem descriptions.
	 * @param object
	 *        The object to validate.
	 */
	void check(Sink<ResKey> problems, TLObject object);
}