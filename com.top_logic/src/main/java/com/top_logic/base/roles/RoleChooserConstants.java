
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.roles;


/**
 * This interface contains some constants used in conjunction with
 * the Role class package
 *
 * @author    Mathias Maul
 */
public interface RoleChooserConstants { 

	/**
	 * These constants are used to generate RoleChooserComponents.
	 */
	public static final String COMP_UNASSIGNEDSEL	= "comp_unasssel";
	public static final String COMP_ASSIGNEDSEL		= "comp_asssel";
	public static final String COMP_USERSEL			= "comp_usersel";

	public static final String COMP_STATUSLINE		= "comp_statusline";

	public static final String COMP_NEWROLEPANE		= "comp_newrolepane";


	/**
	 * The name of the dummy role if an ACL is null (and thus anyone has access).
	 */
	public static final String ROLE_ANYONE			= "ANYONE";

}
