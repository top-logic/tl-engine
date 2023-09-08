/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import java.util.Objects;

import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.model.TLTypePart;

/**
 * {@link ColumnOption} for a {@link TLTypePart}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TypePartColumn implements ColumnOption {

	private final TLTypePart _part;

	/**
	 * Creates a {@link ColumnOption} for the given {@link TLTypePart}.
	 */
	public TypePartColumn(TLTypePart part) {
		_part = part;
	}

	@Override
	public String getLabel() {
		return MetaResourceProvider.INSTANCE.getLabel(_part);
	}

	@Override
	public String getTechnicalName() {
		return _part.getName();
	}

	@Override
	public int hashCode() {
		return Objects.hash(_part);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypePartColumn other = (TypePartColumn) obj;
		return Objects.equals(_part, other._part);
	}

}
