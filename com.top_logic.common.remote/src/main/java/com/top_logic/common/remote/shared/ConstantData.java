/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.shared;

import java.util.Collections;
import java.util.Map;

/**
 * {@link ObjectData} referencing a constant value as generalized shared object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConstantData extends ObjectData {

	private final Object _value;

	/**
	 * Creates a {@link ConstantData}.
	 *
	 * @param scope
	 *        See {@link #scope()}.
	 * @param value
	 *        The constant {@link #handle()} value.
	 */
	public ConstantData(ObjectScope scope, Object value) {
		super(scope);
		_value = value;
	}

	@Override
	public Object handle() {
		return _value;
	}

	@Override
	public Object getDataRaw(String property) {
		return null;
	}

	@Override
	public Object setDataRaw(String property, Object rawValue) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Object> properties() {
		return Collections.emptyMap();
	}

	@Override
	public void updateProperties(Map<String, Object> values) {
		// Ignore.
	}

	@Override
	protected void onDelete() {
		// Nothing to do.
	}

}
