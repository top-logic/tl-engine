/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.object;

import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;

/**
 * {@link ObjectRef} referencing a dynamic type instance.
 * 
 * @deprecated See {@link ValueRef}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public interface TypeRef extends ObjectRef {

	/**
	 * Reference to the object that declares the {@link #getTypeName() type}
	 * this attribute is part of.
	 * 
	 * <p>
	 * If the scope is <code>null</code>, a global type is referenced.
	 * </p>
	 */
	ModelName getScopeRef();

	/** @see #getScopeRef() */
	void setScopeRef(ModelName scopeRef);

	/**
	 * The name of the type, this attribute belongs to.
	 */
	String getTypeName();
	void setTypeName(String value);

}
