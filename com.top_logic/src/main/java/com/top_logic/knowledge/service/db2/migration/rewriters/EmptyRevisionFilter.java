/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import com.google.inject.Inject;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.EmptyChangeSetFilter;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.convert.EventRewriter;

/**
 * {@link EventRewriter} that drops change sets without content.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EmptyRevisionFilter implements EventRewriter {

	private Log _log = new LogProtocol(EmptyRevisionFilter.class);

	/**
	 * Initialises the {@link Log log} .
	 */
	@Inject
	public void initLog(Log log) {
		_log = log;
	}

	@Override
	public void rewrite(ChangeSet cs, EventWriter out) {
		if (EmptyChangeSetFilter.INSTANCE.accept(cs)) {
			// Skip.
			_log.info("Skipping empty revision " + cs.getRevision() + ".", Protocol.DEBUG);
			return;
		}

		out.write(cs);
	}

}
