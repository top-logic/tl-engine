/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.fallback;

import com.top_logic.basic.util.Utils;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.table.AbstractCellClassProvider;
import com.top_logic.layout.table.CellClassProvider;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link CellClassProvider} assigning CSS class values to a table cell depending on the explicit
 * assignment status of the displayed value.
 */
public class FallbackCellClassProvider extends AbstractCellClassProvider {

	private TLStructuredTypePart _attribute;

	private StorageWithFallback _fallbackStorage;

	/**
	 * Creates a {@link FallbackCellClassProvider}.
	 */
	public FallbackCellClassProvider(TLStructuredTypePart attribute, StorageWithFallback fallbackStorage) {
		_attribute = attribute;
		_fallbackStorage = fallbackStorage;
	}

	@Override
	public String getCellClass(Cell cell) {
		if (cell.cellExists() && !(cell.getValue() instanceof FormMember) && !(cell.getValue() instanceof Control)) {
			Object row = cell.getRowObject();
			if (row instanceof TLObject obj) {
				Object explicitValue = _fallbackStorage.getExplicitValue(obj, _attribute);
				if (Utils.isEmpty(explicitValue)) {
					return _fallbackStorage.getCssFallback();
				} else {
					return _fallbackStorage.getCssExplicit();
				}
			}
		}
		return null;
	}

}
