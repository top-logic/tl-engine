/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.json.JsonUtilities;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.service.openapi.server.script.Response;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ServiceMethod} executing a TL-Script expression.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ServiceMethodByExpression implements ServiceMethod {

	private final String _path;

	private final List<String> _parameters;

	private final boolean _transaction;

	private final QueryExecutor _operation;

	/**
	 * Creates a {@link ServiceMethodByExpression}.
	 * 
	 * @param path
	 *        The path, this {@link ServiceMethod} is registered on.
	 * @param parameters
	 *        The list of parameter names to take values from
	 *        {@link #handleRequest(Map, HttpServletResponse) arguments map}. The argument value are
	 *        passed to the script function in the given order.
	 * @param transaction
	 *        Whether to execute the given script in a transaction context.
	 * @param operation
	 *        The script to execute. The result of the operation is sent as response in JSON format.
	 */
	public ServiceMethodByExpression(String path, List<String> parameters, boolean transaction, QueryExecutor operation) {
		_path = path;
		_parameters = parameters;
		_transaction = transaction;
		_operation = operation;
	}

	@Override
	public void handleRequest(Map<String, Object> arguments, HttpServletResponse resp)
			throws IOException, ComputationFailure {
		Object result =
			ThreadContextManager.inSystemInteraction(ServiceMethodBuilderByExpression.class,
				() -> inInteraction(arguments));

		String contentType;
		String charset;
		int status;
		String content;
		if (result instanceof Response) {
			Response response = ((Response) result);
			status = response.getStatus();
			if (response.getResult() == null) {
				resp.sendError(status);
				return;
			}
			try {
				MimeType mimeType = new MimeType(response.getContentType());
				if (JsonUtilities.JSON_CONTENT_TYPE.equals(mimeType.getBaseType())) {
					content = JSON.toString(response.getResult());
				} else {
					content = String.valueOf(response.getResult());
				}
				contentType = mimeType.getBaseType();
				charset = mimeType.getParameter("charset");
				if (charset == null) {
					charset = "utf-8";
				}
			} catch (MimeTypeParseException ex) {
				throw new TopLogicException(I18NConstants.ERROR_INVALID_CONTENT_TYPE__VALUE_MSG
					.fill(response.getContentType(), ex.getMessage()), ex);
			}
		} else {
			status = HttpServletResponse.SC_OK;
			contentType = JsonUtilities.JSON_CONTENT_TYPE;
			charset = JsonUtilities.DEFAULT_JSON_ENCODING;
			content = JSON.toString(result);
		}

		resp.setStatus(status);
		resp.setContentType(contentType);
		resp.setCharacterEncoding(charset);
		resp.getWriter().write(content);
	}

	private Object inInteraction(Map<String, Object> arguments) throws ComputationFailure {
		if (Logger.isDebugEnabled(ServiceMethodByExpression.class)) {
			Logger.debug("Processing request for '" + _path + "', arguments=" + arguments,
				ServiceMethodByExpression.class);
		}
		if (_transaction) {
			try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
				Object result = execute(arguments);
				tx.commit();
				return result;
			}
		} else {
			return execute(arguments);
		}
	}

	private Object execute(Map<String, Object> arguments) throws ComputationFailure {
		try {
			return _operation.execute(arguments(arguments));
		} catch (RuntimeException ex) {
			throw new ComputationFailure(ex.getMessage(), ex);
		}
	}

	private Object[] arguments(Map<String, Object> arguments) {
		int count = _parameters.size();
		Object[] args = new Object[count];
		for (int n = 0; n < count; n++) {
			args[n] = arguments.get(_parameters.get(n));
		}
		return args;
	}
}