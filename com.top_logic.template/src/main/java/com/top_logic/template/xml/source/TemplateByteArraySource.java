/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.xml.source;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A {@link TemplateSource} that has a byte array as template source.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TemplateByteArraySource implements TemplateSource {

	byte[] _source;

	/**
	 * Creates a new {@link TemplateByteArraySource} with a copy of the given byte array as
	 * template source.
	 */
	public TemplateByteArraySource(byte[] source) {
		_source = source.clone();
	}

	@Override
	public InputStream getContent() {
		return new ByteArrayInputStream(_source);
	}

	@Override
	public TemplateSource resolve(TemplateSource context, String templateReference) {
		// Resolving only in global context by calling the factory resolve method without context.
		return TemplateSourceFactory.getInstance().resolve(null, templateReference);
	}

	@Override
	public ResourceTransaction update() {
		return new AbstractResourceTransaction() {

			private ByteArrayOutputStream _out;

			@Override
			protected OutputStream internalOpen() {
				if (_out != null) {
					throw new IllegalStateException("Stream already created.");
				}
				_out = new ByteArrayOutputStream();
				return _out;
			}

			@Override
			protected void internalCommit() {
				if (_out != null) {
					_source = _out.toByteArray();
					_out = null;
				}
			}

			@Override
			protected void internalClose() {
				_out = null;
			}
		};
	}

	@Override
	public void delete() throws IOException {
		throw new IOException("Cannot delete an in-memory resource.");
	}

}
