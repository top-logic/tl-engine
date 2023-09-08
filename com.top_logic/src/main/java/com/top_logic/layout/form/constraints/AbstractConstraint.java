/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;

/**
 * Base class for all {@link Constraint}s without external dependencies.
 * 
 * <p>
 * Sub-classes only depend on the {@link FormField} to which they are attached.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractConstraint implements Constraint {

	// Pure base class.

}
