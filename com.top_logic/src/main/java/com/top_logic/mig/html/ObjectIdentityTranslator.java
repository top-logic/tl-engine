/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.io.Serializable;

/**
 * Default implementation of {@link ObjectTranslator} that resolves all objects
 * to themselves.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ObjectIdentityTranslator implements Serializable,ObjectTranslator {

	/**
	 * Singleton instance of this class.
	 */
	public static final ObjectTranslator INSTANCE = new ObjectIdentityTranslator();

	/**
	 * Singleton constructor.
	 */
	private ObjectIdentityTranslator() {
		super();
	}
	
	@Override
	public Object resolve(Object object) {
		return object;
	}

}
