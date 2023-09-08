/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.config.annotation.Subtypes;

/**
 * Translated {@link Subtypes} data.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class SubtypesDef {

	public static final SubtypesDef NONE =
		new SubtypesDef(Collections.<String, Class<? extends ConfigurationItem>> emptyMap(), true);

	private final Map<String, Class<? extends ConfigurationItem>> _typeByTag;

	private final boolean _adjust;

	public SubtypesDef(Map<String, Class<? extends ConfigurationItem>> typeByTag, boolean adjust) {
		_typeByTag = typeByTag;
		_adjust = adjust;
	}

	public Map<String, Class<? extends ConfigurationItem>> getTypeByTag() {
		return _typeByTag;
	}

	public boolean isAdjust() {
		return _adjust;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (_adjust ? 1231 : 1237);
		result = prime * result + ((_typeByTag == null) ? 0 : _typeByTag.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubtypesDef other = (SubtypesDef) obj;
		if (_adjust != other._adjust)
			return false;
		if (_typeByTag == null) {
			if (other._typeByTag != null)
				return false;
		} else if (!_typeByTag.equals(other._typeByTag))
			return false;
		return true;
	}

}
