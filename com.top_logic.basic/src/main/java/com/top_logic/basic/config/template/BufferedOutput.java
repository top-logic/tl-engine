/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.template;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.template.Expand.Output;

/**
 * Output appending to a {@link List} buffer.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BufferedOutput implements Output {

	private final List<Object> _buffer = new ArrayList<>();

	@Override
	public void add(Object result) {
		_buffer.add(result);
	}

	/**
	 * Access to the underlying buffer.
	 */
	public List<Object> getBuffer() {
		return _buffer;
	}

}
