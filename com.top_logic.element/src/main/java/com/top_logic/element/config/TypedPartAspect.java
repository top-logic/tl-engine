/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import com.top_logic.model.config.PartAspect;
import com.top_logic.model.config.TypeRefMandatory;

/**
 * Definition aspect of a type part, that has a definition of its type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TypedPartAspect extends PartAspect, TypeRefMandatory {

	// Pure sum interface.

}
