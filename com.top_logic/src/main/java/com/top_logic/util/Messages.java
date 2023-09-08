/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import com.top_logic.basic.message.AbstractMessages;

/**
 * Messages for this package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Messages extends AbstractMessages {

	/** Transaction name for initializations done during application startup. */
	public static Template2 APPLICATION_STARTUP__NAME_VERSION;
	public static Template0 CREATING_INITIAL_GROUPS_AND_ROLES;

	/** Transaction name for initialization of layout based security objects. */
	public static Template0 INITIALIZING_LAYOUT_BASED_SECURITY;

	static {
		init(Messages.class);
	}
}
