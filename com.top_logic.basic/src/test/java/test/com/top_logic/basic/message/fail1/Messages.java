/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.message.fail1;

import com.top_logic.basic.message.AbstractMessages;
import com.top_logic.basic.message.Template;

/**
 * Wrong messages class, {@link Template} instead of concrete classes
 * {@link com.top_logic.basic.message.AbstractMessages.Template0}.
 * 
 * @see AbstractMessages
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Messages extends AbstractMessages {

	/** Zero arg message. */
	public static Template MSG_0;
	
	/** One arg message. */
	public static Template MSG_1;
	
	static {
		// Call reflection initializer.
		init(Messages.class);
		
		// Call reflection initializer.
		initDefaults(Messages.class);
	}

}
