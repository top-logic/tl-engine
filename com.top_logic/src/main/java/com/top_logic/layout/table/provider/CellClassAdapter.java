/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import com.top_logic.layout.table.CellClassProvider;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ui.CssClassProvider;
import com.top_logic.model.util.TLTypeContext;

final class CellClassAdapter implements CellClassProvider {
	private final CssClassProvider _provider;

	private final TLStructuredTypePart _attribute;

	/** 
	 * Creates a {@link CellClassAdapter}.
	 */
	private CellClassAdapter(CssClassProvider provider, TLStructuredTypePart attribute) {
		_provider = provider;
		_attribute = attribute;
	}

	@Override
	public String getCellClass(Cell cell) {
		return _provider.getCssClass((TLObject) cell.getRowObject(), _attribute, cell.getValue());
	}

	/**
	 * Wraps the given {@link CssClassProvider} into a {@link CellClassProvider}.
	 */
	public static CellClassProvider wrap(TLTypeContext contentType, CssClassProvider provider) {
		if (provider instanceof CellClassProvider cellClassProvider) {
			return cellClassProvider;
		}

		TLStructuredTypePart attribute = (TLStructuredTypePart) contentType.getTypePart();
		return new CellClassAdapter(provider, attribute);
	}
}