/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.message;

import com.top_logic.basic.message.AbstractMessages;
import com.top_logic.basic.message.Template;

/**
 * Messages for testing {@link AbstractMessages} and {@link Template} instantiation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Messages extends AbstractMessages {
	
	/** Test message with no parameters. */
	public static Template0 MSG_0;
	
	/** Test message with one parameters. */
	public static Template1 MSG_1;
	
	/** Test message with two parameters. */
	public static Template2 MSG_2;
	
	static {
		init(Messages.class);
		initDefaults(Messages.class);
	}
}