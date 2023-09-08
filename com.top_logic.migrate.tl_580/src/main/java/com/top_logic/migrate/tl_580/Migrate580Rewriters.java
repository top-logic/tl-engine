/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl_580;

import java.util.Collection;

import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.migrate.tl.util.LazyEventRewriter;
import com.top_logic.migrate.tl_580._10978.RemoveIsSortedAttribute;

/**
 * Helper class to collect all {@link EventRewriter} needed to migrate to TL 5.8.0.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Migrate580Rewriters {

	public static void addRewriters(Collection<? super LazyEventRewriter> rewriters) {
		rewriters.add(RemoveIsSortedAttribute.LAZY_INSTANCE);
	}

}

