/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer.schema;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.model.instance.importer.UnresolvedRef;

/**
 * Configuration referencing an object in an {@link ObjectsConf object import declaration}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface ValueConf extends ConfigurationItem {

	/**
	 * Visit method for the {@link ValueConf} hierarchy.
	 * 
	 * @param v
	 *        The {@link ValueVisitor} to call.
	 * @param arg
	 *        The visit argument to pass to the visitor.
	 * @return The result returned from the visitor.
	 */
	<R, A> R visit(ValueVisitor<R, A> v, A arg) throws UnresolvedRef;

}
