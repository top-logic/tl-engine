/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for the view access-control system.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Created view security scopes.
	 */
	public static ResKey CREATING_SECURITY_SCOPES;

	/**
	 * @en You may not view this object.
	 */
	public static ResKey ERROR_READ_DENIED;

	/**
	 * @en You may not change this object.
	 */
	public static ResKey ERROR_WRITE_DENIED;

	/**
	 * @en You may not delete this object.
	 */
	public static ResKey ERROR_DELETE_DENIED;

	/**
	 * @en You are not allowed to perform the operation "{0}" on this object.
	 */
	public static ResKey1 ERROR_OPERATION_DENIED__OPERATION;

	/**
	 * @en You may not view "{0}" of this object.
	 */
	public static ResKey1 ERROR_ATTRIBUTE_READ_DENIED__ATTRIBUTE;

	/**
	 * @en You may not change "{0}" of this object.
	 */
	public static ResKey1 ERROR_ATTRIBUTE_WRITE_DENIED__ATTRIBUTE;

	/**
	 * @en You are not allowed to perform the operation "{0}" on "{1}" of this object.
	 */
	public static ResKey2 ERROR_ATTRIBUTE_OPERATION_DENIED__OPERATION_ATTRIBUTE;

	/**
	 * @en You are not allowed to perform the operation "{0}" on objects of type "{1}".
	 */
	public static ResKey2 ERROR_TYPE_OPERATION_DENIED__OPERATION_TYPE;

	/**
	 * @en You may not create an object here.
	 */
	public static ResKey ERROR_CREATE_DENIED;

	/**
	 * @en You may not create an object of type "{0}" here.
	 */
	public static ResKey1 ERROR_CREATE_TYPE_DENIED__TYPE;

	/**
	 * @en Granted command group "{0}" to role "{1}" on "{2}".
	 */
	public static ResKey3 GRANTED_COMMAND_GROUP__GROUP_ROLE_SCOPE;

	/**
	 * @en Revoked command group "{0}" from role "{1}" on "{2}".
	 */
	public static ResKey3 REVOKED_COMMAND_GROUP__GROUP_ROLE_SCOPE;

	static {
		initConstants(I18NConstants.class);
	}
}
