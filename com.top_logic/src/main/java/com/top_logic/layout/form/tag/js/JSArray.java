/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag.js;

import java.io.IOException;

/**
 * Representation of a JavaScript array literal. 
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JSArray extends AbstractJSValue {

	JSExpression[] contents;
	
	public JSArray(JSExpression[] contents) {
		this.contents = contents;
	}

	@Override
	public void eval(Appendable out) throws IOException {
		out.append('[');
		for (int cnt = contents.length, n = 0; n < cnt; n++) {
			if (n > 0) {
				out.append(", ");
			}
			((JSValue) contents[n]).eval(out);
		}
		out.append(']');
	}

	@Override
	public JSValue eval(JSEnvironment env) {
		JSExpression[] evaluatedContents = new JSExpression[contents.length];

		for (int cnt = contents.length, n = 0; n < cnt; n++) {
			evaluatedContents[n] = contents[n].eval(env);
		}

		return new JSArray(evaluatedContents);
	}

	@Override
	public Object toObject() {
		throw new UnsupportedOperationException("There is no server side representation of this JSValue!");
	}

}
