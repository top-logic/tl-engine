/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.model;

import com.top_logic.config.xdiff.util.Utils;


/**
 * A namespace annotated name.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class QName implements Named {

	private final String namespace;
	private final String localName;

	/* package protected */QName(String namespace, String localName) {
		this.namespace = namespace;
		this.localName = localName;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}
	
	@Override
	public String getLocalName() {
		return localName;
	}
	
	@Override
	public final boolean equals(Object obj) {
		return equalsNamed(this, obj);
	}

	@Override
	public final int hashCode() {
		return hashCodeNamed(this);
	}

	/**
	 * Common comparison implementation of {@link Named} instances.
	 */
	public static boolean equalsNamed(Named named, Object other) {
		if (! (other instanceof Named)) {
			return false;
		}
		
		return equalsNamed(named, (Named) other);
	}

	/**
	 * Common comparison implementation of {@link Named} instances.
	 */
	public static boolean equalsNamed(Named self, Named other) {
		return self.getLocalName().equals(other.getLocalName()) && Utils.equalsNullsafe(self.getNamespace(), other.getNamespace());
	}

	/**
	 * Hash code of {@link Named} instances.
	 */
	public static int hashCodeNamed(Named xqName) {
		String namespace = xqName.getNamespace();
		return (namespace != null ? 66499 * namespace.hashCode() : 0) + xqName.getLocalName().hashCode();
	}

	@Override
	public String toString() {
		return (namespace == null ? "" : "[" + namespace + "]") + localName;
	}

}
