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
	 * @en The value "{1}" of "{0}" names no operation: either list at least one operation, or leave
	 *     the value empty to delegate all operations.
	 */
	public static ResKey2 ERROR_NO_OPERATION_NAMES__PROPERTY_VALUE;

	/**
	 * @en A type with an access parent takes over the access definition of that parent and has none
	 *     of its own: either remove the access parent, or clear "{0}".
	 */
	public static ResKey1 ACCESS_PARENT_EXCLUDES_OWN_DEFINITION__PROPERTY;

	/**
	 * @en "Own decision" is no candidate of "Any of": a type deciding for itself has no access
	 *     parent. Remove it, or choose it instead of "Any of".
	 */
	public static ResKey ERROR_SELF_IN_ANY_OF;

	/**
	 * @en Referring objects never decide "Create": nothing refers to an object being created.
	 *     Remove "Create" from the operations, the grants of the type decide it.
	 */
	public static ResKey ERROR_CREATE_FOR_REFERER;

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
	 * @en Objects referencing it via {1} of {0}
	 */
	public static ResKey2 ACCESS_PARENT_REFERER__OWNER_REFERENCE;

	/**
	 * @en One of: {0}
	 */
	public static ResKey1 ACCESS_PARENT_ANY_OF__CANDIDATES;

	/**
	 * @en {0}; {1}
	 */
	public static ResKey2 CANDIDATE_LIST__LIST_CANDIDATE;

	/**
	 * @en {0}, only for {1}
	 */
	public static ResKey2 ACCESS_PARENT_RESTRICTED__RELATION_OPERATIONS;

	/**
	 * @en {0}, {1}
	 */
	public static ResKey2 OPERATION_LIST__LIST_OPERATION;

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
