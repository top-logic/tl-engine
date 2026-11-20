/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 * 
 * @see com.top_logic.layout.ResPrefix
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en A type with an access parent takes over the access definition of that parent and has none
	 *     of its own: either remove the access parent, or clear "{0}".
	 */
	public static ResKey1 ACCESS_PARENT_EXCLUDES_OWN_DEFINITION__PROPERTY;

	/**
	 * @en The access parent decides the operations {1} itself, since it is restricted to them and
	 *     decides Discover together with them: either remove the grants of these operations from
	 *     "{0}", or remove the operations from the access parent.
	 */
	public static ResKey2 ACCESS_PARENT_DECIDES_GRANTED__PROPERTY_OPERATIONS;

	/**
	 * @en container (default)
	 */
	public static ResKey ACCESS_PARENT_CONTAINER_DEFAULT;

	/**
	 * @en container
	 */
	public static ResKey ACCESS_PARENT_CONTAINER;

	/**
	 * @en container (through {0}#{1})
	 */
	public static ResKey2 ACCESS_PARENT_CONTAINER_VIA__OWNER_COMPOSITION;

	/**
	 * @en target of {0}#{1}
	 */
	public static ResKey2 ACCESS_PARENT_TARGET__OWNER_REFERENCE;

	/**
	 * @en {0}, only for {1}
	 */
	public static ResKey2 ACCESS_PARENT_RESTRICTED__RELATION_OPERATIONS;

	/**
	 * @en The value of "{1}" of "{0}" cannot be set to an object you may not discover.
	 */
	public static ResKey2 ERROR_TARGET_NOT_DISCOVERABLE__OBJECT_ATTRIBUTE;

	/**
	 * @en *****
	 */
	public static ResKey MASKED_VALUE;

	static {
		initConstants(I18NConstants.class);
	}
}
