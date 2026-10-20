/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.vector.index.scripting.action;

import com.top_logic.ai.vector.index.VectorIndex;
import com.top_logic.ai.vector.index.scripting.runtime.action.WaitForVectorFinishedActionOp;
import com.top_logic.layout.scripting.action.AwaitAction;


/**
 * {@link AwaitAction} used to wait for {@link VectorIndex} to end its work.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface WaitForVectorFinishedAction extends AwaitAction {

	@Override
	Class<? extends WaitForVectorFinishedActionOp> getImplementationClass();

}

