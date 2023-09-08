/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import java.util.Collection;

import com.top_logic.basic.config.annotation.Indexed;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.impl.generated.TLScopeBase;

/**
 * A scope defining {@link TLType}s.
 * 
 * @see TLModule Static scope that defines global types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLScope extends TLScopeBase {

	/**
	 * Classes defined by this module.
	 * 
	 * <p>
	 * This collection is mutable.
	 * </p>
	 */
	Collection<TLClass> getClasses();
	
	/**
	 * Associations defined by this module.
	 * 
	 * <p>
	 * This collection is mutable.
	 * </p>
	 */
	Collection<TLAssociation> getAssociations();

	/**
	 * Enumerations defined by this module.
	 * 
	 * <p>
	 * This collection is mutable.
	 * </p>
	 */
	Collection<TLEnumeration> getEnumerations();

	/**
	 * Datatypes defined by this module.
	 * 
	 * <p>
	 * This collection is mutable.
	 * </p>
	 */
	Collection<TLPrimitive> getDatatypes();

	/**
	 * All types defined by this module.
	 * 
	 * <p>
	 * This collection is mutable.
	 * </p>
	 */
	@Name(TYPES_ATTR)
	@Key(TLType.NAME_ATTRIBUTE)
	Collection<TLType> getTypes();

	/**
	 * Find the type with the given {@link TLType#getName() name} in this
	 * module.
	 * 
	 * @return The {@link TLType} with the given name, or <code>null</code>, if
	 *         there is no such type in this module.
	 */
	@Indexed(collection = TYPES_ATTR)
	TLType getType(String name);

}
