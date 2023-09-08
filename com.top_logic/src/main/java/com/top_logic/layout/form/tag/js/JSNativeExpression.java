/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag.js;

import java.io.IOException;

/**
 * JavaScript expression, whose evaluation is deferred until it is being
 * processed by the client-side browser.
 * 
 * <p>
 * {@link JSNativeExpression} implements the {@link JSValue} interface instead
 * of the {@link JSExpression} interface, because it cannot be evaluated further
 * on the server. Therefore, on the server, a {@link JSNativeExpression} is
 * treated like a value.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JSNativeExpression implements JSValue {
	private final String code;
	
	public static final JSNativeExpression NULL = new JSNativeExpression("null");
	
	public JSNativeExpression(final String code) {
		this.code = code;
	}

	@Override
	public void eval(Appendable out) throws IOException {
		out.append(code);
	}

	@Override
	public JSValue eval(JSEnvironment env) {
		// Cannot be evaluated further. Evaluation is deferred for "native"
		// evaluation in the browser.
		return this;
	}
	
	@Override
	public Object toObject() {
		throw new UnsupportedOperationException("There is no server side representation of this JSValue!");
	}

}
