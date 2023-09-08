/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

import java.util.ArrayList;
import java.util.List;

/**
 * Description of the location in a layout definition.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SourceLocation {

	private final String _source;

	private final SourceLocation _detail;

	/**
	 * Creates a {@link SourceLocation}.
	 * 
	 * @param source
	 *        See {@link #getSource()}.
	 * @param detail
	 *        See {@link #getDetail()}
	 */
	public SourceLocation(String source, SourceLocation detail) {
		_source = source;
		_detail = detail;
	}

	/**
	 * The source file.
	 */
	public String getSource() {
		return _source;
	}

	/**
	 * The {@link SourceLocation} that was included from this {@link #getSource()}.
	 */
	public SourceLocation getDetail() {
		return _detail;
	}

	/**
	 * Path of {@link #getSource()} references to this source location.
	 * 
	 * <p>
	 * The most detailed source is the first in the resulting list. A source file in the resulting
	 * list is referenced by its following reference.
	 * </p>
	 */
	public List<String> getReferencePath() {
		ArrayList<String> result = new ArrayList<>();
		fillReferencePath(result);
		return result;
	}

	private void fillReferencePath(ArrayList<String> result) {
		if (_detail != null) {
			_detail.fillReferencePath(result);
		}
		result.add(_source);
	}

	@Override
	public String toString() {
		return (_detail == null ? "" : _detail + " referenced from ") + _source;
	}

}
