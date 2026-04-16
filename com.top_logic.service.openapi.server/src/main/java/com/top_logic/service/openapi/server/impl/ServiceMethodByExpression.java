/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.json.JsonUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.service.openapi.server.script.Response;
import com.top_logic.util.TLContextManager;
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
	 *        {@link #handleRequest(Person, Map, HttpServletResponse) arguments map}. The argument value are
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
	public void handleRequest(Person account, Map<String, Object> arguments, HttpServletResponse resp)
			throws IOException, ComputationFailure {
		ComputationEx2<Void, ComputationFailure, IOException> job = () -> {
			writeResponse(inInteraction(arguments), resp);
			return null;
		};
		if (account == null) {
			ThreadContextManager.inSystemInteraction(ServiceMethodBuilderByExpression.class, job);
		} else {
			TLContextManager.inPersonContext(account, job);
		}
	}

	/**
	 * Writes the given script result to the HTTP response.
	 *
	 * <p>
	 * Exposed for direct unit testing; not intended to be called by application code.
	 * </p>
	 */
	@FrameworkInternal
	public final void writeResponse(Object result, HttpServletResponse resp) throws IOException {
		Object body;
		int status;
		String contentType;
		boolean wrapped = result instanceof Response;

		if (wrapped) {
			Response response = ((Response) result);
			status = response.getStatus();
			body = response.getResult();
			if (body == null) {
				resp.sendError(status);
				return;
			}
			contentType = response.getContentType();
		} else {
			status = HttpServletResponse.SC_OK;
			body = result;
			contentType = isBinary(body) ? defaultBinaryContentType(body) : JsonUtilities.JSON_CONTENT_TYPE;
		}

		resp.setStatus(status);

		if (isBinary(body)) {
			resp.setContentType(contentType);
			writeBinary(body, resp.getOutputStream());
		} else {
			writeText(body, contentType, wrapped, resp);
		}
	}

	private static void writeText(Object body, String contentType, boolean wrapped, HttpServletResponse resp)
			throws IOException {
		String baseType;
		String charset;
		if (wrapped) {
			try {
				MimeType mimeType = new MimeType(contentType);
				baseType = mimeType.getBaseType();
				charset = mimeType.getParameter("charset");
			} catch (MimeTypeParseException ex) {
				throw new TopLogicException(I18NConstants.ERROR_INVALID_CONTENT_TYPE__VALUE_MSG
					.fill(contentType, ex.getMessage()), ex);
			}
		} else {
			baseType = JsonUtilities.JSON_CONTENT_TYPE;
			charset = JsonUtilities.DEFAULT_JSON_ENCODING;
		}
		if (charset == null) {
			charset = "utf-8";
		}
		resp.setContentType(baseType);
		resp.setCharacterEncoding(charset);
		String content;
		if (JsonUtilities.JSON_CONTENT_TYPE.equals(baseType)) {
			content = JSON.toString(body);
		} else {
			content = String.valueOf(body);
		}
		resp.getWriter().write(content);
	}

	private static boolean isBinary(Object value) {
		return value instanceof BinaryData || value instanceof byte[] || value instanceof InputStream;
	}

	private static void writeBinary(Object value, OutputStream out) throws IOException {
		if (value instanceof BinaryData) {
			((BinaryData) value).deliverTo(out);
		} else if (value instanceof byte[]) {
			out.write((byte[]) value);
		} else if (value instanceof InputStream) {
			try (InputStream in = (InputStream) value) {
				StreamUtilities.copyStreamContents(in, out);
			}
		} else {
			throw new IllegalStateException("Not a binary value: " + value.getClass().getName());
		}
	}

	private static String defaultBinaryContentType(Object value) {
		if (value instanceof BinaryData) {
			String contentType = ((BinaryData) value).getContentType();
			if (contentType != null && !contentType.isEmpty()) {
				return contentType;
			}
		}
		return BinaryDataSource.CONTENT_TYPE_OCTET_STREAM;
	}

	private Object inInteraction(Map<String, Object> arguments) throws ComputationFailure {
		if (Logger.isDebugEnabled(ServiceMethodByExpression.class)) {
			Logger.debug("Processing request for '" + _path + "', arguments=" + arguments,
				ServiceMethodByExpression.class);
		}
		if (_transaction) {
			try (Transaction tx =
				PersistencyLayer.getKnowledgeBase().beginTransaction(I18NConstants.REST_CALL__NAME.fill(_path))) {
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