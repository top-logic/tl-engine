/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.tool.boundsec.commandhandlers.BookmarkHandler;
import com.top_logic.util.error.TopLogicException;

/**
 * A {@link ObjectNotFound} is thrown if an object can not be resolved by some argument map detects
 * a failure.
 * 
 * @see AbstractCommandHandler#getObject(Map)
 * @see BookmarkHandler#getBookmarkObject(Map)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ObjectNotFound extends TopLogicException {

	/**
	 * Creates a {@link ObjectNotFound}.
	 * 
	 * @param errorKey
	 *        See {@link TopLogicException#TopLogicException(ResKey, Throwable)}.
	 */
	public ObjectNotFound(ResKey errorKey) {
		super(errorKey);
	}

	/**
	 * Creates a {@link ObjectNotFound} exception.
	 * 
	 * @param errorKey
	 *        See {@link TopLogicException#TopLogicException(ResKey, Throwable)}.
	 * @param cause
	 *        See {@link TopLogicException#TopLogicException(ResKey, Throwable)}.
	 */
	public ObjectNotFound(ResKey errorKey, Throwable cause) {
		super(errorKey, cause);
	}

}
