/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.EventWriter;

/**
 * Rewriter that writes a copy of the given {@link ChangeSet}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CopyRewriter implements EventRewriter {

	@Override
	public void rewrite(ChangeSet cs, EventWriter out) {
		out.write(cs.copy());
	}

}

