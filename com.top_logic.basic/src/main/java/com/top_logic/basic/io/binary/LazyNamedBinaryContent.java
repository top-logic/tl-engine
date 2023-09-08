/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import com.top_logic.basic.io.BinaryContent;

/**
 * Takes take of storing and returning the name of the {@link BinaryContent}.
 * <p>
 * Use this class if building the name might be expensive. If building the name is cheap, use
 * {@link AbstractBinaryData}.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class LazyNamedBinaryContent implements BinaryContent {

	private String _name;

	@Override
	public String toString() {
		if (_name == null) {
			_name = buildNameInternal();
		}
		return _name;
	}

	private String buildNameInternal() {
		String name = buildName();
		return name == null ? NO_NAME : name;
	}

	/** Is allowed to be <code>null</code>, which will result in {@link #NO_NAME}. */
	protected abstract String buildName();

}
