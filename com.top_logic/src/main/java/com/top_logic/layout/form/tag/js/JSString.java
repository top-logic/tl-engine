/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag.js;

import java.io.IOException;

import com.top_logic.basic.xml.TagUtil;

/**
 * Representation of a JavaScript string literal. 
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JSString implements JSValue {

	String value;
	
	public JSString(String value) {
		this.value = value;
	}

	@Override
	public JSValue eval(JSEnvironment env) {
		return this;
	}

	public String getValue() {
	    return this.value;
	}

	@Override
	public void eval(Appendable out) throws IOException {
		TagUtil.writeJsString(out, value);
	}

	@Override
	public String toString() {
	    return this.getValue();
	}
	
	@Override
	public Object toObject() {
		return value;
	}
}
