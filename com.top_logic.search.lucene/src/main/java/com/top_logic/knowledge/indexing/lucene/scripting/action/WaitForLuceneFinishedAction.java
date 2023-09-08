/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.indexing.lucene.scripting.action;

import com.top_logic.knowledge.indexing.lucene.LuceneIndex;
import com.top_logic.knowledge.indexing.lucene.scripting.runtime.action.WaitForLuceneFinishedActionOp;
import com.top_logic.layout.scripting.action.AwaitAction;


/**
 * {@link AwaitAction} used to wait for {@link LuceneIndex} to end its work.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface WaitForLuceneFinishedAction extends AwaitAction {

	@Override
	Class<? extends WaitForLuceneFinishedActionOp> getImplementationClass();

}

