/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag.js;

import java.io.IOException;

/**
 * {@link JSValue} representing a boolean.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JSBoolean implements JSValue {

	private boolean value;
	
	public JSBoolean(boolean value) {
		this.value = value;
	}

	@Override
	public JSValue eval(JSEnvironment env) {
		return this;
	}

	@Override
	public void eval(Appendable out) throws IOException {
		out.append(Boolean.toString(this.value));
	}
	
	@Override
	public Object toObject() {
		return Boolean.valueOf(value);
	}

}
