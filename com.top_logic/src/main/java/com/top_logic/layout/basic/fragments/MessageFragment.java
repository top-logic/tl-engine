/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.fragments;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.util.Resources;

/**
 * {@link HTMLFragment} rendering an internationalized text or message.
 * 
 * @see EmptyFragment
 * @see TextFragment
 * @see MessageFragment
 * @see RenderedFragment
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MessageFragment implements HTMLFragment {

	/**
	 * @see Fragments#message(ResKey)
	 */
	static MessageFragment createMessageFragment(ResKey messageKey) {
		return new MessageFragment(messageKey);
	}

	private final ResKey _messageKey;

	/**
	 * Creates a {@link MessageFragment}.
	 * 
	 * @param messageKey
	 *        Encoded message compatible to
	 *        {@link Resources#decodeMessageFromKeyWithEncodedArguments(String)}.
	 */
	MessageFragment(ResKey messageKey) {
		_messageKey = messageKey;
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		out.writeText(context.getResources().decodeMessageFromKeyWithEncodedArguments(_messageKey));
	}

}
