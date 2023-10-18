/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.securityObjectProvider;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.SecurityObjectProvider;

/**
 * {@link SecurityObjectProvider} that uses the current model as security object.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
@Label("Current model")
public class ModelSecurityObjectProvider implements SecurityObjectProvider {

	/** Singleton {@link ModelSecurityObjectProvider} instance. */
	public static final ModelSecurityObjectProvider INSTANCE = new ModelSecurityObjectProvider();

	/**
	 * Creates a new {@link ModelSecurityObjectProvider}.
	 */
	protected ModelSecurityObjectProvider() {
		// singleton instance
	}

	@Override
	public BoundObject getSecurityObject(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup) {
		if (model instanceof BoundObject) {
			return (BoundObject) model;
		}
		return null;
	}

}

