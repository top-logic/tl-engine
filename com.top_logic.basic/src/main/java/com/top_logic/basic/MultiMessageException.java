/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.util.Collections;
import java.util.List;

/**
 * {@link Exception} holding multiple messages instead of only one.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class MultiMessageException extends Exception {

    /** Stores the messages of the exception. */
	private List<String> _messages;


    /**
     * Creates a new instance of this class.
     */
    public MultiMessageException() {
		this(noMessages());
    }

    /**
	 * Creates a new instance of this class.
	 * 
	 * @param messages
	 *        the messages of this exception
	 */
	public MultiMessageException(List<String> messages) {
		this(messages, null);
    }

    /**
     * Creates a new instance of this class.
     *
     * @param aCause
     *        the cause of the exception
     */
    public MultiMessageException(Throwable aCause) {
		this(noMessages(), aCause);
    }

    /**
	 * Creates a new instance of this class.
	 * 
	 * @param messages
	 *        the messages of this exception
	 * @param aCause
	 *        the cause of the exception
	 */
	public MultiMessageException(List<String> messages, Throwable aCause) {
		super(StringServices.toString(messages, "; "), aCause);
		_messages = messages;
    }


    /**
     * Gets the messages of this exception.
     *
     * @return an unmodifiable list with the messages of this exception
     */
	public List<String> getMessages() {
		return _messages;
    }

	private static List<String> noMessages() {
		return Collections.<String> emptyList();
	}

}
