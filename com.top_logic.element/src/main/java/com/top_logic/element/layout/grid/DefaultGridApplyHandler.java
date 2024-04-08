/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.model.TLObject;

/**
 * Default {@link GridApplyHandler}, which stores the changes of the used AttributeUpdateContainer.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class DefaultGridApplyHandler implements GridApplyHandler {

	public static final DefaultGridApplyHandler INSTANCE = new DefaultGridApplyHandler();

	@Override
	public boolean storeChanges(GridComponent component, Object rowObject, FormContainer container) {
		AttributeFormContext formContext = component.getFormContext();
		formContext.store();

		TableViewModel tableModel = component.getTableModel(formContext);
		for (String columnName : tableModel.getColumnNames()) {
			ColumnConfiguration column = tableModel.getColumnDescription(columnName);
			if (column.getFieldProvider() != null) {
				Accessor accessor = column.getAccessor();

				FormGroup rowGroup = component.getRowGroup(rowObject);
				if (rowGroup.hasMember(columnName)) {
					FormField field = rowGroup.getField(columnName);
					if (field.isChanged()) {
						accessor.setValue(rowObject, columnName, field.getValue());
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean allowEdit(GridComponent component, Object rowObject) {
		return !(rowObject instanceof TLObject) || ((TLObject) rowObject).tHistoryContext() == Revision.CURRENT_REV;
	}

}
