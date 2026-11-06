/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

import jakarta.servlet.http.Part;

/**
 * A {@link Part} of a multipart upload holding a text, for tests of upload handlers.
 *
 * <p>
 * The part cannot be {@link #write(String) written} to a file; a handler reading it through
 * {@link #getInputStream()} sees its text.
 * </p>
 */
public class TextPart implements Part {

	private final String _name;

	private final String _fileName;

	private final byte[] _content;

	/**
	 * Creates a {@link TextPart}.
	 *
	 * @param name
	 *        The name of the part in the request, see {@link #getName()}.
	 * @param fileName
	 *        The name of the uploaded file, see {@link #getSubmittedFileName()}.
	 * @param content
	 *        The text the part holds, see {@link #getInputStream()}.
	 */
	public TextPart(String name, String fileName, String content) {
		_name = name;
		_fileName = fileName;
		_content = content.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(_content);
	}

	@Override
	public String getContentType() {
		return "text/plain";
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public String getSubmittedFileName() {
		return _fileName;
	}

	@Override
	public long getSize() {
		return _content.length;
	}

	@Override
	public void write(String fileName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete() {
		// Nothing to delete.
	}

	@Override
	public String getHeader(String name) {
		return null;
	}

	@Override
	public Collection<String> getHeaders(String name) {
		return new ArrayList<>();
	}

	@Override
	public Collection<String> getHeaderNames() {
		return new ArrayList<>();
	}
}
