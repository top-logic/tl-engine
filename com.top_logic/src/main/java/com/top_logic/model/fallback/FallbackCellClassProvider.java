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
 * 
 * @see UpdateFallbackDisplay
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
		if (cell instanceof EditingCell) {
			// The grid component tries to generically wrap cell class providers into field value
			// listeners for dynamically updating CSS classes of fields displayed in the selected
			// row in edit mode. However, the mechanics for fallback values already cares for
			// dynamic CSS class updates for form fields displaying attributes with fallback values.
			// Therefore the grid component's wrapping mechanics is not necessary and will not work
			// correctly either: This implementation here depends on the ability to retrieve the
			// current explicitly set value from the cell's row object through the fallback
			// attribute storage. But this storage cannot retrieve the current value stored in the
			// attribute's field. The storage retrieves the value of the storage attribute which
			// delivers the persistent value at the beginning of the editing operation, not the
			// value of the form field displaying the fallback attribute.
			return null;
		}
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
