/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.message;

import com.top_logic.basic.message.AbstractMessages;

/**
 * Test messages for {@link TestAbstractMessages}.
 * 
 * @see AbstractMessages
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Messages extends AbstractMessages {

	/** Zero arg message. */
	public static Template0 MSG_0;
	
	/** One arg message. */
	public static Template1 MSG_1;
	
	/** Two arg message. */
	public static Template2 MSG_2;
	
	/** Three arg message. */
	public static Template3 MSG_3;
	
	/**
	 * Key with default.
	 */
	public static Template2 MSG_DEFAULT;

	static {
		// Call reflection initializer.
		init(Messages.class);
		
		MSG_DEFAULT.setDefaultTemplate(MSG_2);
		
		// Call reflection initializer.
		initDefaults(Messages.class);
	}

}
