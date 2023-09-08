/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import com.top_logic.basic.message.AbstractMessages;
import com.top_logic.basic.message.Message;

/**
 * The class {@link Messages} contains messages for this package.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Messages extends AbstractMessages {

	/**
	 * Template for {@link Message} that is used in the commit during the startup of the
	 * {@link PersonManager}.
	 */
	public static Template0 STARTUP_PERSON_MANAGER;

	/**
	 * Template for {@link Message} that is used by updating the {@link PersonManager}.
	 */
	public static Template0 UPDATE_PERSON_MANAGER;

	static {
		// Create template instances.
		init(Messages.class);

		// Finalize template defaults.
		initDefaults(Messages.class);
	}

}
