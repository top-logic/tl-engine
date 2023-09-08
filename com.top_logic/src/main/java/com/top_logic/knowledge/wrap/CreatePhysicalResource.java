/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Definition of the time when the physical resource of a {@link WebFolder} must be created.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public enum CreatePhysicalResource implements ExternallyNamed {

	/**
	 * Create the physical resource immediately after creation of {@link WebFolder}.
	 */
	IMMEDIATE("immediate"),

	/**
	 * Create the physical resource of a {@link WebFolder} the first time it is needed.
	 */
	DEFERRED("deferred"),

	;

	private final String _externalName;

	private CreatePhysicalResource(String externalName) {
		this._externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return this._externalName;
	}

}
