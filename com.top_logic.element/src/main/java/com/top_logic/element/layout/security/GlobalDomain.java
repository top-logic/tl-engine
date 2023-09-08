/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.security;

/**
 * Representation of the global domain in {@link AttributeClassifierRolesComponent}.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class GlobalDomain {

	/** Singleton {@link GlobalDomain} instance. */
	public static final GlobalDomain INSTANCE = new GlobalDomain();

	private GlobalDomain() {
		// singleton instance
	}

	@Override
	public String toString() {
		return "global domain";
	}

}

