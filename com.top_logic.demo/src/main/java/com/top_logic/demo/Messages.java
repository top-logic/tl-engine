/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo;

import com.top_logic.basic.message.AbstractMessages;

/**
 * The class {@link Messages} contains messages for demo application.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Messages extends AbstractMessages {

	public static Template0 DELETE_DEMO_TYPES;
	public static Template0 GENERATED_DEMO_TYPES;

	static {
		// Create template instances.
		init(Messages.class);

		// Finalize template defaults.
		initDefaults(Messages.class);
	}

}
