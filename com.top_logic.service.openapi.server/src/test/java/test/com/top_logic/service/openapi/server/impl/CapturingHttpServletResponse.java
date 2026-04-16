/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.server.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * Minimal {@link jakarta.servlet.http.HttpServletResponse} test double used to verify
 * {@code ServiceMethodByExpression#writeResponse}.
 */
public class CapturingHttpServletResponse extends HttpServletResponseWrapper {

	private int _status = 200;

	private String _contentType;

	private String _characterEncoding = "utf-8";

	private final ByteArrayOutputStream _bytes = new ByteArrayOutputStream();

	private ServletOutputStream _out;

	private PrintWriter _writer;

	private boolean _errorSent;

	private int _errorStatus;

	public CapturingHttpServletResponse() {
		super(new NoopResponse());
	}

	@Override
	public void setStatus(int sc) {
		_status = sc;
	}

	@Override
	public int getStatus() {
		return _status;
	}

	@Override
	public void setContentType(String type) {
		_contentType = type;
	}

	@Override
	public String getContentType() {
		return _contentType;
	}

	@Override
	public void setCharacterEncoding(String charset) {
		_characterEncoding = charset;
	}

	@Override
	public String getCharacterEncoding() {
		return _characterEncoding;
	}

	@Override
	public void sendError(int sc) {
		_errorSent = true;
		_errorStatus = sc;
	}

	@Override
	public ServletOutputStream getOutputStream() {
		if (_writer != null) {
			throw new IllegalStateException("getWriter() already called");
		}
		if (_out == null) {
			_out = new ServletOutputStream() {
				@Override
				public void write(int b) {
					_bytes.write(b);
				}

				@Override
				public boolean isReady() {
					return true;
				}

				@Override
				public void setWriteListener(WriteListener writeListener) {
					// no-op
				}
			};
		}
		return _out;
	}

	@Override
	public PrintWriter getWriter() {
		if (_out != null) {
			throw new IllegalStateException("getOutputStream() already called");
		}
		if (_writer == null) {
			_writer = new PrintWriter(new OutputStreamWriter(_bytes,
				_characterEncoding == null ? java.nio.charset.StandardCharsets.UTF_8
					: java.nio.charset.Charset.forName(_characterEncoding)));
		}
		return _writer;
	}

	/** The captured response body as raw bytes. */
	public byte[] bodyBytes() {
		if (_writer != null) {
			_writer.flush();
		}
		return _bytes.toByteArray();
	}

	/** The captured response body decoded with the currently set character encoding. */
	public String bodyString() {
		try {
			if (_writer != null) {
				_writer.flush();
			}
			return _bytes.toString(_characterEncoding == null ? "utf-8" : _characterEncoding);
		} catch (IOException ex) {
			throw new AssertionError(ex);
		}
	}

	public boolean errorSent() {
		return _errorSent;
	}

	public int errorStatus() {
		return _errorStatus;
	}
}
