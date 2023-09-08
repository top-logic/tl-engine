/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.element.model.diff.config.visit.DiffVisitor;

/**
 * Base interface for describing model changes.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface DiffElement extends ConfigurationItem {

	/**
	 * Visit method for the {@link DiffElement} hierarchy.
	 */
	<R, A, E extends Throwable> R visit(DiffVisitor<R, A, E> v, A arg) throws E;

}
