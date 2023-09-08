/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.structure;

import com.top_logic.basic.config.annotation.Abstract;

/**
 * Model describing a right-hand-side value used for comparison in {@link SearchFilter}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface RightHandSide extends WithValueContext {

	/**
	 * Visit method for {@link RightHandSide} parts.
	 */
	<R, A> R visitRightHandSide(RightHandSideVisitor<R, A> v, A arg);

}
