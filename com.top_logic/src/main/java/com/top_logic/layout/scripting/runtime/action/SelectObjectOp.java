/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.json.JSON;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.scripting.action.SelectObject;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ApplicationActionOp} that selects an object within a {@link Selectable}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SelectObjectOp extends ComponentActionOp<SelectObject> {

	private Filter<Object> matcher;

	public SelectObjectOp(InstantiationContext context, SelectObject config) {
		super(context, config);
		
		this.matcher = (Filter<Object>) context.getInstance(config.getMatcherConfig());
	}

	@Override
	protected Object process(ActionContext context, LayoutComponent component, Object argument) {
		// TODO: Add getObjects() to Selectable interface to simplify processing.
		if (component instanceof TableComponent) {
			TableComponent table = (TableComponent) component;

			EditableRowTableModel tableModel = table.getTableModel();
			for (Object rowObject : tableModel.getAllRows()) {
				if (select(table, rowObject)) {
					return argument;
				}
			}
			
			ApplicationAssertions.fail(config,
				"Object matching '" + JSON.toString(config) + "' not found in  '" + component.getName() + "'.");
		}
		
		throw new IllegalArgumentException("Cannot select in a '" + component.getClass() + "' component implementation.");
	}

	protected final boolean select(Selectable selectable, Object selection) {
		if (matcher.accept(selection)) {
			selectable.setSelected(selection);
			return true;
		} else {
			return false;
		}
	}
	
}
