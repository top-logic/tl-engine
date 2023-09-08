/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.attributes;

/**
 * Constants for technical LDAP attribute names that apply to all types (object classes) 
 *
 * @author <a href="mailto:Thomas.Richter@top-logic.com">Thomas Richter</a>
 */
public interface LDAPAttributes {

	/** The attribute "objectClass". */
	public static final String OBJECT_CLASS = "objectClass";

	/** The attribute "distinguished name". */
	public static final String DN = "dn";

}
