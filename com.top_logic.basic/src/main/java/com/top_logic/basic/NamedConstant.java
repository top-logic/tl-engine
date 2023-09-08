/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

/**
 * Place holder objects that are only {@link #equals(Object)} to themselves.
 * 
 * <p>
 * In contrast to <code>new Object()</code>, instances of this class have a
 * debug-friendly {@link #toString()} representation. In contrast to
 * <code>"some-string"</code>, application programmers are forced to use a
 * reference instead of a copied literal.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class NamedConstant {

	private final String name;

	/**
	 * Creates a new named constant object that is only
	 * {@link #equals(Object) equal} to itself.
	 */
	public NamedConstant(String name) {
		this.name = name;
	}

	/**
	 * The name of this {@link NamedConstant} that was given during
	 * construction.
	 * 
	 * @return The name of this {@link NamedConstant}.
	 */
	public String asString() {
		return name;
	}
	
	@Override
	public String toString() {
		return asString();
	}
	
}
