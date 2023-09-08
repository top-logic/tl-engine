/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import com.top_logic.knowledge.service.db2.PersistentObject;

/**
 * Base class for factories of pseudo structures.
 * 
 * @author <a href="mailto:dbu@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractElementFactory extends AbstractWrapperResolver {

	/**
	 * Legacy attribute determining the dynamic type.
	 * 
	 * @deprecated Replaced with {@link PersistentObject#TYPE_REF}.
	 */
	@Deprecated
	public static final String ELEMENT_NAME_ATTRIBUTE = "elementName";

}
