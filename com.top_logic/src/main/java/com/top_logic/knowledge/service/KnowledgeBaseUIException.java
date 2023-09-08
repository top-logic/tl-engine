/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.util.ResKey;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link TopLogicException} created by the {@link KnowledgeBase} to produce internationalised
 * messages for the UI.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class KnowledgeBaseUIException extends TopLogicException {

	/**
	 * @see TopLogicException#TopLogicException(ResKey)
	 */
	public KnowledgeBaseUIException(ResKey errorKey) {
		super(errorKey);
	}

	/**
	 * @see TopLogicException#TopLogicException(ResKey, Throwable)
	 */
	public KnowledgeBaseUIException(ResKey errorKey, Throwable cause) {
		super(errorKey, cause);
	}

}

