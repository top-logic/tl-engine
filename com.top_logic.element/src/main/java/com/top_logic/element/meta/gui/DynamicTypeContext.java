/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.gui;

import com.top_logic.model.TLClass;

/**
 * Interface for components operating on types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DynamicTypeContext {

	/**
	 * The object type of the component.
	 * 
	 * <p>
	 * The type determines the icon displayed in the form header.
	 * </p>
	 */
	TLClass getMetaElement();

}
