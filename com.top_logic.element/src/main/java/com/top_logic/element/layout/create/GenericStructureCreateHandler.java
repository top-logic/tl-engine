/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.create;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.model.TLObject;

/**
 * {@link GenericCreateHandler} that links the created object as new
 * {@link StructuredElement#getChildrenModifiable() child} to its
 * {@link StructuredElement#getParent() parent object} in a structure.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GenericStructureCreateHandler extends GenericCreateHandler {

	/**
	 * Creates a {@link GenericStructureCreateHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GenericStructureCreateHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void linkNewObject(TLObject container, TLObject newObject, Object model) {
		// Link new object to context.
		if (container instanceof StructuredElement && newObject instanceof StructuredElement) {
			((StructuredElement) container).getChildrenModifiable().add((StructuredElement) newObject);
		}
	}

}
