/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.message;

import com.top_logic.basic.message.Message;
import com.top_logic.basic.message.Template;

/**
 * {@link Message}   
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
/*package protected*/
class CustomMessage implements Message, Template {

	private final String namespace;
	private final String localname;
	private final Object[] args;

	public CustomMessage(Message other) {
		this(other.getTemplate().getNameSpace(), other.getTemplate().getLocalName(), other.getArguments().clone());
	}
	
	public CustomMessage(String namespace, String localname) {
		this(namespace, localname, null);
	}

	public CustomMessage(String namespace, String localname, Object[] args) {
		this.namespace = namespace;
		this.localname = localname;
		this.args = args;
	}

	@Override
	public int getParameterCount() {
		if (args == null) {
			return 0;
		} else {
			return args.length;
		}
	}

	@Override
	public Object[] getArguments() {
		return args;
	}

	@Override
	public Template getTemplate() {
		return this;
	}

	@Override
	public String getLocalName() {
		return localname;
	}

	@Override
	public String getNameSpace() {
		return namespace;
	}

}
