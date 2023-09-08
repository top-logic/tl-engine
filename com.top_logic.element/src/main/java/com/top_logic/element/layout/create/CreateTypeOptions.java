/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.create;

import java.util.List;

import com.top_logic.model.TLClass;

/**
 * Strategy for selecting types that can be instantiated in a certain context.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CreateTypeOptions {

	/**
	 * Computes the types that can be instantiated in a given context.
	 * 
	 * <p>
	 * If the resulting list contains more than one type, the user can select from the resulting
	 * types the type he wants to instantiate.
	 * </p>
	 * 
	 * @param contextModel
	 *        The model of the create component defining the create context.
	 */
	List<TLClass> getPossibleTypes(Object contextModel);

	/**
	 * Chooses the default type to instantiate in a given context.
	 * 
	 * @param contextModel
	 *        The model of the create component defining the create context.
	 * @param typeOptions
	 *        The list of possible types computed by {@link #getPossibleTypes(Object)}.
	 * @return An element from the given {@link List type options} or <code>null</code> to request
	 *         the user to explicitly select one.
	 */
	default TLClass getDefaultType(Object contextModel, List<TLClass> typeOptions) {
		if (typeOptions == null || typeOptions.isEmpty()) {
			return null;
		}
		return typeOptions.get(0);
	}

	/**
	 * Whether creation is possible with the given context object.
	 * 
	 * @param contextModel
	 *        The create context of the component, see {@link #getPossibleTypes(Object)}.
	 */
	default boolean supportsContext(Object contextModel) {
		return true;
	}

}
