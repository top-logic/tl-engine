/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import com.top_logic.basic.message.AbstractMessages;

/**
 * Contains messages for this package.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Messages extends AbstractMessages {

	public static Template1 CREATE_GLOBAL_DISPLAY_DESCRIPTION;

	public static Template1 DELETE_GLOBAL_DISPLAY_DESCRIPTION;

	static {
		// Create template instances.
		init(Messages.class);
	}

}

