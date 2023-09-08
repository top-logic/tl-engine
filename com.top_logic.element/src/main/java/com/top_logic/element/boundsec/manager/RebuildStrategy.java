/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.knowledge.security.SecurityStorage;

/**
 * {@link SecurityStorage} rebuild strategy.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public enum RebuildStrategy implements ExternallyNamed {

	/** Answers immediately with no rights. */
	DENY("deny"),

	/** Waits until rebuilding is finished and blocks until then. */
	BLOCK("block"),

	/** Super class answers manually (will greatly reduce system performance). */
	COMPUTE("compute");

	private final String _externalName;

	private RebuildStrategy(String externalName) {
		this._externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}
