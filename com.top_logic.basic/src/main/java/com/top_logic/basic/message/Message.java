/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.message;

import com.top_logic.basic.message.AbstractMessages.Template0;
import com.top_logic.basic.message.AbstractMessages.Template1;
import com.top_logic.basic.message.AbstractMessages.Template2;

/**
 * Internationalizable message.
 * 
 * <p>
 * A {@link Message} is created by filling the parameters of a {@link Template},
 * see e.g. {@link Template1#fill(Object)}.
 * </p>
 * 
 * <p>
 * The text of the message is defined by its {@link Template} from which this
 * message was constructed.
 * </p>
 * 
 * <p>
 * <b>Note:</b> This interface <b>must not</b> be implemented by application
 * code.
 * </p>
 * 
 * @see AbstractMessages
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Message {
	
	/**
	 * The {@link Template}, from which this {@link Message} was constructed.
	 * 
	 * @see Template0#fill()
	 * @see Template1#fill(Object)
	 * @see Template2#fill(Object, Object)
	 */
	Template getTemplate();
	
	/**
	 * The arguments of this {@link Message}.
	 * 
	 * @see Template0#fill()
	 * @see Template1#fill(Object)
	 * @see Template2#fill(Object, Object)
	 */
	Object[] getArguments();
	
}
