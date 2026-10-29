/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * How a {@link DispatchingImporter} handles an element for which it has no handler.
 *
 * <p>
 * In each case, the element is skipped together with its whole subtree.
 * </p>
 *
 * @see DispatchingImporter.Config#getUnknown()
 */
public enum UnknownElements implements ExternallyNamed {

	/**
	 * The element and its subtree are skipped silently.
	 */
	SKIP("skip"),

	/**
	 * The tag name of the element and its document location are reported as warning, the element
	 * and its subtree are skipped.
	 */
	WARN("warn"),

	/**
	 * The tag name of the element and its document location are reported as error, the element and
	 * its subtree are skipped.
	 */
	ERROR("error"),

	;

	private final String _externalName;

	private UnknownElements(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

}
