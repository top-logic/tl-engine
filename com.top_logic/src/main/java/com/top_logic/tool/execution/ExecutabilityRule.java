/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * The rule calculates if a command is executable within the given LayoutComponents context.
 * 
 * @author    <a href=mailto:fsc@top-logic.com>Friedemann Schneider</a>
 */
@Options(fun = AllInAppImplementations.class)
public interface ExecutabilityRule extends ExecutabilityRuleSPI {

	/**
	 * Creates a combined {@link ExecutabilityRule} of this and the given one.
	 */
	default ExecutabilityRule combine(ExecutabilityRule other) {
		return CombinedExecutabilityRule.combine(this, other);
	}

}
