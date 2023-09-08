/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

/**
 * Convenience super-class for {@link Accessor} implementations that do not
 * allow to modify the target object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ReadOnlyAccessor<T> implements Accessor<T> {

	@Override
	public void setValue(T object, String property, Object value) {
		throw new UnsupportedOperationException();
	}

}
