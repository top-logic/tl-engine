/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.demo.command;

import com.top_logic.basic.message.AbstractMessages;

/**
 * The class {@link Messages} contains messages for this package.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Messages extends AbstractMessages {

	/** Create {0} objects at once. */
	public static Template1 KAFKA_DEMO_CREATE_MANY_OBJECTS__COUNT;

	static {
		// Create template instances.
		init(Messages.class);

		// Finalize template defaults.
		initDefaults(Messages.class);
	}

}
