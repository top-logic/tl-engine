/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured;


import com.top_logic.knowledge.wrap.ValueProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.factory.TLFactory;

/**
 * Factory for creating objects in a structure and providing information about
 * the types in that structure.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLStructureFactory extends TLFactory {

	/**
	 * The module name, this factory is responsible for.
	 */
	String getModuleName();

	/**
	 * The module, this factory is responsible for.
	 */
	TLModule getModule();

	/**
	 * @deprecated Use {@link #createObject(TLClass, TLObject, ValueProvider)} instead.
	 */
	@Deprecated
	default Object createObject(Object context, String typeName, ValueProvider initialValues) {
		return createObject((TLClass) getModule().getType(typeName), (TLObject) context, initialValues);
	}

	/**
	 * Looks up the {@link TLClass concrete type} for an object with the given element name in the
	 * context of the given object.
	 * 
	 * @param context
	 *        The context in which the type lookup should be performed.
	 * @param elementName
	 *        The element name to resolve.
	 * @return The concrete type of an object with the given element name.
	 */
	TLClass getNodeType(StructuredElement context, String elementName);

}
