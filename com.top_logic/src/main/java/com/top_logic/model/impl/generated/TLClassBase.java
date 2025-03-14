/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl.generated;

/**
 * Basic interface for {@link #TL_CLASS_TYPE} business objects.
 * 
 * @author Automatically generated by "com.top_logic.element.model.generate.InterfaceGenerator"
 */
public interface TLClassBase extends com.top_logic.model.TLStructuredType {

	/**
	 * Name of type <code>TLClass</code>
	 */
	String TL_CLASS_TYPE = "TLClass";

	/**
	 * Part <code>abstract</code> of <code>TLClass</code>
	 * 
	 * <p>
	 * Declared as <code>tl.core:Boolean</code> in configuration.
	 * </p>
	 */
	String ABSTRACT_ATTR = "abstract";

	/**
	 * Part <code>final</code> of <code>TLClass</code>
	 * 
	 * <p>
	 * Declared as <code>tl.core:Boolean</code> in configuration.
	 * </p>
	 */
	String FINAL_ATTR = "final";

	/**
	 * Part <code>generalizations</code> of <code>TLClass</code>
	 * 
	 * <p>
	 * Declared as <code>tl.model:TLClass</code> in configuration.
	 * </p>
	 */
	String GENERALIZATIONS_ATTR = "generalizations";

	/**
	 * Part <code>specializations</code> of <code>TLClass</code>
	 * 
	 * <p>
	 * Declared as <code>tl.model:TLClass</code> in configuration.
	 * </p>
	 */
	String SPECIALIZATIONS_ATTR = "specializations";

}
