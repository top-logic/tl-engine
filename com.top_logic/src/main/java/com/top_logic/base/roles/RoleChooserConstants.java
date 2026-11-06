
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.roles;


/**
 * This interface contains some constants used in conjunction with
 * the Role class package.
 *
 * @author    Mathias Maul
 */
public interface RoleChooserConstants { 

	/**
	 * These constants are used to generate role chooser components.
	 */
	public static final String COMP_UNASSIGNEDSEL	= "comp_unasssel";
	/** Name of the component selecting assigned roles. */
	public static final String COMP_ASSIGNEDSEL		= "comp_asssel";
	/** Name of the component selecting users. */
	public static final String COMP_USERSEL			= "comp_usersel";

	/** Name of the status line component. */
	public static final String COMP_STATUSLINE		= "comp_statusline";

	/** Name of the pane for creating new roles. */
	public static final String COMP_NEWROLEPANE		= "comp_newrolepane";


	/**
	 * The name of the dummy role if an ACL is null (and thus anyone has access).
	 */
	public static final String ROLE_ANYONE			= "ANYONE";

}
