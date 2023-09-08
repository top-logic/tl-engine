/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

import org.w3c.dom.Element;

/**
 * Parser that creates an {@link MSXDiff} from a DOM element that defines the properties of that
 * operation.
 * 
 * @see MSXDiffSchema
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
interface MSXDiffElementParser {

	/**
	 * Creates an {@link MSXDiff} for the given element from the {@link MSXDiffSchema}.
	 */
	MSXDiff build(Element operationSpec);

}
