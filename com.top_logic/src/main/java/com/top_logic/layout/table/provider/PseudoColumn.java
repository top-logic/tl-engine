/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import java.util.Objects;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLTypePart;
import com.top_logic.util.Resources;

/**
 * A {@link ColumnOption} for columns that are not defined by any {@link TLTypePart}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class PseudoColumn implements ColumnOption {

	private final ResKey _label;

	private final String _technicalName;

	/**
	 * Creates a {@link ColumnOption} with the given label and technical name.
	 */
	public PseudoColumn(ResKey label, String technicalName) {
		_label = label;
		_technicalName = technicalName;
	}

	@Override
	public String getLabel() {
		return Resources.getInstance().getString(_label);
	}

	@Override
	public String getTechnicalName() {
		return _technicalName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_label, _technicalName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PseudoColumn other = (PseudoColumn) obj;
		return Objects.equals(_label, other._label) && Objects.equals(_technicalName, other._technicalName);
	}

}
