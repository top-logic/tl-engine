/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

import com.top_logic.basic.AbstractProtocol;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;

/**
 * Protocol throwing {@link KnowledgeBaseRuntimeException}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class KnowledgeBaseProtocol extends AbstractProtocol {

	@Override
	protected void reportError(String message, Throwable ex) {
		throw new KnowledgeBaseRuntimeException(message, ex);
	}

	@Override
	protected RuntimeException reportFatal(String message, Throwable ex) {
		throw new KnowledgeBaseRuntimeException(message, ex);
	}

	@Override
	protected RuntimeException createAbort() {
		return new KnowledgeBaseRuntimeException();
	}

}

