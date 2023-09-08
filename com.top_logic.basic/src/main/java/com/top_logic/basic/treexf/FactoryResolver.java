/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.treexf;

import java.util.Map;

import com.top_logic.basic.treexf.TreeMaterializer.Factory;

/**
 * Algorithm for finding {@link Factory} instances.
 * 
 * @see TreeMaterializer#TreeMaterializer(FactoryResolver)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FactoryResolver {

	/**
	 * Find the {@link Factory} for the given type.
	 * 
	 * @param type
	 *        An identifier for a node type, can be a {@link Class} or any other object.
	 * 
	 * @return the {@link Factory} for the given type, or <code>null</code>, if this resolver has no
	 *         {@link Factory} for the requested type.
	 */
	Factory getFactory(Object type);

	/**
	 * Creates a static {@link FactoryResolver} from the given type/factory map.
	 */
	static FactoryResolver getInstance(Map<Object, Factory> factoryByType) {
		return new FactoryResolver() {
			@Override
			public Factory getFactory(Object type) {
				return factoryByType.get(type);
			}
		};
	}

}
