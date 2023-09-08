/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform;

import java.text.MessageFormat;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.ProtocolAdaptor;
import com.top_logic.knowledge.search.QueryPart;
import com.top_logic.knowledge.service.db2.expr.visit.ExpressionPrinter;

/**
 * {@link ProtocolAdaptor} with specialized error reporting methods for
 * {@link QueryPart} transformation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExpressionCompileProtocol extends ProtocolAdaptor {

	/**
	 * Creates a {@link ExpressionCompileProtocol}.
	 * 
	 * @param impl
	 *        The {@link Protocol} to dispatch errors to.
	 */
	public ExpressionCompileProtocol(Protocol impl) {
		super(impl);
	}

	/**
	 * Reports an error in the context of the given expression.
	 * 
	 * @param expr
	 *        The expression context.
	 * @param message
	 *        The message.
	 * @param args
	 *        Arguments to the message. See {@link MessageFormat}.
	 */
	public void error(QueryPart expr, String message, Object... args) {
		// TODO: Reference location in source.
		String source = ExpressionPrinter.toString(expr);
		error(MessageFormat.format(message, args) + " Expression: " + source);
	}

}
