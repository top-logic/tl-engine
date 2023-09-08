/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.message;


/**
 * Internal {@link Message} implementation class.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
/*package protected*/ 
final class MessageImpl implements Message {
	private final Template template;
	private final Object[] args;

	/*package protected*/ MessageImpl(Template template, Object ...args) {
		this.template = template;
		this.args = args;
	}
	
	@Override
	public Template getTemplate() {
		return template;
	}
	
	@Override
	public Object[] getArguments() {
		return args;
	}
}