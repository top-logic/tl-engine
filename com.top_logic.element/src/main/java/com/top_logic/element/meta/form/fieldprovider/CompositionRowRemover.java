/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.meta.form.fieldprovider;

import java.util.ArrayList;
import java.util.Collection;

import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.RowObjectRemover;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.model.TLObject;

/**
 * {@link RowObjectRemover} to remove existing rows.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class CompositionRowRemover implements RowObjectRemover {

	private final AttributeUpdateContainer _updateContainer;

	private final boolean _multiple;

	/**
	 * Creates a new {@link CompositionRowRemover}.
	 * 
	 * @param updateContainer
	 *        The {@link AttributeUpdateContainer} of the form.
	 * @param multiple
	 *        Whether table contain multiple values.
	 */
	CompositionRowRemover(AttributeUpdateContainer updateContainer, boolean multiple) {
		_updateContainer = updateContainer;
		_multiple = multiple;
	}

	@Override
	public void removeRow(Object rowObject, Control aControl) {
		TableField table = (TableField) ((TableControl) aControl).getModel();
		TLFormObject row = (TLFormObject) rowObject;

		// Remove updates from update container.
		_updateContainer.removeOverlay(row);

		// Remember deletion of persistent object.
		TLObject editedObject = row.getEditedObject();
		if (editedObject != null) {
			table.mkSet(CompositionFieldProvider.DELETED).add(editedObject);
		}

		// Remove from table field value.
		removeValue(table, row);
	}

	private void removeValue(TableField table, TLObject row) {
		if (_multiple) {
			Collection<Object> value = new ArrayList<>((Collection<?>) table.getValue());
			value.remove(row);
			table.setValue(value);
		} else {
			table.setValue(null);
		}
	}
}
