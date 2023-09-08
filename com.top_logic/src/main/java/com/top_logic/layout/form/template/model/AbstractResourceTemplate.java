/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model;

import com.top_logic.basic.util.ResKey;

/**
 * Base class for template implementations based on a resource key.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractResourceTemplate implements Template {

	private final ResKey _key;

	AbstractResourceTemplate(ResKey key) {
		_key = key;
	}

	/**
	 * The resource key for the text.
	 */
	public ResKey getKey() {
		return _key;
	}

}
