/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag.js;


/**
 * Abstract base class for {@link JSValue} implementations that are best
 * {@link JSValue#eval(Appendable) evaluated} with an external
 * {@link StringBuffer}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractJSValue implements JSValue {

	@Override
	public Object toObject() {
		throw new UnsupportedOperationException();
	}

}
