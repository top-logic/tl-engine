/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.skip;

import com.top_logic.basic.Protocol;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.migrate.tl.util.LazyEventRewriter;

/**
 * {@link LazyEventRewriter} creating a {@link SchemaCompacting}
 * {@link EventRewriter}
 * 
 * @see SchemaCompacting
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class SchemaCompacter implements LazyEventRewriter {
	
	private final Protocol log;

	public SchemaCompacter(Protocol log) {
		this.log = log;
	}

	@Override
	public EventRewriter createRewriter(KnowledgeBase srcKB, KnowledgeBase destKB) {
		return new SchemaCompacting(srcKB, destKB, log);
	}
}
