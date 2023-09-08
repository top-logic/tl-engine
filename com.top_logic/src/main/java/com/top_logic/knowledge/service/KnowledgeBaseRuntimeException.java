/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

/**
 * {@link KnowledgeBaseException} that is not required to be declared.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KnowledgeBaseRuntimeException extends RuntimeException {

	public KnowledgeBaseRuntimeException() {
		super();
	}

	public KnowledgeBaseRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public KnowledgeBaseRuntimeException(String message) {
		super(message);
	}

	public KnowledgeBaseRuntimeException(Throwable cause) {
		super(cause);
	}
	
}
