/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.model.listen.ModelScope;

/**
 * Algorithm that implements the model change event forwarding to the session.
 */
public interface ModelEventForwarder extends ModelScope {

	/**
	 * Pluggable algorithm of synthesizing global change events.
	 * 
	 * <p>
	 * This method is called from the global model validation of each session.
	 * </p>
	 * 
	 * @return Whether any events have been synthesized.
	 */
	public abstract boolean synthesizeModelEvents();

}
